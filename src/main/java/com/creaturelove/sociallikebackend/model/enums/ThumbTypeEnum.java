package com.creaturelove.sociallikebackend.model.enums;

import lombok.Getter;

@Getter
public enum ThumbTypeEnum {
    // doThumb
    INCR(1),
    // undoThumb
    DECR(-1),
    // no change
    NON(0);

    private final int value;

    ThumbTypeEnum(int value) {
        this.value = value;
    }
}
