package com.iitp.domains.order.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record OrderListResponse(
        Long prevCursor,
        Long nextCursor,
        List<OrderMenuListResponse>  orders
) {
}
