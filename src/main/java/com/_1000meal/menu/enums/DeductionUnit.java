package com._1000meal.menu.enums;

import lombok.Getter;

@Getter
public enum DeductionUnit {
    SINGLE(1),
    MULTI_FIVE(5),
    MULTI_TEN(10);

    private final int value;

    DeductionUnit(int value) {
        this.value = value;
    }
}