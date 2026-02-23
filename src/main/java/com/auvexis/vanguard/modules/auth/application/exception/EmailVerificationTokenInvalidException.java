package com.auvexis.vanguard.modules.auth.application.exception;

public class EmailVerificationTokenInvalidException extends RuntimeException {
    public EmailVerificationTokenInvalidException() {
        super("Email verification token is invalid!");
    }
}
