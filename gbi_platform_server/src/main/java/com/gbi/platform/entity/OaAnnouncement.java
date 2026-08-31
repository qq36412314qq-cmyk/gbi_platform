package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * OA公告实体：oa_announcement
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oa_announcement")
public class OaAnnouncement extends BaseEntity {

    /** 公告标题 */
    private String title;

    /** 公告内容 */
    private String content;

    /** 发布范围 1全员 2指定部门 3指定人员 */
    private Integer publishType;

    /** 目标部门ID集合JSON */
    private String targetDeptIds;

    /** 目标人员ID集合JSON */
    private String targetUserIds;

    /** 发布状态 0草稿 1已发布 2已撤回 */
    private Integer publishStatus;

    /** 发布人用户ID */
    private Long publisherId;

    /** 所属子公司ID */
    private Long companyId;

    /** 发布人姓名 */
    private String publisherName;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 备注 */
    private String remark;
}
