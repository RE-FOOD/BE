package com.iitp.domains.member.service.query;

import com.iitp.domains.member.domain.Role;
import com.iitp.domains.member.domain.entity.Location;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.dto.responseDto.LocationResponseDto;
import com.iitp.domains.member.dto.responseDto.MemberProfileResponseDto;
import com.iitp.domains.member.repository.LocationRepository;
import com.iitp.domains.member.repository.MemberRepository;
import com.iitp.global.config.security.SecurityUtil;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberQueryService {

    private final MemberRepository memberRepository;
    private final LocationRepository locationRepository;

    /**
     * 회원 프로필 조회
     */
    @Cacheable(value = "profiles", key = "'member:' + #memberId")
    public MemberProfileResponseDto getMemberProfile(Long memberId) {
        log.debug("회원 프로필 조회 시작 - memberId: {}", memberId);

        // 1. 회원 정보 조회
        Member member = findMemberById(memberId);

        // 2. 역할에 따라 다른 응답 생성
        if (member.getRole() == Role.ROLE_USER) {
            // 개인회원 - 위치 정보 포함
            Optional<Location> location = findMostRecentLocation(memberId);

            return MemberProfileResponseDto.forUser(member, location.orElse(null));
        } else {
            // 사업자회원
            return MemberProfileResponseDto.forStore(member);
        }
    }


    /**
     * 이메일로 회원 조회
     */
    @Cacheable(value = "members", key = "'email:' + #email")
    public Member findMemberByEmail(String email) {
        log.debug("회원 조회 by email: {}", email);

        return memberRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> {
                    log.warn("회원을 찾을 수 없음 - email: {}", email);
                    return new NotFoundException(ExceptionMessage.MEMBER_NOT_FOUND);
                });
    }

    /**
     * 회원 ID로 회원 조회
     */
    @Cacheable(value = "members", key = "'id:' + #memberId")
    public Member findMemberById(Long memberId) {
        log.debug("회원 조회 by id: {}", memberId);

        return memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("회원을 찾을 수 없음 - memberId: {}", memberId);
                    return new NotFoundException(ExceptionMessage.MEMBER_NOT_FOUND);
                });
    }

    /**
     * Refresh Token으로 회원 조회
     */
    public Member findMemberByRefreshToken(String refreshToken) {
        log.debug("회원 조회 by refreshToken");

        return memberRepository.findByRefreshTokenAndIsDeletedFalse(refreshToken)
                .orElseThrow(() -> {
                    log.warn("유효하지 않은 리프레시 토큰");
                    return new NotFoundException(ExceptionMessage.REFRESH_TOKEN_NOT_FOUND);
                });
    }

    /**
     * 닉네임 중복 확인
     */
    public boolean isNicknameExists(String nickname) {
        boolean exists = memberRepository.existsByNicknameAndIsDeletedFalse(nickname);
        log.debug("닉네임 중복 확인 - nickname: {}, exists: {}", nickname, exists);
        return exists;
    }

    /**
     * 회원 존재 여부 확인
     */
    public boolean isMemberExists(Long memberId) {
        boolean exists = memberRepository.existsById(memberId);
        log.debug("회원 존재 확인 - memberId: {}, exists: {}", memberId, exists);
        return exists;
    }
    /**
     * 회원의 가장 최근 위치 조회
     */
    @Cacheable(value = "locations", key = "'member:' + #memberId + ':recent'")
    public Optional<Location> findMostRecentLocation(Long memberId) {
        log.debug("최근 위치 조회 - memberId: {}", memberId);
        return locationRepository.findByMemberIdAndIsMostRecentTrueAndIsDeletedFalse(memberId);
    }

    /**
     * 현재 로그인된 회원 조회
     * SecurityContext - Principal - id를 이용해 현재 로그인된 회원 조회
     */
    public Member findExistingCurrentMember() {
        return memberRepository.findById(SecurityUtil.getCurrentMemberId())
                .orElseThrow(()-> new NotFoundException(ExceptionMessage.MEMBER_NOT_FOUND));
    }

}
