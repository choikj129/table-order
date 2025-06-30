package com.tableorder.mapper;

import com.tableorder.document.Option;
import com.tableorder.dto.OptionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = ObjectIdMapper.class)
public interface OptionMapper {
    OptionMapper INSTANCE = Mappers.getMapper(OptionMapper.class);

    @Mapping(source = "optionId", target = "optionId")
    List<OptionDTO> toDTO(List<Option> optionList);

    @Mapping(source = "optionId", target = "optionId")
    OptionDTO toDTO(Option option);
}
