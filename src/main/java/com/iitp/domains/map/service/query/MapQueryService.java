package com.iitp.domains.map.service.query;

import com.iitp.domains.map.dto.responseDto.MapListResponseDto;
import com.iitp.domains.map.dto.responseDto.MapListScrollResponseDto;
import com.iitp.domains.map.dto.responseDto.MapMarkerResponseDto;
import com.iitp.domains.map.dto.responseDto.MapSummaryResponseDto;
import com.iitp.domains.map.repository.MapRepository;
import com.iitp.domains.review.dto.response.ReviewResponse;
import com.iitp.domains.review.service.query.ReviewQueryService;
import com.iitp.domains.store.domain.SortType;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import com.iitp.global.redis.service.RedisGeoService;
import com.iitp.global.util.map.DistanceCalculator;
import com.iitp.imageUpload.service.query.ImageGetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public interface MapQueryService {

    /**
     * 근처 가게 마커 조회
     */
    List<MapMarkerResponseDto> getNearbyStoreMarkers(Double latitude, Double longitude, Double radiusKm);

    /**
     * 가게 지도 요약 조회 (지도 하단)
     */
    MapSummaryResponseDto getStoreSummary(Long storeId, Double userLatitude, Double userLongitude);

    /**
     * 근처 가게 목록 조회
     */
    List<MapListResponseDto> getNearbyStoreList(Double latitude, Double longitude, Double radiusKm,
                                                       SortType sort);

    /**
     * 근처 가게 목록 조회 - 무한스크롤
     */
    MapListScrollResponseDto getNearbyStoreListWithScroll(Double latitude, Double longitude,
                                                                 Double radiusKm, SortType sort,
                                                                 Long cursorId, Integer limit);


    List<Store> findStoreListByIds(List<Long> storeIds);
}