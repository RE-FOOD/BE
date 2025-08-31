package com.iitp.domains.store.dto.response;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record InsightResponse(
        int salesAmount,
        List<String> popularMenu,
        Map<String, Integer> monthAmount
) {
}
