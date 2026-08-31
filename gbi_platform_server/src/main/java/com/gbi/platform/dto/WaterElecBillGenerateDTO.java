package com.gbi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 月度账单生成入参（抄表读数 + 账单月份，幂等：同摊位同月份不可重复生成）
 * 用量 = 本次读数 - 上期账单本次读数（无历史账单从 0 起算）
 *
 * @author gbi
 */
@Data
@Schema(description = "月度账单生成入参")
public class WaterElecBillGenerateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "账单月份 yyyy-MM", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "账单月份不能为空")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "账单月份格式须为 yyyy-MM")
    private String billMonth;

    @Schema(description = "本次抄表读数列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "抄表读数不能为空")
    @Size(max = 500, message = "单次生成最多500只表")
    @Valid
    private List<MeterReadItem> meterReads;

    /**
     * 单只表抄表读数项
     */
    @Data
    @Schema(description = "抄表读数项")
    public static class MeterReadItem implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "设备ID不能为空")
        private Long meterId;

        @Schema(description = "本次读数", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "本次读数不能为空")
        private BigDecimal currentRead;
    }
}
