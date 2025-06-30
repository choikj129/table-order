package com.flab.tableorder.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter @Setter
public class MenuCategoryDTO extends CategoryDTO implements Serializable {
    private List<MenuDTO> menu;
}
