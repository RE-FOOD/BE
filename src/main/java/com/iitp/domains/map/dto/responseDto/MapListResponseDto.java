package com.iitp.domains.map.dto.responseDto;

import com.iitp.domains.store.domain.Category;
import com.iitp.domains.store.domain.StoreStatus;
import com.iitp.domains.store.domain.entity.Store;
import lombok.Builder;

@Builder
public record MapListResponseDto(
        Long id,
        String name,
        String imageUrl,
        Double distance,
        Double rating,
        Integer reviewCount,
        Integer maxPercent,
        StoreStatus status,
        Category category
) {
    public static MapListResponseDto from(Store store, String imageUrl,
                                          Double distance, Double rating, Integer reviewCount) {
        return MapListResponseDto.builder()
                .id(store.getId())
                .name(store.getName())
                .imageUrl(imageUrl)
                .distance(distance)
                .rating(rating)
                .reviewCount(reviewCount)
                .maxPercent(store.getMaxPercent())
                .status(store.getStatus())
                .category(store.getCategory())
                .build();
    }
}
