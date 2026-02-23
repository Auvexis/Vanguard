package com.auvexis.vanguard.modules.auth.application.exception;

public class EmailAlreadyVerifiedException extends RuntimeException {
    public EmailAlreadyVerifiedException(String email) {
        super("Email " + email + " is already verified");
    }
}
