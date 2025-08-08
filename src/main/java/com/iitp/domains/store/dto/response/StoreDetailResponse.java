package com.iitp.domains.store.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iitp.domains.store.domain.Category;
import com.iitp.domains.store.domain.entity.Store;

import java.time.LocalTime;
import java.util.List;

public record StoreDetailResponse(
        String name,
        String phoneNumber,
        String address,
        String description,
        String origin,
        @JsonFormat(pattern = "HH:mm")
        LocalTime openTime,
        @JsonFormat(pattern = "HH:mm")
        LocalTime closeTime,
        Category category,
        double latitude,
        double longitude,
        List<String> imageUrl,
        List<MenuListResponse> menus,
        boolean like,
        double ratingAvg,
        int count
) {

    public static StoreDetailResponse from(Store store, List<String> imageUrls, List<MenuListResponse> menus, boolean like, double ratingAvg, int count) {
        return new StoreDetailResponse(
                store.getName(),
                store.getPhoneNumber(),
                store.getAddress(),
                store.getDescription(),
                store.getOrigin(),
                store.getOpenTime(),
                store.getCloseTime(),
                store.getCategory(),
                store.getLatitude(),
                store.getLongitude(),
                imageUrls,
                menus,
                like,
                ratingAvg,
                count
        );
    }
}