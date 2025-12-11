package com.taghazout.listingservice.application.service;

import com.taghazout.listingservice.application.dto.CreateListingRequest;
import com.taghazout.listingservice.domain.entity.Listing;
import com.taghazout.listingservice.domain.model.ListingType;
import com.taghazout.listingservice.domain.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateListingServiceTest {

    @Mock
    private ListingRepository listingRepository;

    private CreateListingService createListingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        createListingService = new CreateListingService(listingRepository);
    }

    @Test
    void shouldSaveListingWhenValidRequestProvided() {
        // Arrange
        CreateListingRequest request = new CreateListingRequest();
        request.setType(ListingType.ACTIVITY); // Using Activity to simplify test setup (no detailed nested objects
                                               // needed)

        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Listing result = createListingService.execute(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(ListingType.ACTIVITY, result.getType());
        verify(listingRepository, times(1)).save(any(Listing.class));
    }

    // Additional tests for Hostel type would involve populating the complex DTO
}
