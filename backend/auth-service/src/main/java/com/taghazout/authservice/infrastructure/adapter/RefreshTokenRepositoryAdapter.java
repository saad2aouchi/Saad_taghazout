package com.taghazout.authservice.infrastructure.adapter;

import com.taghazout.authservice.domain.port.RefreshTokenRepositoryPort;
import com.taghazout.authservice.domain.entity.RefreshToken;
import com.taghazout.authservice.domain.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Adapter that implements RefreshTokenRepositoryPort using
 * RefreshTokenJpaRepository.
 * 
 * This adapter bridges the domain layer (port interface) with the
 * infrastructure layer (JPA).
 * 
 * Why separate adapter instead of having JpaRepository implement port directly?
 * - JpaRepository brings many methods we don't want in the port interface (LSP
 * violation)
 * - Cleaner separation of concerns
 * - Easier to test (can mock the port without Spring Data)
 * 
 * SOLID Principles:
 * - DIP: Implements domain port interface
 * - SRP: Only responsible for delegating to JPA repository
 * - ISP: Port interface has only needed methods
 */
@Component
@Transactional
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenJpaRepository jpaRepository;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return jpaRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token);
    }

    @Override
    public List<RefreshToken> findByUser(User user) {
        return jpaRepository.findByUser(user);
    }

    @Override
    public List<RefreshToken> findValidTokensByUserId(Long userId) {
        return jpaRepository.findValidTokensByUserId(userId, LocalDateTime.now());
    }

    @Override
    public void deleteByToken(String token) {
        jpaRepository.deleteByToken(token);
    }

    @Override
    public void deleteByUser(User user) {
        jpaRepository.deleteByUser(user);
    }

    @Override
    public int deleteExpiredTokens() {
        return jpaRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    @Override
    public boolean existsByTokenAndIsValid(String token) {
        return jpaRepository.existsByTokenAndIsValid(token, LocalDateTime.now());
    }
}
