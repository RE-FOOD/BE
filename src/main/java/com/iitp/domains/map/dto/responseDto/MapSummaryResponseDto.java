package com.iitp.domains.map.dto.responseDto;

import com.iitp.domains.store.domain.StoreStatus;
import com.iitp.domains.store.domain.entity.Store;
import lombok.Builder;

@Builder
public record MapSummaryResponseDto(
        Long id,
        String name,
        String imageUrl,
        String pickupTime,
        Double distance,
        Double rating,
        Integer reviewCount,
        Integer maxPercent,
        StoreStatus status,
        String address
) {
    public static MapSummaryResponseDto from(Store store, String imageUrl,
                                             String pickupTime, Double distance, Double rating, Integer reviewCount) {
        return MapSummaryResponseDto.builder()
                .id(store.getId())
                .name(store.getName())
                .imageUrl(imageUrl)
                .pickupTime(pickupTime)
                .distance(distance)
                .rating(rating)
                .reviewCount(reviewCount)
                .maxPercent(store.getMaxPercent())
                .status(store.getStatus())
                .address(store.getAddress())
                .build();
    }
}
