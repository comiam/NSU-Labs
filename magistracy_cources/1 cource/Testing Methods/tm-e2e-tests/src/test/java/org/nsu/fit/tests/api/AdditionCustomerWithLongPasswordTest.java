package org.nsu.fit.tests.api;

import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.nsu.fit.services.rest.RestClient;
import org.nsu.fit.services.rest.data.AccountTokenPojo;
import org.nsu.fit.services.rest.data.CustomerPojo;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AdditionCustomerWithLongPasswordTest {
    private AccountTokenPojo adminToken;

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
        customerPojo.pass = "lyaslkdjfglksdjgflkjslkfdjglskdjfkjsdlkfjsldjfksjdkfjsldjkflksjdflk";
        new RestClient().tryToCreateCustomer(adminToken, customerPojo);
    }
}
