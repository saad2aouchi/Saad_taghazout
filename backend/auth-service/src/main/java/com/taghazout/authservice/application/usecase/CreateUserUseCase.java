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
 * 6. (New) Host profiles must have an organization name
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
    private final HostProfileRepositoryPort hostProfileRepository;
    private final TokenProviderPort tokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor injection (immutable dependencies).
     * 
     * @param userRepository         user persistence port
     * @param refreshTokenRepository token persistence port
     * @param hostProfileRepository  host profile persistence port
     * @param tokenProvider          JWT token generation port
     * @param passwordEncoder        BCrypt password encoder
     */
    public CreateUserUseCase(
            UserRepositoryPort userRepository,
            RefreshTokenRepositoryPort refreshTokenRepository,
            HostProfileRepositoryPort hostProfileRepository,
            TokenProviderPort tokenProvider,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.hostProfileRepository = hostProfileRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Executes user registration use case.
     * 
     * Steps:
     * 1. Check if email already exists (business rule violation)
     * 2. Hash password with BCrypt
     * 3. Create and persist User entity with Role
     * 4. If ROLE is HOST, validate and persist HostProfile
     * 5. Generate JWT access token
     * 6. Create and persist refresh token
     * 7. Return AuthResponse with tokens
     * 
     * @param request registration request containing email and password
     * @param role    role to assign to the new user
     * @return AuthResponse with user data and JWT tokens
     * @throws UserAlreadyExistsException if email is already registered
     * @throws IllegalArgumentException   if validation fails
     */
    public AuthResponse execute(RegisterRequest request, Role role) {
        // Step 1: Check uniqueness constraint
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }

        // Step 1.5: Validate Host requirements
        if (role == Role.HOST && (request.organizationName() == null || request.organizationName().isBlank())) {
            throw new IllegalArgumentException("Organization name is required for Hosts");
        }

        // Step 2: Hash password (BCrypt with strength 12 from SecurityConfig)
        String hashedPassword = passwordEncoder.encode(request.password());

        // Step 3: Create and persist user
        User user = new User(
                request.email(),
                hashedPassword,
                request.firstName(),
                request.lastName(),
                role);
        User savedUser = userRepository.save(user);

        // Step 4: Persist HostProfile if applicable
        if (role == Role.HOST) {
            HostProfile hostProfile = new HostProfile(savedUser, request.organizationName());
            hostProfileRepository.save(hostProfile);
        }

        // Step 5: Generate JWT access token
        String accessToken = tokenProvider.generateAccessToken(savedUser);

        // Step 6: Create and persist database-backed refresh token (UUID)
        // Design: Using UUID tokens stored in DB allows immediate revocation
        // (more secure than JWT refresh tokens which cannot be invalidated)
        RefreshToken refreshToken = new RefreshToken(savedUser, 7);
        refreshTokenRepository.save(refreshToken);

        // Step 7: Build and return response
        return AuthResponse.of(
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                accessToken,
                refreshToken.getToken());
    }
}
