package com.iitp.imageUpload.useCase;


import com.iitp.imageUpload.dto.GetS3UrlDto;

public interface ImageUploadUseCase {
    GetS3UrlDto getPostS3Url(String filename);
}
