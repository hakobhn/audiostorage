package com.epam.training.microservices.audio.storage.mapper;


import com.epam.training.microservices.audio.storage.domain.model.Storage;
import com.epam.training.microservices.audio.storage.dto.StorageDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StorageMapper {

//    @Mapping(source = "type", target = "type", qualifiedByName = "InputStringToEnum")
    Storage dtoToEntity(StorageDto dto);
    StorageDto entityToDto(Storage entity);

//    @Named("InputStringToEnum")
//    default StorageType inputStringToEnum(String name) {
//        return StorageType.valueOf(name);
//    }
}
