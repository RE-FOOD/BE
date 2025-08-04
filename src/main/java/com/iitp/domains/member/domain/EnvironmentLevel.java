package com.iitp.domains.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnvironmentLevel {
    SPROUT(0), //새싹
    SEEDLING(1), // 묘목
    TREE(2), // 나무
    FRUIT(3); // 열매

    private final int value;
}
