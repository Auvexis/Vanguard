package com.auvexis.vanguard.modules.email.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.auvexis.vanguard.shared.events.UserEmailVerificationEvent;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.Emails;
import com.resend.services.emails.model.CreateEmailOptions;

@ExtendWith(MockitoExtension.class)
class AuthEmailServiceTest {

    @Mock
    private Resend resend;

    @Mock
    private Emails emails;

    @InjectMocks
    private AuthEmailService authEmailService;

    private final String clientUrl = "http://localhost:3000";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authEmailService, "clientUrl", clientUrl);
        when(resend.emails()).thenReturn(emails);
    }

    @Test
    @DisplayName("Should send verification email successfully")
    void shouldSendVerificationEmailSuccessfully() throws ResendException {
        // Arrange
        UserEmailVerificationEvent event = new UserEmailVerificationEvent(
                UUID.randomUUID(),
                "test@example.com",
                "John Doe",
                "verification-token");

        // Act
        authEmailService.onUserEmailVerification(event);

        // Assert
        ArgumentCaptor<CreateEmailOptions> captor = ArgumentCaptor.forClass(CreateEmailOptions.class);
        verify(emails).send(captor.capture());

        CreateEmailOptions options = captor.getValue();
        assertEquals("test@example.com", options.getTo().get(0));
        assertEquals("Hey John, Verify your email!", options.getSubject());
        assertEquals("vanguard@auvexis.com", options.getFrom());
    }

    @Test
    @DisplayName("Should throw exception when email sending fails")
    void shouldThrowExceptionWhenEmailSendingFails() throws ResendException {
        // Arrange
        UserEmailVerificationEvent event = new UserEmailVerificationEvent(
                UUID.randomUUID(),
                "test@example.com",
                "John Doe",
                "verification-token");
        doThrow(new ResendException("API Error")).when(emails).send(any());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authEmailService.onUserEmailVerification(event));
        assertEquals("Failed to send verification email", exception.getMessage());
    }

    @Test
    @DisplayName("Should send resend verification email successfully")
    void shouldSendResendVerificationEmailSuccessfully() throws ResendException {
        // Arrange
        UserEmailVerificationEvent event = new UserEmailVerificationEvent(
                UUID.randomUUID(),
                "test@example.com",
                "John Doe",
                "verification-token");

        // Act
        authEmailService.onUserEmailVerificationResend(event);

        // Assert
        ArgumentCaptor<CreateEmailOptions> captor = ArgumentCaptor.forClass(CreateEmailOptions.class);
        verify(emails).send(captor.capture());

        CreateEmailOptions options = captor.getValue();
        assertEquals("test@example.com", options.getTo().get(0));
        assertEquals("Hey John, Resend verification email!", options.getSubject());
    }
}
