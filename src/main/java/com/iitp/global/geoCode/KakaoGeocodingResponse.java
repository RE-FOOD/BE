package com.iitp.global.geoCode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// Jackson이 Record를 제대로 처리하려면
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoGeocodingResponse(
        List<Document> documents,
        Meta meta
) {
    public record Document(
            String address_name,
            String address_type,
            String x, // longitude
            String y, // latitude
            Address address,
            RoadAddress road_address
    ) {}

    public record Address(
            String address_name,
            String region_1depth_name,
            String region_2depth_name,
            String region_3depth_name,
            String mountain_yn,
            String main_address_no,
            String sub_address_no,
            String zip_code
    ) {}

    public record RoadAddress(
            String address_name,
            String road_name,
            String main_building_no,
            String sub_building_no,
            String building_name,
            String zone_no
    ) {}

    public record Meta(
            int total_count,
            int pageable_count,
            boolean is_end
    ) {}
}