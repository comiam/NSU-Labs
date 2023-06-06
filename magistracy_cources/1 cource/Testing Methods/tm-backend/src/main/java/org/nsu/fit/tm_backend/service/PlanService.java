package org.nsu.fit.tm_backend.service;

import java.util.List;
import java.util.UUID;
import org.jvnet.hk2.annotations.Contract;
import org.nsu.fit.tm_backend.repository.data.PlanPojo;

@Contract
public interface PlanService {
    PlanPojo createPlan(PlanPojo plan);

    void deletePlan(UUID id);

    List<PlanPojo> getPlans(UUID customerId);
}
