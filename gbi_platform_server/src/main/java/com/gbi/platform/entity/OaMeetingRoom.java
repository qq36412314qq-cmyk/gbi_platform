package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oa_meeting_room")
public class OaMeetingRoom extends BaseEntity {
    private Long companyId;
    private String roomName;

    /** 所在楼栋，对应数据库列 building */
    @TableField("building")
    private String location;

    /** 所在楼层，对应数据库列 floor */
    @TableField("floor")
    private String floor;

    private Integer capacity;

    /** 是否有投影仪，对应数据库列 has_projector */
    @TableField("has_projector")
    private Integer hasProjector;

    /** 是否有视频会议设备，对应数据库列 has_video_conf */
    @TableField("has_video_conf")
    private Integer hasVideoConf;

    /** 是否有电话，对应数据库列 has_phone */
    @TableField("has_phone")
    private Integer hasPhone;

    /** 状态 0停用 1启用，对应数据库列 status */
    @TableField("status")
    private Integer status;

    /** 会议室描述，对应数据库列 description */
    @TableField("description")
    private String remark;
}
