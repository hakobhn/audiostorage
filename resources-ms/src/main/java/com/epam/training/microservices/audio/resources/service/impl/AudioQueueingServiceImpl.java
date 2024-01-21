package com.epam.training.microservices.audio.resources.service.impl;

import com.epam.training.microservices.audio.resources.dto.AudioMessage;
import com.epam.training.microservices.audio.resources.service.AudioQueueingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AudioQueueingServiceImpl implements AudioQueueingService {
    @Value("${audio.rabbitmq.add.exchange}")
    private String addExchangeName;
    @Value("${audio.rabbitmq.add.routing_key}")
    private String addRoutingKey;

    @Value("${audio.rabbitmq.delete.exchange}")
    private String deleteExchangeName;
    @Value("${audio.rabbitmq.delete.routing_key}")
    private String deleteRoutingKey;

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendMessage(AudioMessage message) {
        log.info("Sending the add request through queue message");
        rabbitTemplate.convertAndSend(addExchangeName, addRoutingKey, message);
    }

    @Override
    public void deleteMessage(long resourceId) {
        log.info("Sending the delete request through queue message");
        rabbitTemplate.convertAndSend(deleteExchangeName, deleteRoutingKey, resourceId);
    }
}
