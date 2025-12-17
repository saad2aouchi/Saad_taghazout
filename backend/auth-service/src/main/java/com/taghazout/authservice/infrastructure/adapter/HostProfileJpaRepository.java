package com.taghazout.authservice.infrastructure.adapter;

import com.taghazout.authservice.domain.entity.HostProfile;
import com.taghazout.authservice.domain.port.HostProfileRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HostProfileJpaRepository extends JpaRepository<HostProfile, Long>, HostProfileRepositoryPort {
}
