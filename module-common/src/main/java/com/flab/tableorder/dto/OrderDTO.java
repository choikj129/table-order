package com.flab.tableorder.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class OrderDTO {
    private String menuId;
    private int quantity;
    private int price;
    private List<OrderOptionDTO> options;
}