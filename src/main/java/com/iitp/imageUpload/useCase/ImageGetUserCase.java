package com.iitp.imageUpload.useCase;


import com.iitp.imageUpload.dto.GetS3UrlDto;

public interface ImageGetUserCase {
    GetS3UrlDto getGetS3Url(String key);
}
