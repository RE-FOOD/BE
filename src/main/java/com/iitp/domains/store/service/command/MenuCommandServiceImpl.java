package com.iitp.domains.store.service.command;

import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.dto.request.MenuCreateRequest;
import com.iitp.domains.store.dto.request.MenuUpdateRequest;
import com.iitp.domains.store.repository.menu.MenuRepository;
import com.iitp.domains.store.validator.MenuValidator;
import com.iitp.domains.store.validator.StoreValidator;
import com.iitp.global.redis.service.MenuRedisService;
import com.iitp.global.redis.service.StoreRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuCommandServiceImpl implements MenuCommandService {
    private final MenuRepository menuRepository;
    private final StoreRedisService cacheService;
    private final MenuRedisService menuRedisService;
    private final StoreValidator storeValidator;
    private final MenuValidator menuValidator;


    public void createMenu(MenuCreateRequest request, Long storeId, Long memberId) {
        Store store = storeValidator.validateStoreExists(storeId);

        Menu menu = menuRepository.save(request.toEntity(store));


        if(store.getMaxPercent() != 0) {
            store.updatePercent(Math.max(store.getMaxPercent(), menu.getDailyDiscountPercent()));
        }else{
            store.updatePercent(menu.getDailyDiscountPercent());
        }

        store.updateStatus();

        menuRedisService.createMenu(memberId, menu.getId(), menu.getDailyQuantity());
    }


    public void updateMenu(MenuUpdateRequest request, Long storeId, Long menuId, Long memberId) {
        storeValidator.validateStoreExists(storeId);
        Menu menu  = menuValidator.validateMenuExists(menuId);

        menu.update(request);

//        // 캐시 삭제
//        cacheService.clearCache();

        menuRedisService.createMenu(memberId, menu.getId(), menu.getDailyQuantity());
    }


    public void deleteMenu(Long storeId, Long menuId, Long memberId) {
        storeValidator.validateStoreExists(storeId);
        Menu menu = menuValidator.validateMenuExists(menuId);
        menu.markAsDeleted();

//        cacheService.clearCache();
        menuRedisService.deleteMenu(memberId, menuId);
    }


}
