package com.taghazout.authservice.application.usecase;

import com.taghazout.authservice.application.dto.AuthResponse;
import com.taghazout.authservice.application.dto.LoginRequest;
import com.taghazout.authservice.domain.entity.RefreshToken;
import com.taghazout.authservice.domain.entity.User;
import com.taghazout.authservice.domain.exception.InvalidCredentialsException;
import com.taghazout.authservice.domain.port.RefreshTokenRepositoryPort;
import com.taghazout.authservice.domain.port.TokenProviderPort;
import com.taghazout.authservice.domain.port.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticateUserUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticateUserUseCase Tests")
class AuthenticateUserUseCaseTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;

    @Mock
    private TokenProviderPort tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthenticateUserUseCase authenticateUserUseCase;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String HASHED_PASSWORD = "$2a$12$hashedPassword";
    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

    @BeforeEach
    void setUp() {
        authenticateUserUseCase = new AuthenticateUserUseCase(
                userRepository,
                refreshTokenRepository,
                tokenProvider,
                passwordEncoder);
    }

    @Test
    @DisplayName("Should successfully authenticate user with valid credentials")
    void shouldSuccessfullyAuthenticateUser() {
        // Given
        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
        User user = new User(TEST_EMAIL, HASHED_PASSWORD, "John", "Doe");
        RefreshToken refreshToken = new RefreshToken(user, 7);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TEST_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
        when(tokenProvider.generateAccessToken(user)).thenReturn(ACCESS_TOKEN);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        // When
        AuthResponse response = authenticateUserUseCase.execute(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(TEST_EMAIL);
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.refreshToken()).isNotNull();

        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(passwordEncoder).matches(TEST_PASSWORD, HASHED_PASSWORD);
        verify(tokenProvider).generateAccessToken(user);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> authenticateUserUseCase.execute(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void shouldThrowExceptionWhenPasswordIncorrect() {
        // Given
        LoginRequest request = new LoginRequest(TEST_EMAIL, "wrongPassword");
        User user = new User(TEST_EMAIL, HASHED_PASSWORD);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", HASHED_PASSWORD)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> authenticateUserUseCase.execute(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder).matches("wrongPassword", HASHED_PASSWORD);
        verify(tokenProvider, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("Should throw exception when user is disabled")
    void shouldThrowExceptionWhenUserDisabled() {
        // Given
        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
        User user = new User(TEST_EMAIL, HASHED_PASSWORD);
        user.disable(); // Disable the user

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TEST_PASSWORD, HASHED_PASSWORD)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> authenticateUserUseCase.execute(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("disabled or locked");

        verify(tokenProvider, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("Should throw exception when user is locked")
    void shouldThrowExceptionWhenUserLocked() {
        // Given
        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
        User user = new User(TEST_EMAIL, HASHED_PASSWORD);
        user.lock(); // Lock the user

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TEST_PASSWORD, HASHED_PASSWORD)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> authenticateUserUseCase.execute(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("disabled or locked");

        verify(tokenProvider, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("Should create new refresh token on each login")
    void shouldCreateNewRefreshTokenOnEachLogin() {
        // Given
        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
        User user = new User(TEST_EMAIL, HASHED_PASSWORD);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TEST_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
        when(tokenProvider.generateAccessToken(user)).thenReturn(ACCESS_TOKEN);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(new RefreshToken(user, 7));

        // When
        authenticateUserUseCase.execute(request);

        // Then
        verify(refreshTokenRepository).save(argThat(token -> token.getUser().equals(user) &&
                !token.isExpired()));
    }

    @Test
    @DisplayName("Should use BCrypt to verify password")
    void shouldUseBCryptToVerifyPassword() {
        // Given
        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
        User user = new User(TEST_EMAIL, HASHED_PASSWORD);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TEST_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
        when(tokenProvider.generateAccessToken(user)).thenReturn(ACCESS_TOKEN);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(new RefreshToken(user, 7));

        // When
        authenticateUserUseCase.execute(request);

        // Then
        verify(passwordEncoder).matches(TEST_PASSWORD, HASHED_PASSWORD);
    }
}
