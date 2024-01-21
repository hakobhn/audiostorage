package com.epam.training.microservices.audio.resource_processor.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigureRabbitMq {

    @Value("${audio.rabbitmq.add.queue}")
    private String addQueueName;
    @Value("${audio.rabbitmq.delete.queue}")
    private String deleteQueueName;
    @Value("${audio.rabbitmq.add.exchange}")
    private String addAudioExchangeName;
    @Value("${audio.rabbitmq.delete.exchange}")
    private String deleteAudioExchangeName;
    @Value("${audio.rabbitmq.add.routing_key}")
    private String addAudioRoutingKey;
    @Value("${audio.rabbitmq.delete.routing_key}")
    private String deleteAudioRoutingKey;

    @Bean
    @Qualifier("addQueue")
    public Queue addQueue() {
        return new Queue(addQueueName, false);
    }

    @Bean
    @Qualifier("addExchange")
    public TopicExchange addExchange() {
        return new TopicExchange(addAudioExchangeName);
    }

    @Bean
    @Qualifier("addBinding")
    public Binding addBinding() {
        return BindingBuilder.bind(addQueue()).to(addExchange()).with(addAudioRoutingKey);
    }

    @Bean
    @Qualifier("deleteQueue")
    public Queue deleteQueue() {
        return new Queue(deleteQueueName, false);
    }

    @Bean
    @Qualifier("deleteExchange")
    public TopicExchange deleteExchange() {
        return new TopicExchange(deleteAudioExchangeName);
    }

    @Bean
    @Qualifier("deleteBinding")
    public Binding deleteBinding() {
        return BindingBuilder.bind(deleteQueue()).to(deleteExchange()).with(deleteAudioRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
