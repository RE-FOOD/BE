package com.iitp.global.config.security;

import com.iitp.domains.member.domain.Role;
import com.iitp.domains.member.domain.entity.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final Member member;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(member.getRole().name()));
    }

    @Override
    public String getPassword() {
        return null; // JWT 방식이므로 비밀번호 불필요
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !member.getIsDeleted(); // 삭제되지 않은 회원만 활성화
    }

    // 커스텀 메서드들
    public Long getMemberId() {
        return member.getId();
    }

    public String getEmail() {
        return member.getEmail();
    }

    public Role getRole() {
        return member.getRole();
    }
}
