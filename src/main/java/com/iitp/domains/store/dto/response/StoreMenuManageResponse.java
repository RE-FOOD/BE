package com.iitp.domains.store.dto.response;


public record StoreMenuManageResponse(
        Long menuId,
        String menuName,
        String info,
        int price,
        String imageUrl
) {
}
