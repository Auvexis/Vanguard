package com.auvexis.vanguard.modules.auth.infrastructure.security;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private RequestMappingHandlerMapping handlerMapping;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HandlerExecutionChain handlerExecutionChain;

    @Mock
    private HandlerMethod handlerMethod;

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter(rateLimitService, handlerMapping);
    }

    @Test
    void doFilter_WhenNoRateLimitAnnotation_ShouldContinueChain() throws IOException, ServletException, Exception {
        when(handlerMapping.getHandler(request)).thenReturn(handlerExecutionChain);
        when(handlerExecutionChain.getHandler()).thenReturn(handlerMethod);
        when(handlerMethod.getMethodAnnotation(RateLimit.class)).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(rateLimitService, never()).tryConsume(anyString(), anyLong(), anyLong(), anyInt());
    }

    @Test
    void doFilter_WhenRateLimitAllowed_ShouldContinueChain() throws IOException, ServletException, Exception {
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.capacity()).thenReturn(5L);
        when(rateLimit.refillTokens()).thenReturn(5L);
        when(rateLimit.refillDurationInMinutes()).thenReturn(1);

        when(handlerMapping.getHandler(request)).thenReturn(handlerExecutionChain);
        when(handlerExecutionChain.getHandler()).thenReturn(handlerMethod);
        when(handlerMethod.getMethodAnnotation(RateLimit.class)).thenReturn(rateLimit);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(handlerMethod.getMethod()).thenReturn(this.getClass().getDeclaredMethods()[0]); // Just any method

        when(rateLimitService.tryConsume(anyString(), anyLong(), anyLong(), anyInt())).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_WhenRateLimitExceeded_ShouldReturn429() throws IOException, ServletException, Exception {
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.capacity()).thenReturn(5L);
        when(rateLimit.refillTokens()).thenReturn(5L);
        when(rateLimit.refillDurationInMinutes()).thenReturn(1);

        when(handlerMapping.getHandler(request)).thenReturn(handlerExecutionChain);
        when(handlerExecutionChain.getHandler()).thenReturn(handlerMethod);
        when(handlerMethod.getMethodAnnotation(RateLimit.class)).thenReturn(rateLimit);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(handlerMethod.getMethod()).thenReturn(this.getClass().getDeclaredMethods()[0]);

        when(rateLimitService.tryConsume(anyString(), anyLong(), anyLong(), anyInt())).thenReturn(false);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(429);
        verify(filterChain, never()).doFilter(request, response);
    }
}
