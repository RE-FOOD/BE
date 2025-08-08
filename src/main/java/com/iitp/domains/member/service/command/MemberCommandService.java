package com.iitp.domains.member.service.command;

import com.iitp.global.config.security.KakaoApiClient;
import com.iitp.global.jwt.JwtUtil;
import com.iitp.domains.member.domain.entity.Location;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.dto.KakaoUserInfoDto;
import com.iitp.domains.member.dto.requestDto.MemberLogInRequestDto;
import com.iitp.domains.member.dto.requestDto.MemberSignupRequestDto;
import com.iitp.domains.member.dto.requestDto.StoreSignupRequestDto;
import com.iitp.domains.member.dto.responseDto.LocationResponseDto;
import com.iitp.domains.member.dto.responseDto.MemberLogInResponseDto;
import com.iitp.domains.member.dto.responseDto.MemberSignupResponseDto;
import com.iitp.domains.member.dto.responseDto.StoreSignupResponseDto;
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
     * 개인회원 카카오 회원가입
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "members", allEntries = true),
            @CacheEvict(value = "locations", allEntries = true)
    })
    public MemberSignupResponseDto memberSignup(MemberSignupRequestDto request) {
        log.info("회원가입 시작 - nickname: {}, phone: {}, address: {}",
                request.nickname(), request.phone(), request.address());

// 1. 카카오에서 사용자 정보 가져오기
        KakaoUserInfoDto kakaoUserInfo = kakaoApiClient.getUserInfo(request.accessToken());

        // 2. 중복 검증
        validateDuplicates(kakaoUserInfo.getEmail(), request.nickname(), request.phone());

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

        // 2. 중복 검증
        validateStoreSignupDuplicates(kakaoUserInfo.getEmail(), request.phone(), request.businessLicenseNumber());

        // 3. 사업자 회원 생성 및 저장
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
     * 카카오 로그인
     */
    @Transactional
    @CacheEvict(value = "members", key = "'id:' + #result.id")
    public MemberLogInResponseDto signin(MemberLogInRequestDto request) {
        log.info("로그인 시작");

        // 1. 카카오에서 사용자 정보 가져오기
        KakaoUserInfoDto kakaoUserInfo = kakaoApiClient.getUserInfo(request.accessToken());

        // 2. 기존 회원 확인
        Member member = memberQueryService.findMemberByEmail(kakaoUserInfo.getEmail());

        // 3. 위치 정보 조회
        Location location = memberQueryService.findMostRecentLocation(member.getId()).orElse(null);

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

        Member member = memberQueryService.findMemberById(memberId);
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

        Member member = memberQueryService.findMemberById(memberId);
        member.markAsDeleted();

        log.info("회원 삭제 완료 - memberId: {}", memberId);
    }
    // 사업자 회원가입 중복 확인 메서드
    private void validateStoreSignupDuplicates(String email, String phone, String businessLicenseNumber) {
        if (memberQueryService.isEmailExists(email)) {
            log.warn("이메일 중복 - email: {}", email);
            throw new BadRequestException(ExceptionMessage.EMAIL_ALREADY_EXISTS);
        }

        if (phone != null && memberQueryService.isPhoneExists(phone)) {
            log.warn("전화번호 중복 - phone: {}", phone);
            throw new BadRequestException(ExceptionMessage.PHONE_ALREADY_EXISTS);
        }

        if (memberQueryService.isBusinessLicenseNumberExists(businessLicenseNumber)) {
            log.warn("사업자번호 중복 - businessLicenseNumber: {}", businessLicenseNumber);
            throw new BadRequestException(ExceptionMessage.BusinessLicenseNumber_ALREADY_EXISTS);
        }
    }

    // 사업자번호 유효성 검증
    private void validateBusinessLicenseNumber(String businessLicenseNumber) {
        if (memberQueryService.isBusinessLicenseNumberExists(businessLicenseNumber)) {
            log.warn("사업자번호 중복 - businessLicenseNumber: {}", businessLicenseNumber);
            throw new BadRequestException(ExceptionMessage.BusinessLicenseNumber_ALREADY_EXISTS);
        }
    }

    // 일반회원 중복 확인 메서드
    private void validateDuplicates(String email, String nickname, String phone) {
        if (memberQueryService.isEmailExists(email)) {
            log.warn("이메일 중복 - email: {}", email);
            throw new BadRequestException(ExceptionMessage.EMAIL_ALREADY_EXISTS);
        }

        if (memberQueryService.isNicknameExists(nickname)) {
            log.warn("닉네임 중복 - nickname: {}", nickname);
            throw new BadRequestException(ExceptionMessage.NICKNAME_ALREADY_EXISTS);
        }

        if (memberQueryService.isPhoneExists(phone)) {
            log.warn("전화번호 중복 - phone: {}", phone);
            throw new BadRequestException(ExceptionMessage.PHONE_ALREADY_EXISTS);
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
