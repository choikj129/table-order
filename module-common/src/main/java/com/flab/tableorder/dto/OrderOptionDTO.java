package com.flab.tableorder.dto;

import lombok.*;

@Getter @Setter @Data 
@NoArgsConstructor @AllArgsConstructor
public class OrderOptionDTO {
    private String optionId;
    private int quantity = 0;
    private int price;
}
