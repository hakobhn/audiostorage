package com.epam.training.microservices.audio.resource_processor.service;

import com.epam.training.microservices.audio.resource_processor.model.AudioInput;
import com.epam.training.microservices.audio.resource_processor.model.AudioShort;

public interface ResourcesService {
    AudioShort save(AudioInput input, String traceId);
    void delete(String key, String traceId);
    void makePermanent(Long id, String traceId);
}
