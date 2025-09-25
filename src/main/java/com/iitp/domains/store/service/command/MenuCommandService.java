package com.iitp.domains.store.service.command;

import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.dto.request.MenuCreateRequest;
import com.iitp.domains.store.dto.request.MenuUpdateRequest;
import com.iitp.domains.store.repository.menu.MenuRepository;
import com.iitp.domains.store.repository.store.StoreRepository;
import com.iitp.domains.store.validator.MenuValidator;
import com.iitp.domains.store.validator.StoreValidator;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import com.iitp.global.redis.service.StoreRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


public interface MenuCommandService {

    void createMenu(MenuCreateRequest request, Long storeId);


    void updateMenu(MenuUpdateRequest request, Long storeId, Long menuId);


    void deleteMenu(Long storeId, Long menuId);


}
