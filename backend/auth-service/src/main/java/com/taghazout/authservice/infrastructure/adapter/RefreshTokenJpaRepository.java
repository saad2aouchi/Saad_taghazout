package com.taghazout.authservice.infrastructure.adapter;

import com.taghazout.authservice.domain.entity.RefreshToken;
import com.taghazout.authservice.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA Repository adapter for RefreshToken entity.
 * 
 * Provides database operations for refresh token management:
 * - Token CRUD operations
 * - Token validation queries
 * - Cleanup operations for expired tokens
 * 
 * SOLID Principles:
 * - SRP: Only responsible for token persistence
 * - DIP: Can be swapped with different implementation
 */
@Repository
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find refresh token by token value (UUID).
     * 
     * @param token the token string
     * @return Optional containing token if found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all refresh tokens for a specific user.
     * 
     * @param user the user
     * @return list of refresh tokens
     */
    List<RefreshToken> findByUser(User user);

    /**
     * Find all valid (non-expired, non-revoked) tokens for a user.
     * 
     * Custom JPQL query for complex conditions.
     * 
     * @param userId the user ID
     * @param now    current timestamp
     * @return list of valid tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId " +
            "AND rt.revoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUserId(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    /**
     * Delete refresh token by token value.
     * 
     * @param token the token string
     */
    void deleteByToken(String token);

    /**
     * Delete all refresh tokens for a user.
     * 
     * Useful for logout-all-devices scenario.
     * 
     * @param user the user
     */
    void deleteByUser(User user);

    /**
     * Delete all expired tokens (cleanup operation).
     * 
     * Should be run periodically to clean up database.
     * Returns number of deleted tokens for monitoring.
     * 
     * @param now current timestamp
     * @return number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Check if token exists and is valid.
     * 
     * More efficient than fetching full token object.
     * 
     * @param token the token string
     * @param now   current timestamp
     * @return true if token exists and is valid
     */
    @Query("SELECT CASE WHEN COUNT(rt) > 0 THEN true ELSE false END " +
            "FROM RefreshToken rt WHERE rt.token = :token " +
            "AND rt.revoked = false AND rt.expiresAt > :now")
    boolean existsByTokenAndIsValid(
            @Param("token") String token,
            @Param("now") LocalDateTime now);
}
