package com.auvexis.vanguard.modules.auth.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.auvexis.vanguard.modules.auth.domain.EmailVerification;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {
    Optional<EmailVerification> findByTokenHashAndUserId(String tokenHash, UUID userId);
    Optional<EmailVerification> findByUserId(UUID userId);
}
