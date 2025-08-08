package com.iitp.domains.store.dto.response;

import com.iitp.domains.store.domain.entity.Menu;

public record MenuResponse(
        Long id,
        String name,
        String info,
        int price,
        int dailyDiscountPercent,
        int dailyQuantity,
        int totalCoast,
        String imageUrl
) {

    public static MenuResponse fromEntity(Menu menu, int totalCoast, String imageUrl) {
        return new MenuResponse(
                menu.getId(),
                menu.getName(),
                menu.getInfo(),
                menu.getPrice(),
                menu.getDailyDiscountPercent(),
                menu.getDailyQuantity(),
                totalCoast,
                imageUrl
        );
    }
}
