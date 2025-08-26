package com.iitp.global.exception;

import lombok.Getter;

@Getter
public class OrderConflictException extends RuntimeException {
    private final String  menuName;

    public OrderConflictException(String menuName) {
        // 부모 생성자에 기본 메시지를 전달할 수도 있습니다.
        super("User not found with id: " + menuName);
        this.menuName = menuName;
    }
}



