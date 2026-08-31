package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oa_clock_record")
public class OaClockRecord extends BaseEntity {
    private Long companyId;
    private Long userId;
    private String userName;
    private Integer clockType;
    private LocalDateTime clockTime;
    private BigDecimal locationLat;
    private BigDecimal locationLng;
    private String locationAddr;
    private Integer isEarly;
    private Integer isLate;
    private Integer isAbsent;
    private Integer deviceType;
    private String remark;
}
