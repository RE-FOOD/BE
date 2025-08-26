package com.iitp.domains.map.dto;

public record StoreDistanceDto(
        Long storeId,
        Double distance
) {
    public static StoreDistanceDto of(Long storeId, Double distance) {
        return new StoreDistanceDto(storeId, distance);
    }
}
