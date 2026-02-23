package com.auvexis.vanguard.modules.email.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.resend.Resend;

@Configuration
public class ResendConfig {

    @Value("${resend.apikey}")
    private String resendApiKey;

    @Bean
    public Resend resend() {
        return new Resend(resendApiKey);
    }

}
