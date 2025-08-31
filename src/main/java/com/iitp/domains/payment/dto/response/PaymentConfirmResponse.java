package com.iitp.domains.payment.dto.response;

import com.iitp.domains.member.domain.EnvironmentLevel;
import lombok.Builder;

@Builder
public record PaymentConfirmResponse(
     boolean levelCheck,
     EnvironmentLevel level,
     Long id
) {
}