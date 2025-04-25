package com.creaturelove.sociallikebackend.model.enums;

import lombok.Getter;

@Getter
public enum LuaStatusEnum {
    // success
    SUCCESS(1),
    // fail
    FAIL(-1);

    private final int value;

    LuaStatusEnum(int value) {
        this.value = value;
    }
}
