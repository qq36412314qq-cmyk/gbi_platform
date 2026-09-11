package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.WaterElecBillGenerateDTO;
import com.gbi.platform.dto.WaterElecBillGenerateDTO.MeterReadItem;
import com.gbi.platform.dto.WaterElecBillQueryDTO;
import com.gbi.platform.entity.BillPlanRel;
import com.gbi.platform.entity.BizFeeBill;
import com.gbi.platform.entity.WaterElecBill;
import com.gbi.platform.entity.WaterElecMeter;
import com.gbi.platform.mapper.BillPlanRelMapper;
import com.gbi.platform.mapper.BizFeeBillMapper;
import com.gbi.platform.mapper.WaterElecBillMapper;
import com.gbi.platform.mapper.WaterElecMeterMapper;
import com.gbi.platform.service.FeeRuleStallRelService;
import com.gbi.platform.service.LeaseStallService;
import com.gbi.platform.service.RecvPayPlanService;
import com.gbi.platform.service.WaterElecBillService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.StallOptionVO;
import com.gbi.platform.vo.StallRuleRelVO;
import com.gbi.platform.vo.WaterElecBillVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import com.gbi.platform.entity.StallContract;
import com.gbi.platform.entity.StallTenant;
import com.gbi.platform.mapper.StallContractMapper;
import com.gbi.platform.mapper.StallTenantMapper;
import com.gbi.platform.service.LeaseContractService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 水电费月度账单服务实现：分页查询 + 月度生成
 * 按收费类别分行存储（水费/电费各一行），缴费状态联动
 * 生成时同时写入 biz_fee_bill（统一账单表），供未支付订单页面聚合展示
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WaterElecBillServiceImpl implements WaterElecBillService {

    private final WaterElecBillMapper billMapper;
    private final BizFeeBillMapper bizFeeBillMapper;
    private final WaterElecMeterMapper meterMapper;
    private final AuditLogUtil auditLogUtil;
    private final RecvPayPlanService recvPayPlanService;
    private final BillPlanRelMapper billPlanRelMapper;
    private final LeaseStallService leaseStallService;
    private final FeeRuleStallRelService feeRuleStallRelService;
    private final StallContractMapper stallContractMapper;
    private final StallTenantMapper stallTenantMapper;
    private final LeaseContractService leaseContractService;

    @Override
    public PageVO<WaterElecBillVO> page(WaterElecBillQueryDTO dto) {
        Page<WaterElecBill> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<WaterElecBill> wrapper = new LambdaQueryWrapper<WaterElecBill>()
                .eq(StringUtils.hasText(dto.getBillMonth()), WaterElecBill::getBillMonth, dto.getBillMonth())
                .eq(dto.getStallId() != null, WaterElecBill::getStallId, dto.getStallId())
                .eq(dto.getPayStatus() != null, WaterElecBill::getPayStatus, dto.getPayStatus())
                .eq(dto.getCategory() != null, WaterElecBill::getCategory, dto.getCategory())
                .orderByDesc(WaterElecBill::getBillMonth)
                .orderByDesc(WaterElecBill::getId);
        Page<WaterElecBill> result = billMapper.selectPage(page, wrapper);
        Map<Long, StallOptionVO> stallMap = loadStallMap(result.getRecords());
        Map<Long, String> tenantNameMap = loadTenantNameMap(result.getRecords());
        Set<Long> feeBillIds = loadFeeBillIds(result.getRecords());
        List<WaterElecBillVO> voList = result.getRecords().stream()
                .map(b -> toVO(b, stallMap, tenantNameMap, feeBillIds)).toList();
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public WaterElecBillVO detail(Long id) {
        WaterElecBill bill = billMapper.selectById(id);
        if (bill == null) {
            throw new BizException("账单不存在或已删除");
        }
        Map<Long, StallOptionVO> stallMap = loadStallMap(List.of(bill));
        Map<Long, String> tenantNameMap = loadTenantNameMap(List.of(bill));
        Set<Long> feeBillIds = loadFeeBillIds(List.of(bill));
        return toVO(bill, stallMap, tenantNameMap, feeBillIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generate(WaterElecBillGenerateDTO dto) { log.info("[DEBUG] generate called: month={}, reads={}", dto.getBillMonth(), dto.getMeterReads());
        LoginUser loginUser = UserContext.getLoginUser();
        Long companyId = loginUser.getCompanyId();

        if (dto.getMeterReads() == null || dto.getMeterReads().isEmpty()) {
            throw new BizException("请至少选择一只表进行抄表");
        }

        Map<Long, MeterAgg> stallAggMap = new LinkedHashMap<>();
        for (MeterReadItem item : dto.getMeterReads()) {
            WaterElecMeter meter = meterMapper.selectById(item.getMeterId()); log.info("[DEBUG] meter: id={}, type={}, currentRead={}, stallId={}", meter.getId(), meter.getMeterType(), meter.getCurrentRead(), meter.getStallId());
            if (meter == null) {
                throw new BizException("设备ID " + item.getMeterId() + " 不存在，请确认后重新选择");
            }
            if (meter.getCurrentRead() != null && item.getCurrentRead().compareTo(meter.getCurrentRead()) < 0) {
                throw new BizException("设备 " + meter.getMeterNo() + " 本次读数 " + item.getCurrentRead()
                        + " 不能小于当前读数 " + meter.getCurrentRead() + "，请检查后重新输入");
            }
            MeterAgg agg = stallAggMap.computeIfAbsent(meter.getStallId(), k -> new MeterAgg());
            if (meter.getMeterType() == CommonConst.METER_TYPE_WATER) {
                agg.prevWaterRead = meter.getCurrentRead();
                agg.waterRead = item.getCurrentRead();
            } else if (meter.getMeterType() == CommonConst.METER_TYPE_ELEC) {
                agg.prevElecRead = meter.getCurrentRead();
                agg.elecRead = item.getCurrentRead();
            }
        }

        List<Long> stallIds = new ArrayList<>(stallAggMap.keySet());
        Map<Long, List<StallRuleRelVO>> ruleRelMap = feeRuleStallRelService.listByStallIds(stallIds); log.info("[DEBUG] ruleRelMap: {}", ruleRelMap);
        Map<Long, StallOptionVO> stallMap = loadStallMap(
                stallIds.stream().map(id -> {
                    WaterElecBill b = new WaterElecBill();
                    b.setStallId(id);
                    return b;
                }).toList());

        int generated = 0;
        for (Map.Entry<Long, MeterAgg> entry : stallAggMap.entrySet()) {
            Long stallId = entry.getKey();
            MeterAgg agg = entry.getValue();
            List<StallRuleRelVO> rels = ruleRelMap.getOrDefault(stallId, Collections.emptyList());
            StallRuleRelVO waterRule = findRuleByCategory(rels, CommonConst.FEE_CATEGORY_WATER);
            StallRuleRelVO elecRule = findRuleByCategory(rels, CommonConst.FEE_CATEGORY_ELEC);

            BigDecimal waterPrice = waterRule == null || waterRule.getPrice() == null ? BigDecimal.ZERO : waterRule.getPrice();
            BigDecimal elecPrice = elecRule == null || elecRule.getPrice() == null ? BigDecimal.ZERO : elecRule.getPrice();

            BigDecimal prevWaterRead = agg.prevWaterRead == null ? BigDecimal.ZERO : agg.prevWaterRead;
            BigDecimal currWater = agg.waterRead == null ? BigDecimal.ZERO : agg.waterRead;
            BigDecimal waterUsage = calcUsage(currWater, prevWaterRead);
            BigDecimal waterAmt = waterUsage.multiply(waterPrice);
            if (waterAmt.signum() > 0) {
                insertCategoryBill(companyId, stallId, dto.getBillMonth(), CommonConst.FEE_CATEGORY_WATER,
                        prevWaterRead, waterUsage, waterPrice, waterAmt);
                generated++;
            }

            BigDecimal prevElecRead = agg.prevElecRead == null ? BigDecimal.ZERO : agg.prevElecRead;
            BigDecimal currElec = agg.elecRead == null ? BigDecimal.ZERO : agg.elecRead;
            BigDecimal elecUsage = calcUsage(currElec, prevElecRead);
            BigDecimal elecAmt = elecUsage.multiply(elecPrice);
            if (elecAmt.signum() > 0) {
                insertCategoryBill(companyId, stallId, dto.getBillMonth(), CommonConst.FEE_CATEGORY_ELEC,
                        prevElecRead, elecUsage, elecPrice, elecAmt);
                generated++;
            }
        }

        for (MeterReadItem item : dto.getMeterReads()) {
            WaterElecMeter meter = new WaterElecMeter();
            meter.setId(item.getMeterId());
            meter.setCurrentRead(item.getCurrentRead());
            meterMapper.updateById(meter);
        }
        log.info("[DEBUG] after insertCategoryBill loop, generated={}", generated); log.info("水电费账单生成完成：month={}, 涉及铺位数={}, 生成账单行数={}", dto.getBillMonth(), stallAggMap.size(), generated);
        return generated;
    }

    private StallRuleRelVO findRuleByCategory(List<StallRuleRelVO> rels, int categoryType) {
        return rels.stream()
                .filter(r -> r.getCategoryType() != null && r.getCategoryType() == categoryType)
                .findFirst().orElse(null);
    }

    private BigDecimal calcUsage(BigDecimal current, BigDecimal prev) {
        BigDecimal c = current == null ? BigDecimal.ZERO : current;
        BigDecimal p = prev == null ? BigDecimal.ZERO : prev;
        if (c.compareTo(p) < 0) {
            throw new BizException("本次读数小于上期读数，请检查抄表数据");
        }
        return c.subtract(p);
    }

    private void insertCategoryBill(Long companyId, Long stallId, String billMonth, int category,
                                    BigDecimal prevMeterRead, BigDecimal usage, BigDecimal unitPrice, BigDecimal amount) {
        WaterElecBill bill = new WaterElecBill();
        bill.setCompanyId(companyId);
        bill.setStallId(stallId);
        bill.setMerchantId(null);
        bill.setBillMonth(billMonth);
        bill.setCategory(category);
        bill.setPrevMeterRead(prevMeterRead);
        bill.setUsage(usage);
        bill.setUnitPrice(unitPrice);
        bill.setTotalAmount(amount);
        bill.setPayStatus(CommonConst.BILL_PAY_STATUS_UNPAID);
        billMapper.insert(bill);
        Long planId = recvPayPlanService.generatePlanForBill(bill);
        if (planId != null) {
            billMapper.update(null, new LambdaUpdateWrapper<WaterElecBill>()
                    .eq(WaterElecBill::getId, bill.getId())
                    .set(WaterElecBill::getPlanId, planId));
            BillPlanRel rel = new BillPlanRel();
            rel.setCompanyId(companyId);
            rel.setBillType(CommonConst.BIZ_TYPE_WATER_ELEC);
            rel.setBillId(bill.getId());
            rel.setPlanId(planId);
            rel.setSplitAmount(bill.getTotalAmount());
            billPlanRelMapper.insert(rel);
        }

        // 同步写入统一账单表 biz_fee_bill
        writeToUnifiedBill(companyId, stallId, billMonth, category, amount);

        auditLogUtil.record(CommonConst.MODULE_WATER_ELEC, CommonConst.OPER_TYPE_ADD,
                String.valueOf(bill.getId()), null, bill);
    }

    /**
     * 将水电费账单行写入统一账单表 biz_fee_bill（幂等：同公司同铺位同月份同类别不重复写）
     */
    private void writeToUnifiedBill(Long companyId, Long stallId, String billMonth, int category, BigDecimal amount) {
        // category: 3=水费, 4=电费，统一用 water_elec biz_type
        BizFeeBill unifiedBill = bizFeeBillMapper.selectOne(new LambdaQueryWrapper<BizFeeBill>()
                .eq(BizFeeBill::getCompanyId, companyId)
                .eq(BizFeeBill::getStallId, stallId)
                .eq(BizFeeBill::getBillMonth, billMonth)
                .eq(BizFeeBill::getBizType, CommonConst.BIZ_TYPE_WATER_ELEC)
                .last("LIMIT 1"));
        if (unifiedBill == null) {
            unifiedBill = new BizFeeBill();
            unifiedBill.setCompanyId(companyId);
            unifiedBill.setBizType(CommonConst.BIZ_TYPE_WATER_ELEC);
            unifiedBill.setStallId(stallId);
            unifiedBill.setBillMonth(billMonth);
            unifiedBill.setOriginalAmount(amount);
            unifiedBill.setDiscountAmount(BigDecimal.ZERO);
            unifiedBill.setAdjustAmount(BigDecimal.ZERO);
            unifiedBill.setRealAmount(amount);
            unifiedBill.setPayStatus(CommonConst.BILL_PAY_STATUS_UNPAID);
            unifiedBill.setLockedFlag(1);
            unifiedBill.setCreateBy(UserContext.getUserIdOrZero());
            bizFeeBillMapper.insert(unifiedBill);
        } else {
            log.info("统一账单已存在，跳过写入，直接同步计划：companyId={}, stallId={}, month={}", companyId, stallId, billMonth);
        }

        Long planId = recvPayPlanService.generatePlanForBizFeeBill(unifiedBill);
        if (planId != null) {
            bizFeeBillMapper.update(null, new LambdaUpdateWrapper<BizFeeBill>()
                    .eq(BizFeeBill::getId, unifiedBill.getId())
                    .set(BizFeeBill::getPlanId, planId));
            // 幂等：同一 bill+plan 已有关联时跳过，避免水电同类重复插入触发 uk_bill_plan 冲突
            long relCount = billPlanRelMapper.selectCount(new LambdaQueryWrapper<BillPlanRel>()
                    .eq(BillPlanRel::getBillType, CommonConst.BIZ_TYPE_WATER_ELEC)
                    .eq(BillPlanRel::getBillId, unifiedBill.getId())
                    .eq(BillPlanRel::getPlanId, planId));
            if (relCount == 0) {
                BillPlanRel rel = new BillPlanRel();
                rel.setCompanyId(companyId);
                rel.setBillType(CommonConst.BIZ_TYPE_WATER_ELEC);
                rel.setBillId(unifiedBill.getId());
                rel.setPlanId(planId);
                rel.setSplitAmount(unifiedBill.getRealAmount());
                billPlanRelMapper.insert(rel);
            } else {
                log.info("BillPlanRel 已存在，跳过插入：billId={}, planId={}", unifiedBill.getId(), planId);
            }
        }
        log.info("水电费账单同步写入统一账单表：companyId={}, stallId={}, month={}, bizBillId={}",
                companyId, stallId, billMonth, unifiedBill.getId());
    }

    private Map<Long, StallOptionVO> loadStallMap(List<WaterElecBill> bills) {
        List<Long> stallIds = bills.stream()
                .map(WaterElecBill::getStallId).filter(Objects::nonNull).distinct().toList();
        return stallIds.isEmpty() ? Collections.emptyMap()
                : leaseStallService.getOptionsByIds(stallIds);
    }

    private WaterElecBillVO toVO(WaterElecBill bill, Map<Long, StallOptionVO> stallMap, Map<Long, String> tenantNameMap, Set<Long> feeBillIds) {
        WaterElecBillVO vo = new WaterElecBillVO();
        vo.setId(bill.getId());
        vo.setCompanyId(bill.getCompanyId());
        vo.setStallId(bill.getStallId());
        StallOptionVO stall = stallMap.get(bill.getStallId());
        vo.setStallNumber(stall == null ? null : stall.getStallNumber());
        vo.setStallName(stall == null ? null : stall.getStallName());
        vo.setStallMarketName(stall == null ? null : stall.getMarketName());
        vo.setCategoryName(stall == null ? null : stall.getCategoryName());
        vo.setMerchantId(bill.getMerchantId());
        vo.setTenantName(tenantNameMap.get(bill.getStallId()));
        vo.setHasFeeBill(feeBillIds.contains(bill.getId()));
        vo.setBillMonth(bill.getBillMonth());
        vo.setCategory(bill.getCategory());
        vo.setCategoryText(bill.getCategory() == null ? null : switch (bill.getCategory()) {
            case CommonConst.FEE_CATEGORY_WATER -> "水费";
            case CommonConst.FEE_CATEGORY_ELEC -> "电费";
            default -> null;
        });
        vo.setPrevMeterRead(bill.getPrevMeterRead());
        vo.setUsage(bill.getUsage());
        vo.setUnitPrice(bill.getUnitPrice());
        vo.setTotalAmount(bill.getTotalAmount());
        vo.setPayStatus(bill.getPayStatus());
        vo.setPayStatusText(bill.getPayStatus() == null ? null : switch (bill.getPayStatus()) {
            case CommonConst.BILL_PAY_STATUS_UNPAID -> "待缴";
            case CommonConst.BILL_PAY_STATUS_PAID -> "已缴";
            case CommonConst.BILL_PAY_STATUS_PART -> "部分缴费";
            default -> "未知";
        });
        vo.setPayTime(bill.getPayTime());
        vo.setCreateTime(bill.getCreateTime());
        return vo;
    }


    /**
     * 批量加载铺位关联的租户名称（通过 stall_contract 表关联查询）
     */
    private Map<Long, String> loadTenantNameMap(List<WaterElecBill> bills) {
        List<Long> stallIds = bills.stream()
                .map(WaterElecBill::getStallId).filter(Objects::nonNull).distinct().toList();
        if (stallIds.isEmpty()) {
            return Collections.emptyMap();
        }
        // 查询生效中合同，获取 stallId -> tenantId 映射
        List<StallContract> contracts = stallContractMapper.selectList(
                new LambdaQueryWrapper<StallContract>()
                        .in(StallContract::getStallId, stallIds)
                        .eq(StallContract::getContractStatus, CommonConst.CONTRACT_STATUS_EFFECTIVE));
        Map<Long, Long> stallTenantMap = contracts.stream()
                .filter(c -> c.getTenantId() != null)
                .collect(Collectors.toMap(StallContract::getStallId, StallContract::getTenantId, (a, b) -> a));
        if (stallTenantMap.isEmpty()) {
            return Collections.emptyMap();
        }
        // 批量查询租户名称
        List<Long> tenantIds = new ArrayList<>(stallTenantMap.values());
        Map<Long, String> tenantNameMap = leaseContractService.mapTenantNames(tenantIds);
        // 组装 stallId -> tenantName
        return stallTenantMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> tenantNameMap.getOrDefault(e.getValue(), null)));
    }

    /**
     * 批量加载已写入 finance_fee_pay_bill 的源账单 ID（用于 hasFeeBill 字段）
     */
    private Set<Long> loadFeeBillIds(List<WaterElecBill> bills) {
        List<Long> billIds = bills.stream()
                .map(WaterElecBill::getId).filter(Objects::nonNull).distinct().toList();
        if (billIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<BizFeeBill> existing = bizFeeBillMapper.selectList(
                new LambdaQueryWrapper<BizFeeBill>()
                        .in(BizFeeBill::getSourceBillId, billIds));
        return existing.stream()
                .map(BizFeeBill::getSourceBillId)
                .collect(Collectors.toSet());
    }

    private static class MeterAgg {
        private BigDecimal prevWaterRead;
        private BigDecimal waterRead;
        private BigDecimal prevElecRead;
        private BigDecimal elecRead;
    }

    @Override
    public String syncToUnpaidBill(Long id) {
        WaterElecBill bill = billMapper.selectById(id);
        if (bill == null) {
            throw new BizException("账单不存在或已删除");
        }
        // 幂等：已写入 biz_fee_bill 则跳过
        Long exists = bizFeeBillMapper.selectCount(new LambdaQueryWrapper<BizFeeBill>()
                .eq(BizFeeBill::getSourceBillId, bill.getId()));
        if (exists != null && exists > 0) {
            log.info("账单已存在，跳过同步：waterElecBillId={}", id);
            return "订单已存在，无需重复生成";
        }
        BizFeeBill unifiedBill = new BizFeeBill();
        unifiedBill.setCompanyId(bill.getCompanyId());
        unifiedBill.setBizType(CommonConst.BIZ_TYPE_WATER_ELEC);
        unifiedBill.setStallId(bill.getStallId());
        unifiedBill.setBillMonth(bill.getBillMonth());
        Long ruleId = (bill.getCategory() != null && bill.getCategory() == CommonConst.FEE_CATEGORY_WATER) ? 4L : 2L;
        unifiedBill.setRuleId(ruleId);
        unifiedBill.setSourceBillId(bill.getId());
        unifiedBill.setOriginalAmount(bill.getTotalAmount());
        unifiedBill.setDiscountAmount(BigDecimal.ZERO);
        unifiedBill.setAdjustAmount(BigDecimal.ZERO);
        unifiedBill.setRealAmount(bill.getTotalAmount());
        unifiedBill.setPayStatus(bill.getPayStatus() != null ? bill.getPayStatus() : CommonConst.BILL_PAY_STATUS_UNPAID); 
        unifiedBill.setLockedFlag(1);
        unifiedBill.setCreateBy(UserContext.getUserIdOrZero());
        try { bizFeeBillMapper.insert(unifiedBill); } catch (Exception ex) { log.warn("统一账单已存在，跳过：waterElecBillId={}", id); return "订单已存在，无需重复生成"; }
        // 关联应收应付计划
        Long planId = recvPayPlanService.generatePlanForBizFeeBill(unifiedBill);
        if (planId != null) {
            bizFeeBillMapper.update(null, new LambdaUpdateWrapper<BizFeeBill>()
                    .eq(BizFeeBill::getId, unifiedBill.getId())
                    .set(BizFeeBill::getPlanId, planId));
            BillPlanRel rel = new BillPlanRel();
            rel.setCompanyId(unifiedBill.getCompanyId());
            rel.setBillType(CommonConst.BIZ_TYPE_WATER_ELEC);
            rel.setBillId(unifiedBill.getId());
            rel.setPlanId(planId);
            rel.setSplitAmount(unifiedBill.getRealAmount());
            billPlanRelMapper.insert(rel);
        }
        log.info("水电费账单同步成功：waterElecBillId={}, bizBillId={}", id, unifiedBill.getId());
        return "同步成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSyncToUnpaidBill(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        int synced = 0;
        for (Long id : ids) {
            try {
                String msg = syncToUnpaidBill(id);
                if (!msg.contains("重复")) {
                    synced++;
                }
            } catch (Exception e) {
                log.warn("批量同步失败，跳过账单ID={}: {}", id, e.getMessage());
            }
        }
        log.info("水电费批量同步完成：请求数={}, 成功数={}", ids.size(), synced);
        return synced;
    }
}
