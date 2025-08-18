package com.iitp.domains.order.domain;

import com.iitp.global.exception.BadRequestException;
import com.iitp.global.exception.ExceptionMessage;
import lombok.Getter;

@Getter
public enum OrderStatus {
    COMPLETED("주문 완료"),
    CANCELED("주문 취소"),
    PENDING("주문 대기");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public static OrderStatus of(String key) {
        return switch (key.toUpperCase()) {
            case "COMPLETED" -> COMPLETED;
            case "CANCELED" -> CANCELED;
            case "PENDING" -> PENDING;
            default -> throw new BadRequestException(ExceptionMessage.INVALID_ORDER_STATUS);
        };
    }
}
