package com.taghazout.authservice.domain.port;

import com.taghazout.authservice.domain.entity.User;

import java.util.Optional;

/**
 * Repository port (interface) for User persistence operations.
 * 
 * SOLID Principles:
 * - DIP: Domain depends on this abstraction, not on concrete implementation
 * - ISP: Minimal interface with only needed operations
 * - SRP: Only responsible for User persistence contract
 * 
 * Design Pattern: Repository Pattern + Hexagonal Architecture
 * 
 * The actual implementation (UserJpaRepository) will be in the infrastructure
 * layer,
 * keeping the domain layer independent of persistence technology.
 */
public interface UserRepositoryPort {

    /**
     * Saves a user to the repository.
     * 
     * @param user the user to save
     * @return the saved user (with generated ID if new)
     */
    User save(User user);

    /**
     * Finds a user by email address.
     * 
     * @param email the email to search for
     * @return Optional containing user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by ID.
     * 
     * @param id the user ID
     * @return Optional containing user if found, empty otherwise
     */
    Optional<User> findById(Long id);

    /**
     * Checks if a user exists by email.
     * 
     * @param email the email to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Deletes a user by ID.
     * 
     * @param id the user ID to delete
     */
    void deleteById(Long id);

    /**
     * Counts total number of users.
     * 
     * @return total user count
     */
    long count();
}
