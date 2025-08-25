package com.iitp.domains.cart.dto.response;

import java.util.ArrayList;
import java.util.List;


public record CartResponse(
        String imageUrl,
        String name,
        int totalCoast,
        List<CartMenuResponse> menus
) {

    public static CartResponse createEmptyCartResponse() {
        return new  CartResponse(
                null,
                null,
                0,
                new ArrayList<>()
        );
    }
}


