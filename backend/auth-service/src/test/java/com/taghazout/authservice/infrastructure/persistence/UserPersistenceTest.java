package com.taghazout.authservice.infrastructure.persistence;

import com.taghazout.authservice.domain.entity.User;
import com.taghazout.authservice.domain.enums.Role;
import com.taghazout.authservice.infrastructure.adapter.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserPersistenceTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Test
    void shouldSaveAndFindUser() {
        // Use manual constructor as @Builder is not present
        // Use BCrypt hash for password to satisfy validation
        String hashedPw = "$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xcx1D1VQRiV9vvwm";
        User user = new User("persistence@test.com", hashedPw, Role.CLIENT);

        User savedUser = ((JpaRepository<User, Long>) userJpaRepository).save(user);
        assertThat(savedUser.getId()).isNotNull();

        Optional<User> foundUser = userJpaRepository.findByEmail("persistence@test.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("persistence@test.com");
    }
}
