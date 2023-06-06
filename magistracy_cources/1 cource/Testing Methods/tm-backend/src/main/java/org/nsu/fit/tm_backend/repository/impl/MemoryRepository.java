package org.nsu.fit.tm_backend.repository.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.nsu.fit.tm_backend.repository.Repository;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.repository.data.PlanPojo;
import org.nsu.fit.tm_backend.repository.data.SubscriptionPojo;
import org.nsu.fit.tm_backend.service.data.AccountTokenBO;
import org.nsu.fit.tm_backend.service.impl.auth.exception.AccessDeniedException;
import org.nsu.fit.tm_backend.shared.JsonMapper;

@Singleton
@Slf4j
public class MemoryRepository implements Repository {
    private static final Object generalMutex = new Object();

    private final Map<String, AccountTokenBO> accountTokens;
    private final Map<UUID, CustomerPojo> customers;
    private final Map<UUID, PlanPojo> plans;
    private final Map<UUID, SubscriptionPojo> subscriptions;

    public MemoryRepository() {
        this.accountTokens = new LinkedHashMap<>();
        this.customers = new LinkedHashMap<>();
        this.plans = new LinkedHashMap<>();
        this.subscriptions = new LinkedHashMap<>();
    }

    @Override
    public AccountTokenBO createAccountToken(AccountTokenBO accountToken) {
        synchronized (generalMutex) {
            log.debug(String.format(
                "Method 'createAccountToken' was called with data: %s",
                accountToken));

            accountTokens.put(accountToken.getToken(), accountToken);

            return accountToken;
        }
    }

    @Override
    public void checkAccountToken(String authenticationToken) {
        synchronized (generalMutex) {
            log.debug(String.format("Method 'checkAccountToken' was called with data: \n%s", authenticationToken));

            if (!accountTokens.containsKey(authenticationToken)) {
                throw new AccessDeniedException("Access Denied!");
            }
        }
    }

    public CustomerPojo createCustomer(CustomerPojo customerData) {
        synchronized (generalMutex) {
            log.debug(String.format(
                "Method 'createCustomer' was called with data: %s",
                customerData));

            customerData.id = UUID.randomUUID();

            customers.put(customerData.id, customerData);

            return customerData;
        }
    }

    public void editCustomer(CustomerPojo customerPojo) {
        synchronized (generalMutex) {
            log.debug("Method 'editCustomer' was called with data: \n{}", JsonMapper.toJson(customerPojo, true));

            customers.put(customerPojo.id, customerPojo);
        }
    }

    @Override
    public void deleteCustomer(UUID id) {
        synchronized (generalMutex) {
            log.debug(String.format("Method 'removeCustomer' was called with data: \n%s", id));

            customers.remove(id);
        }
    }

    public Set<CustomerPojo> getCustomers() {
        synchronized (generalMutex) {
            log.debug("Method 'getCustomers' was called.");

            return new LinkedHashSet<>(customers.values());
        }
    }

    public Set<UUID> getCustomerIds() {
        synchronized (generalMutex) {
            log.debug("Method 'getCustomerIds' was called.");

            return new HashSet<>(customers.keySet());
        }
    }

    public CustomerPojo getCustomer(UUID id) {
        synchronized (generalMutex) {
            log.debug(String.format("Method 'getCustomer' was called with data '%s'.", id));

            return customers.get(id);
        }
    }

    public CustomerPojo getCustomerByLogin(String customerLogin) {
        synchronized (generalMutex) {
            log.debug(String.format("Method 'lookupCustomerByLogin' was called with data '%s'.", customerLogin));

            for (var customer : customers.values()) {
                if (customer.login.equals(customerLogin)) {
                    return customer;
                }
            }
        }
        return null;
    }

    public PlanPojo createPlan(PlanPojo plan) {
        synchronized (generalMutex) {
            log.debug(String.format("Method 'createPlan' was called with data '%s'.", plan));

            plan.id = UUID.randomUUID();

            plans.put(plan.id, plan);

            return plan;
        }
    }

    @Override
    public void deletePlan(UUID id) {
        synchronized (generalMutex) {
            log.debug(String.format("Method 'deletePlan' was called with data: \n%s", id));

            plans.remove(id);
        }
    }

    @Override
    public List<PlanPojo> getPlans() {
        synchronized (generalMutex) {
            log.debug("Method 'getPlans' was called.");

            return new ArrayList<>(plans.values());
        }
    }

    @Override
    public SubscriptionPojo createSubscription(SubscriptionPojo subscriptionPojo) {
        synchronized (generalMutex) {
            log.debug("Method 'createSubscription' was called with data '{}'.", subscriptionPojo);

            subscriptionPojo.id = UUID.randomUUID();

            subscriptions.put(subscriptionPojo.id, subscriptionPojo);

            return subscriptionPojo;
        }
    }

    @Override
    public void deleteSubscription(UUID id) {
        synchronized (generalMutex) {
            log.debug("Method 'deleteSubscription' was called with data: \n{}", id);

            subscriptions.remove(id);
        }
    }

    @Override
    public List<SubscriptionPojo> getSubscriptions() {
        synchronized (generalMutex) {
            log.debug("Method 'getSubscriptions' was called.");

            return new ArrayList<>(subscriptions.values());
        }
    }

    @Override
    public List<SubscriptionPojo> getSubscriptions(UUID customerId) {
        synchronized (generalMutex) {
            log.debug("Method 'getSubscriptions' was called.");

            return subscriptions.values().stream()
                .filter(subscription -> Objects.equals(customerId, subscription.customerId))
                .collect(Collectors.toList());
        }
    }
}
