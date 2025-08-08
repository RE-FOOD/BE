package com.iitp.domains.store.dto.request;

import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.domain.entity.Store;

public record MenuCreateRequest(
        String name,
        String info,
        int price,
        int dailyDiscountPercent,
        int dailyQuantity,
        String imageKey
) {

    public Menu toEntity(Store store) {
        return Menu.builder()
                .name(this.name)
                .info(this.info)
                .price(this.price)
                .dailyDiscountPercent(this.dailyDiscountPercent)
                .dailyQuantity(this.dailyQuantity)
                .imageKey(imageKey)
                .store(store)
                .build();
    }
}