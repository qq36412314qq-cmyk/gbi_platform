package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户列表返回（对齐前端 UserVO，敏感字段脱敏）
 *
 * @author gbi
 */
@Data
@Schema(description = "用户列表")
public class UserListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "所属子公司ID")
    private Long companyId;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号码（脱敏）")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "账号状态 0禁用 1正常")
    private Integer status;

    @Schema(description = "角色名称集合")
    private List<String> roleNames;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
