package com.iitp.domains.member.dto.responseDto;

import com.iitp.domains.store.domain.entity.Menu;
import lombok.Builder;

@Builder
public record DiscountMenuResponseDto(
        Long storeId,
        Long menuId,
        String name,
        String menuName,
        String imageUrl,
        Integer price,
        Integer discountPercent,
        Integer discountPrice,
        Double ratingAvg
) {
    public static DiscountMenuResponseDto of(
            Long storeId,
            Long menuId,
            String name,
            String menuName,
            String imageUrl,
            Integer price,
            Integer discountPercent,
            Integer discountPrice,
            Double ratingAvg
    ) {
        return DiscountMenuResponseDto.builder()
                .storeId(storeId)
                .menuId(menuId)
                .name(name)
                .menuName(menuName)
                .imageUrl(imageUrl)
                .price(price)
                .discountPercent(discountPercent)
                .discountPrice(discountPrice)
                .ratingAvg(ratingAvg)
                .build();
    }
    public static DiscountMenuResponseDto from(Menu menu, Double ratingAvg) {
        return DiscountMenuResponseDto.builder()
                .storeId(menu.getStore().getId())
                .menuId(menu.getId())
                .name(menu.getStore().getName())        // 가게 이름
                .menuName(menu.getName())               // 메뉴 이름
                .imageUrl(null)                         // 이미지 URL은 별도 처리 필요
                .price(menu.getPrice())
                .discountPercent(menu.getDailyDiscountPercent())
                .discountPrice(menu.getDiscountPrice())
                .ratingAvg(ratingAvg != null ? ratingAvg : 0.0)
                .build();
    }
}
