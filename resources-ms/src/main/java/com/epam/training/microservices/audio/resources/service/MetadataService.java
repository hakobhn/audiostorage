package com.epam.training.microservices.audio.resources.service;

import com.epam.training.microservices.audio.resources.dto.AudioMetadata;

public interface MetadataService {
    AudioMetadata extract(byte[] bytes);

    String detectContentType(byte[] bytes);
}
