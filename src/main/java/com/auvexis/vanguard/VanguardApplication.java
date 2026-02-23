package com.auvexis.vanguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Vanguard application.
 * Bootstraps the Spring context, enables JPA auditing, and configures
 * the core server environment.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableScheduling
public class VanguardApplication {

	public static void main(String[] args) {
		SpringApplication.run(VanguardApplication.class, args);
	}

}
