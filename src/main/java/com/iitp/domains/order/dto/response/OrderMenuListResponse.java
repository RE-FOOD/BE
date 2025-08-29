package com.iitp.domains.order.dto.response;

import lombok.Builder;

@Builder
public record OrderMenuListResponse (
        Long orderId,
        Long storeId,
        String storeName,
        String imageUrl,
        boolean status,
        String menuName
){
}
