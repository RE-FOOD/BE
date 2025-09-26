package com.iitp.domains.store.service.command;

import com.iitp.domains.store.dto.request.MenuCreateRequest;
import com.iitp.domains.store.dto.request.MenuUpdateRequest;


public interface MenuCommandService {

    void createMenu(MenuCreateRequest request, Long storeId, Long memberId);


    void updateMenu(MenuUpdateRequest request, Long storeId, Long menuId, Long memberId);


    void deleteMenu(Long storeId, Long menuId, Long memberId);


}
