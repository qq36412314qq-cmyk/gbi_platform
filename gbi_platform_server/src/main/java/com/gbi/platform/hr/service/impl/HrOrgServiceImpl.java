package com.gbi.platform.hr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.hr.dto.PostDTO;
import com.gbi.platform.hr.entity.HrPost;
import com.gbi.platform.hr.mapper.HrPostMapper;
import com.gbi.platform.hr.service.HrOrgService;
import com.gbi.platform.hr.vo.HrPostVO;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HrOrgServiceImpl implements HrOrgService {

    private final HrPostMapper postMapper;
    private final AuditLogUtil auditLogUtil;

    @Override
    public PageVO<HrPostVO> pagePost(Long pageNum, Long pageSize, Integer status) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<HrPost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HrPost> wrapper = new LambdaQueryWrapper<HrPost>()
                .eq(HrPost::getCompanyId, loginUser.getCompanyId())
                .eq(status != null, HrPost::getStatus, status)
                .orderByDesc(HrPost::getCreateTime);
        Page<HrPost> result = postMapper.selectPage(page, wrapper);
        List<HrPostVO> voList = result.getRecords().stream().map(this::toPostVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public void addPost(PostDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        HrPost post = new HrPost();
        post.setCompanyId(loginUser.getCompanyId());
        post.setPostName(dto.getPostName());
        post.setPostCode(dto.getPostCode());
        post.setPostLevel(dto.getPostLevel());
        post.setDeptId(dto.getDeptId());
        post.setStatus(dto.getStatus() != null ? dto.getStatus() : CommonConst.STATUS_ENABLED);
        post.setRemark(dto.getRemark());
        post.setCreateBy(loginUser.getUserId());
        postMapper.insert(post);
        auditLogUtil.record(CommonConst.MODULE_HR_ORG, CommonConst.OPER_TYPE_ADD,
                String.valueOf(post.getId()), null, post);
    }

    @Override
    public void updatePost(PostDTO dto) {
        if (dto.getId() == null) throw new BizException("岗位ID不能为空");
        HrPost oldPost = postMapper.selectById(dto.getId());
        if (oldPost == null) throw new BizException("岗位不存在");
        HrPost post = new HrPost();
        post.setId(dto.getId());
        post.setPostName(dto.getPostName());
        post.setPostCode(dto.getPostCode());
        post.setPostLevel(dto.getPostLevel());
        post.setDeptId(dto.getDeptId());
        post.setStatus(dto.getStatus());
        post.setRemark(dto.getRemark());
        post.setUpdateBy(UserContext.getLoginUser().getUserId());
        postMapper.updateById(post);
        auditLogUtil.record(CommonConst.MODULE_HR_ORG, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(dto.getId()), oldPost, post);
    }

    @Override
    public void deletePost(Long id) {
        HrPost post = postMapper.selectById(id);
        if (post == null) throw new BizException("岗位不存在");
        postMapper.deleteById(id);
        auditLogUtil.record(CommonConst.MODULE_HR_ORG, CommonConst.OPER_TYPE_DELETE,
                String.valueOf(id), post, null);
    }

    private HrPostVO toPostVO(HrPost entity) {
        HrPostVO vo = new HrPostVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setPostName(entity.getPostName());
        vo.setPostCode(entity.getPostCode());
        vo.setPostLevel(entity.getPostLevel());
        vo.setDeptId(entity.getDeptId());
        vo.setStatus(entity.getStatus());
        vo.setStatusText(entity.getStatus() != null && entity.getStatus() == 0 ? "禁用" : "启用");
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}
