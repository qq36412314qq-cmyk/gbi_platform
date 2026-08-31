package com.gbi.platform.hr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.hr.dto.SocialDTO;
import com.gbi.platform.hr.entity.HrEmployee;
import com.gbi.platform.hr.entity.HrSocialSecurity;
import com.gbi.platform.hr.mapper.HrSocialSecurityMapper;
import com.gbi.platform.hr.service.HrSocialService;
import com.gbi.platform.hr.vo.HrSocialVO;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HrSocialServiceImpl implements HrSocialService {

    private final HrSocialSecurityMapper socialMapper;
    private final AuditLogUtil auditLogUtil;

    @Override
    public PageVO<HrSocialVO> pageSocial(Long pageNum, Long pageSize, Long employeeId, Integer status) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<HrSocialSecurity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HrSocialSecurity> wrapper = new LambdaQueryWrapper<HrSocialSecurity>()
                .eq(HrSocialSecurity::getCompanyId, loginUser.getCompanyId())
                .eq(employeeId != null, HrSocialSecurity::getEmployeeId, employeeId)
                .eq(status != null, HrSocialSecurity::getStatus, status)
                .orderByDesc(HrSocialSecurity::getCreateTime);
        Page<HrSocialSecurity> result = socialMapper.selectPage(page, wrapper);
        List<HrSocialVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSocial(SocialDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        HrSocialSecurity social = new HrSocialSecurity();
        social.setCompanyId(loginUser.getCompanyId());
        social.setEmployeeId(dto.getEmployeeId());
        HrEmployee employee = new HrEmployee();
        employee = null;
        social.setSocialSecurityBase(dto.getSocialSecurityBase());
        social.setHousingFundBase(dto.getHousingFundBase());
        social.setSocialSecurityCompany(dto.getSocialSecurityCompany());
        social.setSocialSecurityPersonal(dto.getSocialSecurityPersonal());
        social.setHousingFundCompany(dto.getHousingFundCompany());
        social.setHousingFundPersonal(dto.getHousingFundPersonal());
        social.setStartMonth(dto.getStartMonth());
        social.setStatus(CommonConst.STATUS_ENABLED);
        social.setRemark(dto.getRemark());
        social.setCreateBy(loginUser.getUserId());
        socialMapper.insert(social);
        auditLogUtil.record(CommonConst.MODULE_HR_SOCIAL, CommonConst.OPER_TYPE_ADD,
                String.valueOf(social.getId()), null, social);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSocial(SocialDTO dto) {
        if (dto.getId() == null) throw new BizException("社保记录ID不能为空");
        HrSocialSecurity oldSocial = socialMapper.selectById(dto.getId());
        if (oldSocial == null) throw new BizException("社保记录不存在");
        HrSocialSecurity social = new HrSocialSecurity();
        social.setId(dto.getId());
        social.setSocialSecurityBase(dto.getSocialSecurityBase());
        social.setHousingFundBase(dto.getHousingFundBase());
        social.setSocialSecurityCompany(dto.getSocialSecurityCompany());
        social.setSocialSecurityPersonal(dto.getSocialSecurityPersonal());
        social.setHousingFundCompany(dto.getHousingFundCompany());
        social.setHousingFundPersonal(dto.getHousingFundPersonal());


        social.setRemark(dto.getRemark());
        social.setUpdateBy(UserContext.getLoginUser().getUserId());
        socialMapper.updateById(social);
        auditLogUtil.record(CommonConst.MODULE_HR_SOCIAL, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(dto.getId()), oldSocial, social);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSocial(Long id) {
        HrSocialSecurity social = socialMapper.selectById(id);
        if (social == null) throw new BizException("社保记录不存在");
        socialMapper.deleteById(id);
        auditLogUtil.record(CommonConst.MODULE_HR_SOCIAL, CommonConst.OPER_TYPE_DELETE,
                String.valueOf(id), social, null);
    }

    private HrSocialVO toVO(HrSocialSecurity entity) {
        HrSocialVO vo = new HrSocialVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setEmployeeId(entity.getEmployeeId());
        vo.setEmployeeName(entity.getEmployeeName());
        vo.setSocialSecurityBase(entity.getSocialSecurityBase());
        vo.setHousingFundBase(entity.getHousingFundBase());
        vo.setSocialSecurityCompany(entity.getSocialSecurityCompany());
        vo.setSocialSecurityPersonal(entity.getSocialSecurityPersonal());
        vo.setHousingFundCompany(entity.getHousingFundCompany());
        vo.setHousingFundPersonal(entity.getHousingFundPersonal());
        vo.setStartMonth(entity.getStartMonth());
        vo.setEndMonth(entity.getEndMonth());
        vo.setStatus(entity.getStatus());
        vo.setStatusText(entity.getStatus() != null && entity.getStatus() == 0 ? "停保" : "参保");
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}

