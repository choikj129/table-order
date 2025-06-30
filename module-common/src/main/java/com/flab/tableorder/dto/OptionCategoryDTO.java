package com.flab.tableorder.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class OptionCategoryDTO extends CategoryDTO {
    private List<OptionDTO> options;
}
