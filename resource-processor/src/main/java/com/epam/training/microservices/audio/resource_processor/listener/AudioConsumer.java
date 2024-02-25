package com.epam.training.microservices.audio.resource_processor.listener;

import com.epam.training.microservices.audio.resource_processor.model.AudioMessage;
import com.epam.training.microservices.audio.resource_processor.service.ProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import static com.epam.training.microservices.audio.resource_processor.component.TracingConstants.CURRENT_TRACE_ID_HEADER;
import static com.epam.training.microservices.audio.resource_processor.component.TracingConstants.TRACE_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AudioConsumer {

    private final ProcessorService processService;

    @RabbitListener(queues = "${audio.rabbitmq.add.queue}")
    public void consume(AudioMessage message, @Header(CURRENT_TRACE_ID_HEADER) String traceId) {
        log.info("Received new message {} location {} traceId {}", message.getName(), message.getLocation(), traceId);
        try {
            MDC.put(TRACE_ID, traceId);
            processService.processAudioFile(message, traceId);
            MDC.remove(TRACE_ID);
        } catch (Exception e) {
            log.error("Not valid audio file with location {}", message.getLocation());
        }
    }

    @RabbitListener(queues = "${audio.rabbitmq.delete.queue}")
    public void delete(Long resourceId, @Header(CURRENT_TRACE_ID_HEADER) String traceId) {
        log.info("Received message to delete {}, traceId {}", resourceId, traceId);
        try {
            processService.deleteAudioFile(resourceId, traceId);
        } catch (Exception e) {
            log.warn("Unable to delete audio file with resourceId {}", resourceId);
        }
    }

}
