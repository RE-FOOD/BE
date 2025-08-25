package com.iitp.domains.member.service.query;

import com.iitp.domains.member.domain.entity.Location;
import com.iitp.domains.member.dto.responseDto.LocationResponseDto;
import com.iitp.domains.member.repository.LocationRepository;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class LocationQueryService {
    private final LocationRepository locationRepository;


    /**
     * 회원의 모든 주소 조회
     */
    public List<LocationResponseDto> findMemberAddresses(Long memberId) {
        log.debug("회원 주소 목록 조회 - memberId: {}", memberId);

        List<Location> locations = locationRepository.findAllByMemberIdAndIsDeletedFalseOrderByIsMostRecentDescCreatedAtDesc(memberId);

        return locations.stream()
                .map(LocationResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 기본 주소 조회
     */
    public LocationResponseDto findDefaultAddress(Long memberId) {
        log.debug("기본 주소 조회 - memberId: {}", memberId);

        Location defaultLocation = locationRepository.findByMemberIdAndIsMostRecentTrueAndIsDeletedFalse(memberId)
                .orElseThrow(() -> {
                    log.warn("기본 주소를 찾을 수 없음 - memberId: {}", memberId);
                    return new NotFoundException(ExceptionMessage.DATA_NOT_FOUND);
                });

        return LocationResponseDto.from(defaultLocation);
    }

    /**
     * 특정 주소 조회 (내부 사용)
     */
    public Location findLocationById(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> {
                    log.warn("주소를 찾을 수 없음 - locationId: {}", locationId);
                    return new NotFoundException(ExceptionMessage.DATA_NOT_FOUND);
                });
    }

    /**
     * 회원의 특정 주소 조회 (권한 확인 포함)
     */
    public Location findMemberLocation(Long memberId, Long locationId) {
        Location location = findLocationById(locationId);

        if (!location.getMemberId().equals(memberId)) {
            log.warn("주소 접근 권한 없음 - memberId: {}, locationId: {}, locationOwnerId: {}",
                    memberId, locationId, location.getMemberId());
            throw new NotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }

        return location;
    }

}
