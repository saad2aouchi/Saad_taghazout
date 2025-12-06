package com.taghazout.authservice.application.usecase;

import com.taghazout.authservice.application.dto.AuthResponse;
import com.taghazout.authservice.application.dto.RegisterRequest;
import com.taghazout.authservice.domain.entity.RefreshToken;
import com.taghazout.authservice.domain.entity.User;
import com.taghazout.authservice.domain.exception.UserAlreadyExistsException;
import com.taghazout.authservice.domain.port.RefreshTokenRepositoryPort;
import com.taghazout.authservice.domain.port.TokenProviderPort;
import com.taghazout.authservice.domain.port.UserRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for creating a new user (registration).
 * 
 * SOLID Principles:
 * - SRP: Single responsibility - only handles user registration
 * - OCP: Open for extension (can add email verification, etc.)
 * - DIP: Depends on port interfaces, not concrete implementations
 * 
 * Business Rules:
 * 1. Email must be unique (check before creation)
 * 2. Password must be hashed with BCrypt
 * 3. User account is enabled by default
 * 4. Refresh token is created and stored
 * 5. JWT access token is generated
 * 
 * HTTP Status:
 * - 201 Created: User successfully registered
 * - 409 Conflict: Email already exists
 * - 400 Bad Request: Invalid input (handled by controller validation)
 */
@Service
@Transactional
public class CreateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final TokenProviderPort tokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor injection (immutable dependencies).
     * 
     * @param userRepository         user persistence port
     * @param refreshTokenRepository token persistence port
     * @param tokenProvider          JWT token generation port
     * @param passwordEncoder        BCrypt password encoder
     */
    public CreateUserUseCase(
            UserRepositoryPort userRepository,
            RefreshTokenRepositoryPort refreshTokenRepository,
            TokenProviderPort tokenProvider,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Executes user registration use case.
     * 
     * Steps:
     * 1. Check if email already exists (business rule violation)
     * 2. Hash password with BCrypt
     * 3. Create and persist User entity
     * 4. Generate JWT access token
     * 5. Create and persist refresh token
     * 6. Return AuthResponse with tokens
     * 
     * @param request registration request containing email and password
     * @return AuthResponse with user data and JWT tokens
     * @throws UserAlreadyExistsException if email is already registered
     */
    public AuthResponse execute(RegisterRequest request) {
        // Step 1: Check uniqueness constraint
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }

        // Step 2: Hash password (BCrypt with strength 12 from SecurityConfig)
        String hashedPassword = passwordEncoder.encode(request.password());

        // Step 3: Create and persist user
        User user = new User(
                request.email(),
                hashedPassword,
                request.firstName(),
                request.lastName());
        User savedUser = userRepository.save(user);

        // Step 4: Generate JWT tokens
        String accessToken = tokenProvider.generateAccessToken(savedUser);
        String refreshTokenValue = tokenProvider.generateRefreshToken(savedUser);

        // Step 5: Create and persist refresh token (7 days expiration)
        RefreshToken refreshToken = new RefreshToken(savedUser, 7);
        refreshTokenRepository.save(refreshToken);

        // Step 6: Build and return response
        return AuthResponse.of(
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                accessToken,
                refreshToken.getToken());
    }
}
