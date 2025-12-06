package com.taghazout.authservice.infrastructure.adapter;

import com.taghazout.authservice.domain.entity.User;
import com.taghazout.authservice.domain.port.UserRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository adapter for User entity.
 * 
 * This interface extends both:
 * - JpaRepository (Spring Data JPA)
 * - UserRepositoryPort (our domain interface)
 * 
 * Spring Data JPA automatically provides implementations for:
 * - save()
 * - findById()
 * - existsBy...()
 * - deleteById()
 * - count()
 * 
 * We only need to declare custom query methods.
 * 
 * SOLID Principles:
 * - DIP: Implements domain port (infrastructure depends on domain)
 * - ISP: Inherits minimal interface from port
 * - OCP: Can be extended with custom queries
 */
@Repository
public interface UserJpaRepository extends JpaRepository<User, Long>, UserRepositoryPort {

    /**
     * Find user by email address.
     * 
     * Spring Data JPA automatically implements this based on method name.
     * Query: SELECT * FROM users WHERE email = ?
     * 
     * @param email the email to search for
     * @return Optional containing user if found
     */
    @Override
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by email.
     * 
     * Spring Data JPA automatically implements this.
     * More efficient than findByEmail() when only checking existence.
     * Query: SELECT COUNT(*) FROM users WHERE email = ?
     * 
     * @param email the email to check
     * @return true if user exists
     */
    @Override
    boolean existsByEmail(String email);
}
