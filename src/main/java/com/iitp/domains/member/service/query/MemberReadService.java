package com.iitp.domains.member.service.query;

import com.iitp.domains.member.domain.entity.Location;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.repository.LocationRepository;
import com.iitp.domains.member.repository.MemberRepository;
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
public class MemberReadService {

    private final MemberRepository memberRepository;
    private final LocationRepository locationRepository;

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
     * 이메일 중복 확인
     */
    public boolean isEmailExists(String email) {
        boolean exists = memberRepository.existsByEmailAndIsDeletedFalse(email);
        log.debug("이메일 중복 확인 - email: {}, exists: {}", email, exists);
        return exists;
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
     * 사업자번호 중복 확인
     */
    public boolean isBusinessLicenseNumberExists(String businessLicenseNumber) {
        if (businessLicenseNumber == null) return false;

        boolean exists = memberRepository.existsByBusinessLicenseNumberAndIsDeletedFalse(businessLicenseNumber);
        log.debug("사업자번호 중복 확인 - businessLicenseNumber: {}, exists: {}", businessLicenseNumber, exists);
        return exists;
    }


    /**
     * 전화번호 중복 확인
     */
    public boolean isPhoneExists(String phone) {
        if (phone == null) return false;

        boolean exists = memberRepository.existsByPhoneAndIsDeletedFalse(phone);
        log.debug("전화번호 중복 확인 - phone: {}, exists: {}", phone, exists);
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

}
