package com.taghazout.authservice.application.usecase;

import com.taghazout.authservice.application.dto.AuthResponse;
import com.taghazout.authservice.application.dto.RegisterRequest;
import com.taghazout.authservice.domain.entity.RefreshToken;
import com.taghazout.authservice.domain.entity.User;
import com.taghazout.authservice.domain.exception.UserAlreadyExistsException;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CreateUserUseCase.
 * 
 * Tests use Mockito to mock dependencies:
 * - UserRepositoryPort
 * - RefreshTokenRepositoryPort
 * - TokenProviderPort
 * - PasswordEncoder
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateUserUseCase Tests")
class CreateUserUseCaseTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;

    @Mock
    private TokenProviderPort tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    private CreateUserUseCase createUserUseCase;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String HASHED_PASSWORD = "$2a$12$hashedPassword";
    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
    private static final String REFRESH_TOKEN = "550e8400-e29b-41d4-a716-446655440000";

    @BeforeEach
    void setUp() {
        createUserUseCase = new CreateUserUseCase(
                userRepository,
                refreshTokenRepository,
                tokenProvider,
                passwordEncoder);
    }

    @Test
    @DisplayName("Should successfully register new user")
    void shouldSuccessfullyRegisterNewUser() {
        // Given
        RegisterRequest request = new RegisterRequest(
                TEST_EMAIL,
                TEST_PASSWORD,
                "John",
                "Doe");

        User savedUser = new User(TEST_EMAIL, HASHED_PASSWORD, "John", "Doe");
        RefreshToken savedRefreshToken = new RefreshToken(savedUser, 7);

        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenProvider.generateAccessToken(savedUser)).thenReturn(ACCESS_TOKEN);
        when(tokenProvider.generateRefreshToken(savedUser)).thenReturn(REFRESH_TOKEN);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedRefreshToken);

        // When
        AuthResponse response = createUserUseCase.execute(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(TEST_EMAIL);
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.refreshToken()).isNotNull();

        // Verify interactions
        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(userRepository).save(any(User.class));
        verify(tokenProvider).generateAccessToken(savedUser);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        RegisterRequest request = new RegisterRequest(TEST_EMAIL, TEST_PASSWORD);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> createUserUseCase.execute(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining(TEST_EMAIL);

        // Verify no user was created
        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Should hash password before saving user")
    void shouldHashPasswordBeforeSavingUser() {
        // Given
        RegisterRequest request = new RegisterRequest(TEST_EMAIL, TEST_PASSWORD);
        User savedUser = new User(TEST_EMAIL, HASHED_PASSWORD);

        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn(ACCESS_TOKEN);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(new RefreshToken(savedUser, 7));

        // When
        createUserUseCase.execute(request);

        // Then
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(userRepository).save(argThat(user -> user.getPassword().equals(HASHED_PASSWORD)));
    }

    @Test
    @DisplayName("Should create refresh token with 7 days expiration")
    void shouldCreateRefreshTokenWith7DaysExpiration() {
        // Given
        RegisterRequest request = new RegisterRequest(TEST_EMAIL, TEST_PASSWORD);
        User savedUser = new User(TEST_EMAIL, HASHED_PASSWORD);

        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn(ACCESS_TOKEN);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(new RefreshToken(savedUser, 7));

        // When
        createUserUseCase.execute(request);

        // Then
        verify(refreshTokenRepository).save(argThat(token -> token.getUser().equals(savedUser) &&
                !token.isExpired() &&
                token.getRemainingTimeSeconds() > 0));
    }

    @Test
    @DisplayName("Should register user with email only (no name)")
    void shouldRegisterUserWithEmailOnly() {
        // Given
        RegisterRequest request = new RegisterRequest(TEST_EMAIL, TEST_PASSWORD);
        User savedUser = new User(TEST_EMAIL, HASHED_PASSWORD);

        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn(ACCESS_TOKEN);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(new RefreshToken(savedUser, 7));

        // When
        AuthResponse response = createUserUseCase.execute(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(TEST_EMAIL);
        assertThat(response.firstName()).isNull();
        assertThat(response.lastName()).isNull();
    }
}
