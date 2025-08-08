package com.iitp.domains.store.service.command;

import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.dto.request.MenuCreateRequest;
import com.iitp.domains.store.dto.request.MenuUpdateRequest;
import com.iitp.domains.store.repository.menu.MenuRepository;
import com.iitp.domains.store.repository.store.StoreRepository;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuCommandService {
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;

    public void createMenu(MenuCreateRequest request, Long storeId) {
        Store store = validateStoreExists(storeId);

        menuRepository.save(request.toEntity(store));
    }


    public void updateMenu(MenuUpdateRequest request, Long storeId, Long menuId) {
        validateStoreExists(storeId);
        Menu menu  = validateMenuExists(menuId);

        menu.update(request);
    }


    public void deleteMenu(Long storeId, Long menuId) {
        validateStoreExists(storeId);
        Menu menu = validateMenuExists(menuId);
        menu.markAsDeleted();
    }




    private Store validateStoreExists(Long storeId) {
            return storeRepository.findByStoreId(storeId)
                .orElseThrow( () -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));
    }

    private Menu validateMenuExists(Long menuId) {
        return menuRepository.findByMenuId(menuId)
                .orElseThrow( () -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));
    }
}
