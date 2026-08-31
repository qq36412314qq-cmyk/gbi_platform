package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 市场档案新增/编辑入参（id 为空表示新增）
 *
 * @author gbi
 */
@Data
@Schema(description = "市场档案入参")
public class MarketDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "市场ID（编辑时必填）")
    private Long id;

    @Schema(description = "市场名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "市场名称不能为空")
    @Size(max = 128, message = "市场名称不能超过128字符")
    private String marketName;

    @Schema(description = "市场地址")
    @Size(max = 500, message = "市场地址不能超过500字符")
    private String marketAddress;

    @Schema(description = "市场联系人")
    @Size(max = 64, message = "联系人不能超过64字符")
    private String contactPerson;

    @Schema(description = "联系电话")
    @Size(max = 32, message = "联系电话不能超过32字符")
    private String contactPhone;

    @Schema(description = "状态 0停用 1启用")
    private Integer status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注不能超过500字符")
    private String remark;
}