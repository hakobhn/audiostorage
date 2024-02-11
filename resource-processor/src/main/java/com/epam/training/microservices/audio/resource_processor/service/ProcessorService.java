package com.epam.training.microservices.audio.resource_processor.service;

import com.epam.training.microservices.audio.resource_processor.model.AudioMessage;

public interface ProcessorService {
    void processAudioFile(AudioMessage message);
    void deleteAudioFile(Long resourceId);
}
