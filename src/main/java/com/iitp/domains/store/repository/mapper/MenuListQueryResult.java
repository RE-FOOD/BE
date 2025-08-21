package com.iitp.domains.store.repository.mapper;

public record MenuListQueryResult(
        Long id,
        String name,
        int price,
        int dailyDiscountPercent,
        int dailyQuantity,
        int discountPrice,
        String imageKey
) {
}