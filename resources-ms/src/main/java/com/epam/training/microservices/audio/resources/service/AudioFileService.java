package com.epam.training.microservices.audio.resources.service;

import com.epam.training.microservices.audio.resources.dto.AudioDto;
import com.epam.training.microservices.audio.resources.dto.AudioInput;

import javax.swing.text.html.Option;
import java.util.Optional;


public interface AudioFileService {

    AudioDto create(AudioInput input);
    AudioDto getById(long id);
    Optional<AudioDto> findById(long id);
    void delete(long id);

}
