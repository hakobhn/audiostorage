package com.epam.training.microservices.audio.songs.service;

import com.epam.training.microservices.audio.songs.domain.model.Song;
import com.epam.training.microservices.audio.songs.domain.repository.SongRepository;
import com.epam.training.microservices.audio.songs.dto.SongDto;
import com.epam.training.microservices.audio.songs.exception.NotFoundException;
import com.epam.training.microservices.audio.songs.mapper.SongMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


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

    public Map<String, Object> getPage(String name, Pageable paging) {
        Page<Song> songEntities;
        if (name == null)
            songEntities = songRepository.findAll(paging);
        else
            songEntities = songRepository.findByNameContaining(name, paging);

        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("data", songEntities.getContent().stream()
                .map(songMapper::entityToDto)
                .collect(Collectors.toUnmodifiableList())
        );
        pageResult.put("currentPage", songEntities.getNumber());
        pageResult.put("totalItems", songEntities.getTotalElements());
        pageResult.put("totalPages", songEntities.getTotalPages());

        return pageResult;
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
