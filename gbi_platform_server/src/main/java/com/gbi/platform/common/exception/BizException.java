package com.gbi.platform.common.exception;

import lombok.Getter;

/**
 * 自定义业务异常：业务校验不通过场景统一抛出
 * （账号已存在、铺位已出租、余额不足等）
 *
 * @author gbi
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 业务错误码，默认 500 */
    private final Integer code;

    public BizException(String message) {
        super(message);
        this.code = 500;
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
