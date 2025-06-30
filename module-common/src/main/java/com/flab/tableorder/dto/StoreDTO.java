package com.flab.tableorder.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter @Data
public class StoreDTO {
    private String storeId;
    private String apiKey;

    private List<MenuCategoryDTO> categories;
}
