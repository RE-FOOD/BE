package com.iitp.domains.cart.dto;

import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.cart.domain.entity.CartMenu;
import com.iitp.domains.cart.dto.response.CartMenuResponse;
import com.iitp.domains.store.domain.entity.Store;
import jakarta.persistence.*;
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

    public static Cart toEntity(Store store , Long memberId, CartRedisDto cartRedisDto) {
        return Cart.builder()
                .store(store)
                .memberId(memberId)
                .totalPrice(cartRedisDto.totalCoast)
                .cartMenus(new ArrayList<>())
                .build();
    }

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

    // 기존 메뉴 수량 증가 (새 객체 생성하지 않고 기존 객체 수정)
    public CartRedisDto updateMenuQuantity(Long menuId, int additionalQuantity) {
        if (menus == null) {
            return this; // menus가 null이면 변경 없음
        }

        List<CartMenuRedisDto> updatedMenus = new ArrayList<>();
        boolean menuFound = false;

        for (CartMenuRedisDto menu : menus) {
            if (menu.id().equals(menuId)) {
                // 기존 메뉴 수량 증가
                CartMenuRedisDto updatedMenu = new CartMenuRedisDto(
                        menu.id(), menu.name(), menu.price(), menu.dailyDiscountPercent(),
                        menu.dailyQuantity(), menu.orderQuantity() + additionalQuantity,
                        menu.imageKey(), menu.discountPrice()
                );
                updatedMenus.add(updatedMenu);
                menuFound = true;
            } else {
                // 다른 메뉴는 그대로 유지
                updatedMenus.add(menu);
            }
        }

        // 메뉴를 찾지 못했으면 기존 상태 유지
        if (!menuFound) {
            return this;
        }

        // 총 가격 재계산
        int newTotalCoast = updatedMenus.stream()
                .mapToInt(menu -> menu.discountPrice() * menu.orderQuantity())
                .sum();

        return new CartRedisDto(id, imageKey, name, newTotalCoast, updatedMenus);
    }

    // 메뉴 추가 (새로운 메뉴인 경우)
    public CartRedisDto addNewMenu(CartMenuRedisDto newMenu) {
        List<CartMenuRedisDto> updatedMenus = new ArrayList<>();

        if (menus != null) {
            updatedMenus.addAll(menus);
        }

        updatedMenus.add(newMenu);

        // 총 가격 재계산
        int newTotalCoast = updatedMenus.stream()
                .mapToInt(menu -> menu.discountPrice() * menu.orderQuantity())
                .sum();

        return new CartRedisDto(id, imageKey, name, newTotalCoast, updatedMenus);
    }

}
