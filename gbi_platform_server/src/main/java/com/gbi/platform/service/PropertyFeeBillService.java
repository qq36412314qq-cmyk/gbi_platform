package com.gbi.platform.service;

import com.gbi.platform.dto.PropertyFeeBillGenerateDTO;
import com.gbi.platform.dto.PropertyFeeBillQueryDTO;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.PropertyFeeBillVO;
import com.gbi.platform.vo.PropertyFeeBillPreviewVO;

import java.util.List;

/**
 * 物业费月度账单服务：分页查询 + 批量/单条生成
 *
 * @author gbi
 */
public interface PropertyFeeBillService {

    /** 账单分页（自动 company_id 隔离） */
    PageVO<PropertyFeeBillVO> page(PropertyFeeBillQueryDTO dto);

    /** 账单详情（供缴费弹窗核对金额，多租户隔离） */
    PropertyFeeBillVO detail(Long id);

    /**
     * 批量生成月度物业费账单：按市场筛选已绑定物业费规则的铺位，按月生成
     * 幂等：同公司同铺位同月份已存在则跳过
     * @return 本次生成账单数量
     */
    int generateBatch(PropertyFeeBillGenerateDTO dto);

    /**
     * 单条生成月度物业费账单：指定铺位生成单条
     * 幂等：同公司同铺位同月份已存在则跳过
     * @return 生成账单ID，已存在则返回已有ID
     */
    Long generateSingle(PropertyFeeBillGenerateDTO dto);

    /** 预览账单金额（不创建账单） */
    PropertyFeeBillPreviewVO preview(PropertyFeeBillGenerateDTO dto);

    /**
     * 将已有账单记录同步写入统一账单表 biz_fee_bill（幂等：同账单ID不重复写）
     * 供「生成账单」按钮直接使用现有记录数据
     */
    String syncToUnpaidBill(Long id);

    /**
     * 批量将物业费记录同步写入未支付订单
     * @param ids 账单ID列表
     * @return 成功同步数量
     */
    int batchSyncToUnpaidBill(List<Long> ids);
}