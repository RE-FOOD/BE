package com.iitp.domains.cart.dto.response;

import java.util.List;

public record CartResponse(
        String imageUrl,
        String name,
        int totalCoast,
        List<CartMenuResponse> menus
) {
}


