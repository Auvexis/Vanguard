package com.auvexis.vanguard.modules.email.auth;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthMessagingConfig {

    @Bean
    public Queue userEmailVerificationQueue() {
        return QueueBuilder
                .durable(AuthQueues.USER_EMAIL_VERIFICATION_QUEUE)
                .build();
    }

    @Bean
    public Binding userEmailVerificationBinding(
            Queue userEmailVerificationQueue,
            TopicExchange topicExchange) {
        return BindingBuilder
                .bind(userEmailVerificationQueue)
                .to(topicExchange)
                .with("auth.user.email.verification");
    }

    @Bean
    public Queue userEmailVerificationResendQueue() {
        return QueueBuilder
                .durable(AuthQueues.USER_EMAIL_VERIFICATION_RESEND_QUEUE)
                .build();
    }

    @Bean
    public Binding userEmailVerificationResendBinding(
            Queue userEmailVerificationResendQueue,
            TopicExchange topicExchange) {
        return BindingBuilder
                .bind(userEmailVerificationResendQueue)
                .to(topicExchange)
                .with("auth.user.email.verification.resend");
    }

}
