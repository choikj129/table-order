package com.tableorder.mapper;

import com.tableorder.document.Call;
import com.tableorder.dto.CallDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = ObjectIdMapper.class)
public interface CallMapper {
    CallMapper INSTANCE = Mappers.getMapper(CallMapper.class);

    @Mapping(source = "callId", target = "callId")
    List<CallDTO> toDTO(List<Call> callList);
}
