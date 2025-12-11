package com.taghazout.authservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.taghazout.authservice.application.dto.LoginRequest;
import com.taghazout.authservice.application.dto.RegisterRequest;
import com.taghazout.authservice.domain.entity.User;
import com.taghazout.authservice.infrastructure.adapter.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Auth Service.
 *
 * This spins up the full Spring Boot context on a random port
 * and uses H2 database (in-memory) for persistence.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test") // Use test profile (H2, standard settings)
@Transactional // Rollback after each test
@DisplayName("Auth Service Integration Tests")
class AuthServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Database is cleaned automatically due to @Transactional
    }

    @Test
    @DisplayName("Full flow: Register -> Login -> DB Verification")
    void shouldCompleteFullAuthFlow() throws Exception {
        // 1. Register a new user
        RegisterRequest registerRequest = new RegisterRequest(
                "integration@taghazout.com",
                "securePass123",
                "Integration",
                "Tester");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integration@taghazout.com"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());

        // 2. Verify User in Database
        User savedUser = userRepository.findByEmail("integration@taghazout.com").orElseThrow();
        assertThat(savedUser.getFirstName()).isEqualTo("Integration");
        assertThat(passwordEncoder.matches("securePass123", savedUser.getPassword())).isTrue();

        // 3. Login with the new user
        LoginRequest loginRequest = new LoginRequest("integration@taghazout.com", "securePass123");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@taghazout.com"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    @DisplayName("Should fail login with wrong password")
    void shouldFailLoginWithWrongPassword() throws Exception {
        // 1. Pre-seed user
        User user = new User("fail@taghazout.com", passwordEncoder.encode("correctPass"));
        userRepository.saveAndFlush(user);

        // 2. Attempt login with wrong password
        LoginRequest loginRequest = new LoginRequest("fail@taghazout.com", "wrongPass");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
