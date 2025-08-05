package com.iitp.domains.member.config.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MemberPrincipal {
    private final Long memberId;
    private final String email;
    private final String role;

    public boolean isUser() {
        return "ROLE_USER".equals(role);
    }
    public boolean isStore() {
        return "ROLE_STORE".equals(role);
    }
}
