package com.taghazout.listingservice.domain.entity;

import com.taghazout.listingservice.domain.model.HostelDetails;
import com.taghazout.listingservice.domain.model.ListingType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class Listing {
    private final UUID id;
    private final Long hostId;
    private final ListingType type;
    private final HostelDetails hostelDetails;
    private final LocalDateTime createdAt;

    public Listing(UUID id, Long hostId, ListingType type, HostelDetails hostelDetails, LocalDateTime createdAt) {
        this.id = id;
        this.hostId = hostId;
        this.type = type;
        this.hostelDetails = hostelDetails;
        this.createdAt = createdAt;
        validate();
    }

    private void validate() {
        if (type == ListingType.HOSTEL && hostelDetails == null) {
            throw new IllegalArgumentException("Hostel details are required for HOSTEL listing type");
        }
    }
}
