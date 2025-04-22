package com.creaturelove.sociallikebackend.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseResponse<T> implements Serializable{
    private int code;

    private T data;

    private String message;

    // success response constructor with message
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    // response without message constructor
    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    // error response constructor
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
