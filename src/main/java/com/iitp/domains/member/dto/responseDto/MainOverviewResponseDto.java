package com.iitp.domains.member.dto.responseDto;

import lombok.Builder;

import java.util.List;

@Builder
public record MainOverviewResponseDto(
        Integer cartCount,
//        Boolean notifications,
        LocationResponseDto locations,
        List<DiscountMenuResponseDto> discountMenu,
        List<PopularStoreResponseDto> popularStores
) {
    public static MainOverviewResponseDto of(
            Integer cartCount,
//            Boolean notifications,
            LocationResponseDto locations,
            List<DiscountMenuResponseDto> discountMenu,
            List<PopularStoreResponseDto> popularStores
    ) {
        return MainOverviewResponseDto.builder()
                .cartCount(cartCount)
//                .notifications(notifications)
                .locations(locations)
                .discountMenu(discountMenu)
                .popularStores(popularStores)
                .build();
    }
}
