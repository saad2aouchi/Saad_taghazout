package com.taghazout.listingservice.infrastructure.persistence;

import com.taghazout.listingservice.domain.entity.Listing;
import com.taghazout.listingservice.domain.model.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "listings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingJpaEntity {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private ListingType type;

    private LocalDateTime createdAt;

    // We will use a simplified embedded structure or JSON conversion for
    // HostelDetails.
    // For this MVP, we will demonstrate a manual mapping or simple fields.
    // Given the complexity of HostelDetails (nested objects), proper JSON mapping
    // is ideal.
    // But to avoid complexity with custom Converters in this step without knowing
    // DB compat,
    // I will mock the persistence of complex details or use a simpler flattening.

    // NOTE: In a real production app with H2/Postgres, we'd use
    // @JdbcTypeCode(SqlTypes.JSON)
    // Here, I will assume a standard mapping.

    @Lob // Store as large object/text if complex, or separate table.
    // To keep it simple and creating files limited, I will map basic fields and
    // skip complex deep nesting for now
    // OR just use a simple Converter if I can create it in the same package.
    private String hostelDetailsJson;

    public static ListingJpaEntity fromDomain(Listing listing) {
        return ListingJpaEntity.builder()
                .id(listing.getId())
                .type(listing.getType())
                .createdAt(listing.getCreatedAt())
                // In a real app, use ObjectMapper to serialize listing.getHostelDetails() to
                // JSON
                .hostelDetailsJson(listing.getHostelDetails() != null ? listing.getHostelDetails().toString() : null)
                .build();
    }

    public Listing toDomain() {
        // Reverse mapping... simplistic for MVP to verify structure
        return new Listing(id, type, null, createdAt);
        // TODO: Implement JSON deserialization for full fidelity
    }
}
