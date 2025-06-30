package com.flab.tableorder.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Data
public class CategoryDTO {
    private String categoryId;
    private String categoryName;
    // multiple == true : 다중 선택
    private boolean multiple = false;
    // required == true : 필수 선택
    private boolean required = false;
    // maxSelect == 0 : 수량 제한 없음
    private int maxSelect = 0;
}
