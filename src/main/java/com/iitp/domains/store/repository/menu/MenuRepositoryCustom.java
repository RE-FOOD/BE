package com.iitp.domains.store.repository.menu;

import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.repository.mapper.MenuListQueryResult;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepositoryCustom {

    Optional<Menu> findByMenuId(Long menuId);

    List<MenuListQueryResult> findAllMenu(Long storeId);

    //할인 메뉴 조회
    List<Menu> findDiscountMenusByStoreIds(List<Long> storeIds, int limit);
}
