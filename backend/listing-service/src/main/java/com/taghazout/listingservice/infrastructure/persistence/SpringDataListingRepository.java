package com.taghazout.listingservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataListingRepository extends JpaRepository<ListingJpaEntity, UUID> {
    java.util.List<ListingJpaEntity> findByHostId(Long hostId);
}
