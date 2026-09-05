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
 * 租赁铺位服务（租赁管理-铺位，分类可自定义）
 *
 * @author gbi
 */
public interface LeaseStallService {

    /** 铺位分页查询（含分类名称） */
    PageVO<StallVO> page(StallQueryDTO dto);

    /** 新增铺位（编号同公司唯一，初始状态空置） */
    void add(StallAddDTO dto);

    /** 编辑铺位 */
    void update(StallUpdateDTO dto);

    /** 删除铺位（有合同禁止删除，逻辑删除） */
    void delete(Long id);

    /** 分类下铺位数量（供分类删除前置校验） */
    long countByCategoryId(Long categoryId);

    /** 市场下铺位数量（供市场删除前置校验） */
    long countByMarketId(Long marketId);

    /**
     * 铺位联动下拉选项（市场/租赁分类三级联动选择，company_id 自动隔离）
     *
     * @param marketId       市场ID（可空，空则不限市场）
     * @param stallCategoryId 租赁分类ID（可空，空则不限分类）
     * @param status         铺位状态（可空，空则不限状态；合同选择传0仅返回空置铺位）
     * @return 铺位选项列表
     */
    List<StallOptionVO> listOptions(Long marketId, Long stallCategoryId, Integer status);

    /**
     * 批量获取铺位选项（跨模块供列表组装铺位名称/归属校验，company_id 自动隔离）
     *
     * @param stallIds 铺位ID集合
     * @return key铺位ID value铺位选项
     */
    Map<Long, StallOptionVO> getOptionsByIds(List<Long> stallIds);

    /**
     * 市场下全部铺位ID（跨模块供水电设备按市场过滤，company_id 自动隔离）
     *
     * @param marketId 市场ID
     * @return 铺位ID集合
     */
    List<Long> listStallIdsByMarket(Long marketId);
}