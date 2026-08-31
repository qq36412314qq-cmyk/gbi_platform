package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收费类型返回（租金/物业费/水费/电费/押金/其他等）
 *
 * @author gbi
 */
@Data
@Schema(description = "收费类型返回")
public class FeeItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "收费类型ID")
    private Long id;

    @Schema(description = "收费类型名称")
    private String feeItemName;

    @Schema(description = "收费类别编码 1租金 2物业费 3水费 4电费 5押金 6其他")
    private Integer categoryType;

    @Schema(description = "收费类别文本")
    private String categoryTypeText;

    @Schema(description = "计量单位")
    private String calcUnit;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}