package com.taghazout.authservice.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taghazout.authservice.application.dto.AuthResponse;
import com.taghazout.authservice.application.dto.LoginRequest;
import com.taghazout.authservice.application.dto.RegisterRequest;
import com.taghazout.authservice.application.usecase.AuthenticateUserUseCase;
import com.taghazout.authservice.application.usecase.CreateUserUseCase;
import com.taghazout.authservice.domain.enums.Role;
import com.taghazout.authservice.domain.exception.InvalidCredentialsException;
import com.taghazout.authservice.domain.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for AuthController.
 * 
 * Uses @WebMvcTest for controller layer testing with MockMvc.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Tests")
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private CreateUserUseCase createUserUseCase;

        @MockBean
        private AuthenticateUserUseCase authenticateUserUseCase;

        @Nested
        @DisplayName("POST /api/v1/auth/register/client")
        class ClientRegisterEndpointTests {

                @Test
                @DisplayName("Should register client successfully and return 201 Created")
                void shouldRegisterClientSuccessfully() throws Exception {
                        // Given
                        RegisterRequest request = new RegisterRequest(
                                        "client@example.com",
                                        "password123",
                                        "John",
                                        "Client",
                                        null);

                        AuthResponse response = AuthResponse.of(
                                        "client@example.com",
                                        "John",
                                        "Client",
                                        "access.token.here",
                                        "refresh-token-uuid");

                        when(createUserUseCase.execute(any(RegisterRequest.class), eq(Role.CLIENT)))
                                        .thenReturn(response);

                        // When/Then
                        mockMvc.perform(post("/api/v1/auth/register/client")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.email").value("client@example.com"))
                                        .andExpect(jsonPath("$.firstName").value("John"))
                                        .andExpect(jsonPath("$.lastName").value("Client"))
                                        .andExpect(jsonPath("$.accessToken").value("access.token.here"))
                                        .andExpect(jsonPath("$.refreshToken").value("refresh-token-uuid"));

                        verify(createUserUseCase, times(1)).execute(any(RegisterRequest.class), eq(Role.CLIENT));
                }

                @Test
                @DisplayName("Should return 409 Conflict when client email already exists")
                void shouldReturn409WhenClientEmailExists() throws Exception {
                        // Given
                        RegisterRequest request = new RegisterRequest("client@example.com", "password123");

                        when(createUserUseCase.execute(any(RegisterRequest.class), eq(Role.CLIENT)))
                                        .thenThrow(new UserAlreadyExistsException("client@example.com"));

                        // When/Then
                        mockMvc.perform(post("/api/v1/auth/register/client")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isConflict())
                                        .andExpect(jsonPath("$.status").value(409));
                }
        }

        @Nested
        @DisplayName("POST /api/v1/auth/register/host")
        class HostRegisterEndpointTests {

                @Test
                @DisplayName("Should register host successfully and return 201 Created")
                void shouldRegisterHostSuccessfully() throws Exception {
                        // Given
                        RegisterRequest request = new RegisterRequest(
                                        "host@example.com",
                                        "password123",
                                        "Jane",
                                        "Host",
                                        "Taghazout Camp");

                        AuthResponse response = AuthResponse.of(
                                        "host@example.com",
                                        "Jane",
                                        "Host",
                                        "access.token.here",
                                        "refresh-token-uuid");

                        when(createUserUseCase.execute(any(RegisterRequest.class), eq(Role.HOST))).thenReturn(response);

                        // When/Then
                        mockMvc.perform(post("/api/v1/auth/register/host")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.email").value("host@example.com"))
                                        .andExpect(jsonPath("$.firstName").value("Jane"))
                                        .andExpect(jsonPath("$.lastName").value("Host"));

                        verify(createUserUseCase, times(1)).execute(any(RegisterRequest.class), eq(Role.HOST));
                }

                @Test
                @DisplayName("Should return 400 Bad Request for invalid host email")
                void shouldReturn400ForInvalidHostEmail() throws Exception {
                        // Given
                        RegisterRequest request = new RegisterRequest("invalid-email", "password123");

                        // When/Then
                        mockMvc.perform(post("/api/v1/auth/register/host")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest());

                        verify(createUserUseCase, never()).execute(any(), any());
                }
        }

        @Nested
        @DisplayName("POST /api/v1/auth/login")
        class LoginEndpointTests {

                @Test
                @DisplayName("Should login user successfully and return 200 OK")
                void shouldLoginUserSuccessfully() throws Exception {
                        // Given
                        LoginRequest request = new LoginRequest("test@example.com", "password123");

                        AuthResponse response = AuthResponse.of(
                                        "test@example.com",
                                        "John",
                                        "Doe",
                                        "access.token.here",
                                        "refresh-token-uuid");

                        when(authenticateUserUseCase.execute(any(LoginRequest.class))).thenReturn(response);

                        // When/Then
                        mockMvc.perform(post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.email").value("test@example.com"))
                                        .andExpect(jsonPath("$.firstName").value("John"))
                                        .andExpect(jsonPath("$.lastName").value("Doe"))
                                        .andExpect(jsonPath("$.accessToken").value("access.token.here"))
                                        .andExpect(jsonPath("$.refreshToken").value("refresh-token-uuid"));

                        verify(authenticateUserUseCase, times(1)).execute(any(LoginRequest.class));
                }

                @Test
                @DisplayName("Should return 401 Unauthorized for invalid credentials")
                void shouldReturn401ForInvalidCredentials() throws Exception {
                        // Given
                        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

                        when(authenticateUserUseCase.execute(any(LoginRequest.class)))
                                        .thenThrow(new InvalidCredentialsException());

                        // When/Then
                        mockMvc.perform(post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.status").value(401))
                                        .andExpect(jsonPath("$.error").value("Unauthorized"));
                }

                @Test
                @DisplayName("Should return 400 Bad Request for invalid email format")
                void shouldReturn400ForInvalidEmailFormat() throws Exception {
                        // Given
                        LoginRequest request = new LoginRequest("invalid-email", "password123");

                        // When/Then
                        mockMvc.perform(post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.errors.email").exists());

                        verify(authenticateUserUseCase, never()).execute(any());
                }

                @Test
                @DisplayName("Should return 400 Bad Request for empty email")
                void shouldReturn400ForEmptyEmail() throws Exception {
                        // Given
                        LoginRequest request = new LoginRequest("", "password123");

                        // When/Then
                        mockMvc.perform(post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.errors.email").exists());
                }

                @Test
                @DisplayName("Should return 400 Bad Request for empty password")
                void shouldReturn400ForEmptyPassword() throws Exception {
                        // Given
                        LoginRequest request = new LoginRequest("test@example.com", "");

                        // When/Then
                        mockMvc.perform(post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.errors.password").exists());
                }

                @Test
                @DisplayName("Should return 401 for disabled account")
                void shouldReturn401ForDisabledAccount() throws Exception {
                        // Given
                        LoginRequest request = new LoginRequest("test@example.com", "password123");

                        when(authenticateUserUseCase.execute(any(LoginRequest.class)))
                                        .thenThrow(new InvalidCredentialsException("Account is disabled or locked"));

                        // When/Then
                        mockMvc.perform(post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.message").value(containsString("disabled or locked")));
                }

                @Test
                @DisplayName("Should normalize email to lowercase")
                void shouldNormalizeEmailToLowercase() throws Exception {
                        // Given
                        LoginRequest request = new LoginRequest("TEST@EXAMPLE.COM", "password123");

                        AuthResponse response = AuthResponse.of(
                                        "test@example.com",
                                        "access.token",
                                        "refresh.token");

                        when(authenticateUserUseCase.execute(any(LoginRequest.class))).thenReturn(response);

                        // When/Then
                        mockMvc.perform(post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.email").value("test@example.com"));
                }
        }

        @Nested
        @DisplayName("Content Type Tests")
        class ContentTypeTests {

                @Test
                @DisplayName("Should return 415 for unsupported media type")
                void shouldReturn415ForUnsupportedMediaType() throws Exception {
                        // Given
                        RegisterRequest request = new RegisterRequest("test@example.com", "password123");

                        // When/Then
                        mockMvc.perform(post("/api/v1/auth/register/client")
                                        .contentType(MediaType.TEXT_PLAIN)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isUnsupportedMediaType());
                }

                @Test
                @DisplayName("Should accept application/json content type")
                void shouldAcceptApplicationJson() throws Exception {
                        // Given
                        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
                        AuthResponse response = AuthResponse.of("test@example.com", "at", "rt");

                        when(createUserUseCase.execute(any(), any())).thenReturn(response);

                        // When/Then
                        mockMvc.perform(post("/api/v1/auth/register/client")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
                }
        }
}
