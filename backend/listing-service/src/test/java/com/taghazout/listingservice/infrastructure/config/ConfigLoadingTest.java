package com.taghazout.listingservice.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ConfigLoadingTest {

    @Value("${spring.application.name}")
    private String applicationName;

    @Test
    void shouldLoadApplicationName() {
        assertThat(applicationName).isEqualTo("listing-service");
    }
}
