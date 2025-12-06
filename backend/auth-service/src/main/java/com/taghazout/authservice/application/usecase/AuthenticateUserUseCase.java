package com.taghazout.authservice.application.usecase;

import com.taghazout.authservice.application.dto.AuthResponse;
import com.taghazout.authservice.application.dto.LoginRequest;
import com.taghazout.authservice.domain.entity.RefreshToken;
import com.taghazout.authservice.domain.entity.User;
import com.taghazout.authservice.domain.exception.InvalidCredentialsException;
import com.taghazout.authservice.domain.exception.UserNotFoundException;
import com.taghazout.authservice.domain.port.RefreshTokenRepositoryPort;
import com.taghazout.authservice.domain.port.TokenProviderPort;
import com.taghazout.authservice.domain.port.UserRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for authenticating a user (login).
 * 
 * SOLID Principles:
 * - SRP: Single responsibility - only handles authentication
 * - OCP: Open for extension (can add 2FA, rate limiting, etc.)
 * - DIP: Depends on port interfaces, not concrete implementations
 * 
 * Business Rules:
 * 1. User must exist in database
 * 2. Password must match (BCrypt verification)
 * 3. User account must be enabled
 * 4. User account must not be locked
 * 5. Generate new refresh token on each login
 * 6. Generate new access token
 * 
 * Security Considerations:
 * - Generic error message (don't reveal if email exists)
 * - BCrypt password comparison (timing-safe)
 * - Account status checks (enabled, not locked)
 * 
 * HTTP Status:
 * - 200 OK: Authentication successful
 * - 401 Unauthorized: Invalid credentials, disabled, or locked
 * - 400 Bad Request: Invalid input (handled by controller validation)
 */
@Service
@Transactional
public class AuthenticateUserUseCase {

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
    public AuthenticateUserUseCase(
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
     * Executes user authentication use case.
     * 
     * Steps:
     * 1. Find user by email
     * 2. Verify password with BCrypt
     * 3. Check if user can authenticate (enabled, not locked)
     * 4. Generate new JWT access token
     * 5. Create and persist new refresh token
     * 6. Return AuthResponse with tokens
     * 
     * @param request login request containing email and password
     * @return AuthResponse with user data and JWT tokens
     * @throws InvalidCredentialsException if credentials invalid or account not
     *                                     usable
     */
    public AuthResponse execute(LoginRequest request) {
        // Step 1: Find user by email
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException());

        // Step 2: Verify password (BCrypt - timing-safe comparison)
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // Step 3: Check if user can authenticate
        if (!user.canAuthenticate()) {
            // User is either disabled or locked
            throw new InvalidCredentialsException("Account is disabled or locked");
        }

        // Step 4: Generate JWT access token
        String accessToken = tokenProvider.generateAccessToken(user);

        // Step 5: Create and persist new refresh token
        RefreshToken refreshToken = new RefreshToken(user, 7); // 7 days expiration
        RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshToken);

        // Step 6: Build and return response
        return AuthResponse.of(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                accessToken,
                savedRefreshToken.getToken());
    }
}
