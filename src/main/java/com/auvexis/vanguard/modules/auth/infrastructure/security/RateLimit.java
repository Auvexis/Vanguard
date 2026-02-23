package com.auvexis.vanguard.modules.auth.infrastructure.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify rate limiting on a specific endpoint.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * The maximum number of requests allowed within the duration.
     */
    long capacity() default 5;

    /**
     * The number of tokens refilled every refillDurationInMinutes.
     */
    long refillTokens() default 5;

    /**
     * The duration of the refill in minutes.
     */
    int refillDurationInMinutes() default 1;
}
