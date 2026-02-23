package com.auvexis.vanguard.modules.auth.infrastructure.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RequestMappingHandlerMapping handlerMapping;

    public RateLimitFilter(RateLimitService rateLimitService,
            @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        this.rateLimitService = rateLimitService;
        this.handlerMapping = handlerMapping;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            HandlerExecutionChain handlerExecutionChain = handlerMapping.getHandler(request);
            if (handlerExecutionChain != null && handlerExecutionChain.getHandler() instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handlerExecutionChain.getHandler();
                RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

                if (rateLimit != null) {
                    String key = request.getRemoteAddr() + ":" + handlerMethod.getMethod().getName();
                    boolean allowed = rateLimitService.tryConsume(
                            key,
                            rateLimit.capacity(),
                            rateLimit.refillTokens(),
                            rateLimit.refillDurationInMinutes());

                    if (!allowed) {
                        response.setStatus(429);
                        response.setContentType("text/plain");
                        response.getWriter().write("Too many requests");
                        return;
                    }
                }
            }
        } catch (Exception e) {
            // Silently fall back to allowing the request if rate limiting fails
        }

        filterChain.doFilter(request, response);
    }

}
