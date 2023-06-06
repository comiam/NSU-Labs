package org.nsu.fit.tm_backend.service.data;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticBO {
    private Set<StatisticPerCustomerBO> customers;

    // Общий остаточный баланс.
    private int overallBalance;

    // Общая сумма денег потраченных покупку различных планов.
    private int overallFee;
}
