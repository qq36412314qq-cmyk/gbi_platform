package com.gbi.platform.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一错误码常量（对齐《后端编码规范》《API & 日志规范》）
 * 200 成功 / 400 参数错误 / 401 未登录 / 403 无权限 / 404 数据不存在 / 500 业务异常 / 5001 第三方异常 / 5002 幂等重复
 *
 * @author gbi
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "操作成功"),

    /** 参数校验失败 */
    PARAM_ERROR(400, "参数错误"),

    /** 未登录或登录已失效 */
    UNAUTHORIZED(401, "未登录或登录已失效"),

    /** 无操作权限 */
    FORBIDDEN(403, "无操作权限"),

    /** 数据不存在 */
    NOT_FOUND(404, "数据不存在"),

    /** 业务异常 */
    ERROR(500, "操作失败"),

    /** 第三方对接异常 */
    THIRD_PARTY_ERROR(5001, "第三方服务调用失败"),

    /** 幂等重复操作（重复缴费、重复核销） */
    IDEMPOTENT_REPEAT(5002, "重复操作，请勿重复提交");

    private final Integer code;

    private final String msg;
}
