package com.taghazout.listingservice.infrastructure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Web MVC Configuration for Listing Service.
 * 
 * CORS is only applied when running standalone (not behind API Gateway).
 * When running in Docker behind the gateway, the gateway handles all CORS.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private Environment environment;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Only apply CORS when NOT running behind the API Gateway
        // In Docker mode, the gateway handles all CORS configuration
        if (!isRunningBehindGateway()) {
            registry.addMapping("/**")
                    .allowedOrigins("http://localhost:3000", "http://localhost:8080", "http://localhost:52145")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(false);
        }
        // When running behind gateway, no CORS mappings are added here
        // The API Gateway's CorsWebFilter handles all CORS
    }

    /**
     * Checks if application is running behind the API Gateway.
     * 
     * @return true if 'docker' profile is active
     */
    private boolean isRunningBehindGateway() {
        String[] activeProfiles = environment.getActiveProfiles();
        return Arrays.asList(activeProfiles).contains("docker");
    }
}
