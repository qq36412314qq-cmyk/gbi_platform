package com.gbi.platform.service;

import com.gbi.platform.dto.MarketDTO;
import com.gbi.platform.dto.MarketQueryDTO;
import com.gbi.platform.vo.MarketVO;
import com.gbi.platform.vo.PageVO;

import java.util.List;
import java.util.Map;

/**
 * 市场档案服务：园区/商圈维度维护，摊位与市场地图统一关联
 *
 * @author gbi
 */
public interface MarketService {

    /**
     * 分页查询市场
     *
     * @param dto 查询入参（市场名称/状态）
     * @return 市场分页
     */
    PageVO<MarketVO> page(MarketQueryDTO dto);

    /**
     * 全量列表（下拉选择用，默认仅启用）
     *
     * @return 市场列表
     */
    List<MarketVO> list();

    /**
     * 新增市场
     *
     * @param dto 新增入参
     */
    void add(MarketDTO dto);

    /**
     * 编辑市场
     *
     * @param dto 编辑入参（id 必填）
     */
    void update(MarketDTO dto);

    /**
     * 删除市场（逻辑删除，市场下有摊位禁止删除）
     *
     * @param id 市场ID
     */
    void delete(Long id);

    /**
     * 市场存在性校验（跨模块供摊位/合同调用）
     *
     * @param id 市场ID
     * @return true存在 false不存在
     */
    boolean existsById(Long id);

    /**
     * 批量获取市场名称（跨模块供列表组装）
     *
     * @param ids 市场ID集合
     * @return key市场ID value市场名称
     */
    Map<Long, String> getNamesByIds(List<Long> ids);
}