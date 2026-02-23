package com.auvexis.vanguard.shared.infrastructure.rabbitmq;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for RabbitMQ messaging infrastructure.
 * Defines the primary TopicExchange for the application and sets up JSON
 * message conversion for interoperability between microservices/modules.
 */
@Configuration
public class MessagingConfig {

    public static final String APP_EXCHANGE = "app.exchange";

    @Bean
    public TopicExchange appExchange() {
        return new TopicExchange(APP_EXCHANGE);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
