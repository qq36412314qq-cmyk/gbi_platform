package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 租赁合同退租入参（高危操作：铺位置空、押金退费流水、强制审计）
 *
 * @author gbi
 */
@Data
@Schema(description = "租赁合同退租入参")
public class ContractTerminateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "合同ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "合同ID不能为空")
    private Long contractId;

    @Schema(description = "退租原因备注")
    @Size(max = 500, message = "退租原因不能超过500字符")
    private String remark;
}