package com.gbi.platform.entity.hr;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gbi.platform.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 加班配置实体，对应表 sys_overtime_config
 * 集团全局（company_id=0）+ 子公司可覆盖
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_overtime_config")
public class SysOvertimeConfig extends BaseEntity {

    /** 所属公司ID，0=集团全局 */
    private Long companyId;

    /** 配置名称 */
    private String configName;

    /** 最小加班时长（小时），不满此值不生成记录 */
    private BigDecimal overtimeMinHours;

    /** 时长取整模式 1向上取整 2四舍五入 3向下取整 */
    private Integer overtimeRoundMode;

    /** 工作日加班倍数 */
    private BigDecimal workdayRate;

    /** 休息日加班倍数 */
    private BigDecimal restdayRate;

    /** 法定节假日加班倍数 */
    private BigDecimal holidayRate;

    /** 每月最大加班时长上限（NULL=不限） */
    private BigDecimal maxOvertimeHours;

    /** 补偿优先级 1调休优先 2加班费优先 */
    private Integer compensatePriority;

    /** 是否启用自动识别 0否 1是 */
    private Integer autoDetectEnabled;

    /** 自动识别Cron表达式 */
    private String autoDetectCron;

    /** 状态 0禁用 1启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
