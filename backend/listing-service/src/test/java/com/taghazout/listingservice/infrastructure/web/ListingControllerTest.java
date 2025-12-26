package com.taghazout.listingservice.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taghazout.listingservice.application.dto.CreateListingRequest;
import com.taghazout.listingservice.application.service.CreateListingService;
import com.taghazout.listingservice.domain.entity.Listing;
import com.taghazout.listingservice.domain.model.ListingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ListingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CreateListingService createListingService;

    @InjectMocks
    private ListingController listingController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(listingController).build();
    }

    @Test
    void shouldReturn201CreatedWhenValidListingIsPosted() throws Exception {
        // Arrange
        CreateListingRequest request = new CreateListingRequest();
        request.setType(ListingType.ACTIVITY);

        Listing listing = new Listing(UUID.randomUUID(), 1L, ListingType.ACTIVITY, null, LocalDateTime.now());

        when(createListingService.execute(any(CreateListingRequest.class))).thenReturn(listing);

        // Act & Assert
        mockMvc.perform(post("/api/v1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value("ACTIVITY"));
    }
}
