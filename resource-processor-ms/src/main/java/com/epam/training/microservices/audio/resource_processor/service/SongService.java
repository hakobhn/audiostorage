package com.epam.training.microservices.audio.resource_processor.service;

import com.epam.training.microservices.audio.resource_processor.model.AudioMetadata;

public interface SongService {
    void addSong(AudioMetadata metadata) throws Exception;
    void deleteSong(Long resourceId);
}
