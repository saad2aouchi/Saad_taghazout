package com.taghazout.authservice.domain.entity;

import com.taghazout.authservice.domain.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for User entity
 */
@DisplayName("User Entity Tests")
class UserTest {

    private static final String VALID_EMAIL = "test@example.com";
    private static final String BCRYPT_HASH = "$2a$12$abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNO";

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create user with valid email and password")
        void shouldCreateUserWithValidEmailAndPassword() {
            // When
            User user = new User(VALID_EMAIL, BCRYPT_HASH, Role.CLIENT);

            // Then
            assertThat(user.getEmail()).isEqualTo(VALID_EMAIL);
            assertThat(user.getPassword()).isEqualTo(BCRYPT_HASH);
            assertThat(user.getRole()).isEqualTo(Role.CLIENT);
            assertThat(user.isEnabled()).isTrue();
            assertThat(user.isLocked()).isFalse();
            assertThat(user.getCreatedAt()).isNotNull();
            assertThat(user.canAuthenticate()).isTrue();
        }

        @Test
        @DisplayName("Should create user with full details")
        void shouldCreateUserWithFullDetails() {
            // When
            User user = new User(VALID_EMAIL, BCRYPT_HASH, "John", "Doe", Role.HOST);

            // Then
            assertThat(user.getEmail()).isEqualTo(VALID_EMAIL);
            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Doe");
            assertThat(user.getRole()).isEqualTo(Role.HOST);
        }

        @Test
        @DisplayName("Should throw exception for null email")
        void shouldThrowExceptionForNullEmail() {
            assertThatThrownBy(() -> new User(null, BCRYPT_HASH, Role.CLIENT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw exception for empty email")
        void shouldThrowExceptionForEmptyEmail() {
            assertThatThrownBy(() -> new User("", BCRYPT_HASH, Role.CLIENT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw exception for invalid email format")
        void shouldThrowExceptionForInvalidEmailFormat() {
            assertThatThrownBy(() -> new User("invalid-email", BCRYPT_HASH, Role.CLIENT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid email format");
        }

        @Test
        @DisplayName("Should throw exception for null password")
        void shouldThrowExceptionForNullPassword() {
            assertThatThrownBy(() -> new User(VALID_EMAIL, null, Role.CLIENT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Password cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw exception for non-BCrypt password")
        void shouldThrowExceptionForNonBCryptPassword() {
            assertThatThrownBy(() -> new User(VALID_EMAIL, "plaintext", Role.CLIENT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Password must be BCrypt hashed");
        }

        @Test
        @DisplayName("Should throw exception for null role")
        void shouldThrowExceptionForNullRole() {
            assertThatThrownBy(() -> new User(VALID_EMAIL, BCRYPT_HASH, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Role cannot be null");
        }
    }

    @Nested
    @DisplayName("Business Method Tests")
    class BusinessMethodTests {

        @Test
        @DisplayName("Should update password")
        void shouldUpdatePassword() {
            // Given
            User user = new User(VALID_EMAIL, BCRYPT_HASH, Role.CLIENT);
            String newHash = "$2a$12$newHashValue0123456789ABCDEFGHIJKLMNOPQRSTUV";

            // When
            user.updatePassword(newHash);

            // Then
            assertThat(user.getPassword()).isEqualTo(newHash);
        }

        @Test
        @DisplayName("Should update profile")
        void shouldUpdateProfile() {
            // Given
            User user = new User(VALID_EMAIL, BCRYPT_HASH, Role.CLIENT);

            // When
            user.updateProfile("Jane", "Smith");

            // Then
            assertThat(user.getFirstName()).isEqualTo("Jane");
            assertThat(user.getLastName()).isEqualTo("Smith");
        }

        @Test
        @DisplayName("Should lock account")
        void shouldLockAccount() {
            // Given
            User user = new User(VALID_EMAIL, BCRYPT_HASH, Role.CLIENT);

            // When
            user.lock();

            // Then
            assertThat(user.isLocked()).isTrue();
            assertThat(user.canAuthenticate()).isFalse();
        }

        @Test
        @DisplayName("Should unlock account")
        void shouldUnlockAccount() {
            // Given
            User user = new User(VALID_EMAIL, BCRYPT_HASH, Role.CLIENT);
            user.lock();

            // When
            user.unlock();

            // Then
            assertThat(user.isLocked()).isFalse();
            assertThat(user.canAuthenticate()).isTrue();
        }

        @Test
        @DisplayName("Should disable account")
        void shouldDisableAccount() {
            // Given
            User user = new User(VALID_EMAIL, BCRYPT_HASH, Role.CLIENT);

            // When
            user.disable();

            // Then
            assertThat(user.isEnabled()).isFalse();
            assertThat(user.canAuthenticate()).isFalse();
        }

        @Test
        @DisplayName("Should enable account")
        void shouldEnableAccount() {
            // Given
            User user = new User(VALID_EMAIL, BCRYPT_HASH, Role.CLIENT);
            user.disable();

            // When
            user.enable();

            // Then
            assertThat(user.isEnabled()).isTrue();
            assertThat(user.canAuthenticate()).isTrue();
        }

        @Test
        @DisplayName("Should not allow authentication when locked")
        void shouldNotAllowAuthenticationWhenLocked() {
            // Given
            User user = new User(VALID_EMAIL, BCRYPT_HASH, Role.CLIENT);
            user.lock();

            // Then
            assertThat(user.canAuthenticate()).isFalse();
        }

        @Test
        @DisplayName("Should not allow authentication when disabled")
        void shouldNotAllowAuthenticationWhenDisabled() {
            // Given
            User user = new User(VALID_EMAIL, BCRYPT_HASH, Role.CLIENT);
            user.disable();

            // Then
            assertThat(user.canAuthenticate()).isFalse();
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal based on email")
        void shouldBeEqualBasedOnEmail() {
            // Given
            User user1 = new User(VALID_EMAIL, BCRYPT_HASH, Role.CLIENT);
            User user2 = new User(VALID_EMAIL, BCRYPT_HASH, Role.HOST); // Role doesn't affect equality (only email)

            // Then
            assertThat(user1).isEqualTo(user2);
            assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal with different emails")
        void shouldNotBeEqualWithDifferentEmails() {
            // Given
            User user1 = new User("user1@example.com", BCRYPT_HASH, Role.CLIENT);
            User user2 = new User("user2@example.com", BCRYPT_HASH, Role.CLIENT);

            // Then
            assertThat(user1).isNotEqualTo(user2);
        }
    }
}
