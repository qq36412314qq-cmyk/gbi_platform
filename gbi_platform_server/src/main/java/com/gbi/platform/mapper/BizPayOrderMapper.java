package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.entity.BizPayOrder;
import com.gbi.platform.dto.PayOrderQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 缴费单主表 Mapper
 *
 * @author gbi
 */
@Mapper
public interface BizPayOrderMapper extends BaseMapper<BizPayOrder> {

    /**
     * 根据源账单ID查询缴费单
     */
    @Select("SELECT * FROM finance_pay_order WHERE source_id = #{sourceId} AND is_delete = 0 LIMIT 1")
    BizPayOrder selectBySourceId(@Param("sourceId") Long sourceId);

    /**
     * 分页查询缴费单列表（多租户公司隔离）
     */
    @Select("<script>" +
            "SELECT * FROM finance_pay_order WHERE is_delete = 0" +
            "<if test='dto.payBillNo != null and dto.payBillNo != \"\"'>" +
            " AND pay_bill_no LIKE CONCAT('%', #{dto.payBillNo}, '%')" +
            "</if>" +
            "<if test='dto.sourceType != null and dto.sourceType != \"\"'>" +
            " AND source_type = #{dto.sourceType}" +
            "</if>" +
            "<if test='dto.payStatus != null'>" +
            " AND pay_status = #{dto.payStatus}" +
            "</if>" +
            "<if test='dto.companyId != null and dto.companyId != 0'>" +
            " AND company_id = #{dto.companyId}" +
            "</if>" +
            "<if test='dto.startTime != null and dto.startTime != \"\"'>" +
            " AND create_time &gt;= #{dto.startTime}" +
            "</if>" +
            "<if test='dto.endTime != null and dto.endTime != \"\"'>" +
            " AND create_time &lt;= #{dto.endTime}" +
            "</if>" +
            " ORDER BY create_time DESC" +
            "</script>")
    IPage<BizPayOrder> selectPageByQuery(Page<BizPayOrder> page, @Param("dto") PayOrderQueryDTO dto);
}