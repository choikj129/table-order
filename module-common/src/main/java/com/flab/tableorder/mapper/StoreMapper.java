package com.flab.tableorder.mapper;

import com.flab.tableorder.document.Store;
import com.flab.tableorder.dto.StoreDTO;

import org.mapstruct.Mapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = ObjectIdMapper.class)
public interface StoreMapper {

    StoreMapper INSTANCE = Mappers.getMapper(StoreMapper.class);

    @Mapping(source = "storeId", target = "storeId")
    Store toEntity(StoreDTO storeDTO);
    @Mapping(source = "storeId", target = "storeId")
    StoreDTO toDTO(Store store);

//    @AfterMapping
//    default void linkEntities(@MappingTarget Store store) {
//        for (Category category : store.getCategories()) {
//            if (category.getStore() != null) continue;
//
//            category.setStore(store);
//            for (Menu menu : category.getMenu()) {
//                if (menu.getCategory() == null) menu.setCategory(category);
//                if (menu.getStore() == null) menu.setStore(store);
//            }
//        }
//    }
}
