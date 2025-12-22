package com.taghazout.listingservice.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taghazout.listingservice.domain.entity.Listing;
import com.taghazout.listingservice.domain.model.HostelDetails;
import com.taghazout.listingservice.domain.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ListingRepositoryImpl implements ListingRepository {

    private final SpringDataListingRepository jpaRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Listing save(Listing listing) {
        ListingJpaEntity entity = mapToEntity(listing);
        ListingJpaEntity savedEntity = jpaRepository.save(entity);
        return mapToDomain(savedEntity);
    }

    @Override
    public Optional<Listing> findById(UUID id) {
        return jpaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<Listing> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Listing> findByHostId(Long hostId) {
        return jpaRepository.findByHostId(hostId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private ListingJpaEntity mapToEntity(Listing listing) {
        String detailsJson = null;
        if (listing.getHostelDetails() != null) {
            try {
                detailsJson = objectMapper.writeValueAsString(listing.getHostelDetails());
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error converting HostelDetails to JSON", e);
            }
        }

        return ListingJpaEntity.builder()
                .id(listing.getId())
                .hostId(listing.getHostId())
                .type(listing.getType())
                .createdAt(listing.getCreatedAt())
                .hostelDetailsJson(detailsJson)
                .build();
    }

    private Listing mapToDomain(ListingJpaEntity entity) {
        HostelDetails details = null;
        if (entity.getHostelDetailsJson() != null) {
            try {
                details = objectMapper.readValue(entity.getHostelDetailsJson(), HostelDetails.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error converting JSON to HostelDetails", e);
            }
        }

        return new Listing(
                entity.getId(),
                entity.getHostId(),
                type,
                details,
                entity.getCreatedAt());
    }
}
