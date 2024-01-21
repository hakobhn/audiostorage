package com.epam.training.microservices.audio.resource_processor.listener;

import com.epam.training.microservices.audio.resource_processor.model.AudioMessage;
import com.epam.training.microservices.audio.resource_processor.service.ProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AudioConsumer {

    private final ProcessorService processService;

    @RabbitListener(queues = "${audio.rabbitmq.add.queue}")
    public void consume(AudioMessage message) {
        log.info("Received message {}", message.getLocation());
        try {
            processService.processAudioFile(message);
        } catch (Exception e) {
            log.warn("Not valid audio file with location {}", message.getLocation());
        }
    }

    @RabbitListener(queues = "${audio.rabbitmq.delete.queue}")
    public void delete(Long resourceId) {
        log.info("Received message to delete {}", resourceId);
        try {
            processService.deleteAudioFile(resourceId);
        } catch (Exception e) {
            log.warn("Unable to delete audio file with resourceId {}", resourceId);
        }
    }
}
