package com.iitp.domains.map.dto;

import com.iitp.domains.store.domain.entity.Store;

public record StoreLocationDto(
        Long storeId,
        Double latitude,
        Double longitude
) {
    public static StoreLocationDto from(Long storeId, Double latitude, Double longitude) {
        return new StoreLocationDto(storeId, latitude, longitude);
    }

    public static StoreLocationDto fromStore(Store store) {
        return new StoreLocationDto(
                store.getId(),
                store.getLatitude(),
                store.getLongitude()
        );
    }
}