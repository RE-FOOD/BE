package com.iitp.domains.store.service.query;

import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.dto.response.MenuListResponse;
import com.iitp.domains.store.dto.response.MenuResponse;
import com.iitp.domains.store.dto.response.StoreListResponse;
import com.iitp.domains.store.repository.mapper.MenuListQueryResult;
import com.iitp.domains.store.repository.menu.MenuRepository;
import com.iitp.domains.store.repository.store.StoreRepository;
import com.iitp.domains.store.validator.MenuValidator;
import com.iitp.domains.store.validator.StoreValidator;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import com.iitp.imageUpload.service.query.ImageGetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


public interface MenuQueryService {

    MenuResponse findMenu(Long storeId, Long menuId);


    List<MenuListResponse> findMenus(Long storeId);


    List<StoreMenuManageResponse> findMenuManage(Long storeId, Long cursorId);


    List<Menu> findDiscountMenusByStoreIds(List<Long> storeIds, int limit);

    List<Menu> finAllMenus(List<Long> menuIds);


}
