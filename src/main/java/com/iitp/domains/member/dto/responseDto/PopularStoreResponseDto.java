package com.iitp.domains.member.dto.responseDto;

import com.iitp.domains.store.domain.entity.Store;
import lombok.Builder;

@Builder
public record PopularStoreResponseDto(
        Long id,
        String name,
        String imageUrl,
        Double distance,
        Double ratingAvg
) {
    public static PopularStoreResponseDto of(
            Long id,
            String name,
            String imageUrl,
            Double distance,
            Double ratingAvg
    ) {
        return PopularStoreResponseDto.builder()
                .id(id)
                .name(name)
                .imageUrl(imageUrl)
                .distance(distance)
                .ratingAvg(ratingAvg)
                .build();
    }

    public static PopularStoreResponseDto from(Store store, String imageUrl, Double distance, Double ratingAvg) {
        return PopularStoreResponseDto.builder()
                .id(store.getId())
                .name(store.getName())
                .imageUrl(imageUrl)
                .distance(distance)
                .ratingAvg(ratingAvg != null ? ratingAvg : 0.0)
                .build();
    }
}
