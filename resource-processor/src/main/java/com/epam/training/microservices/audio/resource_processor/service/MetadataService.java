package com.epam.training.microservices.audio.resource_processor.service;

import com.epam.training.microservices.audio.resource_processor.model.AudioMetadata;

public interface MetadataService {
    AudioMetadata extract(byte[] bytes);

    String detectContentType(byte[] bytes);
}
