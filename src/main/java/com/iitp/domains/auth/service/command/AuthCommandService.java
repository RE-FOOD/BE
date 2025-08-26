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
    private final KakaoGeocodingService kakaoGeocodingService;

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
        Location location = createLocationWithCoordinates(
                savedMember.getId(),
                request.address(),
                request.roadAddress(),
                request.latitude(),
                request.longitude()
        );
        Location savedLocation = locationRepository.save(location);

        // 5. JWT 토큰 생성 및 저장
        String[] tokens = generateAndSaveTokens(savedMember);

        log.info("개인 회원가입 완료 - memberId: {}", savedMember.getId());

        return MemberSignupResponseDto.forUser(savedMember, savedLocation, tokens[0], tokens[1]);
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
        return StoreSignupResponseDto.from(savedMember, tokens[0], tokens[1]);
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

        // 3. 사업자 회원인 경우 승인 상태 체크
        if (member.getRole() == Role.ROLE_STORE) {
            BusinessApprovalStatus approvalStatus = member.getIsBusinessApproved();
            if (approvalStatus == BusinessApprovalStatus.PENDING) {
                log.warn("승인 대기 중인 사업자 로그인 시도 - memberId: {}", member.getId());
                throw new BadRequestException(ExceptionMessage.BUSINESS_APPROVAL_PENDING);
            }
        }

        // 4. FCM 토큰 업데이트
        member.updateFcmToken(request.fcmToken());
        log.info("로그인 시 FCM 토큰 업데이트 완료 - memberId: {}", member.getId());

        // 5. JWT 토큰 생성 및 갱신
        String[] tokens = generateAndSaveTokens(member);

        // 6. 변경사항 저장
        memberRepository.save(member);

        log.info("로그인 완료 - memberId: {}", member.getId());
        log.info("로그인 완료 - memberFcmToken: {}", member.getFcmToken());

        return LoginResponseDto.of(tokens[0], tokens[1], member.getFcmToken());
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
        member.removeFcmToken();
        memberRepository.save(member);
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
    private Location createLocation(Long memberId, String address, String roadAddress) {

        return Location.builder()
                .memberId(memberId)
                .address(address)
                .roadAddress(roadAddress)
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

    // 일반회원 중복 확인 메서드
    private void validateDuplicates(String nickname) {
        if (memberQueryService.isNicknameExists(nickname)) {
            log.warn("닉네임 중복 - nickname: {}", nickname);
            throw new BadRequestException(ExceptionMessage.NICKNAME_ALREADY_EXISTS);
        }
    }

    private Location createLocationWithCoordinates(Long memberId, String address, String roadAddress, Double latitude, Double longitude) {
        try {
            log.info("주소 좌표 변환 시작 - fullAddress: {}", address);

            // 카카오 지오코딩 API로 좌표 변환
            GeocodingResult geocodingResult = kakaoGeocodingService.getCoordinates(address);

            log.info("좌표 변환 성공 - lat: {}, lng: {}",
                    geocodingResult.latitude(), geocodingResult.longitude());

            return Location.builder()
                    .memberId(memberId)
                    .address(address)
                    .roadAddress(roadAddress)
                    .latitude(geocodingResult.latitude())
                    .longitude(geocodingResult.longitude())
                    .isMostRecent(true)
                    .build();

        } catch (Exception e) {
            log.warn("좌표 변환 실패 - fullAddress: {}, error: {}", address, e.getMessage());

            // 🔥 좌표 변환 실패 시에도 주소 정보는 저장
            return Location.builder()
                    .memberId(memberId)
                    .address(address)
                    .roadAddress(roadAddress)
                    .latitude(null)  // null로 저장
                    .longitude(null) // null로 저장
                    .isMostRecent(true)
                    .build();
        }
    }

}
