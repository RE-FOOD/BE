package com.iitp.domains.review.dto.response;

import com.iitp.domains.store.domain.entity.Menu;
import lombok.Builder;

@Builder
public record MenuNameResponse(long id, String name) {
    public static MenuNameResponse from(Menu menu) {
        return MenuNameResponse.builder()
                .id(menu.getId())
                .name(menu.getName())
                .build();
    }
}
