package com.iitp.domains.member.service.command;

import com.iitp.domains.member.config.KakaoApiClient;
import com.iitp.domains.member.config.jwt.JwtUtil;
import com.iitp.domains.member.domain.EnvironmentLevel;
import com.iitp.domains.member.domain.JoinType;
import com.iitp.domains.member.domain.Role;
import com.iitp.domains.member.domain.entity.Location;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.dto.KakaoUserInfoDto;
import com.iitp.domains.member.dto.requestDto.MemberLogInRequestDto;
import com.iitp.domains.member.dto.requestDto.MemberSignupRequestDto;
import com.iitp.domains.member.dto.responseDto.LocationResponseDto;
import com.iitp.domains.member.dto.responseDto.MemberLogInResponseDto;
import com.iitp.domains.member.dto.responseDto.MemberSignupResponseDto;
import com.iitp.domains.member.repository.LocationRepository;
import com.iitp.domains.member.repository.MemberRepository;
import com.iitp.domains.member.service.query.MemberReadService;
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
public class MemberCreateService {
    private final MemberRepository memberRepository;
    private final LocationRepository locationRepository;
    private final MemberReadService memberReadService;
    private final KakaoApiClient kakaoApiClient;
    private final JwtUtil jwtUtil;

    /**
     * 카카오 회원가입
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "members", allEntries = true),
            @CacheEvict(value = "locations", allEntries = true)
    })
    public MemberSignupResponseDto signup(MemberSignupRequestDto request) {
        log.info("회원가입 시작 - nickname: {}, phone: {}, address: {}",
                request.nickname(), request.phone(), request.address());

        // 1. 카카오에서 사용자 정보 가져오기
        KakaoUserInfoDto kakaoUserInfo = kakaoApiClient.getUserInfo(request.accessToken());

        // 2. 중복 검증
        validateDuplicates(kakaoUserInfo.getEmail(), request.nickname(), request.phone());

        // 3. 회원 생성 및 저장
        Member member = createMember(kakaoUserInfo, request);
        Member savedMember = memberRepository.save(member);

        // 4. 위치 정보 생성 및 저장
        Location location = createLocation(savedMember.getId(), request.address());
        Location savedLocation = locationRepository.save(location);

        // 5. JWT 토큰 생성 및 저장
        String[] tokens = generateAndSaveTokens(savedMember);

        log.info("회원가입 완료 - memberId: {}, email: {}", savedMember.getId(), savedMember.getEmail());

        return buildSignupResponse(savedMember, savedLocation, tokens);
    }

    /**
     * 카카오 로그인
     */
    @Transactional
    @CacheEvict(value = "members", key = "'id:' + #result.id")
    public MemberLogInResponseDto signin(MemberLogInRequestDto request) {
        log.info("로그인 시작");

        // 1. 카카오에서 사용자 정보 가져오기
        KakaoUserInfoDto kakaoUserInfo = kakaoApiClient.getUserInfo(request.accessToken());

        // 2. 기존 회원 확인
        Member member = memberReadService.findMemberByEmail(kakaoUserInfo.getEmail());

        // 3. 위치 정보 조회
        Location location = memberReadService.findMostRecentLocation(member.getId()).orElse(null);

        // 4. JWT 토큰 생성 및 갱신
        String[] tokens = generateAndSaveTokens(member);

        log.info("로그인 완료 - memberId: {}", member.getId());

        return buildSigninResponse(member, location, tokens);
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

        Member member = memberReadService.findMemberById(memberId);
        member.removeRefreshToken();

        log.info("로그아웃 완료 - memberId: {}", memberId);
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

        Member member = memberReadService.findMemberById(memberId);
        member.markAsDeleted();

        log.info("회원 삭제 완료 - memberId: {}", memberId);
    }

    // 중복 확인 메서드

    private void validateDuplicates(String email, String nickname, String phone) {
        if (memberReadService.isEmailExists(email)) {
            log.warn("이메일 중복 - email: {}", email);
            throw new BadRequestException(ExceptionMessage.EMAIL_ALREADY_EXISTS);
        }

        if (memberReadService.isNicknameExists(nickname)) {
            log.warn("닉네임 중복 - nickname: {}", nickname);
            throw new BadRequestException(ExceptionMessage.NICKNAME_ALREADY_EXISTS);
        }

        if (memberReadService.isPhoneExists(phone)) {
            log.warn("전화번호 중복 - phone: {}", phone);
            throw new BadRequestException(ExceptionMessage.PHONE_ALREADY_EXISTS);
        }
    }

    // 일반 회원 회원가입
    private Member createMember(KakaoUserInfoDto kakaoUserInfo, MemberSignupRequestDto request) {
        return Member.builder()
                .email(kakaoUserInfo.getEmail())
                .nickname(request.nickname())
                .phone(request.phone())
                .role(Role.ROLE_USER)  // 기본값으로 고정
                .joinType(JoinType.KAKAO)
                .environmentLevel(EnvironmentLevel.SPROUT)
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

    private String[] generateAndSaveTokens(Member member) {
        String accessToken = jwtUtil.generateAccessToken(
                member.getId(),
                member.getEmail(),
                member.getRole().name()
        );
        String refreshToken = jwtUtil.generateRefreshToken(member.getId());

        member.updateRefreshToken(refreshToken);

        return new String[]{accessToken, refreshToken};
    }

    private MemberSignupResponseDto buildSignupResponse(Member member, Location location, String[] tokens) {
        return MemberSignupResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .phone(member.getPhone())
                .role(member.getRole())
                .joinType(member.getJoinType())
                .environmentLevel(member.getEnvironmentLevel().getLevel())
                .location(location != null ? LocationResponseDto.builder()
                        .id(location.getId())
                        .address(location.getAddress())
                        .isMostRecent(location.getIsMostRecent())
                        .build() : null)
                .accessToken(tokens[0])
                .refreshToken(tokens[1])
                .build();
    }

    private MemberLogInResponseDto buildSigninResponse(Member member, Location location, String[] tokens) {
        return MemberLogInResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .phone(member.getPhone())
                .role(member.getRole())
                .joinType(member.getJoinType())
                .environmentLevel(member.getEnvironmentLevel().getLevel())
                .location(location != null ? LocationResponseDto.builder()
                        .id(location.getId())
                        .address(location.getAddress())
                        .isMostRecent(location.getIsMostRecent())
                        .build() : null)
                .accessToken(tokens[0])
                .refreshToken(tokens[1])
                .build();
    }

}
