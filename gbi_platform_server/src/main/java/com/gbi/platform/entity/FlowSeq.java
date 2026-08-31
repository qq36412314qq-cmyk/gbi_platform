package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 财务流水单号顺序号计数器（公司+日维度）
 * 配合 FlowNoGenerator 使用：INSERT IGNORE 初始化，UPDATE 原子递增
 *
 * @author gbi
 */
@Data
@TableName("biz_flow_seq")
public class FlowSeq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID（自增，用于 MyBatis-Plus 识别） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属子公司ID */
    private Long companyId;

    /** 日期 yyyy-MM-dd */
    private LocalDate seqDate;

    /** 当日已分配序号（从1开始，每次+1） */
    private Integer seqNo;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
