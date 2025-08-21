package com.iitp.domains.member.service.command;

import com.iitp.domains.member.domain.entity.Location;
import com.iitp.domains.member.dto.requestDto.LocationCreateRequestDto;
import com.iitp.domains.member.dto.responseDto.LocationResponseDto;
import com.iitp.domains.member.repository.LocationRepository;
import com.iitp.domains.member.service.query.LocationQueryService;
import com.iitp.global.exception.BadRequestException;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.geoCode.GeocodingResult;
import com.iitp.global.geoCode.KakaoGeocodingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LocationCommandService {

    private final LocationRepository locationRepository;
    private final LocationQueryService locationQueryService;
    private final KakaoGeocodingService kakaoGeocodingService;

    // 주소 개수 제한
    private static final int MAX_ADDRESS_COUNT = 10;

    /**
     * 새 주소 추가
     */
    @Transactional
    public LocationResponseDto addNewAddress(Long memberId, LocationCreateRequestDto request) {
        log.info("새 주소 추가 시작 - memberId: {}, fullAddress: {}", memberId, request.address());

        // 1. 주소 개수 제한 확인
        validateAddressCount(memberId);

        // 2. 기존 모든 주소를 기본 주소가 아닌 것으로 변경
        locationRepository.updateAllToNotMostRecent(memberId);

        // 3. 새 주소 생성 및 저장
        Location newLocation = createLocationWithCoordinates(memberId, request);
        Location savedLocation = locationRepository.save(newLocation);

        log.info("새 주소 추가 완료 - memberId: {}, locationId: {}", memberId, savedLocation.getId());

        return LocationResponseDto.from(savedLocation);
    }

    /**
     * 기본 주소 변경
     */
    @Transactional
    public LocationResponseDto setDefaultAddress(Long memberId, Long addressId) {
        log.info("기본 주소 변경 시작 - memberId: {}, addressId: {}", memberId, addressId);

        // 1. 회원의 주소인지 확인
        Location targetLocation = locationQueryService.findMemberLocation(memberId, addressId);

        // 2. 이미 기본 주소인 경우
        if (targetLocation.getIsMostRecent()) {
            log.info("이미 기본 주소임 - memberId: {}, addressId: {}", memberId, addressId);
            return LocationResponseDto.from(targetLocation);
        }

        // 3. 기존 모든 주소를 기본 주소가 아닌 것으로 변경
        locationRepository.updateAllToNotMostRecent(memberId);

        // 4. 대상 주소를 기본 주소로 설정
        targetLocation.setAsMostRecent();

        log.info("기본 주소 변경 완료 - memberId: {}, addressId: {}", memberId, addressId);

        return LocationResponseDto.from(targetLocation);
    }

    /**
     * 주소 삭제
     */
    @Transactional
    public void deleteAddress(Long memberId, Long addressId) {
        log.info("주소 삭제 시작 - memberId: {}, addressId: {}", memberId, addressId);

        // 1. 회원의 주소인지 확인
        Location location = locationQueryService.findMemberLocation(memberId, addressId);

        // 2. 유일한 주소인 경우 삭제 불가
        long addressCount = locationRepository.countByMemberIdAndIsDeletedFalse(memberId);
        if (addressCount <= 1) {
            log.warn("유일한 주소 삭제 시도 - memberId: {}, addressId: {}", memberId, addressId);
            throw new BadRequestException(ExceptionMessage.INVALID_REQUEST);
        }

        // 3. 논리적 삭제
        boolean wasDefault = location.getIsMostRecent();
        location.markAsDeleted();

        // 4. 기본 주소였다면 다른 주소를 기본 주소로 설정
        if (wasDefault) {
            setNewDefaultAddress(memberId);
        }

        log.info("주소 삭제 완료 - memberId: {}, addressId: {}, wasDefault: {}", memberId, addressId, wasDefault);
    }

    /**
     * 새로운 기본 주소 설정 (삭제 시 사용)
     */
    private void setNewDefaultAddress(Long memberId) {
        List<Location> remainingAddresses = locationRepository
                .findAllByMemberIdAndIsDeletedFalseOrderByIsMostRecentDescCreatedAtDesc(memberId);

        if (!remainingAddresses.isEmpty()) {
            Location newDefault = remainingAddresses.get(0); // 가장 최근 생성된 주소
            newDefault.setAsMostRecent();
            log.info("새로운 기본 주소 설정 - memberId: {}, newDefaultId: {}", memberId, newDefault.getId());
        }
    }

    /**
     * 주소 개수 제한 확인
     */
    private void validateAddressCount(Long memberId) {
        long count = locationRepository.countByMemberIdAndIsDeletedFalse(memberId);
        if (count >= MAX_ADDRESS_COUNT) {
            log.warn("주소 개수 제한 초과 - memberId: {}, count: {}", memberId, count);
            throw new BadRequestException(ExceptionMessage.INVALID_REQUEST);
        }
    }

    /**
     * 좌표 포함 주소 생성
     */
    private Location createLocationWithCoordinates(Long memberId, LocationCreateRequestDto request) {
        try {
            // 카카오 API로 좌표 변환
            GeocodingResult geocodingResult = kakaoGeocodingService.getCoordinates(request.address());

            return Location.builder()
                    .memberId(memberId)
                    .address(request.address())
                    .roadAddress(request.roadAddress())
                    .latitude(geocodingResult.latitude())
                    .longitude(geocodingResult.longitude())
                    .isMostRecent(true)
                    .build();
        }
        catch (Exception ex) {
            log.warn("주소 좌표 변환 실패 - fullAddress: {}, error: {}", request.address(), ex.getMessage());
            throw new BadRequestException(ExceptionMessage.ADDRESS_GEOCODING_FAILED);
        }
    }
}
