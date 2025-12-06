package com.taghazout.authservice.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * RefreshToken domain entity for managing JWT refresh tokens.
 * 
 * SOLID Principles:
 * - SRP: Only responsible for refresh token lifecycle
 * - OCP: Can be extended for token revocation strategies
 * 
 * Design Decisions:
 * - UUID as token value for security (unguessable, 128-bit entropy)
 * - Many-to-One relationship with User
 * - Expiration timestamp for automatic invalidation
 * - Revocation flag for manual invalidation
 * - Immutable token value after creation
 * 
 * Security Features:
 * - UUID v4 tokens (cryptographically random)
 * - Expiration time tracking
 * - Revocation support (logout, compromised tokens)
 * - One-to-many with User (multiple devices/sessions)
 * 
 * Database Compatibility:
 * - PostgreSQL: UUID type natively supported
 * - H2: UUID stored as VARCHAR(36)
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token", columnList = "token"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_expires_at", columnList = "expires_at")
})
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 36)
    private String token; // UUID string representation

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refresh_token_user"))
    private User user;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /**
     * Default constructor required by JPA.
     */
    protected RefreshToken() {
        // JPA requires no-arg constructor
    }

    /**
     * Creates a new refresh token for a user.
     * 
     * @param user           the user this token belongs to
     * @param expirationDays number of days until token expires
     * @throws IllegalArgumentException if user is null or expirationDays <= 0
     */
    public RefreshToken(User user, int expirationDays) {
        validateUser(user);
        validateExpirationDays(expirationDays);

        this.token = UUID.randomUUID().toString();
        this.user = user;
        this.expiresAt = LocalDateTime.now().plusDays(expirationDays);
        this.revoked = false;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Creates a new refresh token with custom expiration.
     * 
     * @param user      the user this token belongs to
     * @param expiresAt the exact expiration timestamp
     * @throws IllegalArgumentException if user is null or expiresAt is in the past
     */
    public RefreshToken(User user, LocalDateTime expiresAt) {
        validateUser(user);
        validateExpirationTime(expiresAt);

        this.token = UUID.randomUUID().toString();
        this.user = user;
        this.expiresAt = expiresAt;
        this.revoked = false;
        this.createdAt = LocalDateTime.now();
    }

    // === JPA Lifecycle Callbacks ===

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // === Business Methods ===

    /**
     * Checks if this token is valid (not expired and not revoked).
     * 
     * @return true if token is valid
     */
    public boolean isValid() {
        return !isExpired() && !revoked;
    }

    /**
     * Checks if this token has expired.
     * 
     * @return true if current time is after expiration time
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Revokes this token (e.g., during logout).
     * Once revoked, the token cannot be used for authentication.
     */
    public void revoke() {
        this.revoked = true;
        this.revokedAt = LocalDateTime.now();
    }

    /**
     * Gets the user associated with this token.
     * 
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Checks if the token belongs to the specified user.
     * 
     * @param userId the user ID to check
     * @return true if token belongs to user
     */
    public boolean belongsToUser(Long userId) {
        return user != null && Objects.equals(user.getId(), userId);
    }

    /**
     * Gets remaining time until expiration.
     * 
     * @return remaining time in seconds, or 0 if already expired
     */
    public long getRemainingTimeSeconds() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
    }

    // === Validation Methods ===

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
    }

    private void validateExpirationDays(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Expiration days must be positive: " + days);
        }
    }

    private void validateExpirationTime(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            throw new IllegalArgumentException("Expiration time cannot be null");
        }
        if (expiresAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Expiration time cannot be in the past");
        }
    }

    // === Getters ===

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    // === equals() and hashCode() ===

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RefreshToken that = (RefreshToken) o;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    // === toString() ===

    @Override
    public String toString() {
        return "RefreshToken{" +
                "id=" + id +
                ", token='" + token.substring(0, 8) + "...'" + // Only show first 8 chars for security
                ", userId=" + (user != null ? user.getId() : null) +
                ", expiresAt=" + expiresAt +
                ", revoked=" + revoked +
                ", createdAt=" + createdAt +
                '}';
    }
}
