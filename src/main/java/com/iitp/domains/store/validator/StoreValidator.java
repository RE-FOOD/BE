package com.iitp.domains.store.validator;

import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.repository.store.StoreRepository;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoreValidator {
    private final StoreRepository storeRepository;


    public Store validateStoreExists(Long storeId) {
        return storeRepository.findByStoreId(storeId)
                .orElseThrow( () -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));
    }


}
