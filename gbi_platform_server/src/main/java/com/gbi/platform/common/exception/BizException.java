package com.gbi.platform.common.exception;

public class BizException extends RuntimeException {
    private final Integer code;

    public BizException(String message) {
        super(message);
        this.code = 500;
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
