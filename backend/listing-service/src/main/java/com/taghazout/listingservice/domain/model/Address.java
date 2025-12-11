package com.taghazout.listingservice.domain.model;

import lombok.Value;

@Value
public class Address {
    String city;
    String country;
    String street;
}
