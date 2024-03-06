package com.epam.training.microservices.audio.resource_processor.service;

import com.epam.training.microservices.audio.resource_processor.model.AudioInput;
import com.epam.training.microservices.audio.resource_processor.model.AudioShort;

public interface ResourcesService {
    AudioShort save(AudioInput input, String traceId, String accessToken);
    void delete(String key, String traceId, String accessToken);
    void makePermanent(Long id, String traceId);
}
