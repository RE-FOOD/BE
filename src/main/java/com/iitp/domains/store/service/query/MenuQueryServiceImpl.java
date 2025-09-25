package com.iitp.domains.store.service.query;

import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.dto.response.MenuListResponse;
import com.iitp.domains.store.dto.response.MenuResponse;
import com.iitp.domains.store.dto.response.StoreMenuManageResponse;
import com.iitp.domains.store.repository.mapper.MenuListQueryResult;
import com.iitp.domains.store.repository.menu.MenuRepository;
import com.iitp.domains.store.validator.MenuValidator;
import com.iitp.domains.store.validator.StoreValidator;
import com.iitp.imageUpload.service.query.ImageGetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuQueryServiceImpl implements MenuQueryService {

    private final MenuRepository menuRepository;
    private final ImageGetService imageGetService;
    private final StoreValidator storeValidator;
    private final MenuValidator menuValidator;

    public MenuResponse findMenu(Long storeId, Long menuId) {
        storeValidator.validateStoreExists(storeId);
        Menu menu = menuValidator.validateMenuExists(menuId);

        String imageUrl = getImageUrl(menu.getImageKey());

        return MenuResponse.fromEntity(menu,imageUrl);
    }


    public List<MenuListResponse> findMenus(Long storeId) {
        storeValidator.validateStoreExists(storeId);

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


    public List<StoreMenuManageResponse> findMenuManage(Long storeId, Long cursorId) {
        return menuRepository.findMenuManage(storeId, cursorId).stream()
                .map(menu -> new StoreMenuManageResponse(
                        menu.menuId(),
                        menu.menuName(),
                        menu.info(),
                        menu.price(),
                        getImageUrl(menu.imageUrl()) // imageKey를 imageUrl로 변환
                ))
                .toList();
    }


    public List<Menu> findDiscountMenusByStoreIds(List<Long> storeIds, int limit) {
        return menuRepository.findDiscountMenusByStoreIds(storeIds, limit);
    }

    private String getImageUrl(String imageKey) {
        return imageGetService.getGetS3Url(imageKey).preSignedUrl();
    }

    public List<Menu> finAllMenus(List<Long> menuIds) {
        return menuRepository.findAllById(menuIds);
    }
}
