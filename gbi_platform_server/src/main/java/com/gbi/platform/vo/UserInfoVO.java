package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 当前登录用户信息（对齐前端 UserInfoVO）
 *
 * @author gbi
 */
@Data
@Schema(description = "当前登录用户信息")
public class UserInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "所属子公司ID，0集团")
    private Long companyId;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号码（脱敏）")
    private String phone;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "用户类型：1集团 2子公司（一期由 companyId 推导）")
    private Integer userType;

    @Schema(description = "权限标识集合")
    private List<String> permissions;
}
