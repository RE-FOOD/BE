package com.iitp.domains.member.service.command;

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
            @CacheEvict(value = "locations", allEntries = true)
    })
    public void deleteMember(Long memberId) {
        log.info("회원 삭제 시작 - memberId: {}", memberId);

        Member member = memberQueryService.findMemberById(memberId);
        member.markAsDeleted();

        log.info("회원 삭제 완료 - memberId: {}", memberId);
    }

    // 사업자번호 유효성 검증
    private void validateBusinessLicenseNumber(String businessLicenseNumber) {
        if (memberQueryService.isBusinessLicenseNumberExists(businessLicenseNumber)) {
            log.warn("사업자번호 중복 - businessLicenseNumber: {}", businessLicenseNumber);
            throw new BadRequestException(ExceptionMessage.BusinessLicenseNumber_ALREADY_EXISTS);
        }
    }



    /**
     * 사업자 승인 상태 확인
     */
    @Transactional(readOnly = true)
    public boolean isBusinessApproved(Long memberId) {
        Member member = memberQueryService.findMemberById(memberId);
        return member.getIsBusinessApproved() != null && member.getIsBusinessApproved();
    }

    // 주소 생성
    private Location createLocation(Long memberId, String address) {

        return Location.builder()
                .memberId(memberId)
                .address(address)
                .isMostRecent(true)
                .build();
    }
}
