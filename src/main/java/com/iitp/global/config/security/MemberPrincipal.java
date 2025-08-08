package com.iitp.global.config.security;

import com.iitp.domains.member.domain.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MemberPrincipal {
    private final Long memberId;
    private final String email;
    private final Role role;

}
