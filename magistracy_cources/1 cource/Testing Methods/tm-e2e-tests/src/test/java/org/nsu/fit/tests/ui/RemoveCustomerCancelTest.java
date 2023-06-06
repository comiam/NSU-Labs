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
import org.nsu.fit.tests.ui.screen.LoginScreen;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RemoveCustomerCancelTest {
    private Browser browser = null;
    private ContactPojo contactPojo;

    private AdminScreen adminScreen;
    @BeforeClass
    public void beforeClass() {
        browser = BrowserService.openNewBrowser();
    }

    @Test(description = "Create customer via UI.")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Create customer feature")
    public void createCustomer() {
        contactPojo = new ContactPojo();
        Faker faker = new Faker();
        contactPojo.firstName = faker.name().firstName();
        contactPojo.lastName = faker.name().lastName();
        contactPojo.login = faker.internet().emailAddress();
        contactPojo.pass = faker.internet().password(6, 12);
        adminScreen = (AdminScreen) new LoginScreen(browser)
                .loginAsAdmin()
                .createCustomer()
                .fillEmail(contactPojo.login)
                .fillPassword(contactPojo.pass)
                .fillFirstName(contactPojo.firstName)
                .fillLastName(contactPojo.lastName)
                .clickSubmit();
    }

    @Test(description = "Remove customer via UI.", dependsOnMethods = "createCustomer")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Remove customer feature")
    public void removeCustomer() {
        CustomerPojo customerPojo = adminScreen
                .makeFirstCustomer(contactPojo.login)
                .deleteFirstCustomer()
                .deleteFirstCustomerCancel()
                .findFirstCustomer();
        Assert.assertNotNull(customerPojo);
    }

    @AfterClass
    public void afterClass() {
        if (browser != null) {
            browser.close();
        }
    }
}
