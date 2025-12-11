package com.taghazout.listingservice.domain.repository;

import com.taghazout.listingservice.domain.entity.Listing;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingRepository {
    Listing save(Listing listing);

    Optional<Listing> findById(UUID id);

    List<Listing> findAll();
    // This is a port (interface) in the Domain layer.
    // Implementation belongs in Infrastructure.
}
