package com.epam.training.microservices.audio.songs.mapper;


import com.epam.training.microservices.audio.songs.domain.model.Song;
import com.epam.training.microservices.audio.songs.dto.SongDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SongMapper {

    Song dtoToEntity(SongDto dto);
    SongDto entityToDto(Song entity);

}
