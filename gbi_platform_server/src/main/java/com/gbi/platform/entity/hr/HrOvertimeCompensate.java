package com.gbi.platform.entity.hr;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 加班补偿台账实体，对应表 hr_overtime_compensate
 * （追加型记录表，无 audit 基础字段，独立管理生命周期）
 *
 * @author gbi
 */
@Data
@TableName("hr_overtime_compensate")
public class HrOvertimeCompensate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属公司ID */
    private Long companyId;

    /** 员工ID */
    private Long employeeId;

    /** 员工姓名快照 */
    private String employeeName;

    /** 基准月薪（用于计算时薪） */
    private BigDecimal basicSalary;

    /** 补偿月份 yyyy-MM */
    private String compensateMonth;

    /** 累计加班时长（小时） */
    private BigDecimal totalHours;

    /** 工作日加班时长 */
    private BigDecimal workdayHours;

    /** 休息日加班时长 */
    private BigDecimal restdayHours;

    /** 法定节假日加班时长 */
    private BigDecimal holidayHours;

    /** 已使用时长（调休+加班费抵扣） */
    private BigDecimal usedHours;

    /** 剩余可使用时长 */
    private BigDecimal remainHours;

    /** 补偿方式 1调休 2加班费 3混合 */
    private Integer compensateType;

    /** 加班费金额（元） */
    private BigDecimal overtimeAmount;

    /** 发放状态 0待发放 1已发放 2已取消 */
    private Integer payStatus;

    /** 发放时间 */
    private LocalDateTime payTime;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除 0正常 1删除 */
    @TableLogic
    private Integer isDelete;
}
