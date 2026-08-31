package com.gbi.platform.service;

import com.gbi.platform.dto.WaterElecBillGenerateDTO;
import com.gbi.platform.dto.WaterElecBillQueryDTO;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.WaterElecBillVO;

/**
 * 水电物业月度账单服务：分页查询 + 月度生成
 *
 * @author gbi
 */
public interface WaterElecBillService {

    /** 账单分页（自动 company_id 隔离） */
    PageVO<WaterElecBillVO> page(WaterElecBillQueryDTO dto);

    /** 账单详情（供缴费弹窗核对金额，多租户隔离） */
    WaterElecBillVO detail(Long id);

    /**
     * 生成月度账单：用量 = 本次读数 - 上期账单读数；金额按集团配置单价计算
     * 幂等：同公司同摊位同月份已存在则跳过
     * @return 本次生成账单数量
     */
    int generate(WaterElecBillGenerateDTO dto);

    /**
     * 将已有水电费记录同步写入统一账单表 biz_fee_bill（幂等：同账单ID不重复写）
     * 供「生成账单」按钮直接使用现有记录数据
     */
    String syncToUnpaidBill(Long id);
}
