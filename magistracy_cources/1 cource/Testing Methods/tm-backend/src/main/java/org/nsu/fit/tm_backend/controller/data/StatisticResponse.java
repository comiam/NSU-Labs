package org.nsu.fit.tm_backend.controller.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatisticResponse {
    @JsonProperty("customers")
    private Set<StatisticPerCustomerResponse> customers;

    @JsonProperty("overallBalance")
    private Integer overallBalance;

    @JsonProperty("overallFee")
    private Integer overallFee;
}
