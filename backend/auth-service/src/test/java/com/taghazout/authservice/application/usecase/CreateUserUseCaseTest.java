package com.taghazout.authservice.application.usecase;

import com.taghazout.authservice.application.dto.AuthResponse;
import com.taghazout.authservice.application.dto.RegisterRequest;
import com.taghazout.authservice.domain.entity.HostProfile;
import com.taghazout.authservice.domain.entity.RefreshToken;
import com.taghazout.authservice.domain.entity.User;
import com.taghazout.authservice.domain.enums.Role;
import com.taghazout.authservice.domain.exception.UserAlreadyExistsException;
import com.taghazout.authservice.domain.port.HostProfileRepositoryPort;
import com.taghazout.authservice.domain.port.RefreshTokenRepositoryPort;
import com.taghazout.authservice.domain.port.TokenProviderPort;
import com.taghazout.authservice.domain.port.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CreateUserUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateUserUseCase Tests")
class CreateUserUseCaseTest {

        @Mock
        private UserRepositoryPort userRepository;

        @Mock
        private RefreshTokenRepositoryPort refreshTokenRepository;

        @Mock
        private HostProfileRepositoryPort hostProfileRepository;

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
                                hostProfileRepository,
                                tokenProvider,
                                passwordEncoder);
        }

        @Test
        @DisplayName("Should successfully register new user (Client)")
        void shouldSuccessfullyRegisterNewUser() {
                // Given
                RegisterRequest request = new RegisterRequest(
                                TEST_EMAIL,
                                TEST_PASSWORD,
                                "John",
                                "Doe",
                                null);

                User savedUser = new User(TEST_EMAIL, HASHED_PASSWORD, "John", "Doe", Role.CLIENT);
                RefreshToken savedRefreshToken = new RefreshToken(savedUser, 7);

                when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
                when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(HASHED_PASSWORD);
                when(userRepository.save(any(User.class))).thenReturn(savedUser);
                when(tokenProvider.generateAccessToken(savedUser)).thenReturn(ACCESS_TOKEN);
                when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedRefreshToken);

                // When
                AuthResponse response = createUserUseCase.execute(request, Role.CLIENT);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.email()).isEqualTo(TEST_EMAIL);
                assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);

                // Verify no Host Profile saved
                verify(hostProfileRepository, never()).save(any());
                verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should successfully register new HOST with organization name")
        void shouldSuccessfullyRegisterNewHost() {
                // Given
                RegisterRequest request = new RegisterRequest(
                                TEST_EMAIL,
                                TEST_PASSWORD,
                                "Host",
                                "User",
                                "Taghazout Surf Camp");

                User savedUser = new User(TEST_EMAIL, HASHED_PASSWORD, "Host", "User", Role.HOST);
                RefreshToken savedRefreshToken = new RefreshToken(savedUser, 7);

                when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
                when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(HASHED_PASSWORD);
                when(userRepository.save(any(User.class))).thenReturn(savedUser);
                when(tokenProvider.generateAccessToken(savedUser)).thenReturn(ACCESS_TOKEN);
                when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedRefreshToken);

                // When
                AuthResponse response = createUserUseCase.execute(request, Role.HOST);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.email()).isEqualTo(TEST_EMAIL);

                // Verify Host Profile saved
                ArgumentCaptor<HostProfile> hostProfileCaptor = ArgumentCaptor.forClass(HostProfile.class);
                verify(hostProfileRepository).save(hostProfileCaptor.capture());
                HostProfile capturedProfile = hostProfileCaptor.getValue();
                assertThat(capturedProfile.getOrganizationName()).isEqualTo("Taghazout Surf Camp");
                assertThat(capturedProfile.getUser()).isEqualTo(savedUser);
        }

        @Test
        @DisplayName("Should FAIL to register HOST without organization name")
        void shouldFailToRegisterHostWithoutOrgName() {
                // Given
                RegisterRequest request = new RegisterRequest(
                                TEST_EMAIL,
                                TEST_PASSWORD,
                                "Host",
                                "User",
                                null); // Missing org name

                when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);

                // When/Then
                assertThatThrownBy(() -> createUserUseCase.execute(request, Role.HOST))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Organization name is required");

                // Verify nothing saved
                verify(userRepository, never()).save(any());
                verify(hostProfileRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
                // Given
                RegisterRequest request = new RegisterRequest(TEST_EMAIL, TEST_PASSWORD);
                when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

                // When/Then
                assertThatThrownBy(() -> createUserUseCase.execute(request, Role.CLIENT))
                                .isInstanceOf(UserAlreadyExistsException.class)
                                .hasMessageContaining(TEST_EMAIL);

                verify(userRepository, never()).save(any(User.class));
        }
}
