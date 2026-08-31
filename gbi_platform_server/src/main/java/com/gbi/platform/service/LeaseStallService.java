package com.gbi.platform.service;

import com.gbi.platform.dto.StallAddDTO;
import com.gbi.platform.dto.StallQueryDTO;
import com.gbi.platform.dto.StallUpdateDTO;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.StallOptionVO;
import com.gbi.platform.vo.StallVO;

import java.util.List;
import java.util.Map;

/**
 * 租赁摊位服务（租赁管理-摊位，分类可自定义）
 *
 * @author gbi
 */
public interface LeaseStallService {

    /** 摊位分页查询（含分类名称） */
    PageVO<StallVO> page(StallQueryDTO dto);

    /** 新增摊位（编号同公司唯一，初始状态空置） */
    void add(StallAddDTO dto);

    /** 编辑摊位 */
    void update(StallUpdateDTO dto);

    /** 删除摊位（有合同禁止删除，逻辑删除） */
    void delete(Long id);

    /** 分类下摊位数量（供分类删除前置校验） */
    long countByCategoryId(Long categoryId);

    /** 市场下摊位数量（供市场删除前置校验） */
    long countByMarketId(Long marketId);

    /**
     * 摊位联动下拉选项（市场/租赁分类三级联动选择，company_id 自动隔离）
     *
     * @param marketId       市场ID（可空，空则不限市场）
     * @param stallCategoryId 租赁分类ID（可空，空则不限分类）
     * @param status         摊位状态（可空，空则不限状态；合同选择传0仅返回空置摊位）
     * @return 摊位选项列表
     */
    List<StallOptionVO> listOptions(Long marketId, Long stallCategoryId, Integer status);

    /**
     * 批量获取摊位选项（跨模块供列表组装摊位名称/归属校验，company_id 自动隔离）
     *
     * @param stallIds 摊位ID集合
     * @return key摊位ID value摊位选项
     */
    Map<Long, StallOptionVO> getOptionsByIds(List<Long> stallIds);

    /**
     * 市场下全部摊位ID（跨模块供水电设备按市场过滤，company_id 自动隔离）
     *
     * @param marketId 市场ID
     * @return 摊位ID集合
     */
    List<Long> listStallIdsByMarket(Long marketId);
}