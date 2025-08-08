package com.iitp.imageUpload.useCase;

import java.time.Duration;

public interface ImageExpirationUseCase {
    Duration getExpirationDuration();  // Date 대신 Duration 반환
}
