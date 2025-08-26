package com.iitp.imageUpload.dto;

public record GetS3UrlDto (
        String preSignedUrl,
        String key
){
}
