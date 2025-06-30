package com.flab.tableorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @Data
@NoArgsConstructor @AllArgsConstructor
public class KafkaOrderDTO {
    private List<OrderDTO> orderList;
    private String storeId;
    private int tableNum;
}
