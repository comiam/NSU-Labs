package org.nsu.fit.tm_backend.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jvnet.hk2.annotations.Contract;
import org.nsu.fit.tm_backend.repository.Repository;
import org.nsu.fit.tm_backend.repository.data.PlanPojo;
import org.nsu.fit.tm_backend.repository.data.SubscriptionPojo;

@Contract
public interface SubscriptionService {
    SubscriptionPojo createSubscription(SubscriptionPojo subscriptionPojo);

    void deleteSubscription(UUID subscriptionId);

    List<SubscriptionPojo> getSubscriptions(UUID customerId);
}
