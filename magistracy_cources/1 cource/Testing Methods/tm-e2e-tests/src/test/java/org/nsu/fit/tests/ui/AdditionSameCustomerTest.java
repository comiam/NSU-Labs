package org.nsu.fit.tests.ui;

import com.github.javafaker.Faker;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.nsu.fit.services.browser.Browser;
import org.nsu.fit.services.browser.BrowserService;
import org.nsu.fit.services.rest.data.ContactPojo;
import org.nsu.fit.services.rest.data.CustomerPojo;
import org.nsu.fit.tests.ui.screen.AdminScreen;
import org.nsu.fit.tests.ui.screen.CreateCustomerScreen;
import org.nsu.fit.tests.ui.screen.LoginScreen;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AdditionSameCustomerTest {
    private ContactPojo contactPojo;

    @Test(description = "Create customer once via UI.")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Create customer feature")
    public void createCustomer() {
        Browser browser = BrowserService.openNewBrowser();
        contactPojo = new ContactPojo();
        Faker faker = new Faker();
        contactPojo.firstName = faker.name().firstName();
        contactPojo.lastName = faker.name().lastName();
        contactPojo.login = faker.internet().emailAddress();
        contactPojo.pass = faker.internet().password(6, 12);
        AdminScreen adminScreen = (AdminScreen) new LoginScreen(browser)
                .loginAsAdmin()
                .createCustomer()
                .fillEmail(contactPojo.login)
                .fillPassword(contactPojo.pass)
                .fillFirstName(contactPojo.firstName)
                .fillLastName(contactPojo.lastName)
                .clickSubmit();
        CustomerPojo customerPojo = adminScreen
                .findCustomer(contactPojo.login);

        Assert.assertNotNull(customerPojo);
        Assert.assertEquals(customerPojo.login, contactPojo.login);
        Assert.assertEquals(customerPojo.firstName, contactPojo.firstName);
        Assert.assertEquals(customerPojo.lastName, contactPojo.lastName);
        browser.close();
    }

    @Test(description = "Try to create customer again via UI.", dependsOnMethods = "createCustomer")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Create customer feature")
    public void createCustomerAgain() {
        Browser browser = BrowserService.openNewBrowser();
        CreateCustomerScreen createCustomerScreen = (CreateCustomerScreen) new LoginScreen(browser)
                .loginAsAdmin()
                .createCustomer()
                .fillEmail(contactPojo.login)
                .fillPassword(contactPojo.pass)
                .fillFirstName(contactPojo.firstName)
                .fillLastName(contactPojo.lastName)
                .clickSubmit();
        CustomerPojo customerPojo =createCustomerScreen.clickCancel()
                .findCustomer(contactPojo.login);

        Assert.assertNotNull(customerPojo);
        browser.close();
    }
}
