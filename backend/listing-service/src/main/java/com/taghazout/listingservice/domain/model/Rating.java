package com.taghazout.listingservice.domain.model;

import lombok.Value;

@Value
public class Rating {
    double score;
    int reviewCount;
}
