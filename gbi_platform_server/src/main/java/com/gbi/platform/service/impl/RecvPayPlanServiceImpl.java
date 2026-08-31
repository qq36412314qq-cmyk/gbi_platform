package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.result.ResultCode;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.PlanAdjustDTO;
import com.gbi.platform.dto.PlanGenerateDTO;
import com.gbi.platform.dto.PlanTerminateDTO;
import com.gbi.platform.dto.RecvPayPlanQueryDTO;
import com.gbi.platform.dto.ReconcileQueryDTO;
import com.gbi.platform.entity.BillPlanRel;
import com.gbi.platform.entity.BizDiscountApply;
import com.gbi.platform.entity.BizFinanceFlow;
import com.gbi.platform.entity.BizFinanceWriteoff;
import com.gbi.platform.entity.BizRecvPayPlan;
import com.gbi.platform.entity.WaterElecBill;
import com.gbi.platform.entity.StallContract;
import com.gbi.platform.entity.PropertyFeeBill;
import com.gbi.platform.entity.BizFeeBill;
import com.gbi.platform.mapper.StallContractMapper;
import com.gbi.platform.mapper.BillPlanRelMapper;
import com.gbi.platform.mapper.BizDiscountApplyMapper;
import com.gbi.platform.mapper.BizFinanceFlowMapper;
import com.gbi.platform.mapper.BizFinanceWriteoffMapper;
import com.gbi.platform.mapper.BizRecvPayPlanMapper;
import com.gbi.platform.service.ConfigService;
import com.gbi.platform.service.FlowEngineService;
import com.gbi.platform.service.LeaseContractService;
import com.gbi.platform.service.RecvPayPlanService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.BillPlanRelVO;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.RecvPayPlanDetailVO;
import com.gbi.platform.vo.RecvPayPlanVO;
import com.gbi.platform.vo.ReconcileDiffVO;
import com.gbi.platform.vo.WriteoffVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 应收应付计划服务实现（全系统唯一应收应付台账）
 * 关键规则：计划生成幂等（uk_source_period）；调账/作废超阈值走统一审批引擎；
 * 合同退租红冲链（反向冲销计划 + 负向核销 + 反向资金流水）；自动对账以计划为权威源
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecvPayPlanServiceImpl implements RecvPayPlanService {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter SEQ_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter EXPORT_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SNAPSHOT_KEY_WAIVE = "waiveMonths";
    private static final String SNAPSHOT_KEY_RATE = "discountRate";
    private static final String SNAPSHOT_KEY_DEDUCT = "deductAmount";

    private final BizRecvPayPlanMapper planMapper;
    private final StallContractMapper stallContractMapper;
    private final BizFinanceWriteoffMapper writeoffMapper;
    private final BillPlanRelMapper billPlanRelMapper;
    private final BizDiscountApplyMapper discountApplyMapper;
    private final BizFinanceFlowMapper financeFlowMapper;
    private final LeaseContractService leaseContractService;
    private final FlowEngineService flowEngineService;
    private final ConfigService configService;
    private final AuditLogUtil auditLogUtil;
    private final ObjectMapper objectMapper;

    @Value("")
    private String uploadDir;

    @Override
    public PageVO<RecvPayPlanVO> page(RecvPayPlanQueryDTO dto) {
        LambdaQueryWrapper<BizRecvPayPlan> wrapper = new LambdaQueryWrapper<BizRecvPayPlan>()
                .eq(dto.getDirection() != null, BizRecvPayPlan::getDirection, dto.getDirection())
                .eq(StringUtils.hasText(dto.getBizType()), BizRecvPayPlan::getBizType, dto.getBizType())
                .eq(dto.getPlanStatus() != null, BizRecvPayPlan::getPlanStatus, dto.getPlanStatus())
                .eq(dto.getRedFlag() != null, BizRecvPayPlan::getRedFlag, dto.getRedFlag())
                .eq(dto.getStallId() != null, BizRecvPayPlan::getStallId, dto.getStallId())
                .eq(StringUtils.hasText(dto.getPeriodNo()), BizRecvPayPlan::getPeriodNo, dto.getPeriodNo())
                .orderByDesc(BizRecvPayPlan::getCreateTime);
        Page<BizRecvPayPlan> page = planMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        List<BizRecvPayPlan> records = page.getRecords();
        Map<Long, String> stallNos = leaseContractService.mapStallNumbers(
                records.stream().map(BizRecvPayPlan::getStallId).filter(java.util.Objects::nonNull).collect(Collectors.toSet()));
        Map<Long, String> tenantNames = leaseContractService.mapTenantNames(
                records.stream().map(BizRecvPayPlan::getTenantId).filter(java.util.Objects::nonNull).collect(Collectors.toSet()));
        List<RecvPayPlanVO> vos = records.stream().map(p -> toVO(p, stallNos, tenantNames)).collect(Collectors.toList());
        return new PageVO<>(vos, page.getTotal(), dto.getPageNum().longValue(), dto.getPageSize().longValue(), page.getPages());
    }

    @Override
    public RecvPayPlanDetailVO detail(Long planId) {
        BizRecvPayPlan plan = planMapper.selectById(planId);
        if (plan == null) { throw new BizException("计划不存在"); }
        Map<Long, String> stallNos = leaseContractService.mapStallNumbers(Collections.singleton(plan.getStallId()));
        Map<Long, String> tenantNames = leaseContractService.mapTenantNames(Collections.singleton(plan.getTenantId()));
        List<BillPlanRel> rels = billPlanRelMapper.selectList(new LambdaQueryWrapper<BillPlanRel>().eq(BillPlanRel::getPlanId, planId));
        List<BillPlanRelVO> relVOs = rels.stream().map(r -> {
            BillPlanRelVO vo = new BillPlanRelVO();
            vo.setId(r.getId()); vo.setBillType(r.getBillType()); vo.setBillId(r.getBillId());
            vo.setPlanId(r.getPlanId()); vo.setSplitAmount(r.getSplitAmount());
            return vo;
        }).collect(Collectors.toList());
        List<BizFinanceWriteoff> writeoffs = writeoffMapper.selectList(
                new LambdaQueryWrapper<BizFinanceWriteoff>().eq(BizFinanceWriteoff::getPlanId, planId).orderByDesc(BizFinanceWriteoff::getCreateTime));
        List<WriteoffVO> writeoffVOs = writeoffs.stream().map(w -> {
            WriteoffVO vo = new WriteoffVO();
            vo.setId(w.getId()); vo.setFinanceFlowId(w.getFinanceFlowId()); vo.setPlanId(w.getPlanId());
            vo.setBillType(w.getBillType()); vo.setBillId(w.getBillId());
            vo.setWriteoffAmount(w.getWriteoffAmount()); vo.setWriteoffType(w.getWriteoffType());
            vo.setWriteoffTypeText(writeoffTypeText(w.getWriteoffType()));
            vo.setRemark(w.getRemark()); vo.setCreateTime(w.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
        RecvPayPlanDetailVO result = new RecvPayPlanDetailVO();
        result.setPlan(toVO(plan, stallNos, tenantNames));
        result.setBillPlanRels(relVOs);
        result.setWriteoffs(writeoffVOs);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateByContract(Long contractId) {
        StallContract contract = leaseContractService.getContractById(contractId);
        if (contract == null) { throw new BizException("合同不存在"); }
        int waiveMonths = 0;
        BigDecimal discountRate = new BigDecimal("100.00");
        BigDecimal deductAmount = BigDecimal.ZERO;
        BizDiscountApply apply = discountApplyMapper.selectOne(new LambdaQueryWrapper<BizDiscountApply>()
                .eq(BizDiscountApply::getSourceType, CommonConst.PLAN_SOURCE_CONTRACT)
                .eq(BizDiscountApply::getSourceId, String.valueOf(contractId))
                .eq(BizDiscountApply::getApplyStatus, 2)
                .orderByDesc(BizDiscountApply::getCreateTime).last("LIMIT 1"));
        if (apply != null && StringUtils.hasText(apply.getPolicySnapshot())) {
            try {
                JsonNode node = objectMapper.readTree(apply.getPolicySnapshot());
                waiveMonths = node.path(SNAPSHOT_KEY_WAIVE).asInt(0);
                BigDecimal rate = new BigDecimal(node.path(SNAPSHOT_KEY_RATE).asText("100.00"));
                discountRate = rate.compareTo(BigDecimal.ZERO) <= 0 ? new BigDecimal("100.00") : rate;
                deductAmount = new BigDecimal(node.path(SNAPSHOT_KEY_DEDUCT).asText("0"));
            } catch (IOException | NumberFormatException e) {
                log.warn("优惠申请快照解析失败，按原价生成计划 applyId={}", apply.getId(), e);
            }
        }
        insertPlanQuietly(contract, CommonConst.PLAN_BIZ_DEPOSIT, contract.getStartTime().toString(),
                CommonConst.PLAN_PERIOD_ONCE, contract.getStartTime(),
                contract.getDepositAmount() == null ? BigDecimal.ZERO : contract.getDepositAmount(), BigDecimal.ZERO);
        YearMonth start = YearMonth.from(contract.getStartTime());
        YearMonth end = YearMonth.from(contract.getEndTime());
        long totalMonths = ChronoUnit.MONTHS.between(start, end) + 1;
        long rentMonths = Math.max(totalMonths - waiveMonths, 0);
        if (rentMonths <= 0) { return; }
        BigDecimal monthlyRent = contract.getRentAmount() == null ? BigDecimal.ZERO : contract.getRentAmount();
        BigDecimal monthlyOriginal = monthlyRent.multiply(discountRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyDiscount = monthlyRent.subtract(monthlyOriginal);
        BigDecimal perMonthDeduct = deductAmount.divide(new BigDecimal(rentMonths), 2, RoundingMode.HALF_UP);
        BigDecimal remainderDeduct = deductAmount.subtract(perMonthDeduct.multiply(new BigDecimal(rentMonths - 1)));
        long index = 0;
        for (YearMonth ym = start; !ym.isAfter(end); ym = ym.plusMonths(1)) {
            if (index < waiveMonths) { index++; continue; }
            boolean lastMonth = index == totalMonths - 1;
            BigDecimal deduct = lastMonth ? remainderDeduct : perMonthDeduct;
            BigDecimal original = monthlyOriginal.subtract(deduct).max(BigDecimal.ZERO);
            LocalDate dueDate = index == 0 ? contract.getStartTime() : ym.atDay(1);
            insertPlanQuietly(contract, CommonConst.PLAN_BIZ_RENT, ym.format(MONTH_FMT),
                    CommonConst.PLAN_PERIOD_MONTH, dueDate, original, monthlyDiscount);
            index++;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjust(PlanAdjustDTO dto) {
        BizRecvPayPlan plan = planMapper.selectById(dto.getPlanId());
        if (plan == null) { throw new BizException("计划不存在"); }
        checkPlanAdjustable(plan);
        BigDecimal threshold = readThreshold(CommonConst.CONFIG_PLAN_ADJUST_AMOUNT_LIMIT, new BigDecimal("2000.00"));
        if (dto.getAdjustAmount().abs().compareTo(threshold) > 0) {
            flowEngineService.submit(CommonConst.FLOW_DEF_PLAN_ADJUST, "plan",
                    String.valueOf(plan.getId()), buildAdjustTitle(plan.getId(), dto.getAdjustAmount(), dto.getRemark()));
            auditLogUtil.record(CommonConst.MODULE_PLAN, CommonConst.OPER_TYPE_SUBMIT, String.valueOf(plan.getId()), null, "调账超阈值提交审批：" + dto.getAdjustAmount());
            return;
        }
        applyAdjust(plan, dto.getAdjustAmount(), dto.getRemark());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void terminatePlan(PlanTerminateDTO dto) {
        BizRecvPayPlan plan = planMapper.selectById(dto.getPlanId());
        if (plan == null) { throw new BizException("计划不存在"); }
        if (plan.getPlanStatus() == CommonConst.PLAN_STATUS_VOID || plan.getPlanStatus() == CommonConst.PLAN_STATUS_TERMINATED
                || plan.getRedFlag() == CommonConst.PLAN_RED_FLAG_REVERSAL) {
            throw new BizException("该计划已作废/终止或为红冲计划，禁止重复操作");
        }
        BigDecimal threshold = readThreshold(CommonConst.CONFIG_PLAN_ADJUST_AMOUNT_LIMIT, new BigDecimal("2000.00"));
        if (plan.getUnpaidAmount().compareTo(threshold) > 0) {
            flowEngineService.submit(CommonConst.FLOW_DEF_PLAN_ADJUST, "plan",
                    String.valueOf(plan.getId()), buildVoidTitle(plan.getId(), dto.getRemark()));
            auditLogUtil.record(CommonConst.MODULE_PLAN, CommonConst.OPER_TYPE_SUBMIT, String.valueOf(plan.getId()), null, "计划作废超阈值提交审批");
            return;
        }
        voidPlan(plan, dto.getRemark());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyApprovedAdjust(Long planId, BigDecimal adjustAmount, String remark) {
        BizRecvPayPlan plan = planMapper.selectById(planId);
        if (plan == null) { throw new BizException("计划不存在"); }
        checkPlanAdjustable(plan);
        applyAdjust(plan, adjustAmount, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyApprovedVoid(Long planId, String remark) {
        BizRecvPayPlan plan = planMapper.selectById(planId);
        if (plan == null) { throw new BizException("计划不存在"); }
        if (plan.getPlanStatus() == CommonConst.PLAN_STATUS_VOID || plan.getPlanStatus() == CommonConst.PLAN_STATUS_TERMINATED
                || plan.getRedFlag() == CommonConst.PLAN_RED_FLAG_REVERSAL) {
            throw new BizException("该计划已作废/终止或为红冲计划，禁止重复操作");
        }
        voidPlan(plan, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void redChainForContract(Long contractId) {
        List<BizRecvPayPlan> plans = planMapper.selectList(new LambdaQueryWrapper<BizRecvPayPlan>()
                .eq(BizRecvPayPlan::getSourceType, CommonConst.PLAN_SOURCE_CONTRACT)
                .eq(BizRecvPayPlan::getSourceId, String.valueOf(contractId))
                .eq(BizRecvPayPlan::getRedFlag, CommonConst.PLAN_RED_FLAG_NORMAL)
                .in(BizRecvPayPlan::getPlanStatus, CommonConst.PLAN_STATUS_PENDING, CommonConst.PLAN_STATUS_PART, CommonConst.PLAN_STATUS_OVERDUE));
        for (BizRecvPayPlan plan : plans) { redChainPlan(plan); }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean writeOff(Long planId, Long financeFlowId, BigDecimal amount, String billType, Long billId,
                            int writeoffType, String remark) {
        BizRecvPayPlan plan = planMapper.selectById(planId);
        if (plan == null) { log.warn("核销跳过：计划不存在 planId={}", planId); return false; }
        BigDecimal paid = plan.getPaidAmount().add(amount);
        BigDecimal unpaid = plan.getPlanAmount().subtract(paid);
        int status;
        if (unpaid.compareTo(BigDecimal.ZERO) <= 0) { status = CommonConst.PLAN_STATUS_DONE; if (unpaid.compareTo(BigDecimal.ZERO) < 0) { unpaid = BigDecimal.ZERO; } }
        else if (paid.compareTo(BigDecimal.ZERO) != 0) { status = CommonConst.PLAN_STATUS_PART; }
        else { status = CommonConst.PLAN_STATUS_PENDING; }
        planMapper.update(null, new LambdaUpdateWrapper<BizRecvPayPlan>()
                .eq(BizRecvPayPlan::getId, planId)
                .set(BizRecvPayPlan::getPaidAmount, paid)
                .set(BizRecvPayPlan::getUnpaidAmount, unpaid)
                .set(BizRecvPayPlan::getPlanStatus, status));
        BizFinanceWriteoff writeoff = new BizFinanceWriteoff();
        writeoff.setCompanyId(plan.getCompanyId());
        writeoff.setFinanceFlowId(financeFlowId);
        writeoff.setPlanId(planId);
        writeoff.setBillType(billType);
        writeoff.setBillId(billId);
        writeoff.setWriteoffAmount(amount);
        writeoff.setWriteoffType(writeoffType);
        writeoff.setRemark(remark);
        writeoff.setCreateBy(UserContext.getUserIdOrZero());
        writeoffMapper.insert(writeoff);
        if (financeFlowId != null) {
            financeFlowMapper.update(null, new LambdaUpdateWrapper<BizFinanceFlow>()
                    .eq(BizFinanceFlow::getId, financeFlowId)
                    .set(BizFinanceFlow::getPlanId, planId));
        }
        auditLogUtil.record(CommonConst.MODULE_WRITEOFF, writeoffTypeText(writeoffType), String.valueOf(planId), null,
                "核销金额=" + amount + (billType == null ? "" : "，账单=" + billType + "/" + billId));
        return true;
    }

    @Override
    public PageVO<ReconcileDiffVO> reconcile(ReconcileQueryDTO dto) {
        LambdaQueryWrapper<BizRecvPayPlan> wrapper = new LambdaQueryWrapper<BizRecvPayPlan>()
                .eq(BizRecvPayPlan::getRedFlag, CommonConst.PLAN_RED_FLAG_NORMAL)
                .eq(dto.getStallId() != null, BizRecvPayPlan::getStallId, dto.getStallId())
                .eq(StringUtils.hasText(dto.getPeriodNo()), BizRecvPayPlan::getPeriodNo, dto.getPeriodNo())
                .orderByAsc(BizRecvPayPlan::getPlanNo);
        Page<BizRecvPayPlan> page = planMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        List<BizRecvPayPlan> records = page.getRecords();
        Map<Long, BigDecimal> writeoffSumMap = new HashMap<>();
        if (!records.isEmpty()) {
            List<Long> planIds = records.stream().map(BizRecvPayPlan::getId).collect(Collectors.toList());
            List<BizFinanceWriteoff> writeoffs = writeoffMapper.selectList(new LambdaQueryWrapper<BizFinanceWriteoff>().in(BizFinanceWriteoff::getPlanId, planIds));
            for (BizFinanceWriteoff w : writeoffs) { writeoffSumMap.merge(w.getPlanId(), w.getWriteoffAmount(), BigDecimal::add); }
        }
        List<ReconcileDiffVO> diffs = new ArrayList<>();
        for (BizRecvPayPlan p : records) {
            ReconcileDiffVO vo = new ReconcileDiffVO();
            vo.setPlanId(p.getId()); vo.setPlanNo(p.getPlanNo()); vo.setStallId(p.getStallId());
            vo.setPeriodNo(p.getPeriodNo()); vo.setPlanAmount(p.getPlanAmount());
            vo.setPaidAmount(p.getPaidAmount()); vo.setUnpaidAmount(p.getUnpaidAmount());
            BigDecimal writeoffSum = writeoffSumMap.getOrDefault(p.getId(), BigDecimal.ZERO);
            vo.setWriteoffSum(writeoffSum);
            BigDecimal computedRemaining = p.getPlanAmount().subtract(writeoffSum).max(BigDecimal.ZERO);
            vo.setComputedRemaining(computedRemaining);
            vo.setDiff(computedRemaining.subtract(p.getUnpaidAmount()));
            diffs.add(vo);
        }
        return new PageVO<>(diffs, page.getTotal(), dto.getPageNum().longValue(), dto.getPageSize().longValue(), page.getPages());
    }

    @Override
    public String export(RecvPayPlanQueryDTO dto) {
        LambdaQueryWrapper<BizRecvPayPlan> wrapper = new LambdaQueryWrapper<BizRecvPayPlan>()
                .eq(dto.getDirection() != null, BizRecvPayPlan::getDirection, dto.getDirection())
                .eq(StringUtils.hasText(dto.getBizType()), BizRecvPayPlan::getBizType, dto.getBizType())
                .eq(dto.getPlanStatus() != null, BizRecvPayPlan::getPlanStatus, dto.getPlanStatus())
                .eq(dto.getRedFlag() != null, BizRecvPayPlan::getRedFlag, dto.getRedFlag())
                .eq(dto.getStallId() != null, BizRecvPayPlan::getStallId, dto.getStallId())
                .eq(StringUtils.hasText(dto.getPeriodNo()), BizRecvPayPlan::getPeriodNo, dto.getPeriodNo())
                .last("LIMIT 50000");
        List<BizRecvPayPlan> list = planMapper.selectList(wrapper);
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("计划ID,计划编号,方向,业务类型,来源单据,摊位ID,期次,应收日期,原价,优惠,调账,计划应收,已收,未收,状态,红冲标记,溯源计划ID,逾期天数,备注,创建时间");
        for (BizRecvPayPlan p : list) {
            sb.append(p.getId()).append(',').append(escapeCsv(p.getPlanNo())).append(',')
                    .append(p.getDirection() == CommonConst.PLAN_DIRECTION_RECEIVE ? "应收" : "应付").append(',')
                    .append(escapeCsv(p.getBizType())).append(',')
                    .append(escapeCsv(p.getSourceType() == null ? "" : p.getSourceType() + ":" + p.getSourceId())).append(',')
                    .append(p.getStallId() == null ? "" : p.getStallId()).append(',')
                    .append(escapeCsv(p.getPeriodNo())).append(',').append(p.getDueDate()).append(',')
                    .append(p.getOriginalAmount()).append(',').append(p.getDiscountAmount()).append(',')
                    .append(p.getAdjustAmount()).append(',').append(p.getPlanAmount()).append(',')
                    .append(p.getPaidAmount()).append(',').append(p.getUnpaidAmount()).append(',')
                    .append(escapeCsv(planStatusText(p.getPlanStatus()))).append(',')
                    .append(p.getRedFlag() == CommonConst.PLAN_RED_FLAG_REVERSAL ? "红冲" : "正常").append(',')
                    .append(p.getOrigPlanId() == null ? "" : p.getOrigPlanId()).append(',')
                    .append(p.getOverdueDays()).append(',')
                    .append(escapeCsv(p.getRemark())).append(',')
                    .append(p.getCreateTime() == null ? "" : EXPORT_TIME_FMT.format(p.getCreateTime())).append('\n');
        }
        String fileName = "recv_pay_plan_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
        try {
            Path exportDir = Paths.get(uploadDir, "export");
            Files.createDirectories(exportDir);
            Files.writeString(exportDir.resolve(fileName), sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) { log.error("计划导出失败", e); throw new BizException("导出失败，请稍后重试"); }
        auditLogUtil.record(CommonConst.MODULE_PLAN, CommonConst.OPER_TYPE_EXPORT, fileName, null, null);
        return "/upload/export/" + fileName;
    }

    /* ------------------------------ 内部方法 ------------------------------ */

    private void insertPlanQuietly(StallContract contract, String bizType, String periodNo, int periodType,
                                   LocalDate dueDate, BigDecimal original, BigDecimal discount) {
        Long exists = planMapper.selectCount(new LambdaQueryWrapper<BizRecvPayPlan>()
                .eq(BizRecvPayPlan::getCompanyId, contract.getCompanyId())
                .eq(BizRecvPayPlan::getSourceType, CommonConst.PLAN_SOURCE_CONTRACT)
                .eq(BizRecvPayPlan::getSourceId, String.valueOf(contract.getId()))
                .eq(BizRecvPayPlan::getPeriodNo, periodNo));
        if (exists != null && exists > 0) { return; }
        BizRecvPayPlan plan = new BizRecvPayPlan();
        plan.setCompanyId(contract.getCompanyId());
        plan.setPlanNo(buildPlanNo(bizType, contract.getCompanyId()));
        plan.setDirection(CommonConst.PLAN_DIRECTION_RECEIVE);
        plan.setBizType(bizType);
        plan.setSourceType(CommonConst.PLAN_SOURCE_CONTRACT);
        plan.setSourceId(String.valueOf(contract.getId()));
        plan.setStallId(contract.getStallId());
        plan.setTenantId(contract.getTenantId());
        plan.setMerchantId(contract.getMerchantId());
        plan.setPeriodNo(periodNo);
        plan.setPeriodType(periodType);
        plan.setDueDate(dueDate);
        plan.setOriginalAmount(original);
        plan.setDiscountAmount(discount);
        plan.setAdjustAmount(BigDecimal.ZERO);
        plan.setPlanAmount(original.subtract(discount).max(BigDecimal.ZERO));
        plan.setPaidAmount(BigDecimal.ZERO);
        plan.setUnpaidAmount(plan.getPlanAmount());
        plan.setPlanStatus(CommonConst.PLAN_STATUS_PENDING);
        plan.setRedFlag(CommonConst.PLAN_RED_FLAG_NORMAL);
        plan.setOverdueDays(0);
        plan.setCreateBy(UserContext.getUserIdOrZero());
        try { planMapper.insert(plan); }
        catch (DuplicateKeyException e) { log.info("计划已存在，幂等跳过 bizType={} period={} contractId={}", bizType, periodNo, contract.getId()); }
    }

    private String buildPlanNo(String bizType, Long companyId) {
        String prefix = "AR-" + bizType.toUpperCase() + "-" + companyId + "-" + LocalDate.now().format(SEQ_DATE_FMT) + "-";
        return prefix + String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
    }

    private void applyAdjust(BizRecvPayPlan plan, BigDecimal adjustAmount, String remark) {
        BigDecimal beforeAmount = plan.getPlanAmount();
        plan.setAdjustAmount(adjustAmount);
        plan.setPlanAmount(plan.getOriginalAmount().subtract(plan.getDiscountAmount()).add(adjustAmount).max(BigDecimal.ZERO));
        plan.setUnpaidAmount(plan.getPlanAmount().subtract(plan.getPaidAmount()).max(BigDecimal.ZERO));
        if (plan.getUnpaidAmount().compareTo(BigDecimal.ZERO) <= 0 && plan.getPlanAmount().compareTo(BigDecimal.ZERO) > 0) {
            plan.setPlanStatus(CommonConst.PLAN_STATUS_DONE);
        }
        plan.setRemark(StringUtils.hasText(remark) ? remark : plan.getRemark());
        planMapper.updateById(plan);
        Map<String, Object> before = new HashMap<>(); before.put("planAmount", beforeAmount);
        Map<String, Object> after = new HashMap<>();
        after.put("planAmount", plan.getPlanAmount()); after.put("adjustAmount", adjustAmount); after.put("remark", remark);
        auditLogUtil.record(CommonConst.MODULE_PLAN, CommonConst.OPER_TYPE_ADJUST, String.valueOf(plan.getId()), before, after);
    }

    private void voidPlan(BizRecvPayPlan plan, String remark) {
        plan.setPlanStatus(CommonConst.PLAN_STATUS_VOID);
        plan.setRemark(StringUtils.hasText(remark) ? remark : plan.getRemark());
        planMapper.updateById(plan);
        auditLogUtil.record(CommonConst.MODULE_PLAN, CommonConst.OPER_TYPE_VOID, String.valueOf(plan.getId()), null, remark);
    }

    private void checkPlanAdjustable(BizRecvPayPlan plan) {
        if (plan.getRedFlag() == CommonConst.PLAN_RED_FLAG_REVERSAL) { throw new BizException("红冲计划禁止调账"); }
        if (plan.getPlanStatus() == CommonConst.PLAN_STATUS_DONE || plan.getPlanStatus() == CommonConst.PLAN_STATUS_VOID
                || plan.getPlanStatus() == CommonConst.PLAN_STATUS_TERMINATED) { throw new BizException("已完成/作废/终止的计划禁止调账"); }
    }

    private BigDecimal readThreshold(String key, BigDecimal defaultValue) {
        String value = configService.getValueByKey(key);
        if (!StringUtils.hasText(value)) { return defaultValue; }
        try { return new BigDecimal(value); } catch (NumberFormatException e) { return defaultValue; }
    }

    private String buildAdjustTitle(Long planId, BigDecimal amount, String remark) {
        return "ADJUST#" + planId + "#" + amount.toPlainString() + "#" + (remark == null ? "" : remark);
    }

    private String buildVoidTitle(Long planId, String remark) {
        return "VOID#" + planId + "#" + (remark == null ? "" : remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generatePlanForBill(WaterElecBill bill) {
        Long exists = planMapper.selectCount(new LambdaQueryWrapper<BizRecvPayPlan>()
                .eq(BizRecvPayPlan::getCompanyId, bill.getCompanyId())
                .eq(BizRecvPayPlan::getSourceType, CommonConst.PLAN_SOURCE_BILL)
                .eq(BizRecvPayPlan::getSourceId, String.valueOf(bill.getId()))
                .eq(BizRecvPayPlan::getPeriodNo, bill.getBillMonth()));
        if (exists != null && exists > 0) {
            BizRecvPayPlan existing = planMapper.selectOne(new LambdaQueryWrapper<BizRecvPayPlan>()
                    .eq(BizRecvPayPlan::getCompanyId, bill.getCompanyId())
                    .eq(BizRecvPayPlan::getSourceType, CommonConst.PLAN_SOURCE_BILL)
                    .eq(BizRecvPayPlan::getSourceId, String.valueOf(bill.getId()))
                    .eq(BizRecvPayPlan::getPeriodNo, bill.getBillMonth()));
            return existing == null ? null : existing.getId();
        }
        StallContract contract = stallContractMapper.selectOne(new LambdaQueryWrapper<StallContract>()
                .eq(StallContract::getStallId, bill.getStallId())
                .eq(StallContract::getCompanyId, bill.getCompanyId())
                .eq(StallContract::getContractStatus, CommonConst.CONTRACT_STATUS_EFFECTIVE)
                .le(StallContract::getStartTime, bill.getBillMonth().substring(0, 4) + "-" + bill.getBillMonth().substring(5) + "-15")
                .ge(StallContract::getEndTime, bill.getBillMonth().substring(0, 4) + "-" + bill.getBillMonth().substring(5) + "-15")
                .orderByDesc(StallContract::getCreateTime).last("LIMIT 1"));
        BizRecvPayPlan plan = new BizRecvPayPlan();
        plan.setCompanyId(bill.getCompanyId());
        plan.setPlanNo(buildPlanNo(CommonConst.PLAN_BIZ_WATER_ELEC, bill.getCompanyId()));
        plan.setDirection(CommonConst.PLAN_DIRECTION_RECEIVE);
        plan.setBizType(CommonConst.PLAN_BIZ_WATER_ELEC);
        plan.setSourceType(CommonConst.PLAN_SOURCE_BILL);
        plan.setSourceId(String.valueOf(bill.getId()));
        plan.setStallId(bill.getStallId());
        plan.setTenantId(contract != null ? contract.getTenantId() : null);
        plan.setMerchantId(contract != null ? contract.getMerchantId() : bill.getMerchantId());
        plan.setPeriodNo(bill.getBillMonth());
        plan.setPeriodType(CommonConst.PLAN_PERIOD_MONTH);
        plan.setDueDate(YearMonth.parse(bill.getBillMonth()).atDay(1));
        plan.setOriginalAmount(bill.getTotalAmount() != null ? bill.getTotalAmount() : BigDecimal.ZERO);
        plan.setDiscountAmount(BigDecimal.ZERO);
        plan.setAdjustAmount(BigDecimal.ZERO);
        plan.setPlanAmount(bill.getTotalAmount() != null ? bill.getTotalAmount() : BigDecimal.ZERO);
        plan.setPaidAmount(BigDecimal.ZERO);
        plan.setUnpaidAmount(bill.getTotalAmount() != null ? bill.getTotalAmount() : BigDecimal.ZERO);
        plan.setPlanStatus(CommonConst.PLAN_STATUS_PENDING);
        plan.setRedFlag(CommonConst.PLAN_RED_FLAG_NORMAL);
        plan.setOverdueDays(0);
        plan.setCreateBy(UserContext.getUserIdOrZero());
        try { planMapper.insert(plan); }
        catch (DuplicateKeyException e) {
            log.info("账单应收计划已存在，幂等跳过 billId={} month={}", bill.getId(), bill.getBillMonth());
            BizRecvPayPlan existing = planMapper.selectOne(new LambdaQueryWrapper<BizRecvPayPlan>()
                    .eq(BizRecvPayPlan::getSourceType, CommonConst.PLAN_SOURCE_BILL)
                    .eq(BizRecvPayPlan::getSourceId, String.valueOf(bill.getId()))
                    .eq(BizRecvPayPlan::getPeriodNo, bill.getBillMonth()));
            return existing != null ? existing.getId() : null;
        }
        return plan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generatePlanForPropertyBill(PropertyFeeBill bill) {
        Long exists = planMapper.selectCount(new LambdaQueryWrapper<BizRecvPayPlan>()
                .eq(BizRecvPayPlan::getCompanyId, bill.getCompanyId())
                .eq(BizRecvPayPlan::getSourceType, CommonConst.PLAN_SOURCE_BILL)
                .eq(BizRecvPayPlan::getSourceId, String.valueOf(bill.getId()))
                .eq(BizRecvPayPlan::getPeriodNo, bill.getBillMonth()));
        if (exists != null && exists > 0) {
            BizRecvPayPlan existing = planMapper.selectOne(new LambdaQueryWrapper<BizRecvPayPlan>()
                    .eq(BizRecvPayPlan::getCompanyId, bill.getCompanyId())
                    .eq(BizRecvPayPlan::getSourceType, CommonConst.PLAN_SOURCE_BILL)
                    .eq(BizRecvPayPlan::getSourceId, String.valueOf(bill.getId()))
                    .eq(BizRecvPayPlan::getPeriodNo, bill.getBillMonth()));
            return existing == null ? null : existing.getId();
        }
        StallContract contract = stallContractMapper.selectOne(new LambdaQueryWrapper<StallContract>()
                .eq(StallContract::getStallId, bill.getStallId())
                .eq(StallContract::getCompanyId, bill.getCompanyId())
                .eq(StallContract::getContractStatus, CommonConst.CONTRACT_STATUS_EFFECTIVE)
                .le(StallContract::getStartTime, bill.getBillMonth().substring(0, 4) + "-" + bill.getBillMonth().substring(5) + "-15")
                .ge(StallContract::getEndTime, bill.getBillMonth().substring(0, 4) + "-" + bill.getBillMonth().substring(5) + "-15")
                .orderByDesc(StallContract::getCreateTime).last("LIMIT 1"));
        BizRecvPayPlan plan = new BizRecvPayPlan();
        plan.setCompanyId(bill.getCompanyId());
        plan.setPlanNo(buildPlanNo(CommonConst.PLAN_BIZ_PROPERTY, bill.getCompanyId()));
        plan.setDirection(CommonConst.PLAN_DIRECTION_RECEIVE);
        plan.setBizType(CommonConst.PLAN_BIZ_PROPERTY);
        plan.setSourceType(CommonConst.PLAN_SOURCE_BILL);
        plan.setSourceId(String.valueOf(bill.getId()));
        plan.setStallId(bill.getStallId());
        plan.setTenantId(contract != null ? contract.getTenantId() : null);
        plan.setMerchantId(contract != null ? contract.getMerchantId() : bill.getMerchantId());
        plan.setPeriodNo(bill.getBillMonth());
        plan.setPeriodType(CommonConst.PLAN_PERIOD_MONTH);
        plan.setDueDate(YearMonth.parse(bill.getBillMonth()).atDay(1));
        plan.setOriginalAmount(bill.getAmount());
        plan.setDiscountAmount(BigDecimal.ZERO);
        plan.setAdjustAmount(BigDecimal.ZERO);
        plan.setPlanAmount(bill.getAmount());
        plan.setPaidAmount(BigDecimal.ZERO);
        plan.setUnpaidAmount(bill.getAmount());
        plan.setPlanStatus(CommonConst.PLAN_STATUS_PENDING);
        plan.setRedFlag(CommonConst.PLAN_RED_FLAG_NORMAL);
        plan.setOverdueDays(0);
        plan.setCreateBy(UserContext.getUserIdOrZero());
        try { planMapper.insert(plan); }
        catch (DuplicateKeyException e) {
            log.info("物业费账单应收计划已存在，幂等跳过 billId={} month={}", bill.getId(), bill.getBillMonth());
            BizRecvPayPlan existing = planMapper.selectOne(new LambdaQueryWrapper<BizRecvPayPlan>()
                    .eq(BizRecvPayPlan::getSourceType, CommonConst.PLAN_SOURCE_BILL)
                    .eq(BizRecvPayPlan::getSourceId, String.valueOf(bill.getId()))
                    .eq(BizRecvPayPlan::getPeriodNo, bill.getBillMonth()));
            return existing != null ? existing.getId() : null;
        }
        return plan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generatePlanForBizFeeBill(BizFeeBill bill) {
        Long exists = planMapper.selectCount(new LambdaQueryWrapper<BizRecvPayPlan>()
                .eq(BizRecvPayPlan::getCompanyId, bill.getCompanyId())
                .eq(BizRecvPayPlan::getSourceType, CommonConst.PLAN_SOURCE_BILL)
                .eq(BizRecvPayPlan::getSourceId, String.valueOf(bill.getId()))
                .eq(BizRecvPayPlan::getPeriodNo, bill.getBillMonth()));
        if (exists != null && exists > 0) {
            BizRecvPayPlan existing = planMapper.selectOne(new LambdaQueryWrapper<BizRecvPayPlan>()
                    .eq(BizRecvPayPlan::getCompanyId, bill.getCompanyId())
                    .eq(BizRecvPayPlan::getSourceType, CommonConst.PLAN_SOURCE_BILL)
                    .eq(BizRecvPayPlan::getSourceId, String.valueOf(bill.getId()))
                    .eq(BizRecvPayPlan::getPeriodNo, bill.getBillMonth()));
            return existing == null ? null : existing.getId();
        }
        StallContract contract = stallContractMapper.selectOne(new LambdaQueryWrapper<StallContract>()
                .eq(StallContract::getStallId, bill.getStallId())
                .eq(StallContract::getCompanyId, bill.getCompanyId())
                .eq(StallContract::getContractStatus, CommonConst.CONTRACT_STATUS_EFFECTIVE)
                .le(StallContract::getStartTime, bill.getBillMonth().substring(0, 4) + "-" + bill.getBillMonth().substring(5) + "-15")
                .ge(StallContract::getEndTime, bill.getBillMonth().substring(0, 4) + "-" + bill.getBillMonth().substring(5) + "-15")
                .orderByDesc(StallContract::getCreateTime).last("LIMIT 1"));
        String planBizType = CommonConst.PLAN_BIZ_FEE_BILL;
        BizRecvPayPlan plan = new BizRecvPayPlan();
        plan.setCompanyId(bill.getCompanyId());
        plan.setPlanNo(buildPlanNo(planBizType, bill.getCompanyId()));
        plan.setDirection(CommonConst.PLAN_DIRECTION_RECEIVE);
        plan.setBizType(planBizType);
        plan.setSourceType(CommonConst.PLAN_SOURCE_BILL);
        plan.setSourceId(String.valueOf(bill.getId()));
        plan.setStallId(bill.getStallId());
        plan.setTenantId(contract != null ? contract.getTenantId() : null);
        plan.setMerchantId(contract != null ? contract.getMerchantId() : bill.getMerchantId());
        plan.setPeriodNo(bill.getBillMonth());
        plan.setPeriodType(bill.getPeriodType() != null ? bill.getPeriodType() : CommonConst.PLAN_PERIOD_MONTH);
        plan.setDueDate(YearMonth.parse(bill.getBillMonth()).atDay(1));
        plan.setOriginalAmount(bill.getRealAmount() != null ? bill.getRealAmount() : BigDecimal.ZERO);
        plan.setDiscountAmount(bill.getDiscountAmount() != null ? bill.getDiscountAmount() : BigDecimal.ZERO);
        plan.setAdjustAmount(bill.getAdjustAmount() != null ? bill.getAdjustAmount() : BigDecimal.ZERO);
        plan.setPlanAmount(bill.getRealAmount() != null ? bill.getRealAmount() : BigDecimal.ZERO);
        plan.setPaidAmount(BigDecimal.ZERO);
        plan.setUnpaidAmount(bill.getRealAmount() != null ? bill.getRealAmount() : BigDecimal.ZERO);
        plan.setPlanStatus(CommonConst.PLAN_STATUS_PENDING);
        plan.setRedFlag(CommonConst.PLAN_RED_FLAG_NORMAL);
        plan.setOverdueDays(0);
        plan.setCreateBy(UserContext.getUserIdOrZero());
        try { planMapper.insert(plan); }
        catch (DuplicateKeyException e) {
            log.info("统一账单应收计划已存在，幂等跳过 billId={} month={}", bill.getId(), bill.getBillMonth());
            BizRecvPayPlan existing = planMapper.selectOne(new LambdaQueryWrapper<BizRecvPayPlan>()
                    .eq(BizRecvPayPlan::getSourceType, CommonConst.PLAN_SOURCE_BILL)
                    .eq(BizRecvPayPlan::getSourceId, String.valueOf(bill.getId()))
                    .eq(BizRecvPayPlan::getPeriodNo, bill.getBillMonth()));
            return existing != null ? existing.getId() : null;
        }
        return plan.getId();
    }

    private void redChainPlan(BizRecvPayPlan plan) {
        Long reversalCount = planMapper.selectCount(new LambdaQueryWrapper<BizRecvPayPlan>()
                .eq(BizRecvPayPlan::getOrigPlanId, plan.getId())
                .eq(BizRecvPayPlan::getRedFlag, CommonConst.PLAN_RED_FLAG_REVERSAL));
        if (reversalCount != null && reversalCount > 0) { return; }
        BigDecimal unpaid = plan.getUnpaidAmount().max(BigDecimal.ZERO);
        BigDecimal paid = plan.getPaidAmount();
        plan.setPlanStatus(CommonConst.PLAN_STATUS_TERMINATED);
        plan.setRemark("合同退租红冲，被冲销计划取代");
        planMapper.updateById(plan);
        if (unpaid.compareTo(BigDecimal.ZERO) > 0) {
            BizRecvPayPlan reversal = new BizRecvPayPlan();
            reversal.setCompanyId(plan.getCompanyId());
            reversal.setPlanNo(buildPlanNo(plan.getBizType(), plan.getCompanyId()));
            reversal.setDirection(plan.getDirection());
            reversal.setBizType(plan.getBizType());
            reversal.setSourceType(CommonConst.PLAN_SOURCE_CONTRACT);
            reversal.setSourceId(plan.getSourceId() + "#R");
            reversal.setStallId(plan.getStallId());
            reversal.setTenantId(plan.getTenantId());
            reversal.setMerchantId(plan.getMerchantId());
            reversal.setPeriodNo(plan.getPeriodNo());
            reversal.setPeriodType(plan.getPeriodType());
            reversal.setDueDate(LocalDate.now());
            reversal.setOriginalAmount(unpaid.negate());
            reversal.setDiscountAmount(BigDecimal.ZERO);
            reversal.setAdjustAmount(BigDecimal.ZERO);
            reversal.setPlanAmount(unpaid.negate());
            reversal.setPaidAmount(BigDecimal.ZERO);
            reversal.setUnpaidAmount(unpaid.negate());
            reversal.setPlanStatus(CommonConst.PLAN_STATUS_PENDING);
            reversal.setRedFlag(CommonConst.PLAN_RED_FLAG_REVERSAL);
            reversal.setOrigPlanId(plan.getId());
            reversal.setOverdueDays(0);
            reversal.setCreateBy(UserContext.getUserIdOrZero());
            planMapper.insert(reversal);
        }
        if (paid.compareTo(BigDecimal.ZERO) > 0) {
            BizFinanceFlow flow = new BizFinanceFlow();
            flow.setCompanyId(plan.getCompanyId());
            flow.setBusinessType(plan.getBizType());
            flow.setBillId(plan.getPlanNo());
            flow.setMerchantId(plan.getMerchantId());
            flow.setStallId(plan.getStallId());
            flow.setOriginalAmount(paid);
            flow.setDiscountAmount(BigDecimal.ZERO);
            flow.setRealAmount(paid);
            flow.setFlowType(CommonConst.FLOW_TYPE_EXPENSE);
            flow.setStatus(CommonConst.STATUS_ENABLED);
            flow.setRemark("红冲-合同退租冲减已收款项（计划" + plan.getPlanNo() + "）");
            flow.setCreateBy(UserContext.getUserIdOrZero());
            financeFlowMapper.insert(flow);
            writeOff(plan.getId(), flow.getId(), paid.negate(), null, null,
                    CommonConst.WRITEOFF_TYPE_RED_REVERSAL, "红冲-合同退租");
        }
        auditLogUtil.record(CommonConst.MODULE_PLAN, CommonConst.OPER_TYPE_RED_REVERSAL,
                String.valueOf(plan.getId()), null, "红冲链完成：作废计划+反向冲销计划+负向核销+反向流水");
    }

    private RecvPayPlanVO toVO(BizRecvPayPlan p, Map<Long, String> stallNos, Map<Long, String> tenantNames) {
        RecvPayPlanVO vo = new RecvPayPlanVO();
        vo.setId(p.getId()); vo.setCompanyId(p.getCompanyId()); vo.setPlanNo(p.getPlanNo());
        vo.setDirection(p.getDirection());
        vo.setDirectionText(p.getDirection() == CommonConst.PLAN_DIRECTION_RECEIVE ? "应收" : "应付");
        vo.setBizType(p.getBizType()); vo.setBizTypeText(bizTypeText(p.getBizType()));
        vo.setSourceType(p.getSourceType()); vo.setSourceId(p.getSourceId());
        vo.setStallId(p.getStallId()); vo.setStallNumber(stallNos.get(p.getStallId()));
        vo.setTenantId(p.getTenantId()); vo.setTenantName(tenantNames.get(p.getTenantId()));
        vo.setMerchantId(p.getMerchantId()); vo.setPeriodNo(p.getPeriodNo());
        vo.setPeriodType(p.getPeriodType()); vo.setDueDate(p.getDueDate());
        vo.setOriginalAmount(p.getOriginalAmount()); vo.setDiscountAmount(p.getDiscountAmount());
        vo.setAdjustAmount(p.getAdjustAmount()); vo.setPlanAmount(p.getPlanAmount());
        vo.setPaidAmount(p.getPaidAmount()); vo.setUnpaidAmount(p.getUnpaidAmount());
        vo.setPlanStatus(p.getPlanStatus()); vo.setPlanStatusText(planStatusText(p.getPlanStatus()));
        vo.setRedFlag(p.getRedFlag());
        vo.setRedFlagText(p.getRedFlag() == CommonConst.PLAN_RED_FLAG_REVERSAL ? "红冲" : "正常");
        vo.setOrigPlanId(p.getOrigPlanId()); vo.setOverdueDays(p.getOverdueDays());
        vo.setFlowInstanceId(p.getFlowInstanceId()); vo.setDiscountApplyId(p.getDiscountApplyId());
        vo.setRemark(p.getRemark()); vo.setCreateTime(p.getCreateTime());
        return vo;
    }

    private String bizTypeText(String bizType) {
        if (CommonConst.PLAN_BIZ_RENT.equals(bizType)) { return "租金"; }
        if (CommonConst.PLAN_BIZ_DEPOSIT.equals(bizType)) { return "押金"; }
        if (CommonConst.PLAN_BIZ_PROPERTY.equals(bizType)) { return "物业费"; }
        if (CommonConst.PLAN_BIZ_WATER_ELEC.equals(bizType)) { return "水电物业账单"; }
        if (CommonConst.PLAN_BIZ_FEE_BILL.equals(bizType)) { return "收费规则账单"; }
        if (CommonConst.PLAN_BIZ_REIMBURSE.equals(bizType)) { return "报销"; }
        if (CommonConst.PLAN_BIZ_PURCHASE.equals(bizType)) { return "采购"; }
        return bizType;
    }

    private String planStatusText(Integer status) {
        if (status == null) { return ""; }
        return switch (status) {
            case CommonConst.PLAN_STATUS_PENDING -> "待执行";
            case CommonConst.PLAN_STATUS_PART -> "部分核销";
            case CommonConst.PLAN_STATUS_DONE -> "完成";
            case CommonConst.PLAN_STATUS_OVERDUE -> "逾期";
            case CommonConst.PLAN_STATUS_VOID -> "作废";
            case CommonConst.PLAN_STATUS_TERMINATED -> "终止";
            default -> String.valueOf(status);
        };
    }

    private String writeoffTypeText(Integer type) {
        if (type == null) { return ""; }
        return switch (type) {
            case CommonConst.WRITEOFF_TYPE_PAY -> "缴费核销";
            case CommonConst.WRITEOFF_TYPE_REFUND -> "退款冲减";
            case CommonConst.WRITEOFF_TYPE_RED_REVERSAL -> "红冲冲销";
            default -> String.valueOf(type);
        };
    }

    private String escapeCsv(String value) {
        if (value == null) { return ""; }
        if (value.contains(",") || value.contains("\"") || value.contains("\\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @Override
    public void writeOffByBillId(Long companyId, Long billId, BizFinanceFlow flow, int writeoffType, String remark) {
        List<BizRecvPayPlan> plans = planMapper.selectList(
                new LambdaQueryWrapper<BizRecvPayPlan>()
                        .eq(BizRecvPayPlan::getCompanyId, companyId)
                        .eq(BizRecvPayPlan::getSourceType, "bill")
                        .eq(BizRecvPayPlan::getSourceId, String.valueOf(billId)));
        if (plans.isEmpty()) {
            log.warn("writeOffByBillId skip: no plan found companyId={} billId={}", companyId, billId);
            return;
        }
        for (BizRecvPayPlan plan : plans) {
            writeOff(plan.getId(), flow.getId(), flow.getRealAmount(), plan.getBizType(), billId, writeoffType, remark);
        }
    }

}

