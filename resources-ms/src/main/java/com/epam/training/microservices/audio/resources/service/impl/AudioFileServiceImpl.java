package com.epam.training.microservices.audio.resources.service.impl;

import com.epam.training.microservices.audio.resources.domain.model.AudioFile;
import com.epam.training.microservices.audio.resources.domain.repository.AudioFileRepository;
import com.epam.training.microservices.audio.resources.dto.AudioDto;
import com.epam.training.microservices.audio.resources.dto.AudioInput;
import com.epam.training.microservices.audio.resources.exception.NotFoundException;
import com.epam.training.microservices.audio.resources.mapper.AudioMapper;
import com.epam.training.microservices.audio.resources.service.AudioFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
public class AudioFileServiceImpl implements AudioFileService {

    private final AudioFileRepository fileRepository;
    private final AudioMapper audioMapper;

    @Override
    public AudioDto create(AudioInput input) {
        AudioFile entity = audioMapper.fileInputToAudioEntity(input);
        entity = fileRepository.save(entity);
        return audioMapper.fileToAudioDto(entity);
    }

    @Override
    public AudioDto getById(long id) {
        return audioMapper.fileToAudioDto(fileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Audio file with id "+ id +", not found.")));
    }

    @Override
    public Optional<AudioDto> findById(long id) {
        return fileRepository.findById(id).map(audioMapper::fileToAudioDto);
    }

    @Override
    public void delete(long id) {
        fileRepository.deleteById(id);
    }

    @Override
    public void deleteByLocation(String location) {
        fileRepository.deleteByLocation(location);
    }

}
