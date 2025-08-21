package com.iitp.domains.store.service.query;

import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.dto.response.MenuListResponse;
import com.iitp.domains.store.dto.response.MenuResponse;
import com.iitp.domains.store.dto.response.StoreListResponse;
import com.iitp.domains.store.repository.mapper.MenuListQueryResult;
import com.iitp.domains.store.repository.menu.MenuRepository;
import com.iitp.domains.store.repository.store.StoreRepository;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import com.iitp.imageUpload.service.query.ImageGetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuQueryService {
    private final StoreRepository storeRepository;
    private final ImageGetService imageGetService;
    private final MenuRepository menuRepository;

    public MenuResponse findMenu(Long storeId, Long menuId) {
        validateStoreExists(storeId);
        Menu menu = validateMenuExists(menuId);

        String imageUrl = getImageUrl(menu.getImageKey());

        return MenuResponse.fromEntity(menu,imageUrl);
    }


    public List<MenuListResponse> findMenus(Long storeId) {
        validateStoreExists(storeId);

        List<MenuListQueryResult> results = menuRepository.findAllMenu(storeId);

        List<MenuListResponse> menus = new ArrayList<>();

        results.stream()
                        .forEach(result -> {
                            // S3 이미지 경로 호출
                            String imageUrl = getImageUrl(result.imageKey());
                            menus.add(MenuListResponse.fromQueryResult(result,imageUrl));
                        });
        return menus;
    }


    private Store validateStoreExists(Long storeId) {
        return storeRepository.findByStoreId(storeId)
                .orElseThrow( () -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));
    }

    private Menu validateMenuExists(Long menuId) {
        return menuRepository.findByMenuId(menuId)
                .orElseThrow( () -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));
    }

    private String getImageUrl(String imageKey) {
        return imageGetService.getGetS3Url(imageKey).preSignedUrl();
    }
}
