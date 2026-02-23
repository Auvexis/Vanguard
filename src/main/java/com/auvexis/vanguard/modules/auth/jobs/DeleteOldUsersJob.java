package com.auvexis.vanguard.modules.auth.jobs;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.auvexis.vanguard.modules.auth.infrastructure.repository.UserRepository;

@Component
public class DeleteOldUsersJob {

    private final UserRepository userRepository;

    public DeleteOldUsersJob(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 */5 * * * ?", zone = "UTC")
    public void execute() {
        Instant limit = Instant.now().minus(1, ChronoUnit.DAYS);
        userRepository.deleteUsersOlderThan(limit);
    }

}
