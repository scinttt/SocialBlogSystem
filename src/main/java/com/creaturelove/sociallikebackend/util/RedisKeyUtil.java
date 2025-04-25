package com.creaturelove.sociallikebackend.util;

import com.creaturelove.sociallikebackend.constant.ThumbConstant;

public class RedisKeyUtil {

    public static String getUserThumbKey(Long userId) {
        return ThumbConstant.USER_THUMB_KEY_PREFIX + userId;
    }

    // get temporary key for thumb cache
    public static String getTempThumbKey(String time){
        return ThumbConstant.TEMP_THUMB_KEY_PREFIX.formatted(time);
    }
}
