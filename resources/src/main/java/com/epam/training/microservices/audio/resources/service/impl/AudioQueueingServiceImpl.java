package com.epam.training.microservices.audio.resources.service.impl;

import com.epam.training.microservices.audio.resources.component.Tracer;
import com.epam.training.microservices.audio.resources.dto.AudioMessage;
import com.epam.training.microservices.audio.resources.service.AudioQueueingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.epam.training.microservices.audio.resources.component.TracingConstants.AUTH_HEADER;
import static com.epam.training.microservices.audio.resources.component.TracingConstants.CURRENT_TRACE_ID_HEADER;

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

    private final Tracer tracer;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendMessage(AudioMessage message) {
        log.debug("Trace id: {}", tracer.traceId());
        log.info("Passing add new audio massage to queue {}", message.getDetailsId());

        rabbitTemplate.convertAndSend(addExchangeName, addRoutingKey, message, m -> {
            m.getMessageProperties().getHeaders().put(CURRENT_TRACE_ID_HEADER, tracer.traceId());
            m.getMessageProperties().getHeaders().put(AUTH_HEADER,
                    Arrays.stream(tracer.accessToke().split("(?<=\\G.{" + 256 + "})")).collect(Collectors.toList()));
            return m;
        });
    }

    @Override
    public void deleteMessage(long resourceId) {
        log.debug("Trace id: {}", tracer.traceId());
        log.info("Sending the delete request through queue message {}", resourceId);
        rabbitTemplate.convertAndSend(addExchangeName, addRoutingKey, resourceId, m -> {
            m.getMessageProperties().getHeaders().put(CURRENT_TRACE_ID_HEADER, tracer.traceId());
            return m;
        });
    }
}
