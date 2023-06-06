package org.nsu.fit.tests.ui;

import com.github.javafaker.Faker;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.nsu.fit.services.browser.Browser;
import org.nsu.fit.services.browser.BrowserService;
import org.nsu.fit.services.rest.data.PlanPojo;
import org.nsu.fit.tests.ui.screen.LoginScreen;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AdditionPlanTest {
    private Browser browser = null;

    @BeforeClass
    public void beforeClass() {
        browser = BrowserService.openNewBrowser();
    }

    @Test(description = "Create plan via UI.")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Plan feature")
    public void createPlanTest() {
        Faker faker = new Faker();
        String name = faker.pokemon().name() + "Plan";
        String details = faker.pokemon().location();
        int fee = 10;

        PlanPojo plan = new LoginScreen(browser)
                .loginAsAdmin()
                .createPlan()
                .fillName(name)
                .fillDetails(details)
                .fillFee(fee)
                .clickSubmit()
                .findPlan(name);

        Assert.assertEquals(plan.name, name);
        Assert.assertEquals(plan.details, details);
        Assert.assertEquals(plan.fee, fee);
    }
}
