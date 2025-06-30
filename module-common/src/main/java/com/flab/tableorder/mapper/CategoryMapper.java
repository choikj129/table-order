package com.flab.tableorder.mapper;

import com.flab.tableorder.document.Category;
import com.flab.tableorder.dto.MenuCategoryDTO;

import java.util.List;

import com.flab.tableorder.dto.OptionCategoryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = ObjectIdMapper.class)
public interface CategoryMapper {
    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    @Mapping(source = "categoryId", target = "categoryId")
    List<MenuCategoryDTO> toDTO(List<Category> menuCategories);
    @Mapping(source = "categoryId", target = "categoryId")
    List<OptionCategoryDTO> toOptionDTO(List<Category> optionCategories);
}
