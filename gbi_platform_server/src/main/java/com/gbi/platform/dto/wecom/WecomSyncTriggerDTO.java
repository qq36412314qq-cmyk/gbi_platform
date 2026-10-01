package com.gbi.platform.dto.wecom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 手动触发同步入参
 *
 * @author gbi
 */
@Data
public class WecomSyncTriggerDTO {

    /** 同步类型：contact=通讯录，attendance=打卡，overtime=加班 */
    @NotBlank(message = "同步类型不能为空")
    @Pattern(regexp = "^(contact|attendance|overtime)$", message = "同步类型非法，可选值：contact/attendance/overtime")
    private String syncType;

    /** 公司ID，null则全部公司 */
    private Long companyId;
}
