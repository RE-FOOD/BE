package com.iitp.domains.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record KakaoUserInfoDto(Long id,
    @JsonProperty("kakao_account")KakaoAccount kakaoAccount) {
    @Builder
    public record KakaoAccount(
            String email,
            Profile profile
    ) {
        @Builder
        public record Profile(
                String nickname
        ) {
        }
    }

    public String getEmail() {
        return kakaoAccount != null ? kakaoAccount.email() : null;
    }

    public String getNickname() {
        return kakaoAccount != null && kakaoAccount.profile() != null
                ? kakaoAccount.profile().nickname() : null;
    }
}
