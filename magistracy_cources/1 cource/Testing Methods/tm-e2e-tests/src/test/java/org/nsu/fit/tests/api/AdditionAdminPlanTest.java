package org.nsu.fit.tests.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.nsu.fit.services.log.Logger;
import org.nsu.fit.services.rest.RestClient;
import org.nsu.fit.services.rest.data.AccountTokenPojo;
import org.nsu.fit.services.rest.data.PlanPojo;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.UUID;

public class AdditionAdminPlanTest {
    private AccountTokenPojo adminToken;
    RestClient client;
    PlanPojo planPojo;

    @BeforeClass
    public void createCustomer() {
        client = new RestClient();
        adminToken = client.authenticate("admin", "setup");
    }

    @Test(description = "Get user plans as admin")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Get all plans")
    public void createPlansAsAdmin() throws IOException {
        planPojo = new PlanPojo();
        planPojo.id = UUID.randomUUID();
        planPojo.fee = 900;
        planPojo.details = "Strange details";
        planPojo.name = "Swiss watches";

        PlanPojo plans = client.createPlan(adminToken, new ObjectMapper().writeValueAsString(planPojo));
        Logger.debug("Admin plans: " + Logger.toJson(plans));
    }

    @AfterClass
    public void checkPlanCount() {
        Assert.assertNotEquals(
                client.getAdminPlans(adminToken)
                        .stream()
                        .filter(x -> x.id == planPojo.id)
                        .count(),
                1);
    }
}
