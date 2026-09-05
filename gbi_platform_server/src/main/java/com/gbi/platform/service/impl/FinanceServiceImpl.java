package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.FinanceFlowQueryDTO;
import com.gbi.platform.dto.FinanceSummaryQueryDTO;
import com.gbi.platform.entity.BizFinanceFlow;
import com.gbi.platform.mapper.BizFinanceFlowMapper;
import com.gbi.platform.service.FinanceService;
import com.gbi.platform.service.FlowEngineService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.FinanceFlowVO;
import com.gbi.platform.vo.FinanceSummaryVO;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.entity.BizPayOrder;
import com.gbi.platform.entity.BizPayOrderItem;
import com.gbi.platform.mapper.BizPayOrderMapper;
import com.gbi.platform.mapper.BizPayOrderItemMapper;
import com.gbi.platform.vo.PayOrderItemVO;
import com.gbi.platform.dto.PayOrderQueryDTO;
import com.gbi.platform.vo.PayOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 财务流水服务实现：全域统一资金台账（biz_finance_flow）
 * 快照字段直接读本表，不再动态关联；支持冲红/作废/打印
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceServiceImpl implements FinanceService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BizFinanceFlowMapper financeFlowMapper;
    private final BizPayOrderMapper payOrderMapper;
    private final BizPayOrderItemMapper payOrderItemMapper;
    private final AuditLogUtil auditLogUtil;
    private final FlowEngineService flowEngineService;

    @Override
    public PageVO<FinanceFlowVO> page(FinanceFlowQueryDTO dto) {
        Page<BizFinanceFlow> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<BizFinanceFlow> wrapper = buildWrapper(dto, true);
        Page<BizFinanceFlow> result = financeFlowMapper.selectPage(page, wrapper);

        List<FinanceFlowVO> voList = result.getRecords().stream()
                .map(this::toVO).toList();
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public List<FinanceSummaryVO> summary(FinanceSummaryQueryDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        QueryWrapper<BizFinanceFlow> wrapper = new QueryWrapper<BizFinanceFlow>()
                .select("company_id", "business_type", "flow_type",
                        "COUNT(*) AS flow_count", "SUM(real_amount) AS total_amount")
                .eq(StringUtils.hasText(dto.getBusinessType()), "business_type", dto.getBusinessType())
                .eq(dto.getCompanyId() != null && loginUser.isSuperAdmin(), "company_id", dto.getCompanyId())
                .groupBy("company_id", "business_type", "flow_type")
                .orderByAsc("company_id").orderByAsc("flow_type");
        if (StringUtils.hasText(dto.getStartTime())) {
            wrapper.ge("create_time", LocalDateTime.parse(dto.getStartTime(), TIME_FMT));
        }
        if (StringUtils.hasText(dto.getEndTime())) {
            wrapper.le("create_time", LocalDateTime.parse(dto.getEndTime(), TIME_FMT));
        }
        List<java.util.Map<String, Object>> rows = financeFlowMapper.selectMaps(wrapper);
        List<FinanceSummaryVO> result = new ArrayList<>(rows.size());
        for (java.util.Map<String, Object> row : rows) {
            FinanceSummaryVO vo = new FinanceSummaryVO();
            vo.setCompanyId(toLong(row.get("company_id")));
            String businessType = (String) row.get("business_type");
            vo.setBusinessType(businessType);
            vo.setBusinessTypeText(businessTypeText(businessType));
            Integer flowType = toInteger(row.get("flow_type"));
            vo.setFlowType(flowType);
            vo.setFlowTypeText(flowTypeText(flowType));
            vo.setFlowCount(toLong(row.get("flow_count")));
            vo.setTotalAmount(toBigDecimal(row.get("total_amount")));
            result.add(vo);
        }
        return result;
    }
    /** 导出 CSV 文件 */
    @Override
    public String exportCsv(FinanceFlowQueryDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        LambdaQueryWrapper<BizFinanceFlow> wrapper = buildWrapper(dto, false);
        List<BizFinanceFlow> list = financeFlowMapper.selectList(wrapper);

        StringBuilder sb = new StringBuilder();
        sb.append("流水ID,所属公司ID,业务类型,关联单据ID,商户ID,摊位ID,摊位编号,摊位名称,所属市场,分类,商户名称,")
          .append("缴费人姓名,缴费人手机号,缴费人公司,缴费人类型,合同编号,合同ID,")
          .append("应收原价,优惠抵扣,实收金额,支付渠道,流水类型,交易号,流水单号,冲红状态,作废原因,备注,操作人ID,生成时间\n");
        for (BizFinanceFlow f : list) {
            FinanceFlowVO vo = toVO(f);
            sb.append(f.getId()).append(',')
                    .append(f.getCompanyId()).append(',')
                    .append(escapeCsv(businessTypeText(f.getBusinessType()))).append(',')
                    .append(escapeCsv(f.getBillId())).append(',')
                    .append(f.getMerchantId() == null ? "" : f.getMerchantId()).append(',')
                    .append(f.getStallId() == null ? "" : f.getStallId()).append(',')
                    .append(escapeCsv(f.getStallNumber() == null ? "" : f.getStallNumber())).append(',')
                    .append(escapeCsv(f.getStallName() == null ? "" : f.getStallName())).append(',')
                    .append(escapeCsv(f.getStallMarketName() == null ? "" : f.getStallMarketName())).append(',')
                    .append(escapeCsv(f.getCategoryName() == null ? "" : f.getCategoryName())).append(',')
                    .append(escapeCsv(f.getMerchantName() == null ? "" : f.getMerchantName())).append(',')
                    .append(escapeCsv(f.getPayerName() == null ? "" : f.getPayerName())).append(',')
                    .append(escapeCsv(f.getPayerPhone() == null ? "" : f.getPayerPhone())).append(',')
                    .append(escapeCsv(f.getPayerCompanyName() == null ? "" : f.getPayerCompanyName())).append(',')
                    .append(escapeCsv(flowStatusText(f.getFlowStatus()))).append(',')
                    .append(escapeCsv(f.getContractNo() == null ? "" : f.getContractNo())).append(',')
                    .append(f.getContractId() == null ? "" : f.getContractId()).append(',')
                    .append(f.getOriginalAmount() == null ? "0.00" : f.getOriginalAmount().toPlainString()).append(',')
                    .append(f.getDiscountAmount() == null ? "0.00" : f.getDiscountAmount().toPlainString()).append(',')
                    .append(f.getRealAmount() == null ? "0.00" : f.getRealAmount().toPlainString()).append(',')
                    .append(escapeCsv(payTypeText(f.getPayType()))).append(',')
                    .append(escapeCsv(flowTypeText(f.getFlowType()))).append(',')
                    .append(escapeCsv(f.getTradeNo() == null ? "" : f.getTradeNo())).append(',')
                    .append(escapeCsv(f.getFlowNo() == null ? "" : f.getFlowNo())).append(',')
                    .append(escapeCsv(flowStatusText(f.getFlowStatus()))).append(',')
                    .append(escapeCsv(f.getVoidReason() == null ? "" : f.getVoidReason())).append(',')
                    .append(escapeCsv(f.getRemark() == null ? "" : f.getRemark())).append(',')
                    .append(f.getCreateBy()).append(',')
                    .append(f.getCreateTime() == null ? "" : f.getCreateTime().format(TIME_FMT)).append('\n');
        }
        String fileName = "finance_flow_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
        try {
            Path exportDir = Paths.get(System.getProperty("java.io.tmpdir"), "gbi_export");
            Files.createDirectories(exportDir);
            Files.writeString(exportDir.resolve(fileName), sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("财务流水导出失败", e);
            throw new BizException("导出失败，请稍后重试");
        }
        auditLogUtil.record(CommonConst.MODULE_FINANCE, CommonConst.OPER_TYPE_EXPORT, fileName, null, null);
        return "/upload/export/" + fileName;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void redFlush(Long flowId, String reason) {
        LoginUser loginUser = UserContext.getLoginUser();
        BizFinanceFlow flow = financeFlowMapper.selectById(flowId);
        if (flow == null) {
            throw new BizException("流水不存在");
        }
        if (!(CommonConst.FINANCE_FLOW_STATUS_NORMAL == flow.getFlowStatus())) {
            throw new BizException("该流水已冲红或已作废，不可再次冲红");
        }
        // 1. 标记原流水为"冲红中"
        BizFinanceFlow update1 = new BizFinanceFlow();
        update1.setId(flowId);
        update1.setFlowStatus(CommonConst.FINANCE_FLOW_STATUS_FLUSHING);
        update1.setRemark((flow.getRemark() == null ? "" : flow.getRemark()) + "【冲红审批中：" + reason + "】");
        financeFlowMapper.updateById(update1);

        // 2. 创建冲红反向流水（等待审批通过后正式写入反向金额）
        BizFinanceFlow reverseFlow = new BizFinanceFlow();
        reverseFlow.setCompanyId(flow.getCompanyId());
        reverseFlow.setBusinessType(flow.getBusinessType());
        reverseFlow.setBillId(flow.getBillId());
        reverseFlow.setPlanId(flow.getPlanId());
        reverseFlow.setMerchantId(flow.getMerchantId());
        reverseFlow.setStallId(flow.getStallId());
        reverseFlow.setStallNumber(flow.getStallNumber());
        reverseFlow.setStallName(flow.getStallName());
        reverseFlow.setStallMarketName(flow.getStallMarketName());
        reverseFlow.setCategoryName(flow.getCategoryName());
        reverseFlow.setMerchantName(flow.getMerchantName());
        reverseFlow.setPayerName(flow.getPayerName());
        reverseFlow.setPayerPhone(flow.getPayerPhone());
        reverseFlow.setPayerCompanyName(flow.getPayerCompanyName());
        reverseFlow.setPayerType(flow.getPayerType());
        reverseFlow.setContractNo(flow.getContractNo());
        reverseFlow.setContractId(flow.getContractId());
        reverseFlow.setOriginalAmount(flow.getOriginalAmount());
        reverseFlow.setDiscountAmount(flow.getDiscountAmount());
        reverseFlow.setRealAmount(flow.getRealAmount().negate());
        reverseFlow.setPayType(flow.getPayType());
        reverseFlow.setFlowType(flow.getFlowType() == CommonConst.FLOW_TYPE_INCOME
                ? CommonConst.FLOW_TYPE_EXPENSE : CommonConst.FLOW_TYPE_INCOME);
        reverseFlow.setStatus(CommonConst.STATUS_DISABLED); // 待审批，暂时禁用
        reverseFlow.setFlowStatus(CommonConst.FINANCE_FLOW_STATUS_FLUSHING);
        reverseFlow.setTradeNo(flow.getTradeNo());
        reverseFlow.setFlowNo(flow.getFlowNo() + "-RED"); // 标记为冲红单
        reverseFlow.setRemark("冲红反向流水（原流水ID=" + flowId + "，审批通过后生效）");
        reverseFlow.setCreateBy(loginUser.getUserId());
        financeFlowMapper.insert(reverseFlow);

        // 3. 提交冲红审批流程

        auditLogUtil.record(CommonConst.MODULE_FINANCE, "冲红申请", String.valueOf(flowId), flow, null);
        // 提交审批流程
        Long instanceId = flowEngineService.submit(
                CommonConst.FLOW_DEF_FINANCE_RED_FLUSH,
                "finance_flow",
                String.valueOf(flowId),
                "冲红申请-流水ID:" + flowId
        );
        log.info("冲红审批流程已提交，flowId={}, instanceId={}", flowId, instanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRedFlush(Long flowId, Long redFlushFlowId) {
        LoginUser loginUser = UserContext.getLoginUser();
        // 1. 标记原流水为已冲红
        BizFinanceFlow update1 = new BizFinanceFlow();
        update1.setId(flowId);
        update1.setFlowStatus(CommonConst.FINANCE_FLOW_STATUS_FLUSHED);
        update1.setRedFlushFlowId(redFlushFlowId);
        financeFlowMapper.updateById(update1);

        // 2. 激活反向流水
        BizFinanceFlow update2 = new BizFinanceFlow();
        update2.setId(redFlushFlowId);
        update2.setStatus(CommonConst.STATUS_ENABLED);
        update2.setFlowStatus(CommonConst.FINANCE_FLOW_STATUS_NORMAL);
        financeFlowMapper.updateById(update2);

        auditLogUtil.record(CommonConst.MODULE_FINANCE, "冲红审批通过", String.valueOf(flowId), null, null);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRedFlush(Long flowId) {
        LoginUser loginUser = UserContext.getLoginUser();
        BizFinanceFlow flow = financeFlowMapper.selectById(flowId);
        if (flow == null) throw new BizException("流水不存在");
        if (flow.getFlowStatus() != CommonConst.FINANCE_FLOW_STATUS_FLUSHING) {
            throw new BizException("该流水不是冲红中状态");
        }
        // 恢复原流水状态为正常
        BizFinanceFlow update = new BizFinanceFlow();
        update.setId(flowId);
        update.setFlowStatus(CommonConst.FINANCE_FLOW_STATUS_NORMAL);
        financeFlowMapper.updateById(update);
        // 删除冲红反向流水
        if (flow.getRedFlushFlowId() != null) {
            financeFlowMapper.deleteById(flow.getRedFlushFlowId());
        }
        auditLogUtil.record(CommonConst.MODULE_FINANCE, "冲红审批驳回", String.valueOf(flowId), null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidFlow(Long flowId, String reason) {
        LoginUser loginUser = UserContext.getLoginUser();
        BizFinanceFlow flow = financeFlowMapper.selectById(flowId);
        if (flow == null) {
            throw new BizException("流水不存在");
        }
        if (!(CommonConst.FINANCE_FLOW_STATUS_NORMAL == flow.getFlowStatus())) {
            throw new BizException("该流水已冲红或已作废，不可再次操作");
        }
        // 作废仅允许草稿/未记账（status=0 或 flow_status=1正常且未关联计划）
        if (flow.getPlanId() != null) {
            throw new BizException("已关联应收应付计划的流水不可作废，请通过冲红流程处理");
        }
        BizFinanceFlow update = new BizFinanceFlow();
        update.setId(flowId);
        update.setFlowStatus(CommonConst.FINANCE_FLOW_STATUS_VOIDED);
        update.setVoidReason(reason);
        update.setRemark((flow.getRemark() == null ? "" : flow.getRemark()) + "【已作废：" + reason + "】");
        financeFlowMapper.updateById(update);
        auditLogUtil.record(CommonConst.MODULE_FINANCE, "作废", String.valueOf(flowId), flow, null);
    }

    @Override
    public String getPrintHtml(Long flowId) {
        BizFinanceFlow flow = financeFlowMapper.selectById(flowId);
        if (flow == null) {
            throw new BizException("流水不存在");
        }
        FinanceFlowVO vo = toVO(flow);
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>财务收据</title>")
            .append("<style>body{font-family:SimSun,serif;padding:40px;}table{width:100%;border-collapse:collapse;}")
            .append("td,th{border:1px solid #000;padding:8px;text-align:left;}th{background:#f0f0f0;}")
            .append(".money{text-align:right;font-weight:bold;}h2{text-align:center;}</style></head><body>")
            .append("<h2>财务收据</h2>")
            .append("<table><tr><th>流水单号</th><td>").append(escapeHtml(vo.getFlowNo() == null ? "" : vo.getFlowNo()))
            .append("</td><th>生成时间</th><td>").append(vo.getCreateTime() == null ? "" : vo.getCreateTime().toString())
            .append("</td></tr>")
            .append("<tr><th>业务类型</th><td>").append(escapeHtml(vo.getBusinessTypeText() == null ? "" : vo.getBusinessTypeText()))
            .append("</td><th>收支方向</th><td>").append(escapeHtml(vo.getFlowTypeText() == null ? "" : vo.getFlowTypeText()))
            .append("</td></tr>")
            .append("<tr><th>摊位</th><td>").append(escapeHtml(buildStallLabel(vo)))
            .append("</td><th>缴费人</th><td>").append(escapeHtml(vo.getPayerName() == null ? (vo.getMerchantName() == null ? "-" : vo.getMerchantName()) : vo.getPayerName()))
            .append("</td></tr>")
            .append("<tr><th>应收原价</th><td class=\"money\">").append(vo.getOriginalAmount() == null ? "0.00" : vo.getOriginalAmount().toPlainString())
            .append("</td><th>优惠抵扣</th><td class=\"money\">").append(vo.getDiscountAmount() == null ? "0.00" : vo.getDiscountAmount().toPlainString())
            .append("</td></tr>")
            .append("<tr><th>实收金额</th><td class=\"money\" style=\"font-size:18px;color:red;\">")
            .append(vo.getRealAmount() == null ? "0.00" : vo.getRealAmount().toPlainString())
            .append("</td><th>支付渠道</th><td>").append(escapeHtml(vo.getPayTypeText() == null ? "-" : vo.getPayTypeText()))
            .append("</td></tr>")
            .append("<tr><th>流水单号</th><td colspan=\"3\">").append(escapeHtml(vo.getFlowNo() == null ? "" : vo.getFlowNo()))
            .append("</td></tr>")
            .append("<tr><th>备注</th><td colspan=\"3\">").append(escapeHtml(vo.getRemark() == null ? "" : vo.getRemark()))
            .append("</td></tr></table>")
            .append("<p style=\"text-align:right;margin-top:40px;\">制单人：").append(escapeHtml(String.valueOf(vo.getCreateBy())))
            .append("  生成时间：").append(vo.getCreateTime() == null ? "" : vo.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .append("</p></body></html>");
        return html.toString();
    }

    private LambdaQueryWrapper<BizFinanceFlow> buildWrapper(FinanceFlowQueryDTO dto, boolean orderByIdDesc) {
        LoginUser loginUser = UserContext.getLoginUser();
        LambdaQueryWrapper<BizFinanceFlow> wrapper = new LambdaQueryWrapper<BizFinanceFlow>()
                .eq(StringUtils.hasText(dto.getBusinessType()), BizFinanceFlow::getBusinessType, dto.getBusinessType())
                .eq(dto.getFlowType() != null, BizFinanceFlow::getFlowType, dto.getFlowType())
                .eq(dto.getCompanyId() != null && loginUser.isSuperAdmin(), BizFinanceFlow::getCompanyId, dto.getCompanyId());
        if (dto.getStartTimeValue() != null) {
            wrapper.ge(BizFinanceFlow::getCreateTime, dto.getStartTimeValue());
        }
        if (dto.getEndTimeValue() != null) {
            wrapper.le(BizFinanceFlow::getCreateTime, dto.getEndTimeValue());
        }
        if (orderByIdDesc) {
            wrapper.orderByDesc(BizFinanceFlow::getId);
        }
        return wrapper;
    }

    private FinanceFlowVO toVO(BizFinanceFlow f) {
        FinanceFlowVO vo = new FinanceFlowVO();
        vo.setId(f.getId());
        vo.setCompanyId(f.getCompanyId());
        vo.setBusinessType(f.getBusinessType());
        vo.setBusinessTypeText(businessTypeText(f.getBusinessType()));
        vo.setBillId(f.getBillId());
        vo.setMerchantId(f.getMerchantId());
        vo.setStallId(f.getStallId());
        // 快照字段直接读
        vo.setStallNumber(f.getStallNumber());
        vo.setStallName(f.getStallName());
        vo.setStallMarketName(f.getStallMarketName());
        vo.setCategoryName(f.getCategoryName());
        vo.setMerchantName(f.getMerchantName());
        // 缴费人快照
        vo.setPayerName(f.getPayerName());
        vo.setPayerPhone(f.getPayerPhone());
        vo.setPayerCompanyName(f.getPayerCompanyName());
        vo.setPayerType(f.getPayerType());
        vo.setPayerTypeText(payerTypeText(f.getPayerType()));
        // 合同快照
        vo.setContractNo(f.getContractNo());
        vo.setContractId(f.getContractId());
        // 金额
        vo.setOriginalAmount(f.getOriginalAmount());
        vo.setDiscountAmount(f.getDiscountAmount());
        vo.setRealAmount(f.getRealAmount());
        // 渠道/方向
        vo.setPayType(f.getPayType());
        vo.setPayTypeText(payTypeText(f.getPayType()));
        vo.setFlowType(f.getFlowType());
        vo.setFlowTypeText(flowTypeText(f.getFlowType()));
        // 冲红/作废
        vo.setFlowStatus(f.getFlowStatus());
        vo.setFlowStatusText(flowStatusText(f.getFlowStatus()));
        vo.setRedFlushFlowId(f.getRedFlushFlowId());
        vo.setVoidReason(f.getVoidReason());
        // 其他
        vo.setTradeNo(f.getTradeNo());
        vo.setFlowNo(f.getFlowNo());
        vo.setRemark(f.getRemark());
        vo.setCreateBy(f.getCreateBy());
        vo.setCreateTime(f.getCreateTime());
        return vo;
    }

    private String buildStallLabel(FinanceFlowVO vo) {
        if (vo.getStallName() != null) {
            return (vo.getStallMarketName() == null ? "" : vo.getStallMarketName())
                    + " / " + (vo.getCategoryName() == null ? "" : vo.getCategoryName())
                    + " / " + vo.getStallName() + "（" + (vo.getStallNumber() == null ? "" : vo.getStallNumber()) + "）";
        }
        return vo.getStallId() == null ? "-" : "摊位#" + vo.getStallId();
    }

    private String businessTypeText(String businessType) {
        if (businessType == null) return null;
        return switch (businessType) {
            case CommonConst.BIZ_TYPE_RENT -> "租金";
            case CommonConst.BIZ_TYPE_WATER_ELEC -> "水电物业";
            case CommonConst.BIZ_TYPE_DEPOSIT -> "押金";
            case CommonConst.BIZ_TYPE_MARKETING -> "营销抵扣";
            default -> businessType;
        };
    }

    private String payTypeText(Integer payType) {
        if (payType == null) return null;
        return switch (payType) {
            case CommonConst.PAY_TYPE_WECHAT -> "微信";
            case CommonConst.PAY_TYPE_ALIPAY -> "支付宝";
            case CommonConst.PAY_TYPE_CASH -> "线下现金";
            default -> "未知";
        };
    }

    private String flowTypeText(Integer flowType) {
        if (flowType == null) return null;
        return switch (flowType) {
            case CommonConst.FLOW_TYPE_INCOME -> "收入";
            case CommonConst.FLOW_TYPE_EXPENSE -> "支出退费";
            default -> "未知";
        };
    }

    private String flowStatusText(Integer flowStatus) {
        if (flowStatus == null) return null;
        return switch (flowStatus) {
            case CommonConst.FINANCE_FLOW_STATUS_NORMAL -> "正常";
            case CommonConst.FINANCE_FLOW_STATUS_FLUSHING -> "冲红中";
            case CommonConst.FINANCE_FLOW_STATUS_FLUSHED -> "已冲红";
            case CommonConst.FINANCE_FLOW_STATUS_VOIDED -> "已作废";
            default -> "未知";
        };
    }

    private String payerTypeText(Integer payerType) {
        if (payerType == null) return null;
        return switch (payerType) {
            case CommonConst.PAYER_TYPE_PERSON -> "个人";
            case CommonConst.PAYER_TYPE_COMPANY -> "企业";
            default -> "未知";
        };
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private Long toLong(Object value) {
        return value == null ? null : Long.valueOf(value.toString());
    }

    private Integer toInteger(Object value) {
        return value == null ? null : Integer.valueOf(value.toString());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        return new BigDecimal(value.toString());
    }

    @Override
    public List<PayOrderItemVO> getPayOrderItems(Long flowId) {
        log.info("根据流水ID查询缴费单明细，flowId={}", flowId);
        BizFinanceFlow flow = financeFlowMapper.selectById(flowId);
        if (flow == null) { return Collections.emptyList(); }
        BizPayOrder payOrder = payOrderMapper.selectBySourceId(Long.valueOf(flow.getBillId()));
        if (payOrder == null) { return Collections.emptyList(); }
        LambdaQueryWrapper<BizPayOrderItem> wrapper = new LambdaQueryWrapper<BizPayOrderItem>()
                .eq(BizPayOrderItem::getPayBillId, payOrder.getId())
                .orderByAsc(BizPayOrderItem::getCreateTime);
        List<BizPayOrderItem> items = payOrderItemMapper.selectList(wrapper);
        return items.stream().map(item -> {
            PayOrderItemVO vo = new PayOrderItemVO();
            vo.setId(item.getId());
            vo.setPayBillId(item.getPayBillId());
            vo.setBillId(item.getBillId());
            vo.setBizType(item.getBizType());
            vo.setBizTypeText(bizTypeText(item.getBizType()));
            vo.setRuleName(item.getRuleName());
            vo.setFeeItemType(item.getFeeItemType());
            vo.setBillMonth(item.getBillMonth());
            vo.setAmount(item.getAmount());
            vo.setDiscountAmount(item.getDiscountAmount());
            vo.setPaidAmount(item.getPaidAmount());
            vo.setUnpaidAmount(item.getUnpaidAmount());
            vo.setCreateTime(item.getCreateTime());
            return vo;
        }).toList();
    }


    @Override
    public PageVO<PayOrderVO> pagePayOrders(PayOrderQueryDTO dto) {
        Page<BizPayOrder> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        IPage<BizPayOrder> result = payOrderMapper.selectPageByQuery(page, dto);
        List<PayOrderVO> records = result.getRecords().stream().map(order -> {
            PayOrderVO vo = new PayOrderVO();
            vo.setId(order.getId());
            vo.setPayBillNo(order.getPayBillNo());
            vo.setCompanyId(order.getCompanyId());
            vo.setSourceType(order.getSourceType());
            vo.setSourceTypeText(sourceTypeText(order.getSourceType()));
            vo.setSourceId(order.getSourceId());
            vo.setStallId(order.getStallId());
            vo.setMerchantId(order.getMerchantId());
            vo.setTotalAmount(order.getTotalAmount());
            vo.setPaidAmount(order.getPaidAmount());
            vo.setUnpaidAmount(order.getUnpaidAmount());
            vo.setPayStatus(order.getPayStatus());
            vo.setPayStatusText(payStatusText(order.getPayStatus()));
            vo.setPayTime(order.getPayTime());
            vo.setCreateTime(order.getCreateTime());
            return vo;
        }).toList();
        return new PageVO<>(records, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    private String sourceTypeText(String sourceType) {
        if (sourceType == null) return null;
        return switch (sourceType) {
            case "fee_bill" -> "物业费";
            case "water_elec" -> "水电费";
            default -> sourceType;
        };
    }

    private String payStatusText(Integer payStatus) {
        if (payStatus == null) return null;
        return switch (payStatus) {
            case 0 -> "待缴";
            case 1 -> "已缴";
            case 2 -> "部分缴费";
            default -> String.valueOf(payStatus);
        };
    }


    @Override
    public List<PayOrderItemVO> getPayOrderItemsById(Long payOrderId) {
        LambdaQueryWrapper<BizPayOrderItem> wrapper = new LambdaQueryWrapper<BizPayOrderItem>()
                .eq(BizPayOrderItem::getPayBillId, payOrderId)
                .orderByAsc(BizPayOrderItem::getCreateTime);
        List<BizPayOrderItem> items = payOrderItemMapper.selectList(wrapper);
        return items.stream().map(item -> {
            PayOrderItemVO vo = new PayOrderItemVO();
            vo.setId(item.getId());
            vo.setPayBillId(item.getPayBillId());
            vo.setBillId(item.getBillId());
            vo.setBizType(item.getBizType());
            vo.setBizTypeText(bizTypeText(item.getBizType()));
            vo.setRuleName(item.getRuleName());
            vo.setFeeItemType(item.getFeeItemType());
            vo.setBillMonth(item.getBillMonth());
            vo.setAmount(item.getAmount());
            vo.setDiscountAmount(item.getDiscountAmount());
            vo.setPaidAmount(item.getPaidAmount());
            vo.setUnpaidAmount(item.getUnpaidAmount());
            vo.setCreateTime(item.getCreateTime());
            return vo;
        }).toList();
    }

    private String bizTypeText(String bizType) {
        if (bizType == null) return null;
        return switch (bizType) {
            case CommonConst.BIZ_TYPE_WATER_ELEC -> "水电费";
            case CommonConst.BIZ_TYPE_PROPERTY_FEE -> "物业费";
            default -> bizType;
        };
    }
}