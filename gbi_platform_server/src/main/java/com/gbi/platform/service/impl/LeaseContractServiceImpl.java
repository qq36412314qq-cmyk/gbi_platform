package com.gbi.platform.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.ContractAddDTO;
import com.gbi.platform.dto.ContractQueryDTO;
import com.gbi.platform.dto.ContractTerminateDTO;
import com.gbi.platform.entity.BillPlanRel;
import com.gbi.platform.entity.FeeRuleStallRel;
import com.gbi.platform.entity.BizFinanceFlow;
import com.gbi.platform.entity.BizFeeBill;
import com.gbi.platform.entity.StallCategory;
import com.gbi.platform.entity.StallContract;
import com.gbi.platform.entity.StallInfo;
import com.gbi.platform.entity.MarketInfo;
import com.gbi.platform.entity.StallTenant;
import com.gbi.platform.mapper.BillPlanRelMapper;
import com.gbi.platform.mapper.FeeRuleStallRelMapper;
import com.gbi.platform.mapper.FeeRuleMapper;
import com.gbi.platform.entity.FeeRule;
import com.gbi.platform.mapper.BizFinanceFlowMapper;
import com.gbi.platform.mapper.BizFeeBillMapper;
import com.gbi.platform.mapper.StallCategoryMapper;
import com.gbi.platform.mapper.StallContractMapper;
import com.gbi.platform.mapper.StallInfoMapper;
import com.gbi.platform.mapper.MarketInfoMapper;
import com.gbi.platform.mapper.StallTenantMapper;
import com.gbi.platform.service.ConfigService;
import com.gbi.platform.service.DiscountApplyService;
import com.gbi.platform.service.LeaseContractService;
import com.gbi.platform.service.RecvPayPlanService;
import com.gbi.platform.service.TenantService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.util.FlowNoGenerator;
import com.gbi.platform.mapper.FlowSeqMapper;
import com.gbi.platform.vo.ContractVO;
import com.gbi.platform.vo.PageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 租赁合同服务实现：租户+铺位租赁合同
 * 新增：校验铺位空置 -> 合同生效 -> 铺位置为已租 -> 押金写收入流水 -> 同步写入 biz_fee_bill（统一账单表），供未支付订单页面聚合展示；
 * 退租（高危）：合同终止 -> 铺位置空 -> 押金退费支出流水 -> 强制审计；
 * 数据一致性：押金收支统一写入 biz_finance_flow（业务类型 deposit）
 *
 * @author gbi
 */
@Slf4j
@Service
public class LeaseContractServiceImpl implements LeaseContractService {

    private final StallContractMapper contractMapper;

    private final StallInfoMapper stallMapper;

    private final MarketInfoMapper marketInfoMapper;

    private final StallCategoryMapper categoryMapper;

    private final StallTenantMapper tenantMapper;

    private final BizFinanceFlowMapper financeFlowMapper;

    private final FlowNoGenerator flowNoGenerator;

    private final TenantService tenantService;

    private final AuditLogUtil auditLogUtil;

    private final RecvPayPlanService recvPayPlanService;

    private final BizFeeBillMapper bizFeeBillMapper;

    private final BillPlanRelMapper billPlanRelMapper;

    private final FeeRuleStallRelMapper feeRuleStallRelMapper;

    private final FeeRuleMapper feeRuleMapper;

    private final ConfigService configService;

    private final DiscountApplyService discountApplyService;

    public LeaseContractServiceImpl(StallContractMapper contractMapper, StallInfoMapper stallMapper, MarketInfoMapper marketInfoMapper,
                                    StallCategoryMapper categoryMapper, StallTenantMapper tenantMapper,
                                    BizFinanceFlowMapper financeFlowMapper, TenantService tenantService,
                                    AuditLogUtil auditLogUtil,
                                    @Lazy RecvPayPlanService recvPayPlanService,
                                    @Lazy DiscountApplyService discountApplyService,
                                    ConfigService configService,
                                    FlowNoGenerator flowNoGenerator,
                                    BizFeeBillMapper bizFeeBillMapper,
                                    BillPlanRelMapper billPlanRelMapper,
                                    FeeRuleStallRelMapper feeRuleStallRelMapper,
                                    FeeRuleMapper feeRuleMapper) {
        this.contractMapper = contractMapper;
        this.stallMapper = stallMapper;
        this.marketInfoMapper = marketInfoMapper;
        this.categoryMapper = categoryMapper;
        this.tenantMapper = tenantMapper;
        this.financeFlowMapper = financeFlowMapper;
        this.tenantService = tenantService;
        this.auditLogUtil = auditLogUtil;
        this.recvPayPlanService = recvPayPlanService;
        this.discountApplyService = discountApplyService;
        this.configService = configService;
        this.flowNoGenerator = flowNoGenerator;
        this.bizFeeBillMapper = bizFeeBillMapper;
        this.billPlanRelMapper = billPlanRelMapper;
        this.feeRuleStallRelMapper = feeRuleStallRelMapper;
        this.feeRuleMapper = feeRuleMapper;
    }

    @Override
    public PageVO<ContractVO> page(ContractQueryDTO dto) {
        Page<StallContract> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<StallContract> wrapper = new LambdaQueryWrapper<StallContract>()
                .eq(dto.getTenantId() != null, StallContract::getTenantId, dto.getTenantId())
                .eq(dto.getStallId() != null, StallContract::getStallId, dto.getStallId())
                .eq(dto.getContractStatus() != null, StallContract::getContractStatus, dto.getContractStatus())
                .orderByDesc(StallContract::getCreateTime);
        Page<StallContract> result = contractMapper.selectPage(page, wrapper);

        // 批量查铺位信息
        List<Long> stallIds = result.getRecords().stream()
                .map(StallContract::getStallId).distinct().toList();
        Map<Long, StallInfo> stallMap = stallMapper.selectBatchIds(stallIds).stream()
                .collect(Collectors.toMap(StallInfo::getId, s -> s));

        // 批量查分类名称
        Set<Long> catIds = stallMap.values().stream()
                .map(StallInfo::getStallCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> categoryNameMap = catIds.isEmpty() ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(catIds).stream()
                        .collect(Collectors.toMap(StallCategory::getId, StallCategory::getCategoryName));

        // 批量查市场名称
        Set<Long> marketIds = stallMap.values().stream()
                .map(StallInfo::getMarketId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> marketNameMap = marketIds.isEmpty() ? Collections.emptyMap()
                : marketInfoMapper.selectBatchIds(marketIds).stream()
                        .collect(Collectors.toMap(MarketInfo::getId, MarketInfo::getMarketName));

        // 批量查租金收费规则周期类型
        Map<Long, Integer> rentPeriodTypeMap = new HashMap<>();
        for (Long stallId : stallIds) {
            List<FeeRuleStallRel> rels = feeRuleStallRelMapper.selectList(
                    new LambdaQueryWrapper<FeeRuleStallRel>()
                            .select(FeeRuleStallRel::getRuleId)
                            .eq(FeeRuleStallRel::getCompanyId, UserContext.getLoginUser().getCompanyId() != null ? UserContext.getLoginUser().getCompanyId() : 0L)
                            .eq(FeeRuleStallRel::getStallId, stallId)
                            .eq(FeeRuleStallRel::getIsDelete, 0));
            if (!rels.isEmpty()) {
                List<Long> ruleIds = rels.stream().map(FeeRuleStallRel::getRuleId).toList();
                List<FeeRule> rules = feeRuleMapper.selectBatchIds(ruleIds);
                rules.stream()
                        .filter(r -> r != null && r.getFeeItemId() != null && r.getFeeItemId() == 1L)
                        .findFirst()
                        .ifPresent(r -> rentPeriodTypeMap.put(stallId, r.getPeriodType()));
            }
        }

        // 批量查租户名称
        Set<Long> tenantIds = result.getRecords().stream()
                .map(StallContract::getTenantId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> tenantNameMap = tenantIds.isEmpty() ? Collections.emptyMap()
                : tenantMapper.selectBatchIds(tenantIds).stream()
                        .collect(Collectors.toMap(StallTenant::getId, StallTenant::getTenantName));

        List<ContractVO> voList = result.getRecords().stream().map(c -> {
            ContractVO vo = new ContractVO();
            vo.setId(c.getId());
            vo.setContractNo(c.getContractNo());
            vo.setTenantId(c.getTenantId());
            vo.setStallId(c.getStallId());
            vo.setRentAmount(c.getRentAmount());
            vo.setDepositAmount(c.getDepositAmount());
            vo.setStartTime(c.getStartTime());
            vo.setEndTime(c.getEndTime());
            vo.setContractStatus(c.getContractStatus());
            vo.setContractStatusText(contractStatusText(c.getContractStatus()));
            vo.setAttachmentUrl(c.getAttachmentUrl());
            vo.setRemark(c.getRemark());
            vo.setCreateTime(c.getCreateTime());
            StallInfo stall = stallMap.get(c.getStallId());
            if (stall != null) {
                vo.setStallNumber(stall.getStallNumber());
                vo.setCategoryName(categoryNameMap.get(stall.getStallCategoryId()));
                vo.setMarketName(marketNameMap.get(stall.getMarketId()));
            }
            vo.setRentPeriodType(rentPeriodTypeMap.get(c.getStallId()));
            vo.setTenantName(tenantNameMap.get(c.getTenantId()));
            return vo;
        }).toList();
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    public ContractVO detail(Long id) {
        StallContract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new BizException("合同不存在或已删除");
        }
        ContractVO vo = new ContractVO();
        vo.setId(contract.getId());
        vo.setContractNo(contract.getContractNo());
        vo.setTenantId(contract.getTenantId());
        vo.setStallId(contract.getStallId());
        vo.setRentAmount(contract.getRentAmount());
        if (contract.getTenantId() != null) {
            StallTenant _t = tenantMapper.selectById(contract.getTenantId());
            if (_t != null) vo.setTenantName(_t.getTenantName());
        }
        vo.setDepositAmount(contract.getDepositAmount());
        vo.setStartTime(contract.getStartTime());
        vo.setEndTime(contract.getEndTime());
        vo.setContractStatus(contract.getContractStatus());
        vo.setContractStatusText(contractStatusText(contract.getContractStatus()));
        vo.setAttachmentUrl(contract.getAttachmentUrl());
        vo.setRemark(contract.getRemark());
        vo.setCreateTime(contract.getCreateTime());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(ContractAddDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        Long companyId = loginUser.getCompanyId();

        if (!tenantService.existsById(dto.getTenantId())) {
            throw new BizException("租户不存在或已删除");
        }

        StallInfo stall = stallMapper.selectById(dto.getStallId());
        if (stall == null) {
            throw new BizException("铺位不存在或已删除");
        }
        if (!Objects.equals(stall.getStatus(), CommonConst.STALL_STATUS_EMPTY)) {
            throw new BizException("铺位当前非空置状态，无法签订租赁合同");
        }

        if (dto.getEndTime() != null && dto.getStartTime() != null
                && !dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new BizException("租赁到期日期必须晚于开始日期");
        }

        Map<String, String> configValues = configService.getValuesByKeys(List.of(
                "contract.rent_editable", "contract.discount_editable"));
        boolean rentEditable = !"0".equals(configValues.get("contract.rent_editable"));
        boolean discountEditable = !"0".equals(configValues.get("contract.discount_editable"));
        if (!rentEditable) {
            if (dto.getRentAmount() == null) {
                throw new BizException("合同租金不能为空（请先为铺位绑定租金收费规则，系统自动带出）");
            }
            if (dto.getDepositAmount() == null) {
                throw new BizException("合同押金不能为空（请先为铺位绑定押金收费规则，系统自动带出）");
            }
        }
        if (!discountEditable) {
            if (dto.getWaiveMonths() != null || dto.getDiscountRate() != null || dto.getDeductAmount() != null) {
                throw new BizException("优惠参数禁止手动修改，请使用优惠策略自动带出");
            }
        }

        StallContract contract = new StallContract();
        contract.setCompanyId(companyId);
        contract.setContractNo("HT" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())
                + RandomUtil.randomNumbers(4));
        contract.setTenantId(dto.getTenantId());
        contract.setStallId(dto.getStallId());
        contract.setRentAmount(dto.getRentAmount() == null ? BigDecimal.ZERO : dto.getRentAmount());
        contract.setDepositAmount(dto.getDepositAmount() == null ? BigDecimal.ZERO : dto.getDepositAmount());
        contract.setStartTime(dto.getStartTime());
        contract.setEndTime(dto.getEndTime());
        contract.setContractStatus(CommonConst.CONTRACT_STATUS_SIGNING);
        contract.setAttachmentUrl(dto.getAttachmentUrl());
        contract.setRemark(dto.getRemark());
        contractMapper.insert(contract);
        // 更新铺位状态为已租赁
        StallInfo stallUpdate = new StallInfo();
        stallUpdate.setId(stall.getId());
        stallUpdate.setStatus(CommonConst.STALL_STATUS_RENTED);
        stallMapper.updateById(stallUpdate);

        // 插入押金流水
        // if (contract.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {// 押金不能为空
        //     insertDepositFlow(companyId, contract.getId(), contract.getStallId(), dto.getTenantId(),
        //             contract.getDepositAmount(), CommonConst.FLOW_TYPE_INCOME, "合同签订押金");
        // }
        // 插入租金流水
        // if (contract.getRentAmount().compareTo(BigDecimal.ZERO) > 0) {// 租金不能为空
        //     insertRentFlow(companyId, contract.getId(), contract.getStallId(), dto.getTenantId(),
        //             contract.getRentAmount(), CommonConst.FLOW_TYPE_INCOME, "合同签订租金");
        // }
        // 处理优惠申请
        boolean hasDiscount = dto.getPolicyId() != null || dto.getWaiveMonths() != null
                || dto.getDiscountRate() != null || dto.getDeductAmount() != null;
        if (hasDiscount) {
            discountApplyService.createForContract(contract.getId(), dto.getPolicyId(), dto.getWaiveMonths(),
                    dto.getDiscountRate(), dto.getDeductAmount(), dto.getDiscountRemark());
        } else {
            recvPayPlanService.generateByContract(contract.getId());
        }

        // 写入租金和押金业务费用流水
        writeRentAndDepositBizFeeBills(companyId, contract, dto);

        auditLogUtil.record(CommonConst.MODULE_LEASE_CONTRACT, CommonConst.OPER_TYPE_ADD,
                String.valueOf(contract.getId()), null, contract);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void terminate(ContractTerminateDTO dto) {
        StallContract contract = contractMapper.selectById(dto.getContractId());
        if (contract == null) {
            throw new BizException("合同不存在或已删除");
        }
        if (!Objects.equals(contract.getContractStatus(), CommonConst.CONTRACT_STATUS_EFFECTIVE)) {
            throw new BizException("合同当前状态不可退租");
        }

        StallContract contractUpdate = new StallContract();
        contractUpdate.setId(contract.getId());
        contractUpdate.setContractStatus(CommonConst.CONTRACT_STATUS_TERMINATED);
        contractMapper.updateById(contractUpdate);

        StallInfo stallUpdate = new StallInfo();
        stallUpdate.setId(contract.getStallId());
        stallUpdate.setStatus(CommonConst.STALL_STATUS_EMPTY);
        stallMapper.updateById(stallUpdate);

        if (contract.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
            insertDepositFlow(contract.getCompanyId(), contract.getId(), contract.getStallId(), contract.getTenantId(),
                    contract.getDepositAmount(), CommonConst.FLOW_TYPE_EXPENSE, "合同退租押金退还");
        }

        recvPayPlanService.redChainForContract(contract.getId());

        auditLogUtil.record(CommonConst.MODULE_LEASE_CONTRACT, CommonConst.OPER_TYPE_TERMINATE,
                String.valueOf(contract.getId()), contract, contractUpdate);
    }

    @Override
    public boolean hasEffectiveContractByTenant(Long tenantId) {
        Long count = contractMapper.selectCount(new LambdaQueryWrapper<StallContract>()
                .eq(StallContract::getTenantId, tenantId)
                .or().eq(StallContract::getContractStatus, CommonConst.CONTRACT_STATUS_SIGNING)
                .eq(StallContract::getContractStatus, CommonConst.CONTRACT_STATUS_EFFECTIVE));
        return count != null && count > 0;
    }

    @Override
    public boolean hasAnyContractByStall(Long stallId) {
        Long count = contractMapper.selectCount(new LambdaQueryWrapper<StallContract>()
                .eq(StallContract::getStallId, stallId));
        return count != null && count > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateContract(Long contractId) {
        StallContract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw new BizException("合同不存在或已删除");
        }
        if (contract.getContractStatus() != CommonConst.CONTRACT_STATUS_SIGNING) {
            throw new BizException("合同当前状态不可激活，仅签约中状态可转为生效中");
        }
        StallContract update = new StallContract();
        update.setId(contractId);
        update.setContractStatus(CommonConst.CONTRACT_STATUS_EFFECTIVE);
        contractMapper.updateById(update);
        StallInfo stall = stallMapper.selectById(contract.getStallId());
        if (stall != null && stall.getStatus() != CommonConst.STALL_STATUS_RENTED) {
            StallInfo stallUpdate = new StallInfo();
            stallUpdate.setId(contract.getStallId());
            stallUpdate.setStatus(CommonConst.STALL_STATUS_RENTED);
            stallMapper.updateById(stallUpdate);
        }
        log.info("合同激活成功：contractId={}, 签约中->生效中", contractId);
    }

    @Override
    public StallContract getContractById(Long contractId) {
        return contractMapper.selectById(contractId);
    }

    @Override
    public Map<Long, String> mapStallNumbers(Collection<Long> stallIds) {
        if (stallIds == null || stallIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return stallMapper.selectBatchIds(stallIds).stream()
                .collect(Collectors.toMap(StallInfo::getId, s -> s.getStallNumber() == null ? "-" : s.getStallNumber()));
    }

    @Override
    public Map<Long, String> mapTenantNames(Collection<Long> tenantIds) {
        if (tenantIds == null || tenantIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return tenantMapper.selectBatchIds(tenantIds).stream()
                .collect(Collectors.toMap(StallTenant::getId, StallTenant::getTenantName));
    }

    /**
     * 插入押金流水
     */
    private void insertDepositFlow(Long companyId, Long contractId, Long stallId, Long tenantId,
                                   BigDecimal amount, Integer flowType, String remark) {
        BizFinanceFlow flow = new BizFinanceFlow();
        flow.setCompanyId(companyId);
        flow.setBusinessType(CommonConst.BIZ_TYPE_DEPOSIT);
        flow.setBillId(String.valueOf(contractId));
        flow.setStallId(stallId);
        flow.setOriginalAmount(amount);
        flow.setDiscountAmount(BigDecimal.ZERO);
        flow.setRealAmount(amount);
        flow.setFlowType(flowType);
        flow.setStatus(1);
        flow.setFlowNo(flowNoGenerator.generate(companyId));
        flow.setRemark(remark + "-合同" + contractId);
        financeFlowMapper.insert(flow);
    }

    /**
     * 插入租金流水
     */
    private void insertRentFlow(Long companyId, Long contractId, Long stallId, Long tenantId,
                                BigDecimal amount, Integer flowType, String remark) {
        BizFinanceFlow flow = new BizFinanceFlow();
        flow.setCompanyId(companyId);
        flow.setBusinessType(CommonConst.BIZ_TYPE_RENT);
        flow.setBillId(String.valueOf(contractId));
        flow.setStallId(stallId);
        flow.setOriginalAmount(amount);
        flow.setDiscountAmount(BigDecimal.ZERO);
        flow.setRealAmount(amount);
        flow.setFlowType(flowType);
        flow.setStatus(1);
        flow.setFlowNo(flowNoGenerator.generate(companyId));
        flow.setRemark(remark + "-合同" + contractId);
        financeFlowMapper.insert(flow);
    }
    /**
     * 合同状态文本
     */
    private String contractStatusText(Integer status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case CommonConst.CONTRACT_STATUS_SIGNING -> "签约中";
            case CommonConst.CONTRACT_STATUS_EFFECTIVE -> "生效中";
            case CommonConst.CONTRACT_STATUS_TERMINATED -> "已退租";
            case CommonConst.CONTRACT_STATUS_EXPIRED -> "已到期";
            default -> "未知";
        };
    }
    /**
     * 写入租金和押金账单
     */
    private void writeRentAndDepositBizFeeBills(Long companyId, StallContract contract, ContractAddDTO dto) {
        log.info("写入租金和押金业务费用流水：contractId={}", contract.getId());
        if (contract.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
            String depositBillMonth = contract.getStartTime().toString().substring(0, 7);
            FeeRule depositRule = resolveFeeRule(companyId, contract.getStallId(), CommonConst.BIZ_TYPE_DEPOSIT);
            int depositPeriodType = depositRule != null && depositRule.getPeriodType() != null ? depositRule.getPeriodType() : CommonConst.PLAN_PERIOD_ONCE;
            writeBizFeeBillWithPlan(companyId, contract.getStallId(), depositBillMonth,
                CommonConst.BIZ_TYPE_DEPOSIT, contract.getDepositAmount(),
                BigDecimal.ZERO, BigDecimal.ZERO, depositPeriodType);
        }
        log.info("写入租金业务费用流水：contractId={}", contract.getId());
        // 查询租金收费规则，获取 periodType 决定账单周期
        FeeRule rentRule = resolveFeeRule(companyId, contract.getStallId(), CommonConst.BIZ_TYPE_RENT);
        if (rentRule == null) {
            log.error("未找到租金收费规则，跳过租金账单写入：companyId={}, stallId={}", companyId, contract.getStallId());
            return;
        }
        int rentPeriodType = rentRule.getPeriodType() != null ? rentRule.getPeriodType() : CommonConst.PLAN_PERIOD_MONTH;
        // periodType: 1=按年 2=按月 3=按日
        // 账单周期：按年用"yyyy"，按月用"yyyy-MM"，按日用"yyyy-MM-dd"
        int billPeriodType = (rentPeriodType == 1) ? CommonConst.PLAN_PERIOD_MONTH : rentPeriodType;

        int waiveMonths = dto.getWaiveMonths() != null ? dto.getWaiveMonths() : 0;
        BigDecimal discountRate = dto.getDiscountRate() != null
                ? dto.getDiscountRate().compareTo(BigDecimal.ZERO) <= 0
                    ? new BigDecimal("100") : dto.getDiscountRate()
                : new BigDecimal("100");
        BigDecimal deductAmount = dto.getDeductAmount() != null ? dto.getDeductAmount() : BigDecimal.ZERO;

        YearMonth start = YearMonth.from(contract.getStartTime());
        YearMonth end = YearMonth.from(contract.getEndTime());
        long totalMonths = ChronoUnit.MONTHS.between(start, end) + 1;
        long rentMonths = Math.max(totalMonths - waiveMonths, 0);
        if (rentMonths <= 0) {
            return;
        }
        log.info("写入租金业务费用流水：contractId={}, rentMonths={}, periodType={}", contract.getId(), rentMonths, rentPeriodType);

        if (rentPeriodType == 1) {
            // 按年收费：只生成1条年度账单
            BigDecimal annualOriginal = contract.getRentAmount() == null ? BigDecimal.ZERO : contract.getRentAmount();
            BigDecimal annualDiscount = annualOriginal.multiply(BigDecimal.ONE.subtract(discountRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))).max(BigDecimal.ZERO);
            BigDecimal annualReal = annualOriginal.subtract(annualDiscount).add(deductAmount).max(BigDecimal.ZERO);
            String billYear = start.getYear() + "";
            writeBizFeeBillWithPlan(companyId, contract.getStallId(), billYear,
                    CommonConst.BIZ_TYPE_RENT,
                    annualOriginal, annualDiscount, deductAmount,
                    rentPeriodType);
        } else {
            // 按月收费：生成多条月度账单
            BigDecimal monthlyRent = contract.getRentAmount() == null ? BigDecimal.ZERO : contract.getRentAmount();
            BigDecimal monthlyOriginal = monthlyRent.multiply(discountRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            BigDecimal perMonthDeduct = BigDecimal.ZERO;
            BigDecimal remainderDeduct = BigDecimal.ZERO;
            if (deductAmount.compareTo(BigDecimal.ZERO) > 0 && rentMonths > 0) {
                perMonthDeduct = deductAmount.divide(new BigDecimal(rentMonths), 2, RoundingMode.HALF_UP);
                remainderDeduct = deductAmount.subtract(perMonthDeduct.multiply(new BigDecimal(rentMonths - 1)));
            }
            log.info("写入租金业务费用流水：contractId={}, perMonthDeduct={}, remainderDeduct={}",
                    contract.getId(), perMonthDeduct, remainderDeduct);
            long index = 0;
            for (YearMonth ym = start; !ym.isAfter(end); ym = ym.plusMonths(1)) {
                if (index < waiveMonths) {
                    index++;
                    continue;
                }
                boolean lastMonth = index == totalMonths - 1;
                BigDecimal deduct = lastMonth ? remainderDeduct : perMonthDeduct;
                BigDecimal realAmount = monthlyOriginal.subtract(deduct).max(BigDecimal.ZERO);
                BigDecimal discountAmount = monthlyOriginal.subtract(realAmount).max(BigDecimal.ZERO);

                writeBizFeeBillWithPlan(companyId, contract.getStallId(), ym.toString(),
                        CommonConst.BIZ_TYPE_RENT,
                        monthlyOriginal, discountAmount, BigDecimal.ZERO,
                        CommonConst.PLAN_PERIOD_MONTH);
                index++;
            }
        }
    }

    private void writeBizFeeBillWithPlan(Long companyId, Long stallId, String billMonth,
                                          String bizType, BigDecimal originalAmount,
                                          BigDecimal discountAmount, BigDecimal adjustAmount,
                                          int periodType) {
        Long existing = bizFeeBillMapper.selectCount(new LambdaQueryWrapper<BizFeeBill>()
                .eq(BizFeeBill::getCompanyId, companyId)
                .eq(BizFeeBill::getStallId, stallId)
                .eq(BizFeeBill::getBillMonth, billMonth)
                .eq(BizFeeBill::getBizType, bizType));
        if (existing != null && existing > 0) {
            log.info("统一账单已存在，跳过写入：companyId={}, stallId={}, month={}, bizType={}",
                    companyId, stallId, billMonth, bizType);
            return;
        }

        // 根据铺位绑定的收费规则和 bizType 查询对应的 rule_id
        Long ruleId = resolveRuleId(companyId, stallId, bizType);
        if (ruleId == null) {
            log.error("未找到铺位绑定的收费规则：companyId={}, stallId={}, bizType={}", companyId, stallId, bizType);
            return;
        }

        BizFeeBill unifiedBill = new BizFeeBill();
        unifiedBill.setCompanyId(companyId);
        unifiedBill.setBizType(bizType);
        unifiedBill.setStallId(stallId);
        unifiedBill.setBillMonth(billMonth);
        unifiedBill.setRuleId(ruleId);
        unifiedBill.setOriginalAmount(originalAmount);
        unifiedBill.setDiscountAmount(discountAmount);
        unifiedBill.setAdjustAmount(adjustAmount);
        unifiedBill.setRealAmount(originalAmount.subtract(discountAmount).add(adjustAmount));
        unifiedBill.setPayStatus(CommonConst.BILL_PAY_STATUS_UNPAID);
        unifiedBill.setPeriodType(periodType);
        unifiedBill.setLockedFlag(1);
        unifiedBill.setCreateBy(UserContext.getUserIdOrZero());

        try {
            bizFeeBillMapper.insert(unifiedBill);
        } catch (Exception ex) {
            log.warn("统一账单写入冲突（唯一键 uk_stall_rule_period），查询已有记录：companyId={}, stallId={}, month={}, bizType={}",
                    companyId, stallId, billMonth, bizType);
            // 使用唯一键字段查询：company_id, stall_id, rule_id, bill_month, is_delete
            // 由于 rule_id 可能为 null，使用 selectList 获取所有匹配记录后取第一条
            List<BizFeeBill> existingBills = bizFeeBillMapper.selectList(new LambdaQueryWrapper<BizFeeBill>()
                    .eq(BizFeeBill::getCompanyId, companyId)
                    .eq(BizFeeBill::getStallId, stallId)
                    .eq(BizFeeBill::getBillMonth, billMonth)
                    .eq(BizFeeBill::getIsDelete, 0));
            if (existingBills == null || existingBills.isEmpty()) {
                log.error("统一账单写入失败且未查到已有记录：companyId={}, stallId={}, month={}, bizType={}",
                        companyId, stallId, billMonth, bizType);
                return;
            }
            unifiedBill = existingBills.get(0);
            log.info("复用已有统一账单：companyId={}, stallId={}, month={}, bizType={}, billId={}",
                    companyId, stallId, billMonth, bizType, unifiedBill.getId());
        }

        Long planId = recvPayPlanService.generatePlanForBizFeeBill(unifiedBill);
        if (planId != null) {
            bizFeeBillMapper.update(null, new LambdaUpdateWrapper<BizFeeBill>()
                    .eq(BizFeeBill::getId, unifiedBill.getId())
                    .set(BizFeeBill::getPlanId, planId));
            BillPlanRel rel = new BillPlanRel();
            rel.setCompanyId(companyId);
            rel.setBillType(bizType);
            rel.setBillId(unifiedBill.getId());
            rel.setPlanId(planId);
            rel.setSplitAmount(unifiedBill.getRealAmount());
            billPlanRelMapper.insert(rel);
        }
        log.info("租赁合同统一账单写入成功：companyId={}, stallId={}, month={}, bizType={}, billId={}, ruleId={}",
                companyId, stallId, billMonth, bizType, unifiedBill.getId(), ruleId);
    }

    /**
     * 根据铺位绑定的收费规则和 bizType 解析对应的 rule_id
     * <p>通过 biz_fee_rule_stall_rel 查询铺位绑定的规则，
     * 关联 biz_fee_rule 筛选匹配的 feeItemId，返回正确的 rule_id
     */
    private Long resolveRuleId(Long companyId, Long stallId, String bizType) {
        // bizType → feeItemId 映射
        Long feeItemId;
        if (CommonConst.BIZ_TYPE_RENT.equals(bizType)) {
            feeItemId = 1L; // 租金
        } else if (CommonConst.BIZ_TYPE_DEPOSIT.equals(bizType)) {
            feeItemId = 5L; // 押金
        } else {
            log.warn("不支持的 bizType：{}", bizType);
            return null;
        }
        // 查询铺位绑定的规则 ID 列表
        List<Long> ruleIds = feeRuleStallRelMapper.selectList(new LambdaQueryWrapper<FeeRuleStallRel>()
                .select(FeeRuleStallRel::getRuleId)
                .eq(FeeRuleStallRel::getCompanyId, companyId)
                .eq(FeeRuleStallRel::getStallId, stallId)
                .eq(FeeRuleStallRel::getIsDelete, 0))
                .stream().map(FeeRuleStallRel::getRuleId).collect(Collectors.toList());
        if (ruleIds == null || ruleIds.isEmpty()) {
            log.warn("铺位未绑定收费规则：companyId={}, stallId={}", companyId, stallId);
            return null;
        }
        // 关联 biz_fee_rule 筛选匹配的 feeItemId
        List<FeeRule> rules = feeRuleMapper.selectBatchIds(ruleIds);
        for (FeeRule rule : rules) {
            if (rule != null && feeItemId.equals(rule.getFeeItemId())) {
                return rule.getId();
            }
        }
        log.warn("未找到匹配的收费规则：companyId={}, stallId={}, feeItemId={}", companyId, stallId, feeItemId);
        return null;
    }

    /**
     * 查询铺位绑定的收费规则信息（ruleId + periodType）
     * <p>用于根据收费周期决定账单生成策略
     */
    private FeeRule resolveFeeRule(Long companyId, Long stallId, String bizType) {
        Long feeItemId;
        if (CommonConst.BIZ_TYPE_RENT.equals(bizType)) {
            feeItemId = 1L;
        } else if (CommonConst.BIZ_TYPE_DEPOSIT.equals(bizType)) {
            feeItemId = 5L;
        } else {
            return null;
        }
        List<Long> ruleIds = feeRuleStallRelMapper.selectList(new LambdaQueryWrapper<FeeRuleStallRel>()
                .select(FeeRuleStallRel::getRuleId)
                .eq(FeeRuleStallRel::getCompanyId, companyId)
                .eq(FeeRuleStallRel::getStallId, stallId)
                .eq(FeeRuleStallRel::getIsDelete, 0))
                .stream().map(FeeRuleStallRel::getRuleId).collect(Collectors.toList());
        if (ruleIds == null || ruleIds.isEmpty()) {
            return null;
        }
        List<FeeRule> rules = feeRuleMapper.selectBatchIds(ruleIds);
        for (FeeRule rule : rules) {
            if (rule != null && feeItemId.equals(rule.getFeeItemId())) {
                return rule;
            }
        }
        return null;
    }
}
