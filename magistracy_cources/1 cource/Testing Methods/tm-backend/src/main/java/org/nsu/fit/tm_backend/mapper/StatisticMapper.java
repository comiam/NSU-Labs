package org.nsu.fit.tm_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.nsu.fit.tm_backend.controller.data.StatisticPerCustomerResponse;
import org.nsu.fit.tm_backend.controller.data.StatisticResponse;
import org.nsu.fit.tm_backend.service.data.StatisticBO;
import org.nsu.fit.tm_backend.service.data.StatisticPerCustomerBO;

@Mapper
public interface StatisticMapper {
    StatisticMapper INSTANCE = Mappers.getMapper(StatisticMapper.class);

    @Mappings( {
        @Mapping(target = "customers", source = "customers"),
        @Mapping(target = "overallBalance", source = "overallBalance"),
        @Mapping(target = "overallFee", source = "overallFee")
    })
    StatisticResponse toStatisticResponse(StatisticBO statistic);

    @Mappings( {
        @Mapping(target = "customerId", source = "customerId"),
        @Mapping(target = "overallBalance", source = "overallBalance"),
        @Mapping(target = "overallFee", source = "overallFee")
    })
    StatisticPerCustomerResponse toStatisticPerCustomerResponse(StatisticPerCustomerBO statisticPerCustomer);
}
