package org.nsu.fit.services.rest.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionPojo {
    @JsonProperty("id")
    public UUID id;

    @JsonProperty("customer_id")
    public UUID customerId;

    @JsonProperty("plan_id")
    public UUID planId;

    @JsonProperty("plan_name")
    public String planName;

    @JsonProperty("plan_details")
    public String planDetails;

    @JsonProperty("plan_fee")
    public Integer planFee;
}
