package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OA公告阅读记录实体：oa_announcement_read
 *
 * @author gbi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oa_announcement_read")
public class OaAnnouncementRead extends BaseEntity {

    /** 公告ID */
    private Long announcementId;

    /** 阅读人用户ID */
    private Long userId;

    /** 阅读时间 */
    private java.time.LocalDateTime readTime;
}
