package com.iitp.global.geoCode;

public record GeocodingResult(
        double latitude,
        double longitude,
        String formattedAddress,
        String addressType
) {}