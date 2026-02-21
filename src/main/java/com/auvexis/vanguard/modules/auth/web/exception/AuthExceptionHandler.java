package com.auvexis.vanguard.modules.auth.web.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.auvexis.vanguard.modules.auth.application.exception.EmailAlreadyInUseException;
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
}
