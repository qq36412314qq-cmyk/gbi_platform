package com.gbi.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oa_meeting_room")
public class OaMeetingRoom extends BaseEntity {
    private Long companyId;
    private String roomName;
    private String location;
    private Integer capacity;
    private String facilities;
    private Integer status;
    private String remark;
}
