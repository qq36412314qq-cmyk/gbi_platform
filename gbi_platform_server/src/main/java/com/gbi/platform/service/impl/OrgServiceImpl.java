package com.gbi.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.OrgDTO;
import com.gbi.platform.entity.SysOrg;
import com.gbi.platform.mapper.SysOrgMapper;
import com.gbi.platform.service.OrgService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.OrgTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组织服务实现：树形维护，公司数据隔离由多租户拦截器保证
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrgServiceImpl implements OrgService {

    private final SysOrgMapper orgMapper;

    private final AuditLogUtil auditLogUtil;

    @Override
    public List<OrgTreeVO> tree() {
        List<SysOrg> all = orgMapper.selectList(new LambdaQueryWrapper<SysOrg>()
                .orderByAsc(SysOrg::getSortOrder)
                .orderByAsc(SysOrg::getId));
        // 组装树
        Map<Long, OrgTreeVO> map = all.stream().collect(Collectors.toMap(SysOrg::getId, this::toVO));
        List<OrgTreeVO> roots = new ArrayList<>();
        for (SysOrg org : all) {
            OrgTreeVO vo = map.get(org.getId());
            if (org.getParentId() == null || org.getParentId() == 0L) {
                roots.add(vo);
            } else {
                OrgTreeVO parent = map.get(org.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    roots.add(vo);
                }
            }
        }
        return roots;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(OrgDTO dto) {
        // 同名校验（同一上级下）
        Long exist = orgMapper.selectCount(new LambdaQueryWrapper<SysOrg>()
                .eq(SysOrg::getParentId, dto.getParentId())
                .eq(SysOrg::getOrgName, dto.getOrgName()));
        if (exist != null && exist > 0) {
            throw new BizException("同级下已存在组织：" + dto.getOrgName());
        }
        SysOrg org = new SysOrg();
        // 组织归属：子公司账号只能维护本公司组织（companyId 强制登录人）
        org.setCompanyId(UserContext.getLoginUser().getCompanyId());
        org.setParentId(dto.getParentId());
        org.setOrgName(dto.getOrgName());
        org.setOrgType(dto.getOrgType());
        org.setSortOrder(dto.getSortOrder());
        org.setStatus(dto.getStatus());
        orgMapper.insert(org);
        auditLogUtil.record(CommonConst.MODULE_ORG, CommonConst.OPER_TYPE_ADD,
                String.valueOf(org.getId()), null, org);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(OrgDTO dto) {
        SysOrg org = orgMapper.selectById(dto.getId());
        if (org == null) {
            throw new BizException("组织不存在");
        }
        // 不能把上级设为自己或自己的子孙
        if (dto.getParentId() != null && !dto.getParentId().equals(0L) && !dto.getParentId().equals(org.getParentId())) {
            if (dto.getParentId().equals(org.getId()) || isDescendant(org.getId(), dto.getParentId())) {
                throw new BizException("上级组织不能设置为自身或下级组织");
            }
        }
        Long exist = orgMapper.selectCount(new LambdaQueryWrapper<SysOrg>()
                .eq(SysOrg::getParentId, dto.getParentId())
                .eq(SysOrg::getOrgName, dto.getOrgName())
                .ne(SysOrg::getId, dto.getId()));
        if (exist != null && exist > 0) {
            throw new BizException("同级下已存在组织：" + dto.getOrgName());
        }
        SysOrg before = new SysOrg();
        BeanUtil.copyProperties(org, before);
        org.setParentId(dto.getParentId());
        org.setOrgName(dto.getOrgName());
        org.setOrgType(dto.getOrgType());
        org.setSortOrder(dto.getSortOrder());
        org.setStatus(dto.getStatus());
        orgMapper.updateById(org);
        auditLogUtil.record(CommonConst.MODULE_ORG, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(org.getId()), before, org);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysOrg org = orgMapper.selectById(id);
        if (org == null) {
            throw new BizException("组织不存在");
        }
        Long children = orgMapper.selectCount(new LambdaQueryWrapper<SysOrg>()
                .eq(SysOrg::getParentId, id));
        if (children != null && children > 0) {
            throw new BizException("存在下级组织，不允许删除");
        }
        orgMapper.deleteById(id);
        auditLogUtil.record(CommonConst.MODULE_ORG, CommonConst.OPER_TYPE_DELETE, String.valueOf(id), org, null);
    }

    /**
     * 判断 candidateId 是否为 orgId 的子孙节点
     */
    private boolean isDescendant(Long orgId, Long candidateId) {
        List<Long> children = orgMapper.selectList(new LambdaQueryWrapper<SysOrg>()
                        .eq(SysOrg::getParentId, orgId))
                .stream().map(SysOrg::getId).toList();
        if (children.isEmpty()) {
            return false;
        }
        if (children.contains(candidateId)) {
            return true;
        }
        for (Long child : children) {
            if (isDescendant(child, candidateId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 实体转树节点
     */
    private OrgTreeVO toVO(SysOrg org) {
        OrgTreeVO vo = new OrgTreeVO();
        vo.setId(org.getId());
        vo.setCompanyId(org.getCompanyId());
        vo.setParentId(org.getParentId());
        vo.setOrgName(org.getOrgName());
        vo.setOrgType(org.getOrgType());
        vo.setSortOrder(org.getSortOrder());
        vo.setStatus(org.getStatus());
        vo.setChildren(new ArrayList<>());
        return vo;
    }
}