package org.nsu.fit.tests.ui;

import com.github.javafaker.Faker;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.nsu.fit.services.browser.Browser;
import org.nsu.fit.services.browser.BrowserService;
import org.nsu.fit.services.rest.data.PlanPojo;
import org.nsu.fit.tests.ui.screen.AdminScreen;
import org.nsu.fit.tests.ui.screen.LoginScreen;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RemovePlanTest {
    private Browser browser = null;
    private PlanPojo planPojo;

    private AdminScreen adminScreen;
    @BeforeClass
    public void beforeClass() {
        browser = BrowserService.openNewBrowser();
        Faker faker = new Faker();
        planPojo = new PlanPojo();
        planPojo.name = faker.pokemon().name() + "Plan";
        planPojo.details = faker.pokemon().location();
        planPojo.fee = 10;

        adminScreen = new LoginScreen(browser)
                .loginAsAdmin()
                .createPlan()
                .fillName(planPojo.name)
                .fillDetails(planPojo.details)
                .fillFee(planPojo.fee)
                .clickSubmit();
    }

    @Test(description = "Remove plan via UI.")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Plan feature")
    public void removePlan() {
        adminScreen.findPlan(planPojo.name);
        PlanPojo foundedPlan = adminScreen
                .deleteFirstPlan()
                .deleteFirstPlanSave()
                .findPlan(planPojo.name);
        Assert.assertNull(foundedPlan);
    }

    @AfterClass
    public void afterClass() {
        if (browser != null) {
            browser.close();
        }
    }
}
