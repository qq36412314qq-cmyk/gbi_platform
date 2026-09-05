package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.MarketDTO;
import com.gbi.platform.dto.MarketQueryDTO;
import com.gbi.platform.entity.MarketInfo;
import com.gbi.platform.mapper.MarketInfoMapper;
import com.gbi.platform.service.LeaseStallService;
import com.gbi.platform.service.MarketService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.MarketVO;
import com.gbi.platform.vo.PageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 市场档案服务实现：园区/商圈维度维护
 * 同公司市场名称唯一；删除前置校验：市场下存在铺位禁止删除（跨模块调用 LeaseStallService）；
 * 新增/编辑/删除强制审计（oper_module=market_info）
 *
 * @author gbi
 */
@Slf4j
@Service
public class MarketServiceImpl implements MarketService {

    private final MarketInfoMapper marketInfoMapper;

    private final LeaseStallService leaseStallService;

    private final AuditLogUtil auditLogUtil;

    /**
     * @Lazy 标注在构造器参数上，打破与 LeaseStallServiceImpl 的业务互查循环依赖
     * （市场删除校验铺位 / 铺位新增编辑校验市场存在）
     * 注意：Lombok @RequiredArgsConstructor 不会把字段上的 @Lazy 传播到构造器参数，必须手写构造器
     */
    public MarketServiceImpl(MarketInfoMapper marketInfoMapper,
                             @Lazy LeaseStallService leaseStallService,
                             AuditLogUtil auditLogUtil) {
        this.marketInfoMapper = marketInfoMapper;
        this.leaseStallService = leaseStallService;
        this.auditLogUtil = auditLogUtil;
    }

    @Override
    public PageVO<MarketVO> page(MarketQueryDTO dto) {
        Page<MarketInfo> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<MarketInfo> wrapper = new LambdaQueryWrapper<MarketInfo>()
                .like(StringUtils.hasText(dto.getMarketName()), MarketInfo::getMarketName, dto.getMarketName())
                .eq(dto.getStatus() != null, MarketInfo::getStatus, dto.getStatus())
                .orderByDesc(MarketInfo::getId);
        Page<MarketInfo> result = marketInfoMapper.selectPage(page, wrapper);
        List<MarketVO> voList = result.getRecords().stream().map(this::toVO).toList();
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public List<MarketVO> list() {
        // 下拉选择仅返回启用市场
        List<MarketInfo> markets = marketInfoMapper.selectList(new LambdaQueryWrapper<MarketInfo>()
                .eq(MarketInfo::getStatus, CommonConst.STATUS_ENABLED)
                .orderByDesc(MarketInfo::getId));
        return markets.stream().map(this::toVO).toList();
    }

    @Override
    public void add(MarketDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        checkNameUnique(null, dto.getMarketName());

        MarketInfo market = new MarketInfo();
        market.setCompanyId(loginUser.getCompanyId());
        market.setMarketName(dto.getMarketName());
        market.setMarketAddress(dto.getMarketAddress());
        market.setContactPerson(dto.getContactPerson());
        market.setContactPhone(dto.getContactPhone());
        market.setStatus(dto.getStatus() == null ? CommonConst.STATUS_ENABLED : dto.getStatus());
        market.setRemark(dto.getRemark());
        marketInfoMapper.insert(market);

        auditLogUtil.record(CommonConst.MODULE_MARKET, CommonConst.OPER_TYPE_ADD,
                String.valueOf(market.getId()), null, market);
    }

    @Override
    public void update(MarketDTO dto) {
        if (dto.getId() == null) {
            throw new BizException("市场ID不能为空");
        }
        MarketInfo old = getExists(dto.getId());
        checkNameUnique(dto.getId(), dto.getMarketName());

        MarketInfo market = new MarketInfo();
        market.setId(dto.getId());
        market.setMarketName(dto.getMarketName());
        market.setMarketAddress(dto.getMarketAddress());
        market.setContactPerson(dto.getContactPerson());
        market.setContactPhone(dto.getContactPhone());
        market.setStatus(dto.getStatus());
        market.setRemark(dto.getRemark());
        marketInfoMapper.updateById(market);

        auditLogUtil.record(CommonConst.MODULE_MARKET, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(dto.getId()), old, market);
    }

    @Override
    public void delete(Long id) {
        MarketInfo market = getExists(id);
        // 市场下存在铺位禁止删除（跨模块调用铺位 Service 接口统计）
        if (leaseStallService.countByMarketId(id) > 0) {
            throw new BizException("该市场下存在铺位，禁止删除");
        }
        marketInfoMapper.deleteById(id);

        auditLogUtil.record(CommonConst.MODULE_MARKET, CommonConst.OPER_TYPE_DELETE,
                String.valueOf(id), market, null);
    }

    @Override
    public boolean existsById(Long id) {
        return id != null && marketInfoMapper.selectById(id) != null;
    }

    @Override
    public Map<Long, String> getNamesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<MarketInfo> markets = marketInfoMapper.selectBatchIds(ids);
        return markets.stream().collect(Collectors.toMap(MarketInfo::getId, MarketInfo::getMarketName));
    }

    /**
     * 同公司市场名称唯一校验
     */
    private void checkNameUnique(Long excludeId, String marketName) {
        Long count = marketInfoMapper.selectCount(new LambdaQueryWrapper<MarketInfo>()
                .eq(MarketInfo::getMarketName, marketName)
                .ne(excludeId != null, MarketInfo::getId, excludeId));
        if (count != null && count > 0) {
            throw new BizException("市场名称已存在");
        }
    }

    private MarketInfo getExists(Long id) {
        MarketInfo market = marketInfoMapper.selectById(id);
        if (market == null) {
            throw new BizException("市场不存在或已删除");
        }
        return market;
    }

    private MarketVO toVO(MarketInfo m) {
        MarketVO vo = new MarketVO();
        vo.setId(m.getId());
        vo.setCompanyId(m.getCompanyId());
        vo.setMarketName(m.getMarketName());
        vo.setMarketAddress(m.getMarketAddress());
        vo.setContactPerson(m.getContactPerson());
        vo.setContactPhone(m.getContactPhone());
        vo.setStatus(m.getStatus());
        vo.setRemark(m.getRemark());
        vo.setCreateTime(m.getCreateTime());
        return vo;
    }
}