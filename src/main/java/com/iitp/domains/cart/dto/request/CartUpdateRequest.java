package com.iitp.domains.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CartUpdateRequest(
        @NotNull
        Long id,
        List<CartMenuListRequest> menus
){
        public record CartMenuListRequest(
                @NotNull
                Long id,
                int quantity
        ) {
        }
}
