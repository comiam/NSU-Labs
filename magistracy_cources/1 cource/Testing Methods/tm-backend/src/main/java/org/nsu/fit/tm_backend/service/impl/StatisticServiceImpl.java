package org.nsu.fit.tm_backend.service.impl;

import java.util.HashSet;
import java.util.UUID;
import javax.inject.Inject;
import lombok.var;
import org.jvnet.hk2.annotations.Service;
import org.nsu.fit.tm_backend.service.CustomerService;
import org.nsu.fit.tm_backend.service.StatisticService;
import org.nsu.fit.tm_backend.service.SubscriptionService;
import org.nsu.fit.tm_backend.service.data.StatisticBO;
import org.nsu.fit.tm_backend.service.data.StatisticPerCustomerBO;

@Service
public class StatisticServiceImpl implements StatisticService {
    @Inject
    private CustomerService customerService;

    @Inject
    private SubscriptionService subscriptionService;

    public StatisticBO calculate() {
        var customers = new HashSet<StatisticPerCustomerBO>();

        int overallBalance = 0;
        int overallFee = 0;
        var customerIds = customerService.getCustomerIds();
        for (var customerId : customerIds) {
            var customer = calculate(customerId);

            if (customer == null) {
                continue;
            }

            customers.add(customer);

            overallBalance += customer.getOverallBalance();

            overallFee += customer.getOverallFee();
        }

        return StatisticBO.builder()
            .customers(customers)
            .overallBalance(overallBalance)
            .overallFee(overallFee)
            .build();
    }

    public StatisticPerCustomerBO calculate(UUID customerId) {
        var customer = customerService.lookupCustomer(customerId);

        if (customer == null) {
            return null;
        }

        var result = new StatisticPerCustomerBO();
        result.setOverallBalance(customer.balance);

        var subscriptions = subscriptionService.getSubscriptions(customerId);
        var subscriptionIds = new HashSet<UUID>();
        int overallFee = 0;
        for (var subscription : subscriptions) {
            subscriptionIds.add(subscription.id);
            overallFee += subscription.planFee;
        }

        result.setOverallFee(overallFee);

        result.setSubscriptionIds(subscriptionIds);

        return result;
    }
}
