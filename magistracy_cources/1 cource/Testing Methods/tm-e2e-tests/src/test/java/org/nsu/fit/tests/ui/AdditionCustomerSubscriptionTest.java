package org.nsu.fit.tests.ui;

import com.github.javafaker.Faker;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.nsu.fit.services.browser.Browser;
import org.nsu.fit.services.browser.BrowserService;
import org.nsu.fit.services.rest.data.ContactPojo;
import org.nsu.fit.services.rest.data.PlanPojo;
import org.nsu.fit.shared.Screen;
import org.nsu.fit.tests.ui.screen.AdminScreen;
import org.nsu.fit.tests.ui.screen.CustomerScreen;
import org.nsu.fit.tests.ui.screen.LoginScreen;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AdditionCustomerSubscriptionTest {
    private Browser browser = null;
    private PlanPojo planPojo;
    private ContactPojo contactPojo;

    private Screen screen;
    @BeforeClass
    public void beforeClass() {
        browser = BrowserService.openNewBrowser();
        Faker faker = new Faker();
        planPojo = new PlanPojo();
        planPojo.name = faker.pokemon().name() + "Plan";
        planPojo.details = faker.pokemon().location();
        planPojo.fee = 10;

        screen = new LoginScreen(browser)
                .loginAsAdmin()
                .createPlan()
                .fillName(planPojo.name)
                .fillDetails(planPojo.details)
                .fillFee(planPojo.fee)
                .clickSubmit();

        contactPojo = new ContactPojo();
        contactPojo.firstName = faker.name().firstName();
        contactPojo.lastName = faker.name().lastName();
        contactPojo.login = faker.internet().emailAddress();
        contactPojo.pass = faker.internet().password(6, 12);
        screen = ((AdminScreen)screen)
                .createCustomer()
                .fillEmail(contactPojo.login)
                .fillPassword(contactPojo.pass)
                .fillFirstName(contactPojo.firstName)
                .fillLastName(contactPojo.lastName)
                .clickSubmit();
        browser.close();
        browser = BrowserService.openNewBrowser();
    }

    @Test(description = "Create subscription via UI.")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Subscription feature")
    public void createSubscriptionTest() {

        screen = new LoginScreen(browser)
                .loginAsCustomer(contactPojo.login, contactPojo.pass);

        ((CustomerScreen)screen)
                .buyFirstPlan()
                .buyFirstPlanOk();
    }

}

