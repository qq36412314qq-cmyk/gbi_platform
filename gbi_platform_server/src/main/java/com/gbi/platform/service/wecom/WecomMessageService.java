package com.gbi.platform.service.wecom;

import com.gbi.platform.dto.wecom.WecomMessageQueryDTO;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.wecom.WecomMessageVO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 企微消息记录服务接口
 *
 * @author gbi
 */
public interface WecomMessageService {

    /** 分页查询消息记录 */
    PageVO<WecomMessageVO> pageMessages(WecomMessageQueryDTO dto);

    /** 查看消息详情 */
    WecomMessageVO getMessageDetail(Long id);

    /** 导出消息记录为 Excel */
    void exportMessages(WecomMessageQueryDTO dto, HttpServletResponse response) throws IOException;
}
