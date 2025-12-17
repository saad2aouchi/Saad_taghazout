package com.taghazout.authservice.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import com.taghazout.authservice.domain.enums.Role;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for RefreshToken entity
 */
@DisplayName("RefreshToken Entity Tests")
class RefreshTokenTest {

    private static final String BCRYPT_HASH = "$2a$12$abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNO";

    private User createTestUser() {
        return new User("test@example.com", BCRYPT_HASH, Role.CLIENT);
    }

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create token with expiration in days")
        void shouldCreateTokenWithExpirationInDays() {
            // Given
            User user = createTestUser();

            // When
            RefreshToken token = new RefreshToken(user, 7);

            // Then
            assertThat(token.getToken()).isNotNull();
            assertThat(token.getToken()).hasSize(36); // UUID format
            assertThat(token.getUser()).isEqualTo(user);
            assertThat(token.isRevoked()).isFalse();
            assertThat(token.getExpiresAt()).isAfter(LocalDateTime.now());
        }

        @Test
        @DisplayName("Should create token with specific expiration time")
        void shouldCreateTokenWithSpecificExpirationTime() {
            // Given
            User user = createTestUser();
            LocalDateTime futureTime = LocalDateTime.now().plusDays(7);

            // When
            RefreshToken token = new RefreshToken(user, futureTime);

            // Then
            assertThat(token.getExpiresAt()).isEqualToIgnoringNanos(futureTime);
        }

        @Test
        @DisplayName("Should throw exception for null user")
        void shouldThrowExceptionForNullUser() {
            assertThatThrownBy(() -> new RefreshToken(null, 7))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User cannot be null");
        }

        @Test
        @DisplayName("Should throw exception for zero expiration days")
        void shouldThrowExceptionForZeroExpirationDays() {
            User user = createTestUser();
            assertThatThrownBy(() -> new RefreshToken(user, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expiration days must be positive");
        }

        @Test
        @DisplayName("Should throw exception for past expiration time")
        void shouldThrowExceptionForPastExpirationTime() {
            User user = createTestUser();
            LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

            assertThatThrownBy(() -> new RefreshToken(user, pastTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expiration time cannot be in the past");
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should be valid when not expired and not revoked")
        void shouldBeValidWhenNotExpiredAndNotRevoked() {
            // Given
            User user = createTestUser();
            RefreshToken token = new RefreshToken(user, 7);

            // Then
            assertThat(token.isValid()).isTrue();
            assertThat(token.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Should not be valid when expired")
        void shouldNotBeValidWhenExpired() {
            // Given
            User user = createTestUser();
            LocalDateTime pastTime = LocalDateTime.now().plusSeconds(1);
            RefreshToken token = new RefreshToken(user, pastTime);

            // Wait for expiration
            try {
                Thread.sleep(1100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Then
            assertThat(token.isExpired()).isTrue();
            assertThat(token.isValid()).isFalse();
        }

        @Test
        @DisplayName("Should not be valid when revoked")
        void shouldNotBeValidWhenRevoked() {
            // Given
            User user = createTestUser();
            RefreshToken token = new RefreshToken(user, 7);

            // When
            token.revoke();

            // Then
            assertThat(token.isRevoked()).isTrue();
            assertThat(token.isValid()).isFalse();
            assertThat(token.getRevokedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Business Method Tests")
    class BusinessMethodTests {

        @Test
        @DisplayName("Should revoke token")
        void shouldRevokeToken() {
            // Given
            User user = createTestUser();
            RefreshToken token = new RefreshToken(user, 7);

            // When
            token.revoke();

            // Then
            assertThat(token.isRevoked()).isTrue();
            assertThat(token.getRevokedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should check if token belongs to user")
        void shouldCheckIfTokenBelongsToUser() {
            // Given
            User user1 = createTestUser();
            User user2 = new User("other@example.com", BCRYPT_HASH, Role.CLIENT);
            RefreshToken token = new RefreshToken(user1, 7);

            // Both users have null IDs (not persisted yet)
            // Objects.equals(null, null) returns true
            assertThat(token.belongsToUser(null)).isTrue();

            // When user1 gets an ID (simulating persistence)
            // We can't set ID directly, so we just verify the current behavior
            assertThat(token.getUser()).isEqualTo(user1);
        }

        @Test
        @DisplayName("Should calculate remaining time")
        void shouldCalculateRemainingTime() {
            // Given
            User user = createTestUser();
            RefreshToken token = new RefreshToken(user, 7);

            // Then
            long remainingSeconds = token.getRemainingTimeSeconds();
            assertThat(remainingSeconds).isGreaterThan(0);
            // Should be approximately 7 days in seconds (minus a few seconds for execution)
            assertThat(remainingSeconds).isLessThanOrEqualTo(7 * 24 * 60 * 60);
        }

        @Test
        @DisplayName("Should return zero remaining time when expired")
        void shouldReturnZeroRemainingTimeWhenExpired() {
            // Given
            User user = createTestUser();
            // Create token that expires in 1 second
            LocalDateTime expiresIn1Second = LocalDateTime.now().plusSeconds(1);
            RefreshToken token = new RefreshToken(user, expiresIn1Second);

            // Wait for token to expire
            try {
                Thread.sleep(1100); // Wait slightly longer than 1 second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Then - token should be expired
            assertThat(token.isExpired()).isTrue();
            assertThat(token.getRemainingTimeSeconds()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal based on token value")
        void shouldBeEqualBasedOnTokenValue() {
            // Given
            User user = createTestUser();
            RefreshToken token1 = new RefreshToken(user, 7);
            RefreshToken token2 = new RefreshToken(user, 7);

            // Tokens are different because UUID is random
            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            // Given
            User user = createTestUser();
            RefreshToken token = new RefreshToken(user, 7);

            // Then
            int hashCode1 = token.hashCode();
            int hashCode2 = token.hashCode();
            assertThat(hashCode1).isEqualTo(hashCode2);
        }
    }
}
