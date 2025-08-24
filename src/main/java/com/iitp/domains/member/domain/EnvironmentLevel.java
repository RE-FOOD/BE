package com.iitp.domains.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnvironmentLevel {
    SPROUT(1), //새싹
    SEEDLING(2), // 묘목
    TREE(3), // 나무
    FRUIT(4); // 열매

    private final int level;
}
