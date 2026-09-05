package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.dto.UnpaidBillQueryDTO;
import com.gbi.platform.entity.BizFeeBill;
import com.gbi.platform.mapper.BizFeeBillMapper;
import com.gbi.platform.service.LeaseStallService;
import com.gbi.platform.service.UnpaidBillService;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.StallOptionVO;
import com.gbi.platform.vo.UnpaidBillVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 未支付订单聚合服务
 * 数据来源：biz_fee_bill（统一账单表），新增费用类型无需改表结构
 * 查询待缴(0)和部分缴费(1)状态的订单，供未支付订单页面展示
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnpaidBillServiceImpl implements UnpaidBillService {

    private final BizFeeBillMapper bizFeeBillMapper;
    private final LeaseStallService leaseStallService;

    @Override
    public PageVO<UnpaidBillVO> page(UnpaidBillQueryDTO dto) {
        // 查询待缴(0)和部分缴费(1)的未支付订单
        LambdaQueryWrapper<BizFeeBill> wrapper = new LambdaQueryWrapper<BizFeeBill>()
                .in(BizFeeBill::getPayStatus, CommonConst.BILL_PAY_STATUS_UNPAID, CommonConst.BILL_PAY_STATUS_PART)
                .orderByDesc(BizFeeBill::getBillMonth)
                .orderByDesc(BizFeeBill::getId);
        List<BizFeeBill> bills = bizFeeBillMapper.selectList(wrapper);

        List<Long> stallIds = bills.stream()
                .map(BizFeeBill::getStallId).filter(java.util.Objects::nonNull).distinct().toList();
        Map<Long, StallOptionVO> stallMap = stallIds.isEmpty()
                ? Collections.emptyMap()
                : leaseStallService.getOptionsByIds(stallIds);

        List<UnpaidBillVO> allRecords = bills.stream().map(bill -> {
            UnpaidBillVO vo = new UnpaidBillVO();
            vo.setId(bill.getId());
            vo.setCompanyId(bill.getCompanyId());
            vo.setBusinessType(bill.getBizType());
            vo.setBusinessTypeText(bizTypeText(bill.getBizType()));
            vo.setBillMonth(bill.getBillMonth());
            vo.setStallId(bill.getStallId());
            StallOptionVO stall = stallMap.get(bill.getStallId());
            vo.setStallNumber(stall == null ? null : stall.getStallNumber());
            vo.setStallName(stall == null ? null : stall.getStallName());
            vo.setStallMarketName(stall == null ? null : stall.getMarketName());
            vo.setCategoryName(stall == null ? null : stall.getCategoryName());
            vo.setAmount(bill.getRealAmount());
            vo.setSourceBillId(bill.getSourceBillId());
            vo.setPayStatus(bill.getPayStatus());
            vo.setPayStatusText(toPayStatusText(bill.getPayStatus()));
            vo.setPayTime(bill.getPayTime());
            vo.setCreateTime(bill.getCreateTime());
            return vo;
        }).toList();

        int total = allRecords.size();
        int fromIndex = (dto.getPageNum() - 1) * dto.getPageSize();
        int toIndex = Math.min(fromIndex + dto.getPageSize(), total);
        List<UnpaidBillVO> pageRecords = fromIndex < total
                ? allRecords.subList(fromIndex, toIndex)
                : Collections.emptyList();
        int totalPages = Math.max(1, (total + dto.getPageSize() - 1) / dto.getPageSize());

        return new PageVO<>(pageRecords, (long) total, (long) dto.getPageNum(),
                (long) dto.getPageSize(), (long) totalPages);
    }

    private String bizTypeText(String bizType) {
        if (bizType == null) return null;
        return switch (bizType) {
            case "property_fee" -> "物业费";
            case "water_elec" -> "水电费";
            case "rent" -> "租赁费";
            case "kindergarten" -> "幼儿园费";
            case "deposit" -> "押金";
            default -> bizType;
        };
    }

    private String toPayStatusText(Integer status) {
        if (status == null) return null;
        return switch (status) {
            case CommonConst.BILL_PAY_STATUS_UNPAID -> "待缴";
            case CommonConst.BILL_PAY_STATUS_PART -> "部分缴费";
            case CommonConst.BILL_PAY_STATUS_PAID -> "已缴";
            default -> "未知";
        };
    }
}
