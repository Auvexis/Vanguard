package com.auvexis.vanguard.modules.email.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import com.auvexis.vanguard.shared.events.UserEmailVerificationEvent;

@ExtendWith(MockitoExtension.class)
class AuthEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private AuthEmailService authEmailService;

    @BeforeEach
    void setUp() {
        authEmailService = new AuthEmailService(mailSender);
        ReflectionTestUtils.setField(authEmailService, "smtpFrom", "noreply@vanguard.com");
        ReflectionTestUtils.setField(authEmailService, "clientUrl", "http://localhost:3000");
    }

    @Test
    void onUserEmailVerification_ShouldSendEmail() {
        UserEmailVerificationEvent event = new UserEmailVerificationEvent(
                UUID.randomUUID(),
                "test@example.com",
                "Test User",
                "token123");

        authEmailService.onUserEmailVerification(event);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void onUserEmailVerificationResend_ShouldSendEmail() {
        UserEmailVerificationEvent event = new UserEmailVerificationEvent(
                UUID.randomUUID(),
                "test@example.com",
                "Test User",
                "token123");

        authEmailService.onUserEmailVerificationResend(event);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
