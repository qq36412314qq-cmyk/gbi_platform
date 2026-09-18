package com.gbi.platform.service.impl.hr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.hr.PostDTO;
import com.gbi.platform.entity.SysOrg;
import com.gbi.platform.entity.hr.HrPost;
import com.gbi.platform.mapper.hr.HrPostMapper;
import com.gbi.platform.service.hr.HrOrgService;
import com.gbi.platform.vo.hr.HrPostVO;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HrOrgServiceImpl implements HrOrgService {

    private final HrPostMapper postMapper;
    private final com.gbi.platform.mapper.SysOrgMapper orgMapper;
    private final AuditLogUtil auditLogUtil;

    /**
     * 构建组织完整路径："公司-部门-科室"，集团直属部门则显示 "集团-部门-科室" 或纯部门名
     */
    private String buildOrgPath(Long deptId, Map<Long, SysOrg> orgMap) {
        if (deptId == null) return null;
        List<String> parts = new ArrayList<>();
        Long curr = deptId;
        while (curr != null && curr != 0L) {
            SysOrg org = orgMap.get(curr);
            if (org == null) break;
            parts.add(0, org.getOrgName());
            Long pid = org.getParentId();
            // 集团直属部门（parentId=0/Null）且 companyId=0：不再向上追溯公司前缀
            if ((pid == null || pid == 0L) && org.getCompanyId() != null && org.getCompanyId() == 0L) {
                break;
            }
            curr = pid;
        }
        return String.join("-", parts);
    }

    @Override
    public PageVO<HrPostVO> pagePost(Long pageNum, Long pageSize, Integer status) {
        LoginUser loginUser = UserContext.getLoginUser();
        Page<HrPost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HrPost> wrapper = new LambdaQueryWrapper<HrPost>()
                .eq(HrPost::getCompanyId, loginUser.getCompanyId())
                .eq(status != null, HrPost::getStatus, status)
                .orderByDesc(HrPost::getCreateTime);
        Page<HrPost> result = postMapper.selectPage(page, wrapper);

        // 一次性加载所有组织，构建 ID→实体 Map，避免 N+1 查询
        Map<Long, SysOrg> orgMap = orgMapper.selectList(new LambdaQueryWrapper<SysOrg>())
                .stream().collect(Collectors.toMap(SysOrg::getId, o -> o, (a, b) -> a));

        List<HrPostVO> voList = result.getRecords().stream()
                .map(e -> toPostVO(e, orgMap))
                .collect(Collectors.toList());
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

    @Override
    public List<HrPostVO> getPostByDept(Long deptId) {
        LoginUser loginUser = UserContext.getLoginUser();
        LambdaQueryWrapper<HrPost> wrapper = new LambdaQueryWrapper<HrPost>()
                .eq(HrPost::getCompanyId, loginUser.getCompanyId())
                .eq(HrPost::getDeptId, deptId)
                .eq(HrPost::getStatus, 1)
                .orderByAsc(HrPost::getPostName);
        List<HrPost> posts = postMapper.selectList(wrapper);
        // 一次性加载所有组织，构建 ID→实体 Map
        Map<Long, SysOrg> orgMap = orgMapper.selectList(new LambdaQueryWrapper<SysOrg>())
                .stream().collect(Collectors.toMap(SysOrg::getId, o -> o, (a, b) -> a));
        return posts.stream().map(e -> toPostVO(e, orgMap)).collect(Collectors.toList());
    }

    private HrPostVO toPostVO(HrPost entity, Map<Long, SysOrg> orgMap) {
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
        // 计算所属部门完整路径
        if (entity.getDeptId() != null) {
            String path = buildOrgPath(entity.getDeptId(), orgMap);
            if (path != null) vo.setOrgName(path);
        }
        return vo;
    }
}
