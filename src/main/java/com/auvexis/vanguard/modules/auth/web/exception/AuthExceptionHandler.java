package com.auvexis.vanguard.modules.auth.web.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.auvexis.vanguard.modules.auth.application.exception.EmailAlreadyInUseException;
import com.auvexis.vanguard.modules.auth.application.exception.EmailAlreadyVerifiedException;
import com.auvexis.vanguard.modules.auth.application.exception.EmailNotFoundException;
import com.auvexis.vanguard.modules.auth.application.exception.EmailNotVerifiedException;
import com.auvexis.vanguard.modules.auth.application.exception.EmailVerificationTokenInvalidException;
import com.auvexis.vanguard.modules.auth.application.exception.InvalidCredentialsException;
import com.auvexis.vanguard.modules.auth.application.exception.RefreshTokenExpiredException;
import com.auvexis.vanguard.shared.web.ApiResponse;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ApiResponse<Void> handleInvalidCredentialsException(InvalidCredentialsException e) {
        return ApiResponse.error_generic(401, null, e.getMessage());
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ApiResponse<Void> handleEmailAlreadyInUseException(EmailAlreadyInUseException e) {
        return ApiResponse.error_generic(409, null, e.getMessage());
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ApiResponse<Void> handleRefreshTokenExpiredException(RefreshTokenExpiredException e) {
        return ApiResponse.error_generic(401, null, e.getMessage());
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ApiResponse<Void> handleEmailNotVerifiedException(EmailNotVerifiedException e) {
        return ApiResponse.error_generic(401, null, e.getMessage());
    }

    @ExceptionHandler(EmailVerificationTokenInvalidException.class)
    public ApiResponse<Void> handleEmailVerificationTokenInvalidException(EmailVerificationTokenInvalidException e) {
        return ApiResponse.error_generic(401, null, e.getMessage());
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ApiResponse<Void> handleEmailNotFoundException(EmailNotFoundException e) {
        return ApiResponse.error_generic(404, null, e.getMessage());
    }

    @ExceptionHandler(EmailAlreadyVerifiedException.class)
    public ApiResponse<Void> handleEmailAlreadyVerifiedException(EmailAlreadyVerifiedException e) {
        return ApiResponse.error_generic(409, null, e.getMessage());
    }
}
