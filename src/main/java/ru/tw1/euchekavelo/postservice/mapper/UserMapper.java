package ru.tw1.euchekavelo.postservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.tw1.euchekavelo.postservice.dto.external.response.UserResponseDto;
import ru.tw1.euchekavelo.postservice.model.external.ExternalUser;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    ExternalUser UserResponseDtoToExternalUser(UserResponseDto userResponseDto);
}
