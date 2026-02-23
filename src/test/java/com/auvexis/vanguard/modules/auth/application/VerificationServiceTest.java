package com.auvexis.vanguard.modules.auth.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auvexis.vanguard.modules.auth.application.exception.EmailAlreadyVerifiedException;
import com.auvexis.vanguard.modules.auth.application.exception.EmailNotFoundException;
import com.auvexis.vanguard.modules.auth.application.exception.EmailVerificationTokenInvalidException;
import com.auvexis.vanguard.modules.auth.domain.EmailVerification;
import com.auvexis.vanguard.modules.auth.domain.User;
import com.auvexis.vanguard.modules.auth.infrastructure.repository.EmailVerificationRepository;
import com.auvexis.vanguard.modules.auth.infrastructure.repository.UserRepository;
import com.auvexis.vanguard.modules.auth.messaging.UserPublisher;
import com.auvexis.vanguard.shared.events.UserEmailVerificationEvent;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    private UserRepository repo;
    @Mock
    private EmailVerificationRepository emailVerificationRepository;
    @Mock
    private UserPublisher userPublisher;

    private VerificationService verificationService;

    @BeforeEach
    void setUp() {
        verificationService = new VerificationService(repo, emailVerificationRepository, userPublisher);
    }

    @Test
    void verifyEmail_ShouldUpdateUser_WhenTokenIsValid() {
        UUID userId = UUID.randomUUID();
        String token = "validToken";
        User user = new User();
        user.setEmailVerified(false);
        EmailVerification verification = new EmailVerification(user, token);

        when(emailVerificationRepository.findByTokenHashAndUserId(token, userId)).thenReturn(Optional.of(verification));

        verificationService.verifyEmail(userId, token);

        assertTrue(user.isEmailVerified());
        verify(repo).save(user);
        verify(emailVerificationRepository).save(verification);
    }

    @Test
    void verifyEmail_ShouldThrowException_WhenTokenIsInvalid() {
        when(emailVerificationRepository.findByTokenHashAndUserId(any(), any())).thenReturn(Optional.empty());

        assertThrows(EmailVerificationTokenInvalidException.class,
                () -> verificationService.verifyEmail(UUID.randomUUID(), "invalid"));
    }

    @Test
    void resendVerificationEmail_ShouldPublishEvent() {
        String email = "test@example.com";
        User user = new User("Test", email, "pwd");
        user.setId(UUID.randomUUID());

        EmailVerification verification = new EmailVerification(user, "token123");

        when(repo.findByEmail(email)).thenReturn(Optional.of(user));
        when(emailVerificationRepository.findByUserId(user.getId())).thenReturn(Optional.of(verification));

        verificationService.resendVerificationEmail(email);

        verify(userPublisher).publishUserEmailVerificationResend(any(UserEmailVerificationEvent.class));
    }

    @Test
    void resendVerificationEmail_ShouldThrowException_WhenUserNotFound() {
        when(repo.findByEmail(any())).thenReturn(Optional.empty());

        assertThrows(EmailNotFoundException.class,
                () -> verificationService.resendVerificationEmail("unknown@test.com"));
    }

    @Test
    void resendVerificationEmail_ShouldThrowException_WhenAlreadyVerified() {
        User user = new User();
        user.setEmailVerified(true);
        when(repo.findByEmail(any())).thenReturn(Optional.of(user));

        assertThrows(EmailAlreadyVerifiedException.class,
                () -> verificationService.resendVerificationEmail("test@test.com"));
    }
}
