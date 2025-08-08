package com.iitp.domains.store.dto.response;

import com.iitp.domains.store.repository.mapper.MenuListQueryResult;

public record MenuListResponse(
        Long id,
        String name,
        int price,
        int dailyDiscountPercent,
        int dailyQuantity,
        String imageUrl
) {

    public static MenuListResponse fromQueryResult(MenuListQueryResult result, String imageUrl) {
        return new MenuListResponse(
                result.id(),
                result.name(),
                result.price(),
                result.dailyDiscountPercent(),
                result.dailyQuantity(),
                imageUrl
        );
    }
}
