package com.epam.training.microservices.audio.resource_processor.service;

import com.epam.training.microservices.audio.resource_processor.model.AudioMetadata;

public interface SongService {
    void addSong(AudioMetadata metadata, String traceId) throws Exception;
    void deleteSong(Long resourceId, String traceId) throws Exception;
}
