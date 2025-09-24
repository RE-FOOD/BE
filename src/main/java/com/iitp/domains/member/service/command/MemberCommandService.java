package com.iitp.domains.member.service.command;

import com.iitp.domains.member.domain.BusinessApprovalStatus;
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


public interface MemberCommandService {

    /**
     * 사업자 승인 처리
     */
    void approveBusinessMember(Long memberId);

    /**
     * 사업자 승인 상태 확인
     */
    @Transactional(readOnly = true)
    boolean isBusinessApproved(Long memberId);

    /**
     * 사업자 승인 상태 조회
     */
    @Transactional(readOnly = true)
    BusinessApprovalStatus getBusinessApprovalStatus(Long memberId);


    /**
     * 새 위치 추가
     */
    @Transactional
    @CacheEvict(value = "locations", allEntries = true)
    Location addNewLocation(Long memberId, String address, boolean setAsMostRecent) ;

    /**
     * 회원 삭제 (논리적 삭제)
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "members", allEntries = true),
            @CacheEvict(value = "mypage", allEntries = true),
            @CacheEvict(value = "locations", allEntries = true)
    })
    void deleteMember(Long memberId) ;

    /**
     * 닉네임 수정
     */
    @Transactional
    @CacheEvict(value = "members", allEntries = true)
    MemberUpdateNicknameResponseDto updateNickname(Long memberId, MemberUpdateNicknameRequestDto request) ;

}
