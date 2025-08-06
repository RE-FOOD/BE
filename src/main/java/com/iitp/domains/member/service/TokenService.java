package com.iitp.domains.member.service;

import com.iitp.domains.member.config.jwt.JwtUtil;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.dto.responseDto.TokenRefreshResponseDto;
import com.iitp.domains.member.service.query.MemberReadService;
import com.iitp.global.exception.BadRequestException;
import com.iitp.global.exception.ExceptionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TokenService {
    private final JwtUtil jwtUtil;
    private final MemberReadService memberReadService;

    /**
     * 토큰 갱신
     */
    @CacheEvict(value = "members", key = "'id:' + #result.memberId")
    public TokenRefreshResponseDto refreshToken(String refreshToken) {
        log.info("토큰 갱신 시작");

        // 1. Refresh Token 유효성 검증
        if (!jwtUtil.isValidToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            log.warn("유효하지 않은 리프레시 토큰");
            throw new BadRequestException(ExceptionMessage.INVALID_TOKEN);
        }

        // 2. 토큰 만료 확인
        if (jwtUtil.isTokenExpired(refreshToken)) {
            log.warn("만료된 리프레시 토큰");
            throw new BadRequestException(ExceptionMessage.EXPIRED_TOKEN);
        }

        // 3. DB에서 회원 조회
        Member member = memberReadService.findMemberByRefreshToken(refreshToken);

        // 4. 새로운 토큰 생성
        String newAccessToken = jwtUtil.generateAccessToken(
                member.getId(),
                member.getEmail(),
                member.getRole().name()
        );
        String newRefreshToken = jwtUtil.generateRefreshToken(member.getId());

        // 5. DB에 새로운 Refresh Token 저장
        member.updateRefreshToken(newRefreshToken);

        log.info("토큰 갱신 완료 - memberId: {}", member.getId());

        return new TokenRefreshResponseDto(newAccessToken, newRefreshToken);
    }

}
