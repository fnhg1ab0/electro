package com.electro.mapper.customer;

import com.electro.dto.customer.CustomerRequest;
import com.electro.dto.customer.CustomerResponse;
import com.electro.entity.customer.Customer;
import com.electro.mapper.GenericMapper;
import com.electro.mapper.authentication.UserMapper;
import com.electro.utils.MapperUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {MapperUtils.class, UserMapper.class})
public interface CustomerMapper extends GenericMapper<Customer, CustomerRequest, CustomerResponse> {

    @Override
    @Mapping(source = "customerGroupId", target = "customerGroup", qualifiedByName = "mapCustomerGroupIdToCustomerGroup")
    @Mapping(source = "customerResourceId", target = "customerResource", qualifiedByName = "mapCustomerResourceIdToCustomerResource")
    @Mapping(source = "customerStatusId", target = "customerStatus", qualifiedByName = "mapCustomerStatusIdToCustomerStatus")
    Customer requestToEntity(CustomerRequest request);

    @Override
    @Mapping(source = "customerGroupId", target = "customerGroup", qualifiedByName = "mapCustomerGroupIdToCustomerGroup")
    @Mapping(source = "customerResourceId", target = "customerResource", qualifiedByName = "mapCustomerResourceIdToCustomerResource")
    @Mapping(source = "customerStatusId", target = "customerStatus", qualifiedByName = "mapCustomerStatusIdToCustomerStatus")
    Customer partialUpdate(@MappingTarget Customer entity, CustomerRequest request);

}
