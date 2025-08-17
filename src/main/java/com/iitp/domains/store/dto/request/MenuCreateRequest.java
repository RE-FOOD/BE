package com.iitp.domains.store.dto.request;

import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.domain.entity.Store;

public record MenuCreateRequest(
        String name,
        String info,
        int price,
        int dailyDiscountPrice,
        int dailyQuantity,
        String imageKey
) {

    public Menu toEntity(Store store) {
        return Menu.builder()
                .name(this.name)
                .info(this.info)
                .price(this.price)
                .dailyDiscountPercent((int) Math.round(((double)(this.price - this.dailyDiscountPrice) / this.price) * 100))
                .dailyQuantity(this.dailyQuantity)
                .discountPrice(this.dailyDiscountPrice)
                .imageKey(imageKey)
                .store(store)
                .build();
    }
}
