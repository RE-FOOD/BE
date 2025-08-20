package com.iitp.global.config.security;

import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.service.query.MemberQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberQueryService memberQueryService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("사용자 조회 시작 - email: {}", email);

        try {
            Member member = memberQueryService.findMemberByEmail(email);
            log.debug("사용자 조회 성공 - memberId: {}, role: {}", member.getId(), member.getRole());

            return new CustomUserDetails(member);
        } catch (Exception e) {
            log.warn("사용자 조회 실패 - email: {}, error: {}", email, e.getMessage());
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email, e);
        }
    }
    /**
     * ID로 사용자 조회 (JWT에서 사용)
     */
    public UserDetails loadUserById(Long memberId) throws UsernameNotFoundException {
        log.debug("사용자 조회 시작 - memberId: {}", memberId);

        try {
            Member member = memberQueryService.findMemberById(memberId);
            log.debug("사용자 조회 성공 - email: {}, role: {}", member.getEmail(), member.getRole());

            return new CustomUserDetails(member);
        } catch (Exception e) {
            log.warn("사용자 조회 실패 - memberId: {}, error: {}", memberId, e.getMessage());
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + memberId, e);
        }
    }
}
