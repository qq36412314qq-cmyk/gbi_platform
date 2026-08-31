package com.gbi.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "OA考勤打卡返回")
public class OaClockRecordVO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Schema(description = "主键ID") private Long id;
    @Schema(description = "所属子公司ID") private Long companyId;
    @Schema(description = "打卡人用户ID") private Long userId;
    @Schema(description = "打卡人姓名") private String userName;
    @Schema(description = "打卡类型 1上班打卡 2下班打卡") private Integer clockType;
    @Schema(description = "打卡类型文本") private String clockTypeText;
    @Schema(description = "打卡时间") private LocalDateTime clockTime;
    @Schema(description = "纬度") private BigDecimal locationLat;
    @Schema(description = "经度") private BigDecimal locationLng;
    @Schema(description = "打卡地址") private String locationAddr;
    @Schema(description = "是否早退 0否 1是") private Integer isEarly;
    @Schema(description = "是否迟到 0否 1是") private Integer isLate;
    @Schema(description = "是否缺卡 0否 1是") private Integer isAbsent;
    @Schema(description = "设备类型 1移动端 2PC端") private Integer deviceType;
    @Schema(description = "备注") private String remark;
    @Schema(description = "创建时间") private LocalDateTime createTime;
}
