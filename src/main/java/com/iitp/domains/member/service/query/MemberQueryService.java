package com.iitp.domains.member.service.query;

import com.iitp.domains.member.domain.Role;
import com.iitp.domains.member.domain.entity.Location;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.dto.responseDto.LocationResponseDto;
import com.iitp.domains.member.dto.responseDto.MemberProfileResponseDto;
import com.iitp.domains.member.repository.LocationRepository;
import com.iitp.domains.member.repository.MemberRepository;
import com.iitp.domains.member.service.command.LocationCommandService;
import com.iitp.global.config.security.SecurityUtil;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface MemberQueryService {

    /**
     * 회원 프로필 조회
     */
    @Cacheable(value = "profiles", key = "'member:' + #memberId")
    MemberProfileResponseDto getMemberProfile(Long memberId);


    /**
     * 이메일로 회원 조회
     */
    @Cacheable(value = "members", key = "'email:' + #email")
    Member findMemberByEmail(String email);

    /**
     * 회원 ID로 회원 조회
     */
    @Cacheable(value = "members", key = "'id:' + #memberId")
    Member findMemberById(Long memberId);

    /**
     * Refresh Token으로 회원 조회
     */
    Member findMemberByRefreshToken(String refreshToken);

    /**
     * 닉네임 중복 확인
     */
    boolean isNicknameExists(String nickname);

    /**
     * 회원 존재 여부 확인
     */
    boolean isMemberExists(Long memberId);
    /**
     * 회원의 가장 최근 위치 조회
     */
    @Cacheable(value = "locations", key = "'member:' + #memberId + ':recent'")
    Optional<Location> findMostRecentLocation(Long memberId);

    /**
     * 현재 로그인된 회원 조회
     * SecurityContext - Principal - id를 이용해 현재 로그인된 회원 조회
     */
    Member findExistingCurrentMember();

    Integer sumAllOrderCount();

    Integer sumAllDishCount();
}
