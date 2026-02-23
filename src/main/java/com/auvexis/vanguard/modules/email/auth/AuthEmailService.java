package com.auvexis.vanguard.modules.email.auth;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.auvexis.vanguard.shared.events.UserEmailVerificationEvent;

/**
 * Asynchronous consumer for authentication-related email events.
 * Manages the dispatch of system emails (e.g., verification tokens) using
 * Spring's JavaMailSender, triggered by RabbitMQ events.
 */
@Service
public class AuthEmailService {

    @Value("${vanguard.client.url}")
    private String clientUrl;

    @Value("${vanguard.smtp.username}")
    private String smtpFrom;

    private final JavaMailSender mailSender;

    public AuthEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
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

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(smtpFrom);
        message.setTo(event.email());
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    @RabbitListener(queues = AuthQueues.USER_EMAIL_VERIFICATION_RESEND_QUEUE)
    public void onUserEmailVerificationResend(UserEmailVerificationEvent event) {
        String subject = "Hey " + event.name().split(" ")[0] + ", Resend verification email!";
        String body = "Hello " + event.name().split(" ")[0] + ",\n\nHere is your verification email:\n\n" +
                "Please click on the link below to verify your email address:\n\n" +
                clientUrl + "/auth/verify-email?user_id=" + event.id() +
                "&email_token=" + event.token() +
                "\n\nIf you did not request this email, please ignore this email.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(smtpFrom);
        message.setTo(event.email());
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

}
