package com.auvexis.vanguard.modules.auth.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.auvexis.vanguard.shared.events.UserEmailVerificationEvent;
import com.auvexis.vanguard.shared.infrastructure.rabbitmq.MessagingConfig;

@Service
public class UserPublisher {

    private final RabbitTemplate rabbitTemplate;

    public UserPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserEmailVerification(UserEmailVerificationEvent event) {
        rabbitTemplate.convertAndSend(
                MessagingConfig.APP_EXCHANGE,
                "auth.user.email.verification",
                event);
    }

    public void publishUserEmailVerificationResend(UserEmailVerificationEvent event) {
        rabbitTemplate.convertAndSend(
                MessagingConfig.APP_EXCHANGE,
                "auth.user.email.verification.resend",
                event);
    }

}
