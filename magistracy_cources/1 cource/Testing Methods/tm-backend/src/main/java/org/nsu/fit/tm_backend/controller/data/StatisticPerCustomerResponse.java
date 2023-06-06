package org.nsu.fit.tm_backend.controller.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nsu.fit.tm_backend.service.data.StatisticPerCustomerBO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatisticPerCustomerResponse {
    @JsonProperty("customerId")
    private UUID customerId;

    @JsonProperty("overallBalance")
    private Integer overallBalance;

    @JsonProperty("overallFee")
    private Integer overallFee;
}
