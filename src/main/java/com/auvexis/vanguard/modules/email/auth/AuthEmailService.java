package com.auvexis.vanguard.modules.email.auth;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auvexis.vanguard.shared.events.UserEmailVerificationEvent;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;

/**
 * Asynchronous consumer for authentication-related email events.
 * Manages the dispatch of system emails (e.g., verification tokens) using
 * Spring's JavaMailSender, triggered by RabbitMQ events.
 */
@Service
public class AuthEmailService {

    @Value("${vanguard.client.url}")
    private String clientUrl;

    private final Resend resend;

    public AuthEmailService(Resend resend) {
        this.resend = resend;
    }

    /**
     * Listener for initial user registration events.
     * Generates and sends a welcome email containing the account verification link.
     */
    @RabbitListener(queues = AuthQueues.USER_EMAIL_VERIFICATION_QUEUE)
    public void onUserEmailVerification(UserEmailVerificationEvent event) {
        String subject = "Hey " + event.name().split(" ")[0] + ", Verify your email!";
        String body = "Hello " + event.name().split(" ")[0] + ",\n\nThanks for joining Vanguard!\n\n" +
                "Please click on the link below to verify your email address:\n\n" +
                clientUrl + "/auth/verify-email?user_id=" + event.id() +
                "&email_token=" + event.token() +
                "\n\nIf you did not create this account, please ignore this email.";

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("vanguard@auvexis.com")
                .to(event.email())
                .subject(subject)
                .html(body)
                .build();

        try {
            resend.emails().send(params);
        } catch (ResendException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @RabbitListener(queues = AuthQueues.USER_EMAIL_VERIFICATION_RESEND_QUEUE)
    public void onUserEmailVerificationResend(UserEmailVerificationEvent event) {
        String subject = "Hey " + event.name().split(" ")[0] + ", Resend verification email!";
        String body = "Hello " + event.name().split(" ")[0] + ",\n\nHere is your verification email:\n\n" +
                "Please click on the link below to verify your email address:\n\n" +
                clientUrl + "/auth/verify-email?user_id=" + event.id() +
                "&email_token=" + event.token() +
                "\n\nIf you did not request this email, please ignore this email.";

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("vanguard@auvexis.com")
                .to(event.email())
                .subject(subject)
                .html(body)
                .build();

        try {
            resend.emails().send(params);
        } catch (ResendException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

}
