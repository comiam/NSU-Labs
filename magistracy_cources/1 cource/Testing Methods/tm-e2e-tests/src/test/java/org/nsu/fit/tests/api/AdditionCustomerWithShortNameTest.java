package org.nsu.fit.tests.api;

import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.nsu.fit.services.rest.RestClient;
import org.nsu.fit.services.rest.data.AccountTokenPojo;
import org.nsu.fit.services.rest.data.CustomerPojo;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AdditionCustomerWithShortNameTest {
    private AccountTokenPojo adminToken;
    private final String shortName = "L";

    @BeforeClass
    public void authAsAdminTest() {
        adminToken = new RestClient().authenticate("admin", "setup");
    }

    @Test(description = "Add customer with short password.",
            expectedExceptions = {javax.ws.rs.BadRequestException.class})
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Create customer feature")
    public void tryToAddCustomer() {
        CustomerPojo customerPojo = new CustomerPojo();
        customerPojo.firstName = shortName;
        new RestClient().tryToCreateCustomer(adminToken, customerPojo);
    }

    @AfterClass
    public void checkCustomerCount() {
        Assert.assertEquals(
                new RestClient().getCustomers(adminToken).stream().filter(x -> x.firstName.equals(shortName)).count(), 0);
    }
}
