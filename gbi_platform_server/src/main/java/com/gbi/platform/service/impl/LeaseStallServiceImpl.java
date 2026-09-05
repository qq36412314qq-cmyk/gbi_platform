package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.StallAddDTO;
import com.gbi.platform.dto.StallQueryDTO;
import com.gbi.platform.dto.StallUpdateDTO;
import com.gbi.platform.entity.StallCategory;
import com.gbi.platform.entity.StallInfo;
import com.gbi.platform.mapper.StallCategoryMapper;
import com.gbi.platform.mapper.StallInfoMapper;
import com.gbi.platform.service.LeaseContractService;
import com.gbi.platform.service.LeaseStallService;
import com.gbi.platform.service.FeeRuleStallRelService;
import com.gbi.platform.service.MarketService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.StallOptionVO;
import com.gbi.platform.vo.StallRuleRelVO;
import com.gbi.platform.vo.StallVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 租赁铺位服务实现：租赁标的（商铺/仓库/车位等，分类可自定义）
 * 编号同公司唯一；新增初始状态空置；删除前置校验：存在合同禁止删除；
 * 新增/编辑/删除强制审计（oper_module=stall_lease）
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaseStallServiceImpl implements LeaseStallService {

    private final StallInfoMapper stallMapper;

    private final StallCategoryMapper categoryMapper;

    /** 跨模块调用市场 Service 接口校验市场存在与名称组装 */
    private final MarketService marketService;

    private final LeaseContractService leaseContractService;

    /** 跨模块调用收费规则绑定 Service 接口：铺位收费规则全量替换绑定（同收费类型限选一条） */
    private final FeeRuleStallRelService feeRuleStallRelService;

    private final AuditLogUtil auditLogUtil;

    @Override
    public PageVO<StallVO> page(StallQueryDTO dto) {
        Page<StallInfo> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<StallInfo> wrapper = new LambdaQueryWrapper<StallInfo>()
                .like(StringUtils.hasText(dto.getStallNumber()), StallInfo::getStallNumber, dto.getStallNumber())
                .eq(dto.getStallCategoryId() != null, StallInfo::getStallCategoryId, dto.getStallCategoryId())
                .eq(dto.getStatus() != null, StallInfo::getStatus, dto.getStatus())
                .eq(dto.getMarketId() != null, StallInfo::getMarketId, dto.getMarketId())
                .orderByAsc(StallInfo::getStallNumber);
        Page<StallInfo> result = stallMapper.selectPage(page, wrapper);

        // 批量组装分类名称（租赁管理模块内部 mapper 组装）
        List<Long> categoryIds = result.getRecords().stream()
                .map(StallInfo::getStallCategoryId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> categoryNameMap = categoryIds.isEmpty() ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(StallCategory::getId, StallCategory::getCategoryName));

        // 批量组装市场名称（跨模块调用市场 Service 接口）
        List<Long> marketIds = result.getRecords().stream()
                .map(StallInfo::getMarketId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> marketNameMap = marketService.getNamesByIds(marketIds);

        // 批量组装已绑定收费规则（跨模块调用绑定 Service 接口，一次查询避免 N+1）
        List<Long> stallIds = result.getRecords().stream().map(StallInfo::getId).toList();
        Map<Long, List<StallRuleRelVO>> feeRuleMap = feeRuleStallRelService.listByStallIds(stallIds);

        List<StallVO> voList = result.getRecords().stream().map(r -> {
            StallVO vo = new StallVO();
            vo.setId(r.getId());
            vo.setCompanyId(r.getCompanyId());
            vo.setMarketId(r.getMarketId());
            vo.setMarketName(r.getMarketId() == null ? null
                    : marketNameMap.getOrDefault(r.getMarketId(), "-"));
            vo.setStallCategoryId(r.getStallCategoryId());
            vo.setCategoryName(r.getStallCategoryId() == null ? null
                    : categoryNameMap.getOrDefault(r.getStallCategoryId(), "-"));
            vo.setStallNumber(r.getStallNumber());
            vo.setStallName(r.getStallName());
            vo.setStallArea(r.getStallArea());
            vo.setStatus(r.getStatus());
            vo.setStatusText(stallStatusText(r.getStatus()));
            vo.setRemark(r.getRemark());
            vo.setCreateTime(r.getCreateTime());
            vo.setFeeRules(feeRuleMap.getOrDefault(r.getId(), Collections.emptyList()));
            return vo;
        }).toList();
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(StallAddDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        checkNumberUnique(null, dto.getStallNumber());
        checkCategory(dto.getStallCategoryId());
        checkMarket(dto.getMarketId());

        StallInfo stall = new StallInfo();
        stall.setCompanyId(loginUser.getCompanyId());
        stall.setMarketId(dto.getMarketId());
        stall.setStallCategoryId(dto.getStallCategoryId());
        stall.setStallNumber(dto.getStallNumber());
        stall.setStallName(dto.getStallName());
        stall.setStallArea(dto.getStallArea());
        stall.setStatus(CommonConst.STALL_STATUS_EMPTY);
        stall.setRemark(dto.getRemark());
        stallMapper.insert(stall);

        // 铺位+收费规则绑定同事务，任一失败整体回滚
        feeRuleStallRelService.saveBindings(stall.getId(), dto.getRuleIds());

        auditLogUtil.record(CommonConst.MODULE_LEASE_STALL, CommonConst.OPER_TYPE_ADD,
                String.valueOf(stall.getId()), null, stall);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(StallUpdateDTO dto) {
        StallInfo old = getExists(dto.getId());
        checkNumberUnique(dto.getId(), dto.getStallNumber());
        checkCategory(dto.getStallCategoryId());
        checkMarket(dto.getMarketId());

        StallInfo stall = new StallInfo();
        stall.setId(dto.getId());
        stall.setMarketId(dto.getMarketId());
        stall.setStallCategoryId(dto.getStallCategoryId());
        stall.setStallNumber(dto.getStallNumber());
        stall.setStallName(dto.getStallName());
        stall.setStallArea(dto.getStallArea());
        stall.setStatus(dto.getStatus());
        stall.setRemark(dto.getRemark());
        stallMapper.updateById(stall);

        // 铺位+收费规则绑定同事务，任一失败整体回滚
        feeRuleStallRelService.saveBindings(dto.getId(), dto.getRuleIds());

        auditLogUtil.record(CommonConst.MODULE_LEASE_STALL, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(dto.getId()), old, stall);
    }

    @Override
    public void delete(Long id) {
        StallInfo stall = getExists(id);
        // 存在合同禁止删除（跨模块调用合同 Service 接口校验）
        if (leaseContractService.hasAnyContractByStall(id)) {
            throw new BizException("该铺位存在租赁合同，禁止删除");
        }
        stallMapper.deleteById(id);

        auditLogUtil.record(CommonConst.MODULE_LEASE_STALL, CommonConst.OPER_TYPE_DELETE,
                String.valueOf(id), stall, null);
    }

    @Override
    public long countByCategoryId(Long categoryId) {
        Long count = stallMapper.selectCount(new LambdaQueryWrapper<StallInfo>()
                .eq(StallInfo::getStallCategoryId, categoryId));
        return count == null ? 0L : count;
    }

    @Override
    public long countByMarketId(Long marketId) {
        Long count = stallMapper.selectCount(new LambdaQueryWrapper<StallInfo>()
                .eq(StallInfo::getMarketId, marketId));
        return count == null ? 0L : count;
    }

    @Override
    public List<StallOptionVO> listOptions(Long marketId, Long stallCategoryId, Integer status) {
        // 市场/分类/状态三条件可选过滤，company_id 由多租户拦截器自动隔离
        // status=0 空置（合同新增选择用）；水电表绑定不传 status 查全部状态
        List<StallInfo> stalls = stallMapper.selectList(new LambdaQueryWrapper<StallInfo>()
                .eq(marketId != null, StallInfo::getMarketId, marketId)
                .eq(stallCategoryId != null, StallInfo::getStallCategoryId, stallCategoryId)
                .eq(status != null, StallInfo::getStatus, status)
                .orderByAsc(StallInfo::getStallNumber));
        return assembleOptions(stalls);
    }

    @Override
    public Map<Long, StallOptionVO> getOptionsByIds(List<Long> stallIds) {
        if (stallIds == null || stallIds.isEmpty()) {
            return Collections.emptyMap();
        }
        // selectBatchIds 查询同样受 company_id 拦截器约束，跨公司铺位查询不到
        List<StallInfo> stalls = stallMapper.selectBatchIds(stallIds);
        return assembleOptions(stalls).stream()
                .collect(Collectors.toMap(StallOptionVO::getId, o -> o));
    }

    @Override
    public List<Long> listStallIdsByMarket(Long marketId) {
        return stallMapper.selectList(new LambdaQueryWrapper<StallInfo>()
                        .eq(StallInfo::getMarketId, marketId))
                .stream().map(StallInfo::getId).toList();
    }

    /**
     * 铺位选项组装（批量补充分类名称与市场名称）
     */
    private List<StallOptionVO> assembleOptions(List<StallInfo> stalls) {
        if (stalls.isEmpty()) {
            return Collections.emptyList();
        }
        // 分类名称（本模块 mapper 组装）
        List<Long> categoryIds = stalls.stream()
                .map(StallInfo::getStallCategoryId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> categoryNameMap = categoryIds.isEmpty() ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(StallCategory::getId, StallCategory::getCategoryName));
        // 市场名称（跨模块调用市场 Service 接口）
        List<Long> marketIds = stalls.stream()
                .map(StallInfo::getMarketId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> marketNameMap = marketService.getNamesByIds(marketIds);

        return stalls.stream().map(s -> {
            StallOptionVO vo = new StallOptionVO();
            vo.setId(s.getId());
            vo.setStallNumber(s.getStallNumber());
            vo.setStallName(s.getStallName());
            vo.setMarketId(s.getMarketId());
            vo.setMarketName(s.getMarketId() == null ? null
                    : marketNameMap.getOrDefault(s.getMarketId(), "-"));
            vo.setStallCategoryId(s.getStallCategoryId());
            vo.setCategoryName(s.getStallCategoryId() == null ? null
                    : categoryNameMap.getOrDefault(s.getStallCategoryId(), "-"));
            vo.setStallArea(s.getStallArea());
            return vo;
        }).toList();
    }

    /**
     * 市场存在性校验（跨模块调用市场 Service 接口，防止铺位挂靠不存在的市场）
     */
    private void checkMarket(Long marketId) {
        if (marketId == null) {
            return;
        }
        if (!marketService.existsById(marketId)) {
            throw new BizException("市场不存在或已删除");
        }
    }

    /**
     * 铺位编号同公司唯一校验
     */
    private void checkNumberUnique(Long excludeId, String stallNumber) {
        Long count = stallMapper.selectCount(new LambdaQueryWrapper<StallInfo>()
                .eq(StallInfo::getStallNumber, stallNumber)
                .ne(excludeId != null, StallInfo::getId, excludeId));
        if (count != null && count > 0) {
            throw new BizException("铺位编号已存在");
        }
    }

    /**
     * 分类存在性校验
     */
    private void checkCategory(Long categoryId) {
        if (categoryId == null) {
            return;
        }
        if (categoryMapper.selectById(categoryId) == null) {
            throw new BizException("租赁分类不存在或已删除");
        }
    }

    private StallInfo getExists(Long id) {
        StallInfo stall = stallMapper.selectById(id);
        if (stall == null) {
            throw new BizException("铺位不存在或已删除");
        }
        return stall;
    }

    private String stallStatusText(Integer status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case CommonConst.STALL_STATUS_EMPTY -> "空置";
            case CommonConst.STALL_STATUS_RENTED -> "已租";
            case CommonConst.STALL_STATUS_OVERDUE -> "欠费";
            case CommonConst.STALL_STATUS_EXPIRE_SOON -> "即将到期";
            default -> "未知";
        };
    }
}