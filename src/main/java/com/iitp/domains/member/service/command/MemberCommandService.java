package com.iitp.domains.member.service.command;

import com.iitp.domains.member.domain.Role;
import com.iitp.domains.member.dto.requestDto.MemberUpdateNicknameRequestDto;
import com.iitp.domains.member.dto.responseDto.MemberUpdateNicknameResponseDto;
import com.iitp.global.config.security.KakaoApiClient;
import com.iitp.global.jwt.JwtUtil;
import com.iitp.domains.member.domain.entity.Location;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.repository.LocationRepository;
import com.iitp.domains.member.repository.MemberRepository;
import com.iitp.domains.member.service.query.MemberQueryService;
import com.iitp.global.exception.BadRequestException;
import com.iitp.global.exception.ExceptionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberCommandService {
    private final MemberRepository memberRepository;
    private final LocationRepository locationRepository;
    private final MemberQueryService memberQueryService;
    private final KakaoApiClient kakaoApiClient;
    private final JwtUtil jwtUtil;
    private final EmailCreateService emailCreateService;


    /**
     * 사업자 승인 처리
     */
    @CacheEvict(value = "members", allEntries = true)
    public void approveBusinessMember(Long memberId) {
        log.info("사업자 승인 처리 시작 - memberId: {}", memberId);

        // 1. 회원 조회
        Member member = memberQueryService.findMemberById(memberId);

        // 2. 사업자 회원인지 확인
        if (!member.getRole().name().equals("ROLE_STORE")) {
            log.warn("사업자가 아닌 회원의 승인 요청 - memberId: {}, role: {}", memberId, member.getRole());
            throw new BadRequestException(ExceptionMessage.ACCESS_DENIED);
        }

        // 3. 이미 승인된 경우 체크
        if (member.getIsBusinessApproved() != null && member.getIsBusinessApproved()) {
            log.warn("이미 승인된 사업자 - memberId: {}", memberId);
//            throw new BadRequestException(ExceptionMessage.BUSINESS_ALREADY_APPROVED);
        }

        // 4. 승인 처리
        member.approveBusinessRegistration();
        memberRepository.save(member);

        log.info("사업자 승인 처리 완료 - memberId: {}", memberId);
    }

    /**
     * 새 위치 추가
     */
    @Transactional
    @CacheEvict(value = "locations", allEntries = true)
    public Location addNewLocation(Long memberId, String address, boolean setAsMostRecent) {
        log.info("새 위치 추가 시작 - memberId: {}, address: {}, setAsMostRecent: {}",
                memberId, address, setAsMostRecent);

        // 기존 위치들을 최근이 아닌 것으로 변경 (필요시)
        if (setAsMostRecent) {
            locationRepository.updateAllToNotMostRecent(memberId);
        }

        // 새 위치 생성 및 저장
        Location location = createLocation(memberId, address);
        location = locationRepository.save(location);

        log.info("새 위치 추가 완료 - locationId: {}", location.getId());
        return location;
    }

    /**
     * 회원 삭제 (논리적 삭제)
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "members", allEntries = true),
            @CacheEvict(value = "mypage", allEntries = true),
            @CacheEvict(value = "locations", allEntries = true)
    })
    public void deleteMember(Long memberId) {
        log.info("회원 삭제 시작 - memberId: {}", memberId);

        Member member = memberQueryService.findMemberById(memberId);
        // 2. 이미 탈퇴한 회원인지 확인
        if (member.getIsDeleted()) {
            log.warn("이미 탈퇴한 회원 - memberId: {}", memberId);
            throw new BadRequestException(ExceptionMessage.INVALID_REQUEST);
        }

        // 3. 논리적 삭제 처리
        member.markAsDeleted();
        // 4. 리프레시 토큰 제거
        member.removeRefreshToken();
        memberRepository.save(member);
        log.info("회원 탈퇴 완료 - memberId: {}, email: {}", memberId, member.getEmail());
    }

    /**
     * 사업자 승인 상태 확인
     */
    @Transactional(readOnly = true)
    public boolean isBusinessApproved(Long memberId) {
        Member member = memberQueryService.findMemberById(memberId);
        return member.getIsBusinessApproved() != null && member.getIsBusinessApproved();
    }

    /**
     * 닉네임 수정
     */
    @Transactional
    @CacheEvict(value = "members", allEntries = true)
    public MemberUpdateNicknameResponseDto updateNickname(Long memberId, MemberUpdateNicknameRequestDto request) {
        log.info("닉네임 수정 시작 - memberId: {}, newNickname: {}", memberId, request.nickname());

        // 1. 회원 조회
        Member member = memberQueryService.findMemberById(memberId);

        // 2. 닉네임 중복 확인 (자신의 닉네임이 아닌 경우만)
        if (!request.nickname().equals(member.getNickname()) &&
                memberQueryService.isNicknameExists(request.nickname())) {
            log.warn("닉네임 중복 - nickname: {}", request.nickname());
            throw new BadRequestException(ExceptionMessage.NICKNAME_ALREADY_EXISTS);
        }

        // 3. 개인 회원인지 확인 (사업자는 닉네임이 없음)
        if (member.getRole() != Role.ROLE_USER) {
            log.warn("사업자 회원의 닉네임 수정 시도 - memberId: {}", memberId);
            throw new BadRequestException(ExceptionMessage.ACCESS_DENIED);
        }

        // 4. 닉네임 업데이트
        member.updateNickname(request.nickname());
        memberRepository.save(member);

        log.info("닉네임 수정 완료 - memberId: {}, newNickname: {}", memberId, request.nickname());

        return MemberUpdateNicknameResponseDto.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .build();
    }


    // 주소 생성
    private Location createLocation(Long memberId, String address) {

        return Location.builder()
                .memberId(memberId)
                .address(address)
                .isMostRecent(true)
                .build();
    }

    /**
     * FCM 토큰 업데이트
     */
    @Transactional
    @CacheEvict(value = "members", key = "'id:' + #memberId")
    public void updateFcmToken(Long memberId, String fcmToken) {
        log.info("FCM 토큰 업데이트 시작 - memberId: {}", memberId);

        Member member = memberQueryService.findMemberById(memberId);
        member.updateFcmToken(fcmToken);
        memberRepository.save(member);

        log.info("FCM 토큰 업데이트 완료 - memberId: {}", memberId);
    }

    /**
     * FCM 토큰 삭제
     */
    @Transactional
    @CacheEvict(value = "members", key = "'id:' + #memberId")
    public void removeFcmToken(Long memberId) {
        log.info("FCM 토큰 삭제 시작 - memberId: {}", memberId);

        Member member = memberQueryService.findMemberById(memberId);
        member.updateFcmToken(null);
        memberRepository.save(member);

        log.info("FCM 토큰 삭제 완료 - memberId: {}", memberId);
    }
}
