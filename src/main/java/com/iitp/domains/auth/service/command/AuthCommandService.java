package com.iitp.domains.auth.service.command;

import com.iitp.domains.auth.dto.responseDto.*;
import com.iitp.domains.member.domain.BusinessApprovalStatus;
import com.iitp.domains.member.domain.Role;
import com.iitp.domains.member.domain.entity.Location;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.dto.KakaoUserInfoDto;
import com.iitp.domains.auth.dto.requestDto.MemberLogInRequestDto;
import com.iitp.domains.auth.dto.requestDto.MemberSignupRequestDto;
import com.iitp.domains.auth.dto.requestDto.StoreSignupRequestDto;
import com.iitp.domains.member.dto.responseDto.LocationResponseDto;
import com.iitp.domains.member.repository.LocationRepository;
import com.iitp.domains.member.repository.MemberRepository;
import com.iitp.domains.member.service.command.EmailCreateService;
import com.iitp.domains.member.service.query.MemberQueryService;
import com.iitp.global.config.security.KakaoApiClient;
import com.iitp.global.exception.BadRequestException;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.geoCode.GeocodingResult;
import com.iitp.global.geoCode.KakaoGeocodingService;
import com.iitp.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


public interface AuthCommandService {
    /**
     * 개인회원 카카오 회원가입
     */
    MemberSignupResponseDto memberSignup(MemberSignupRequestDto request);

    /**
     * 사업자 회원가입
     */
    StoreSignupResponseDto signupStore(StoreSignupRequestDto request) ;

    /**
     * 카카오 로그인
     */
    LoginResponseDto signin(MemberLogInRequestDto request);

    /**
     * 로그아웃
     */
    void signout(Long memberId);

    /**
     * 토큰 갱신
     */
    TokenRefreshResponseDto refreshToken(String refreshToken);


}
