package com.auvexis.vanguard.modules.auth.application.exception;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException() {
        super("Email is not verified!");
    }
}
