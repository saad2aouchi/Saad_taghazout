package com.taghazout.listingservice.infrastructure.persistence;

import com.taghazout.listingservice.domain.entity.Listing;
import com.taghazout.listingservice.domain.model.ListingType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ListingPersistenceTest {

    @Autowired
    private SpringDataListingRepository listingRepository;

    @Test
    void shouldSaveAndFindListing() {
        UUID id = UUID.randomUUID();
        ListingJpaEntity entity = ListingJpaEntity.builder()
                .id(id)
                .hostId(1L)
                .type(ListingType.ACTIVITY)
                .createdAt(LocalDateTime.now())
                .build();

        listingRepository.save(entity);

        Optional<ListingJpaEntity> found = listingRepository.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo(ListingType.ACTIVITY);
        assertThat(found.get().getHostId()).isEqualTo(1L);
    }
}
