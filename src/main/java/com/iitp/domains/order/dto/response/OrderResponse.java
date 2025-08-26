package com.iitp.domains.order.dto.response;

import lombok.Builder;

import java.time.LocalTime;
import java.util.List;

@Builder
public record OrderResponse(
    String name,
    String address,
    Integer totalCoast,
    LocalTime openTime,
    LocalTime closeTime,
    List<OrderMenuResponse> menus

) {
}