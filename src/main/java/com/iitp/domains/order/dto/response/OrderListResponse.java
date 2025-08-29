package com.iitp.domains.order.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record OrderListResponse(
        int prevCursor,
        int nextCursor,
        List<OrderMenuListResponse>  orders
) {
}
