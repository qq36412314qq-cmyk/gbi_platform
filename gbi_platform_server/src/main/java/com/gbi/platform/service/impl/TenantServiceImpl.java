package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.TenantAddDTO;
import com.gbi.platform.dto.TenantQueryDTO;
import com.gbi.platform.dto.TenantUpdateDTO;
import com.gbi.platform.entity.StallTenant;
import com.gbi.platform.mapper.StallTenantMapper;
import com.gbi.platform.service.LeaseContractService;
import com.gbi.platform.service.TenantService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.TenantVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * 租户档案服务实现：商户/租户入驻建档
 * 敏感字段（手机/身份证/银行账号）VO 脱敏返回；新增/编辑/删除强制审计；
 * 删除前置校验：存在生效合同禁止删除（跨模块调用 LeaseContractService 接口）
 *
 * @author gbi
 */
@Slf4j
@Service
public class TenantServiceImpl implements TenantService {

    private final StallTenantMapper tenantMapper;

    private final LeaseContractService leaseContractService;

    private final AuditLogUtil auditLogUtil;

    /**
     * @Lazy 标注在构造器参数上，打破与 LeaseContractServiceImpl 的业务互查循环依赖
     * （租户删除校验合同 / 合同新增校验租户）
     */
    public TenantServiceImpl(StallTenantMapper tenantMapper,
                             @Lazy LeaseContractService leaseContractService,
                             AuditLogUtil auditLogUtil) {
        this.tenantMapper = tenantMapper;
        this.leaseContractService = leaseContractService;
        this.auditLogUtil = auditLogUtil;
    }

    @Override
    public PageVO<TenantVO> page(TenantQueryDTO dto) {
        Page<StallTenant> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<StallTenant> wrapper = new LambdaQueryWrapper<StallTenant>()
                .like(StringUtils.hasText(dto.getTenantName()), StallTenant::getTenantName, dto.getTenantName())
                .eq(dto.getTenantType() != null, StallTenant::getTenantType, dto.getTenantType())
                .like(StringUtils.hasText(dto.getContactPhone()), StallTenant::getContactPhone, dto.getContactPhone())
                .eq(dto.getStatus() != null, StallTenant::getStatus, dto.getStatus())
                .orderByDesc(StallTenant::getId);
        Page<StallTenant> result = tenantMapper.selectPage(page, wrapper);

        List<TenantVO> voList = result.getRecords().stream().map(r -> {
            TenantVO vo = new TenantVO();
            vo.setId(r.getId());
            vo.setCompanyId(r.getCompanyId());
            vo.setTenantName(r.getTenantName());
            vo.setTenantType(r.getTenantType());
            vo.setTenantTypeText(tenantTypeText(r.getTenantType()));
            vo.setContactPerson(r.getContactPerson());
            // 敏感字段脱敏返回（数据库保留完整/密文）
            vo.setContactPhone(maskPhone(r.getContactPhone()));
            vo.setIdCardNo(maskIdCard(r.getIdCardNo()));
            vo.setSocialCreditCode(r.getSocialCreditCode());
            vo.setBankAccount(maskBank(r.getBankAccount()));
            vo.setAddress(r.getAddress());
            vo.setStatus(r.getStatus());
            vo.setRemark(r.getRemark());
            vo.setCreateTime(r.getCreateTime());
            return vo;
        }).toList();
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public void add(TenantAddDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        // 同公司租户名称唯一校验（防止重复建档）
        Long count = tenantMapper.selectCount(new LambdaQueryWrapper<StallTenant>()
                .eq(StallTenant::getTenantName, dto.getTenantName()));
        if (count != null && count > 0) {
            throw new BizException("租户名称已存在，请勿重复建档");
        }

        StallTenant tenant = new StallTenant();
        tenant.setCompanyId(loginUser.getCompanyId());
        tenant.setTenantName(dto.getTenantName());
        tenant.setTenantType(dto.getTenantType() == null ? CommonConst.TENANT_TYPE_PERSONAL : dto.getTenantType());
        tenant.setContactPerson(dto.getContactPerson());
        tenant.setContactPhone(dto.getContactPhone());
        tenant.setIdCardNo(dto.getIdCardNo());
        tenant.setSocialCreditCode(dto.getSocialCreditCode());
        tenant.setBankAccount(dto.getBankAccount());
        tenant.setAddress(dto.getAddress());
        tenant.setMiniOpenid(dto.getMiniOpenid());
        tenant.setStatus(dto.getStatus() == null ? CommonConst.STATUS_ENABLED : dto.getStatus());
        tenant.setRemark(dto.getRemark());
        tenantMapper.insert(tenant);

        auditLogUtil.record(CommonConst.MODULE_TENANT, CommonConst.OPER_TYPE_ADD,
                String.valueOf(tenant.getId()), null, tenant);
    }

    @Override
    public void update(TenantUpdateDTO dto) {
        StallTenant old = getExists(dto.getId());
        // 同公司同名唯一校验（排除自身）
        Long count = tenantMapper.selectCount(new LambdaQueryWrapper<StallTenant>()
                .eq(StallTenant::getTenantName, dto.getTenantName())
                .ne(StallTenant::getId, dto.getId()));
        if (count != null && count > 0) {
            throw new BizException("租户名称已存在");
        }

        StallTenant tenant = new StallTenant();
        tenant.setId(dto.getId());
        tenant.setTenantName(dto.getTenantName());
        tenant.setTenantType(dto.getTenantType());
        tenant.setContactPerson(dto.getContactPerson());
        tenant.setContactPhone(dto.getContactPhone());
        tenant.setIdCardNo(dto.getIdCardNo());
        tenant.setSocialCreditCode(dto.getSocialCreditCode());
        tenant.setBankAccount(dto.getBankAccount());
        tenant.setAddress(dto.getAddress());
        tenant.setMiniOpenid(dto.getMiniOpenid());
        tenant.setStatus(dto.getStatus() == null ? CommonConst.STATUS_ENABLED : dto.getStatus());
        tenant.setRemark(dto.getRemark());
        tenantMapper.updateById(tenant);

        auditLogUtil.record(CommonConst.MODULE_TENANT, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(dto.getId()), old, tenant);
    }

    @Override
    public void delete(Long id) {
        StallTenant tenant = getExists(id);
        // 存在生效合同禁止删除（跨模块调用合同 Service 接口校验）
        if (leaseContractService.hasEffectiveContractByTenant(id)) {
            throw new BizException("该租户存在生效租赁合同，禁止删除");
        }
        tenantMapper.deleteById(id);

        auditLogUtil.record(CommonConst.MODULE_TENANT, CommonConst.OPER_TYPE_DELETE,
                String.valueOf(id), tenant, null);
    }

    @Override
    public boolean existsById(Long id) {
        return tenantMapper.selectById(id) != null;
    }

    /**
     * 查询租户并校验存在（多租户拦截器自动带 company_id 条件）
     */
    private StallTenant getExists(Long id) {
        StallTenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BizException("租户不存在或已删除");
        }
        return tenant;
    }

    /** 租户类型文本：1个体工商户 2企业 3个人 */
    private String tenantTypeText(Integer type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case CommonConst.TENANT_TYPE_PERSONAL -> "个体工商户";
            case CommonConst.TENANT_TYPE_COMPANY -> "企业";
            case CommonConst.TENANT_TYPE_INDIVIDUAL -> "个人";
            default -> "未知";
        };
    }

    /** 手机号脱敏：138****1234 */
    private String maskPhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /** 身份证脱敏：前4后3 */
    private String maskIdCard(String idCard) {
        if (!StringUtils.hasText(idCard) || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 3);
    }

    /** 银行账号脱敏：仅展示后四位 */
    private String maskBank(String bankAccount) {
        if (!StringUtils.hasText(bankAccount)) {
            return null;
        }
        return "****" + bankAccount.substring(bankAccount.length() - 4);
    }
}