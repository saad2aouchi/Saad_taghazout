package com.taghazout.listingservice.infrastructure.web;

import com.taghazout.listingservice.application.dto.CreateListingRequest;
import com.taghazout.listingservice.application.service.CreateListingService;
import com.taghazout.listingservice.domain.entity.Listing;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/listings")
public class ListingController {

    private final CreateListingService createListingService;

    public ListingController(CreateListingService createListingService) {
        this.createListingService = createListingService;
    }

    @PostMapping
    public ResponseEntity<Listing> create(@RequestBody CreateListingRequest request) {
        Listing listing = createListingService.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(listing);
    }
}
