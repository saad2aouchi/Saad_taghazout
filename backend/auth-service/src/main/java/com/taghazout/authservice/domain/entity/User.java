package com.taghazout.authservice.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * User domain entity representing an authenticated user.
 * 
 * SOLID Principles:
 * - SRP: Only responsible for user data and identity
 * - OCP: Can be extended for additional user properties
 * - LSP: Pure JPA entity, substitutable in any JPA context
 * 
 * Design Decisions:
 * - Uses Long ID for PostgreSQL compatibility (auto-increment)
 * - Email as unique identifier (cannot be changed after creation)
 * - Password stored as BCrypt hash (never plain text)
 * - Audit fields (createdAt, updatedAt) for tracking
 * - Immutable after creation (defensive design)
 * 
 * Database Compatibility:
 * - H2: IDENTITY generation strategy works
 * - PostgreSQL: IDENTITY maps to SERIAL/BIGSERIAL
 * - All annotations are JPA standard (portable)
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 60)
    private String password; // BCrypt hash (always 60 characters)

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "is_locked", nullable = false)
    private boolean locked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Default constructor required by JPA.
     * Do not use directly - use builder or factory method instead.
     */
    protected User() {
        // JPA requires no-arg constructor
    }

    /**
     * Creates a new User with email and hashed password.
     * 
     * @param email          user's email (must be unique)
     * @param hashedPassword BCrypt-hashed password
     * @throws IllegalArgumentException if email or password is null/empty
     */
    public User(String email, String hashedPassword) {
        validateEmail(email);
        validatePassword(hashedPassword);

        this.email = email;
        this.password = hashedPassword;
        this.enabled = true;
        this.locked = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Full constructor for user with all details.
     * 
     * @param email          user's email
     * @param hashedPassword BCrypt-hashed password
     * @param firstName      user's first name
     * @param lastName       user's last name
     */
    public User(String email, String hashedPassword, String firstName, String lastName) {
        this(email, hashedPassword);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // === JPA Lifecycle Callbacks ===

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // === Business Methods ===

    /**
     * Updates user's password with a new BCrypt hash.
     * 
     * @param newHashedPassword new BCrypt-hashed password
     */
    public void updatePassword(String newHashedPassword) {
        validatePassword(newHashedPassword);
        this.password = newHashedPassword;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates user's profile information.
     * 
     * @param firstName new first name
     * @param lastName  new last name
     */
    public void updateProfile(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Locks the user account (prevents login).
     */
    public void lock() {
        this.locked = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Unlocks the user account.
     */
    public void unlock() {
        this.locked = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Disables the user account.
     */
    public void disable() {
        this.enabled = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Enables the user account.
     */
    public void enable() {
        this.enabled = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if user can authenticate.
     * 
     * @return true if user is enabled and not locked
     */
    public boolean canAuthenticate() {
        return this.enabled && !this.locked;
    }

    // === Validation Methods ===

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        // BCrypt hashes are always 60 characters
        if (!password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
            throw new IllegalArgumentException("Password must be BCrypt hashed");
        }
    }

    // === Getters ===

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isLocked() {
        return locked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // === equals() and hashCode() ===

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    // === toString() ===

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", enabled=" + enabled +
                ", locked=" + locked +
                ", createdAt=" + createdAt +
                '}';
    }
}
