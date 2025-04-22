package com.creaturelove.sociallikebackend.common;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "params error"),
    NOT_LOGIN_ERROR(40001, "not login"),
    NO_AUTH_ERROR(40101, "no auth"),
    NOT_FOUND_ERROR(40400, "not found"),
    FORBIDDEN_ERROR(40300, "forbidden"),
    OPERATION_ERROR(50001, "operation error");


    // error code
    private final int code;

    // error message
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
