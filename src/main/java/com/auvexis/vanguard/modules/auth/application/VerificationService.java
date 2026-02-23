package com.auvexis.vanguard.modules.auth.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.auvexis.vanguard.modules.auth.application.exception.EmailAlreadyVerifiedException;
import com.auvexis.vanguard.modules.auth.application.exception.EmailNotFoundException;
import com.auvexis.vanguard.modules.auth.application.exception.EmailVerificationTokenInvalidException;
import com.auvexis.vanguard.modules.auth.domain.EmailVerification;
import com.auvexis.vanguard.modules.auth.domain.User;
import com.auvexis.vanguard.modules.auth.infrastructure.repository.EmailVerificationRepository;
import com.auvexis.vanguard.modules.auth.infrastructure.repository.UserRepository;
import com.auvexis.vanguard.modules.auth.messaging.UserPublisher;
import com.auvexis.vanguard.shared.events.UserEmailVerificationEvent;

/**
 * Service specialized in managing the email verification lifecycle.
 * Handles the validation of verification tokens and the resending of
 * verification requests.
 */
@Service
public class VerificationService {

    private final UserRepository repo;
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserPublisher userPublisher;

    public VerificationService(UserRepository repo, EmailVerificationRepository emailVerificationRepository,
            UserPublisher userPublisher) {
        this.repo = repo;
        this.emailVerificationRepository = emailVerificationRepository;
        this.userPublisher = userPublisher;
    }

    /**
     * Validates a verification token for a specific user.
     * If valid, updates the user's status to 'verified' and records the
     * verification timestamp.
     * 
     * @param userID     The ID of the user attempting verification.
     * @param emailToken The verification token hash.
     */
    public void verifyEmail(UUID userID, String emailToken) {
        emailVerificationRepository.findByTokenHashAndUserId(emailToken, userID)
                .map(emailVerification -> {
                    User user = emailVerification.getUser();
                    user.setEmailVerified(true);
                    repo.save(user);
                    emailVerification.setVerifiedAt(Instant.now());
                    emailVerificationRepository.save(emailVerification);

                    return emailVerification;
                })
                .orElseThrow(() -> new EmailVerificationTokenInvalidException());
    }

    public void resendVerificationEmail(String email) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException(email));

        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException(email);
        }

        String token = emailVerificationRepository.findByUserId(user.getId())
                .map(EmailVerification::getTokenHash)
                .orElseThrow(() -> new EmailVerificationTokenInvalidException());

        userPublisher.publishUserEmailVerificationResend(UserEmailVerificationEvent.from(user, token));
    }
}
