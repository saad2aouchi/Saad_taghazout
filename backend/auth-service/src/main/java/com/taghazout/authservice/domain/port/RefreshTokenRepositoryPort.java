package com.taghazout.authservice.domain.port;

import com.taghazout.authservice.domain.entity.RefreshToken;
import com.taghazout.authservice.domain.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository port (interface) for RefreshToken persistence operations.
 * 
 * SOLID Principles:
 * - DIP: Domain depends on this abstraction, not on concrete implementation
 * - ISP: Minimal interface with only needed operations
 * - SRP: Only responsible for RefreshToken persistence contract
 * 
 * Design Pattern: Repository Pattern + Hexagonal Architecture
 * 
 * The actual implementation will be in the infrastructure layer.
 */
public interface RefreshTokenRepositoryPort {

    /**
     * Saves a refresh token to the repository.
     * 
     * @param refreshToken the token to save
     * @return the saved token (with generated ID if new)
     */
    RefreshToken save(RefreshToken refreshToken);

    /**
     * Finds a refresh token by its token value.
     * 
     * @param token the token string (UUID)
     * @return Optional containing token if found, empty otherwise
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Finds all refresh tokens for a specific user.
     * 
     * @param user the user
     * @return list of refresh tokens (may be empty)
     */
    List<RefreshToken> findByUser(User user);

    /**
     * Finds all valid (non-expired, non-revoked) tokens for a user.
     * 
     * @param userId the user ID
     * @return list of valid tokens
     */
    List<RefreshToken> findValidTokensByUserId(Long userId);

    /**
     * Deletes a refresh token by its token value.
     * 
     * @param token the token string to delete
     */
    void deleteByToken(String token);

    /**
     * Deletes all refresh tokens for a user.
     * 
     * @param user the user whose tokens to delete
     */
    void deleteByUser(User user);

    /**
     * Deletes all expired tokens (cleanup operation).
     * 
     * @return number of tokens deleted
     */
    int deleteExpiredTokens();

    /**
     * Checks if a token exists and is valid.
     * 
     * @param token the token string
     * @return true if token exists and is valid
     */
    boolean existsByTokenAndIsValid(String token);
}
