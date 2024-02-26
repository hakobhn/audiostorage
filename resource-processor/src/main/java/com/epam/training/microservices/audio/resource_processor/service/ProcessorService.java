package com.epam.training.microservices.audio.resource_processor.service;

import com.epam.training.microservices.audio.resource_processor.model.AudioMessage;

public interface ProcessorService {
    void processAudioFile(AudioMessage message, String traceId);
    void deleteAudioFile(Long resourceId, String traceId);
}
