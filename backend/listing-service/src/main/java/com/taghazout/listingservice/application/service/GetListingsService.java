package com.taghazout.listingservice.application.service;

import com.taghazout.listingservice.domain.entity.Listing;
import com.taghazout.listingservice.domain.repository.ListingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetListingsService {

    private final ListingRepository listingRepository;

    public GetListingsService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public List<Listing> execute(Long hostId) {
        if (hostId != null) {
            return listingRepository.findByHostId(hostId);
        }
        return listingRepository.findAll();
    }
}
