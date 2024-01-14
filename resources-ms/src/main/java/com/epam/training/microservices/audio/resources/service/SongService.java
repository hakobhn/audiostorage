package com.epam.training.microservices.audio.resources.service;

import com.epam.training.microservices.audio.resources.dto.AudioMetadata;

public interface SongService {
    void addSong(AudioMetadata metadata);
}
