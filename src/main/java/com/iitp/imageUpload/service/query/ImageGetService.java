package com.iitp.imageUpload.service.query;

import com.iitp.imageUpload.dto.GetS3UrlDto;
import com.iitp.imageUpload.useCase.ImageExpirationUseCase;
import com.iitp.imageUpload.useCase.ImageGetUserCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@RequiredArgsConstructor
@Service
public class ImageGetService implements ImageGetUserCase {

    private final S3Presigner s3Presigner;
    private final ImageExpirationUseCase imageExpirationUseCase;

    // 버킷 이름
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 업로드 된 이미지 URL 호출
    @Override
    public GetS3UrlDto getGetS3Url(String key) {

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(imageExpirationUseCase.getExpirationDuration())
                .getObjectRequest(objectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        return new GetS3UrlDto(presignedRequest.url().toExternalForm(), key);
    }
}
