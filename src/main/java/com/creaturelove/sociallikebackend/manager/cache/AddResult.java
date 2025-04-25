package com.creaturelove.sociallikebackend.manager.cache;

import lombok.Data;

@Data
public class AddResult {
    // expelled key
    private final String expelledKey;

    // if current key is entered TopK
    private final boolean isHotKey;

    // current operating key
    private final String currentKey;

    public AddResult(String expelledKey, boolean isHotKey, String currentKey){
        this.expelledKey = expelledKey;
        this.isHotKey = isHotKey;
        this.currentKey = currentKey;
    }
}
