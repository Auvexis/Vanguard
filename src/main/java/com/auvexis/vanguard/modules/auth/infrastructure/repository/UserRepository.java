package com.auvexis.vanguard.modules.auth.infrastructure.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.auvexis.vanguard.modules.auth.domain.User;

import jakarta.transaction.Transactional;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.createdAt < :limit")
    int deleteUsersOlderThan(@Param("limit") Instant limit);

}
