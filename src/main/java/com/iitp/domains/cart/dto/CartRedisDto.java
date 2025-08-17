package com.iitp.domains.cart.dto;

import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.cart.domain.entity.CartMenu;
import com.iitp.domains.cart.dto.response.CartMenuResponse;
import com.iitp.domains.store.domain.entity.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


public record CartRedisDto(
        Long id,
        String imageKey,
        String name,
        int totalCoast,
        List<CartMenuRedisDto> menus
) {
    public static CartRedisDto fromEntity(Store store) {
        return new CartRedisDto(
                store.getId(),
                store.getStoreImages().get(0).getImageKey(),
                store.getName(),
                0,
                new ArrayList<>()
        );
    }

    public  CartRedisDto addMenu(CartMenuRedisDto item, int price) {
        List<CartMenuRedisDto> newMenus = new ArrayList<>();
        if(menus == null){
            newMenus.add(item);
            return new CartRedisDto(id,imageKey, name, totalCoast + price, newMenus);
        }else{
            menus.add(item);
            return new CartRedisDto(id,imageKey, name, totalCoast + price, menus);
        }
    }

//    public CartRedisDto addPrice(int menuPrice) {
//        return new CartRedisDto(id,imageKey, name, totalCoast + menuPrice, menus);
//    }

//    public void clearItems() {
//
//        menus.clear();
//        totalCoast = 0;
//    }
}
