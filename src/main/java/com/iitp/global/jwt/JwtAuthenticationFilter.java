package com.iitp.global.jwt;

import com.iitp.domains.member.domain.Role;
import com.iitp.global.config.security.MemberPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (token != null && jwtUtil.isValidToken(token) && jwtUtil.isAccessToken(token)) {
                setAuthentication(token);
            }
        } catch (Exception e) {
            log.warn("JWT 인증 처리 중 오류 발생: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    private void setAuthentication(String token) {
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        String email = jwtUtil.getEmailFromToken(token);
        Role role = jwtUtil.getRoleFromToken(token);

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role.name()));

        MemberPrincipal principal = new MemberPrincipal(memberId, email, role);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
