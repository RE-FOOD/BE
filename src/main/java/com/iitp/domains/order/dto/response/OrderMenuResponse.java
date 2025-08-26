package com.iitp.domains.order.dto.response;

import lombok.Builder;

@Builder
public record OrderMenuResponse (
    String name,
    Integer orderQuantity,
    String imageUrl,
    int discountPrice
){
}