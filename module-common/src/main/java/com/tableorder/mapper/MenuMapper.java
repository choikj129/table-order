package com.tableorder.mapper;

import com.tableorder.document.Menu;
import com.tableorder.dto.MenuDTO;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = ObjectIdMapper.class)
public interface MenuMapper {
    MenuMapper INSTANCE = Mappers.getMapper(MenuMapper.class);

    @Mapping(source = "menuId", target = "menuId")
    List<MenuDTO> toDTO(List<Menu> menuList);

    @Mapping(source = "menuId", target = "menuId")
    MenuDTO toDTO(Menu menu);
}
