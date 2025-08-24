package com.iitp.domains.map.dto.responseDto;

import com.iitp.domains.store.domain.StoreStatus;
import com.iitp.domains.store.domain.entity.Store;
import lombok.Builder;

@Builder
public record MapMarkerResponseDto(
        Long id,
        String name,
        Double latitude,
        Double longitude,
        StoreStatus status,
        Integer maxPercent
) {
    public static MapMarkerResponseDto from(Store store) {
        return MapMarkerResponseDto.builder()
                .id(store.getId())
                .name(store.getName())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .status(store.getStatus())
                .maxPercent(store.getMaxPercent())
                .build();
    }
}
