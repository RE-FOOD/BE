package com.iitp.domains.cart.dto;

import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.cart.domain.entity.CartMenu;
import com.iitp.domains.store.domain.entity.Menu;

public record CartMenuRedisDto (
        Long id,
        String name,
        int price,
        int dailyDiscountPercent,
        int dailyQuantity,
        int orderQuantity,
        String imageKey,
        int discountPrice
){

    public static CartMenu toEntity(Cart cart, Long menuId, CartMenuRedisDto cartMenuRedisDto) {
        return CartMenu.builder()
                .cartId(cart.getId())
                .cart(cart)
                .menuId(menuId)
                .price(cartMenuRedisDto.price)
                .dailyDiscountPercent(cartMenuRedisDto.dailyDiscountPercent)
                .orderQuantity(cartMenuRedisDto.orderQuantity)
                .discountPrice(cartMenuRedisDto.discountPrice)
                .build();
    }

    public static CartMenuRedisDto fromEntity(Menu menu, int orderQuantity) {
        return new CartMenuRedisDto(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getDailyDiscountPercent(),
                menu.getDailyQuantity(),
                orderQuantity,
                menu.getImageKey(),
                menu.getDiscountPrice()
        );
    }
}
