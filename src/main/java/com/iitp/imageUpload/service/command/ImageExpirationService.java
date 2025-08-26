package com.iitp.imageUpload.service.command;

import com.iitp.imageUpload.useCase.ImageExpirationUseCase;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ImageExpirationService implements ImageExpirationUseCase {
    @Override
    public Duration getExpirationDuration() {
        return Duration.ofHours(1);
    }
}