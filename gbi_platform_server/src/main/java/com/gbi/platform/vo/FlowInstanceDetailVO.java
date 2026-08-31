package com.gbi.platform.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程实例详情视图（含流转轨迹 flow_record 全展示）
 *
 * @author gbi
 */
@Data
public class FlowInstanceDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 实例信息 */
    private FlowInstanceVO instance;

    /** 审批任务列表 */
    private List<FlowTaskVO> tasks;

    /** 流转记录（轨迹） */
    private List<FlowRecordVO> records;
}