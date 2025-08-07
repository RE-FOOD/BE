package com.iitp.domains.member.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record StoreSignupRequestDto(
    @NotBlank(message = "액세스 토큰은 필수입니다.")
    String accessToken,

    @NotBlank(message = "사업자 번호는 필수입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$|^\\d{10}$", message = "올바른 사업자 번호 형식이 아닙니다.")
    String businessLicenseNumber,

    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 전화번호 형식이 아닙니다.")
    String phone
){
}
