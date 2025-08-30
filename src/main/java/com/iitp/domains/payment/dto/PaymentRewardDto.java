package com.iitp.domains.payment.dto;

import com.iitp.domains.member.domain.EnvironmentLevel;
import lombok.Builder;

@Builder
public record PaymentRewardDto(
        boolean levelCheck,
        EnvironmentLevel level
) {
}
