package com.iitp.global.config.security;

import com.iitp.domains.member.domain.Role;
import com.iitp.global.exception.AuthenticationException;
import com.iitp.global.exception.ExceptionMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


public class SecurityUtil {
    private SecurityUtil() {
        // 유틸리티 클래스는 인스턴스화 방지
    }

    /**
     * 현재 로그인한 사용자의 회원 ID를 가져옵니다.
     */
    public static Long getCurrentMemberId() {
        return getCurrentUserDetails().getMemberId();
    }

    /**
     * 현재 로그인한 사용자의 이메일을 가져옵니다.
     */
    public static String getCurrentMemberEmail() {
        return getCurrentUserDetails().getEmail();
    }

    /**
     * 현재 로그인한 사용자의 역할을 가져옵니다.
     */
    public static Role getCurrentMemberRole() {
        return getCurrentUserDetails().getRole();
    }

    /**
     * 현재 로그인한 사용자가 일반 사용자인지 확인합니다.
     */
    public static boolean isCurrentMemberUser() {
        return Role.ROLE_USER.equals(getCurrentMemberRole());
    }

    /**
     * 현재 로그인한 사용자가 사장님인지 확인합니다.
     */
    public static boolean isCurrentMemberStore() {
        return Role.ROLE_STORE.equals(getCurrentMemberRole());
    }

    /**
     * 현재 로그인한 사용자의 CustomUserDetails를 가져옵니다.
     */
    public static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException(ExceptionMessage.AUTHENTICATION_MISSING);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            throw new AuthenticationException(ExceptionMessage.INVALID_PRINCIPAL_TYPE);
        }

        return (CustomUserDetails) principal;
    }

    /**
     * 사용자가 로그인되어 있는지 확인합니다.
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails;
    }
}
