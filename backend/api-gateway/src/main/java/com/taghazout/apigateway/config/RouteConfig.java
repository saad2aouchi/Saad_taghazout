package com.taghazout.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class RouteConfig {

    @Bean
    public List<String> openEndpoints(@Value("${gateway.open-endpoints:/auth/login,/auth/register,/actuator/health}") String endpoints) {
        return Arrays.asList(endpoints.split(","));
    }
}