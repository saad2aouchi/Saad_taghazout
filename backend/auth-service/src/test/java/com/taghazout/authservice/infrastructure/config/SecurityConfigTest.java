package com.taghazout.authservice.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SecurityConfig
 * 
 * Tests verify:
 * - Security filter chain bean is created
 * - Password encoder bean is created
 * - BCrypt encoder is properly configured
 * - Password encoding and verification works correctly
 */
@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("Security Configuration Tests")
class SecurityConfigTest {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Should create SecurityFilterChain bean")
    void shouldCreateSecurityFilterChain() {
        assertThat(securityFilterChain).isNotNull();
    }

    @Test
    @DisplayName("Should create PasswordEncoder bean")
    void shouldCreatePasswordEncoder() {
        assertThat(passwordEncoder).isNotNull();
    }

    @Test
    @DisplayName("Should use BCryptPasswordEncoder")
    void shouldUseBCryptPasswordEncoder() {
        assertThat(passwordEncoder.getClass().getSimpleName())
                .isEqualTo("BCryptPasswordEncoder");
    }

    @Test
    @DisplayName("Should encode password with BCrypt")
    void shouldEncodePasswordWithBCrypt() {
        // Given
        String rawPassword = "mySecurePassword123!";

        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Then
        assertThat(encodedPassword)
                .isNotNull()
                .isNotEqualTo(rawPassword)
                .startsWith("$2a$") // BCrypt prefix
                .hasSize(60); // BCrypt hash length
    }

    @Test
    @DisplayName("Should verify correct password")
    void shouldVerifyCorrectPassword() {
        // Given
        String rawPassword = "testPassword456!";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // When
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

        // Then
        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("Should reject incorrect password")
    void shouldRejectIncorrectPassword() {
        // Given
        String rawPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // When
        boolean matches = passwordEncoder.matches(wrongPassword, encodedPassword);

        // Then
        assertThat(matches).isFalse();
    }

    @Test
    @DisplayName("Should generate different hashes for same password")
    void shouldGenerateDifferentHashesForSamePassword() {
        // Given
        String password = "samePassword";

        // When
        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);

        // Then - BCrypt uses random salt, so hashes differ
        assertThat(hash1).isNotEqualTo(hash2);

        // But both should match the original password
        assertThat(passwordEncoder.matches(password, hash1)).isTrue();
        assertThat(passwordEncoder.matches(password, hash2)).isTrue();
    }

    @Test
    @DisplayName("Should handle empty password")
    void shouldHandleEmptyPassword() {
        // Given
        String emptyPassword = "";

        // When
        String encodedPassword = passwordEncoder.encode(emptyPassword);

        // Then
        assertThat(encodedPassword).isNotNull();
        assertThat(passwordEncoder.matches(emptyPassword, encodedPassword)).isTrue();
    }

    @Test
    @DisplayName("Should handle special characters in password")
    void shouldHandleSpecialCharactersInPassword() {
        // Given
        String specialPassword = "P@ssw0rd!#$%^&*()_+-=[]{}|;:',.<>?/~`";

        // When
        String encodedPassword = passwordEncoder.encode(specialPassword);

        // Then
        assertThat(passwordEncoder.matches(specialPassword, encodedPassword)).isTrue();
    }
}
