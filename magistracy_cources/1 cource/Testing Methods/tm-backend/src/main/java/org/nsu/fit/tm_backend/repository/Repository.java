package org.nsu.fit.tm_backend.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.repository.data.PlanPojo;
import org.nsu.fit.tm_backend.repository.data.SubscriptionPojo;
import org.nsu.fit.tm_backend.service.data.AccountTokenBO;

public interface Repository {
    CustomerPojo createCustomer(CustomerPojo customerPojo);

    void editCustomer(CustomerPojo customerPojo);

    void deleteCustomer(UUID id);

    Set<CustomerPojo> getCustomers();

    Set<UUID> getCustomerIds();

    CustomerPojo getCustomer(UUID id);

    CustomerPojo getCustomerByLogin(String customerLogin);

    AccountTokenBO createAccountToken(AccountTokenBO accountToken);

    void checkAccountToken(String authenticationToken);

    PlanPojo createPlan(PlanPojo plan);

    void deletePlan(UUID id);

    List<PlanPojo> getPlans();

    SubscriptionPojo createSubscription(SubscriptionPojo subscriptionPojo);

    void deleteSubscription(UUID id);

    List<SubscriptionPojo> getSubscriptions();

    List<SubscriptionPojo> getSubscriptions(UUID customerId);
}
