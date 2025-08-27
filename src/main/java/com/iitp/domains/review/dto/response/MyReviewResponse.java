package com.iitp.domains.review.dto.response;

import com.iitp.domains.review.domain.entity.Review;
import com.iitp.domains.store.domain.entity.Menu;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record MyReviewResponse(
        long id,
        long memberId,
        String storeName,
        String content,
        int rating,
        LocalDateTime createdAt,
        List<MenuNameResponse> menuList
) {
    public static MyReviewResponse from(Review review, List<Menu> menuList) {
        return MyReviewResponse.builder()
                .id(review.getId())
                .memberId(review.getMember().getId())
                .storeName(review.getStore().getName())
                .content(review.getContent())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                // TODO: 장바구니 - 주문 연결해야 되지?
                .menuList(menuList.stream().map(MenuNameResponse::from).toList())
                .build();
    }
}
