package com.gbi.platform.service.wecom;

import com.gbi.platform.dto.wecom.WecomSyncQueryDTO;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.wecom.WecomSyncVO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 企微同步记录服务接口
 *
 * @author gbi
 */
public interface WecomSyncService {

    /** 分页查询同步记录 */
    PageVO<WecomSyncVO> pageSyncs(WecomSyncQueryDTO dto);

    /** 查看同步详情 */
    WecomSyncVO getSyncDetail(Long id);

    /** 导出同步记录为 Excel */
    void exportSyncs(WecomSyncQueryDTO dto, HttpServletResponse response) throws IOException;
}
