package com.epam.training.microservices.audio.resources.mapper;

import com.epam.training.microservices.audio.resources.domain.model.AudioFile;
import com.epam.training.microservices.audio.resources.dto.AudioDto;
import com.epam.training.microservices.audio.resources.dto.AudioInput;
import com.epam.training.microservices.audio.resources.util.DataUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = DataUtils.class)
public interface AudioMapper {

    @Mapping(source="createdAt", target = "postedAt")
    @Mapping(target="size", expression="java(DataUtils.toHumanReadableSIPrefixes(file.getBytes()))" )
    AudioDto fileToAudioDto(AudioFile file);

    AudioFile fileInputToAudioEntity(AudioInput input);

}
