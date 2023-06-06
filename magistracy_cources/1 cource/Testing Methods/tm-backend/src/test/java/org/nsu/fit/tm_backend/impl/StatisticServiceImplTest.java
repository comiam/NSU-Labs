package org.nsu.fit.tm_backend.impl;

import lombok.var;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.repository.data.SubscriptionPojo;
import org.nsu.fit.tm_backend.service.CustomerService;
import org.nsu.fit.tm_backend.service.SubscriptionService;
import org.nsu.fit.tm_backend.service.data.StatisticBO;
import org.nsu.fit.tm_backend.service.data.StatisticPerCustomerBO;
import org.nsu.fit.tm_backend.service.impl.StatisticServiceImpl;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;


// Лабораторная 2: покрыть unit тестами класс StatisticServiceImpl на 100%.
// Чтобы протестировать метод calculate() используйте Mockito.spy(statisticService) и переопределите метод
// calculate(UUID customerId) чтобы использовать стратегию "разделяй и властвуй".
@ExtendWith(MockitoExtension.class)
public class StatisticServiceImplTest {
    private static final int CUSTOMER_BALANCE = 1000;
    private static final int SUBSCRIPTION_FEE = 100;
    private static final int SUBSCRIPTION_PER_CUSTOMER_COUNT = 2;

    @Mock
    private CustomerService customerService;

    @Mock
    SubscriptionService subscriptionService;

    @InjectMocks
    private StatisticServiceImpl statisticService;

    private final Set<UUID> customerUUIDSet = Arrays.stream(new String[]{
            "02d79e8d-4004-47e0-af15-a6ac076718f0",
            "e839b22f-c59b-4270-b272-05fcbd022ebd",
            "1313bca8-3617-43fb-8ee0-fd32534b24dd",
            "6c65ad19-7eb8-4fe7-8974-1eee023048e5"
    }).map(UUID::fromString).collect(Collectors.toSet());

    private Map<UUID, CustomerPojo> getTestCustomers() {
        var map = new HashMap<UUID, CustomerPojo>();
        var uuidArray = customerUUIDSet.toArray(new UUID[]{});

        map.put(uuidArray[0], null);
        for (int i = 1; i < uuidArray.length; i++) {
            var customerPojo = new CustomerPojo();
            customerPojo.id = uuidArray[i];
            customerPojo.firstName = "ryan" + i;
            customerPojo.lastName = "gosling" + i;
            customerPojo.login = "ryan" + i + "@gosling.com";
            customerPojo.pass = "itsnotryanrednolds" + i;
            customerPojo.balance = CUSTOMER_BALANCE;

            map.put(uuidArray[i], customerPojo);
        }

        return map;
    }

    private List<SubscriptionPojo> generateSubscriptionsForCustomer(CustomerPojo customerPojo) {
        if (customerPojo == null) {
            return Collections.emptyList();
        }

        var list = new ArrayList<SubscriptionPojo>();

        for (int i = 0; i < SUBSCRIPTION_PER_CUSTOMER_COUNT; i++) {
            var sub = new SubscriptionPojo();
            sub.customerId = customerPojo.id;
            sub.id = UUID.randomUUID();
            sub.planDetails = "Some details" + i;
            sub.planFee = SUBSCRIPTION_FEE;
            sub.planId = UUID.randomUUID();
            sub.planName = "Plan " + i;

            list.add(sub);
        }

        return list;
    }

    @Test
    void testCommonCalculate() {
        lenient().when(customerService.getCustomerIds()).thenReturn(customerUUIDSet);

        var setupCustomerMap = getTestCustomers();
        var spyStatistics = Mockito.spy(statisticService);

        var customerStatsArray = new HashSet<StatisticPerCustomerBO>();

        for (var entry : setupCustomerMap.entrySet()) {
            if (entry.getValue() == null) {
                doReturn(null).when(spyStatistics).calculate(entry.getKey());
                continue;
            }

            var customerSubs = generateSubscriptionsForCustomer(entry.getValue());
            var customerStats = new StatisticPerCustomerBO();

            customerStats.setOverallBalance(entry.getValue().balance);

            var subscriptionIds = new HashSet<UUID>();
            int overallFee = 0;
            for (var subscription : customerSubs) {
                subscriptionIds.add(subscription.id);
                overallFee += subscription.planFee;
            }

            customerStats.setOverallFee(overallFee);
            customerStats.setSubscriptionIds(subscriptionIds);

            customerStatsArray.add(customerStats);

            doReturn(customerStats).when(spyStatistics).calculate(entry.getKey());
        }

        var customerRealCount = customerUUIDSet.size() - 1; // мы исключаем кастомера, что не смогли найти
        assertEquals(spyStatistics.calculate(), new StatisticBO(
                customerStatsArray,
                customerRealCount * CUSTOMER_BALANCE, // на данный момент 3000,
                customerRealCount * SUBSCRIPTION_PER_CUSTOMER_COUNT * SUBSCRIPTION_FEE // на данный момент 600
        ));
    }

    @Test
    void testCustomerCalculate() {
        lenient().when(customerService.getCustomerIds()).thenReturn(customerUUIDSet);

        var setupCustomerMap = getTestCustomers();

        var customerSubsMap = new HashMap<UUID, Set<UUID>>();
        for (var entry : setupCustomerMap.entrySet()) {
            lenient().when(customerService.lookupCustomer(entry.getKey())).thenReturn(entry.getValue());

            var customerSubs = generateSubscriptionsForCustomer(entry.getValue());
            customerSubsMap.put(entry.getKey(), customerSubs.stream().map(i -> i.id).collect(Collectors.toSet()));

            lenient().when(subscriptionService.getSubscriptions(entry.getKey())).thenReturn(customerSubs);
        }

        var uuidArray = customerUUIDSet.toArray(new UUID[]{});
        for (int i = 0; i < uuidArray.length; i++) {
            var customerUUID = uuidArray[i];
            var subscriptionInfo = i == 0 ? null : new StatisticPerCustomerBO(
                    null,
                    customerSubsMap.get(customerUUID),
                    CUSTOMER_BALANCE,
                    SUBSCRIPTION_PER_CUSTOMER_COUNT * SUBSCRIPTION_FEE
            );

            assertEquals(statisticService.calculate(customerUUID), subscriptionInfo);
        }
    }
}
