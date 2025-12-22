package com.taghazout.listingservice.application.dto;

import com.taghazout.listingservice.domain.model.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class CreateListingRequest {
    private Long hostId;
    private ListingType type;
    private HostelDetailsRequest hostelDetails;

    @Data
    @NoArgsConstructor
    public static class HostelDetailsRequest {
        private String name;
        private String description;
        private AddressRequest address;
        private MoneyRequest pricePerNight;
        private RatingRequest rating;
        private Set<Amenity> amenities;
        private AvailabilityRequest availability;
        private List<String> images;
    }

    @Data
    @NoArgsConstructor
    public static class AddressRequest {
        private String city;
        private String country;
        private String street;

        public Address toDomain() {
            return new Address(city, country, street);
        }
    }

    @Data
    @NoArgsConstructor
    public static class MoneyRequest {
        private BigDecimal amount;
        private String currency;

        public Money toDomain() {
            return new Money(amount, currency);
        }
    }

    @Data
    @NoArgsConstructor
    public static class RatingRequest {
        private double score;
        private int reviewCount;

        public Rating toDomain() {
            return new Rating(score, reviewCount);
        }
    }

    @Data
    @NoArgsConstructor
    public static class AvailabilityRequest {
        private int totalBeds;
        private int availableBeds;

        public Availability toDomain() {
            return new Availability(totalBeds, availableBeds);
        }
    }

    public HostelDetails toHostelDetailsDomain() {
        if (hostelDetails == null)
            return null;
        return HostelDetails.builder()
                .name(hostelDetails.name)
                .description(hostelDetails.description)
                .address(hostelDetails.address.toDomain())
                .pricePerNight(hostelDetails.pricePerNight.toDomain())
                .rating(hostelDetails.rating.toDomain())
                .amenities(hostelDetails.amenities)
                .availability(hostelDetails.availability.toDomain())
                .images(hostelDetails.images.stream().map(ImageUrl::new).collect(Collectors.toList()))
                .build();
    }
}
