package com.taghazout.listingservice.domain.model;

import lombok.Builder;
import lombok.Value;
import java.util.List;
import java.util.Set;

@Value
@Builder
public class HostelDetails {
    String name;
    String description;
    Address address;
    Money pricePerNight;
    Rating rating;
    Set<Amenity> amenities;
    Availability availability;
    List<ImageUrl> images;
}
