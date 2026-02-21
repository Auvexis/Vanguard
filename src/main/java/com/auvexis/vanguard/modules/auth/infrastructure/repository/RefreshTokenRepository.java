package com.auvexis.vanguard.modules.auth.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.auvexis.vanguard.modules.auth.domain.RefreshToken;
import com.auvexis.vanguard.modules.auth.domain.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);

    int deleteByUser(User user);
}
