package com.taghazout.listingservice.infrastructure.web;

import com.taghazout.listingservice.application.dto.CreateListingRequest;
import com.taghazout.listingservice.application.service.CreateListingService;
import com.taghazout.listingservice.application.service.GetListingsService;
import com.taghazout.listingservice.domain.entity.Listing;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/listings")
public class ListingController {

    private final CreateListingService createListingService;
    private final GetListingsService getListingsService;

    public ListingController(CreateListingService createListingService, GetListingsService getListingsService) {
        this.createListingService = createListingService;
        this.getListingsService = getListingsService;
    }

    @PostMapping
    public ResponseEntity<Listing> create(@RequestBody CreateListingRequest request) {
        Listing listing = createListingService.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(listing);
    }

    @GetMapping
    public ResponseEntity<List<Listing>> getListings(@RequestParam(required = false) Long hostId) {
        List<Listing> listings = getListingsService.execute(hostId);
        return ResponseEntity.ok(listings);
    }
}
