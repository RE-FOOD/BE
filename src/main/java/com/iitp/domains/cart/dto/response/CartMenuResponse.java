package com.iitp.domains.cart.dto.response;

import com.iitp.domains.cart.dto.CartMenuRedisDto;
import com.iitp.domains.cart.dto.CartRedisDto;

public record CartMenuResponse(
        Long id,
        String name,
        int price,
        int dailyDiscountPercent,
        int dailyQuantity,
		int orderQuantity,
		String imageUrl,
		int discountPrice
) {

	public static CartMenuResponse insertImgURL(CartMenuRedisDto dto, String imageUrl) {
		return new  CartMenuResponse(
				dto.id(),
				dto.name(),
				dto.price(),
				dto.dailyDiscountPercent(),
				dto.dailyQuantity(),
				dto.orderQuantity(),
				imageUrl,
				dto.discountPrice()
		);
	}
}