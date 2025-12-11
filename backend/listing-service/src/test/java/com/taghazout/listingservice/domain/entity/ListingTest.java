package com.taghazout.listingservice.domain.entity;

import com.taghazout.listingservice.domain.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ListingTest {

    @Test
    void shouldCreateHostelListingResultSuccess() {
        HostelDetails details = HostelDetails.builder()
                .name("Test Hostel")
                .build();

        Listing listing = new Listing(
                UUID.randomUUID(),
                ListingType.HOSTEL,
                details,
                LocalDateTime.now()
        );

        assertNotNull(listing);
        assertEquals(ListingType.HOSTEL, listing.getType());
        assertEquals(details, listing.getHostelDetails());
    }

    @Test
    void shouldThrowExceptionWhenHostelDetailsMissingForHostelType() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Listing(
                    UUID.randomUUID(),
                    ListingType.HOSTEL,
                    null,
                    LocalDateTime.now()
            );
        });

        assertEquals("Hostel details are required for HOSTEL listing type", exception.getMessage());
    }

    @Test
    void shouldCreateActivityListingWithoutHostelDetails() {
        Listing listing = new Listing(
                UUID.randomUUID(),
                ListingType.ACTIVITY,
                null,
                LocalDateTime.now()
        );

        assertNotNull(listing);
        assertEquals(ListingType.ACTIVITY, listing.getType());
        assertNull(listing.getHostelDetails());
    }
}
