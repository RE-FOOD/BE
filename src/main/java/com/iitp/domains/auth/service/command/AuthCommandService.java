package com.iitp.domains.auth.service.command;

import com.iitp.domains.auth.dto.responseDto.*;
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
import com.iitp.global.jwt.JwtUtil;
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
public class AuthCommandService {
    private final MemberRepository memberRepository;
    private final LocationRepository locationRepository;
    private final MemberQueryService memberQueryService;
    private final KakaoApiClient kakaoApiClient;
    private final JwtUtil jwtUtil;
    private final EmailCreateService emailCreateService;


    /**
     * 개인회원 카카오 회원가입
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "members", allEntries = true),
            @CacheEvict(value = "locations", allEntries = true)
    })
    public MemberSignupResponseDto memberSignup(MemberSignupRequestDto request) {
        log.info("회원가입 시작 - nickname: {}",
                request.nickname());

// 1. 카카오에서 사용자 정보 가져오기
        KakaoUserInfoDto kakaoUserInfo = kakaoApiClient.getUserInfo(request.accessToken());

        // 2. 중복 검증
        validateDuplicates(request.nickname());

        // 3. 개인 회원 생성 및 저장
        Member member = Member.createMember(
                kakaoUserInfo.getEmail(),
                request.nickname(),
                request.phone()
        );
        Member savedMember = memberRepository.save(member);

        // 4. 위치 정보 생성 및 저장
        Location location = createLocation(savedMember.getId(), request.address());
        Location savedLocation = locationRepository.save(location);

        // 5. JWT 토큰 생성 및 저장
        String[] tokens = generateAndSaveTokens(savedMember);

        log.info("개인 회원가입 완료 - memberId: {}", savedMember.getId());
        return buildSignupResponse(savedMember, savedLocation, tokens);
    }

    /**
     * 사업자 회원가입
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "members", allEntries = true),
            @CacheEvict(value = "locations", allEntries = true)
    })
    public StoreSignupResponseDto signupStore(StoreSignupRequestDto request) {
        log.info("사업자 회원가입 시작 - businessLicense: {}", request.businessLicenseNumber());

        // 1. 카카오에서 사용자 정보 가져오기
        KakaoUserInfoDto kakaoUserInfo = kakaoApiClient.getUserInfo(request.accessToken());

        // 2. 사업자 회원 생성 및 저장
        Member member = Member.createStore(
                kakaoUserInfo.getEmail(),
                request.phone(),
                request.businessLicenseNumber()
        );
        Member savedMember = memberRepository.save(member);

        // 4. JWT 토큰 생성 및 저장
        String[] tokens = generateAndSaveTokens(savedMember);

        // 5. 사업자 승인 이메일 발송 (비동기)
        try {
            emailCreateService.sendBusinessApprovalEmail(
                    savedMember.getEmail(),
                    savedMember.getBusinessLicenseNumber(),
                    savedMember.getId()
            );
            log.info("사업자 승인 이메일 발송 요청 완료 - memberId: {}", savedMember.getId());
        } catch (Exception e) {
            log.warn("사업자 승인 이메일 발송 실패 - memberId: {}, error: {}", savedMember.getId(), e.getMessage());
            // 이메일 발송 실패해도 회원가입은 성공 처리
        }

        log.info("사업자 회원가입 완료 - memberId: {}", savedMember.getId());
        return buildStoreSignupResponse(savedMember, tokens);
    }

    /**
     * 카카오 로그인
     */
    @Transactional
    public LoginResponseDto signin(MemberLogInRequestDto request) {
        log.info("로그인 시작");

        // 1. 카카오에서 사용자 정보 가져오기
        KakaoUserInfoDto kakaoUserInfo = kakaoApiClient.getUserInfo(request.accessToken());

        // 2. 기존 회원 확인
        Member member = memberQueryService.findMemberByEmail(kakaoUserInfo.getEmail());

        // 3. JWT 토큰 생성 및 갱신
        String[] tokens = generateAndSaveTokens(member);

        log.info("로그인 완료 - memberId: {}", member.getId());

        return LoginResponseDto.of(tokens[0], tokens[1]);
    }

    /**
     * 로그아웃
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "members", key = "'id:' + #memberId"),
            @CacheEvict(value = "members", allEntries = true)
    })
    public void signout(Long memberId) {
        log.info("로그아웃 시작 - memberId: {}", memberId);

        Member member = memberQueryService.findMemberById(memberId);
        member.removeRefreshToken();

        log.info("로그아웃 완료 - memberId: {}", memberId);
    }

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
        Member member = memberQueryService.findMemberByRefreshToken(refreshToken);

        // 4. 새로운 토큰 생성
        String newAccessToken = jwtUtil.generateAccessToken(
                member.getId(),
                member.getEmail(),
                member.getRole()
        );
        String newRefreshToken = jwtUtil.generateRefreshToken(member.getId());

        // 5. DB에 새로운 Refresh Token 저장
        member.updateRefreshToken(newRefreshToken);

        log.info("토큰 갱신 완료 - memberId: {}", member.getId());

        return new TokenRefreshResponseDto(newAccessToken, newRefreshToken);
    }

    // 주소 생성
    private Location createLocation(Long memberId, String address) {

        return Location.builder()
                .memberId(memberId)
                .address(address)
                .isMostRecent(true)
                .build();
    }

    // 토큰 저장
    private String[] generateAndSaveTokens(Member member) {
        String accessToken = jwtUtil.generateAccessToken(
                member.getId(),
                member.getEmail(),
                member.getRole()
        );
        String refreshToken = jwtUtil.generateRefreshToken(member.getId());

        member.updateRefreshToken(refreshToken);

        return new String[]{accessToken, refreshToken};
    }

    /**
     * 개인 회원가입 응답 생성
     */
    private MemberSignupResponseDto buildSignupResponse(Member member, Location location, String[] tokens) {
        return MemberSignupResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .phone(member.getPhone())
                .role(member.getRole())
                .joinType(member.getJoinType())
                .environmentLevel(member.getEnvironmentLevel().getLevel())
                .businessLicenseNumber(member.getBusinessLicenseNumber())
                .location(location != null ? LocationResponseDto.builder()
                        .id(location.getId())
                        .address(location.getAddress())
                        .isMostRecent(location.getIsMostRecent())
                        .build() : null)
                .accessToken(tokens[0])
                .refreshToken(tokens[1])
                .build();
    }

    /**
     * 사업자 회원가입 응답 생성
     */
    private StoreSignupResponseDto buildStoreSignupResponse(Member member, String[] tokens) {
        return StoreSignupResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .phone(member.getPhone())
                .role(member.getRole())
                .businessLicenseNumber(member.getBusinessLicenseNumber())
                .isBusinessApproved(member.getIsBusinessApproved() != null ? member.getIsBusinessApproved().toString() : "미승인")
                .accessToken(tokens[0])
                .refreshToken(tokens[1])
                .build();
    }

    // 일반회원 중복 확인 메서드
    private void validateDuplicates(String nickname) {
        if (memberQueryService.isNicknameExists(nickname)) {
            log.warn("닉네임 중복 - nickname: {}", nickname);
            throw new BadRequestException(ExceptionMessage.NICKNAME_ALREADY_EXISTS);
        }
    }
}
