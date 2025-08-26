package com.iitp.domains.cart.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartCreateRequest(
        boolean checkNew,
        @NotNull
        Long storeId,
        @NotNull
        Long menuId,
        @Min(1)
        int quantity
) {
}
