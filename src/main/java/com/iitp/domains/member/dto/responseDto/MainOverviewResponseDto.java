package com.iitp.domains.member.dto.responseDto;

import lombok.Builder;

import java.util.List;

@Builder
public record MainOverviewResponseDto(
        Integer cartCount,
        boolean hasUnread,
        LocationResponseDto locations,
        List<DiscountMenuResponseDto> discountMenu,
        List<PopularStoreResponseDto> popularStores
) {
    public static MainOverviewResponseDto of(
            Integer cartCount,
            boolean hasUnreadNotification,
            LocationResponseDto locations,
            List<DiscountMenuResponseDto> discountMenu,
            List<PopularStoreResponseDto> popularStores
    ) {
        return MainOverviewResponseDto.builder()
                .cartCount(cartCount)
                .hasUnread(hasUnreadNotification)
                .locations(locations)
                .discountMenu(discountMenu)
                .popularStores(popularStores)
                .build();
    }
}
