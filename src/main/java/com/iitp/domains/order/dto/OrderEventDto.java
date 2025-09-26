package com.iitp.domains.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class OrderEventDto {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IssueMessage {
        private Long memberId;
        private Long menuId;
        private Long stockQuantity;
    }
}
