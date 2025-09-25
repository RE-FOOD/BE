package com.iitp.domains.store.service.query;

import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.dto.response.MenuListResponse;
import com.iitp.domains.store.dto.response.MenuResponse;
import com.iitp.domains.store.dto.response.StoreMenuManageResponse;

import java.util.List;


public interface MenuQueryService {

    MenuResponse findMenu(Long storeId, Long menuId);


    List<MenuListResponse> findMenus(Long storeId);


    List<StoreMenuManageResponse> findMenuManage(Long storeId, Long cursorId);


    List<Menu> findDiscountMenusByStoreIds(List<Long> storeIds, int limit);

    List<Menu> finAllMenus(List<Long> menuIds);


}
