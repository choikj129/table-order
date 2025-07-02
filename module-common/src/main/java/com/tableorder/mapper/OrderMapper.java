package com.tableorder.mapper;

import com.tableorder.document.Order;
import com.tableorder.dto.OrderDTO;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = ObjectIdMapper.class)
public interface OrderMapper {
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    List<OrderDTO> toDTO(List<Order> optionList);

    Order toEntity(OrderDTO optionList);
}
