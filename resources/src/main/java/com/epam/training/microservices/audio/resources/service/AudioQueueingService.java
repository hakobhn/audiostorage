package com.epam.training.microservices.audio.resources.service;

import com.epam.training.microservices.audio.resources.dto.AudioMessage;

public interface AudioQueueingService {
    void sendMessage(AudioMessage message);
    void deleteMessage(long resourceId);
}
