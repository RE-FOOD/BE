package com.iitp.domains.store.validator;

import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.repository.menu.MenuRepository;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MenuValidator {
    private final MenuRepository menuRepository;


    public Menu validateMenuExists(Long menuId) {
        return menuRepository.findByMenuId(menuId)
                .orElseThrow( () -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));
    }

    public Menu validateMenuExistsWithNull(Long menuId) {
        return menuRepository.findByMenuId(menuId)
                .orElse(null);
    }
}
