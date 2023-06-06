package org.nsu.fit.tm_backend.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.jvnet.hk2.annotations.Service;
import org.nsu.fit.tm_backend.repository.Repository;
import org.nsu.fit.tm_backend.repository.data.PlanPojo;
import org.nsu.fit.tm_backend.repository.data.SubscriptionPojo;
import org.nsu.fit.tm_backend.service.SubscriptionService;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    @Inject
    private Repository repository;

    /**
     * Метод создает подписку. Ограничения:
     * 1. Подписки с таким планом пользователь не имеет.
     * 2. Стоймость подписки не превышает текущего баланса кастомера и после покупки вычитается из его баласа.
     */
    public SubscriptionPojo createSubscription(SubscriptionPojo subscriptionPojo) {
        return repository.createSubscription(subscriptionPojo);
    }

    public void deleteSubscription(UUID subscriptionId) {
        repository.deleteSubscription(subscriptionId);
    }

    /**
     * Возвращает список подписок для указанного customer'а.
     */
    public List<SubscriptionPojo> getSubscriptions(UUID customerId) {
        Map<UUID, PlanPojo> planIdToPlan = repository.getPlans().stream()
                .collect(Collectors.toMap(plan -> plan.id, plan -> plan));

        List<SubscriptionPojo> subscriptions;
        if (customerId == null) {
            subscriptions = repository.getSubscriptions();
        } else {
            subscriptions = repository.getSubscriptions(customerId);
        }

        // Дозаполняем поля, типа planName, planDetails и planFee.
        for (SubscriptionPojo subscription : subscriptions) {
            PlanPojo plan = planIdToPlan.getOrDefault(subscription.planId, null);
            if (plan != null) {
                subscription.planName = plan.name;
                subscription.planDetails = plan.details;
                subscription.planFee = plan.fee;
            }
        }

        return subscriptions;
    }
}
