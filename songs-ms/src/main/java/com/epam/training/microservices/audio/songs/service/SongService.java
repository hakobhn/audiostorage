package com.epam.training.microservices.audio.songs.service;

import com.epam.training.microservices.audio.songs.domain.model.Song;
import com.epam.training.microservices.audio.songs.domain.repository.SongRepository;
import com.epam.training.microservices.audio.songs.dto.SongDto;
import com.epam.training.microservices.audio.songs.exception.NotFoundException;
import com.epam.training.microservices.audio.songs.mapper.SongMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;
    private final SongMapper songMapper;

    public SongDto create(SongDto dto) {
        Song entity = songMapper.dtoToEntity(dto);
        entity = songRepository.save(entity);
        dto.setId(entity.getId());
        return dto;
    }

    public SongDto getById(String id) {
        return songMapper.entityToDto(songRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Song with id "+ id +", not found.")));
    }

    public Optional<SongDto> findByResourceId(long id) {
        return songRepository.findByResourceId(id).map(songMapper::entityToDto);
    }

    public void delete(String id) {
        songRepository.deleteById(id);
    }

    public void deleteByResourceId(long id) {
        songRepository.findByResourceId(id).ifPresent(
                song -> songRepository.deleteById(song.getId())
        );
    }

}
