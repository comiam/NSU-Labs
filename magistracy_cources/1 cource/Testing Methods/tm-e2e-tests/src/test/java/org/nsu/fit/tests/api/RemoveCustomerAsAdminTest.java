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

public class RemoveCustomerAsAdminTest {
    AccountTokenPojo adminToken;
    CustomerPojo customerPojo;

    @BeforeClass
    public void createCustomer() {
        RestClient client = new RestClient();
        adminToken = client.authenticate("admin", "setup");
        customerPojo = client.createAutoGeneratedCustomer(adminToken);
    }

    @Test(description = "Delete customer as admin")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Remove customer feature")
    public void deleteCustomerAsAdminTest() {
        new RestClient().removeCustomer(customerPojo.id, adminToken);
    }

    @AfterClass
    public void checkCustomerCount() {
        Assert.assertEquals( new RestClient().getCustomers(adminToken).stream().filter(
                x -> x.login.equals(customerPojo.login)
        ).count(), 0);
    }
}
