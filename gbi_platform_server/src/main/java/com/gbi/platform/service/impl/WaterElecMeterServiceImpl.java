package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.WaterElecMeterAddDTO;
import com.gbi.platform.dto.WaterElecMeterQueryDTO;
import com.gbi.platform.dto.WaterElecMeterUpdateDTO;
import com.gbi.platform.dto.WaterElecReadDTO;
import com.gbi.platform.dto.WaterElecSwitchDTO;
import com.gbi.platform.entity.WaterElecMeter;
import com.gbi.platform.mapper.WaterElecMeterMapper;
import com.gbi.platform.service.LeaseStallService;
import com.gbi.platform.service.WaterElecMeterService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.StallOptionVO;
import com.gbi.platform.vo.WaterElecMeterVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 智能水电表设备服务实现：设备台账 + 远程抄表 + 合闸断电
 * 多租户：所有查询由拦截器自动拼接 company_id，新增从登录上下文取值
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WaterElecMeterServiceImpl implements WaterElecMeterService {

    private final WaterElecMeterMapper meterMapper;

    private final AuditLogUtil auditLogUtil;

    /** 跨模块调用租赁摊位 Service 接口：校验绑定摊位归属 + 组装摊位名称 */
    private final LeaseStallService leaseStallService;

    @Override
    public PageVO<WaterElecMeterVO> page(WaterElecMeterQueryDTO dto) {
        Page<WaterElecMeter> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<WaterElecMeter> wrapper = new LambdaQueryWrapper<WaterElecMeter>()
                .like(StringUtils.hasText(dto.getMeterNo()), WaterElecMeter::getMeterNo, dto.getMeterNo())
                .eq(dto.getMeterType() != null, WaterElecMeter::getMeterType, dto.getMeterType())
                .eq(dto.getStatus() != null, WaterElecMeter::getStatus, dto.getStatus());
        // 按绑定摊位所属市场过滤（跨模块调用租赁摊位 Service 接口，company 自动隔离）
        if (dto.getMarketId() != null) {
            List<Long> marketStallIds = leaseStallService.listStallIdsByMarket(dto.getMarketId());
            if (marketStallIds.isEmpty()) {
                // 该市场下无摊位，直接返回空页
                return new PageVO<WaterElecMeterVO>(List.of(), 0L,
                        dto.getPageNum().longValue(), dto.getPageSize().longValue(), 0L);
            }
            wrapper.in(WaterElecMeter::getStallId, marketStallIds);
        }
        wrapper.orderByDesc(WaterElecMeter::getId);
        Page<WaterElecMeter> result = meterMapper.selectPage(page, wrapper);

        // 批量组装绑定摊位信息（跨模块调用租赁摊位 Service 接口，company 自动隔离）
        List<Long> stallIds = result.getRecords().stream()
                .map(WaterElecMeter::getStallId).filter(Objects::nonNull).distinct().toList();
        Map<Long, StallOptionVO> stallMap = leaseStallService.getOptionsByIds(stallIds);

        List<WaterElecMeterVO> voList = result.getRecords().stream()
                .map(m -> toVO(m, stallMap)).toList();
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(WaterElecMeterAddDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        checkMeterNoUnique(loginUser.getCompanyId(), dto.getMeterNo(), null);
        checkStallBindable(dto.getStallId());
        WaterElecMeter meter = new WaterElecMeter();
        meter.setCompanyId(loginUser.getCompanyId());
        meter.setStallId(dto.getStallId());
        meter.setMeterNo(dto.getMeterNo());
        meter.setMeterType(dto.getMeterType());
        meter.setGatewayCode(dto.getGatewayCode());
        meter.setCurrentRead(dto.getCurrentRead() == null ? BigDecimal.ZERO : dto.getCurrentRead());
        meter.setBalanceAmount(dto.getBalanceAmount() == null ? BigDecimal.ZERO : dto.getBalanceAmount());
        meter.setStatus(CommonConst.METER_STATUS_POWER_ON);
        meterMapper.insert(meter);
        auditLogUtil.record(CommonConst.MODULE_WATER_ELEC, CommonConst.OPER_TYPE_ADD,
                String.valueOf(meter.getId()), null, meter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WaterElecMeterUpdateDTO dto) {
        WaterElecMeter meter = getMeter(dto.getId());
        checkMeterNoUnique(meter.getCompanyId(), dto.getMeterNo(), dto.getId());
        checkStallBindable(dto.getStallId());
        WaterElecMeter before = copyBefore(meter);
        meter.setStallId(dto.getStallId());
        meter.setMeterNo(dto.getMeterNo());
        meter.setMeterType(dto.getMeterType());
        meter.setGatewayCode(dto.getGatewayCode());
        if (dto.getCurrentRead() != null) {
            // 编辑时读数只允许上调，防止数据回退
            if (meter.getCurrentRead() != null && dto.getCurrentRead().compareTo(meter.getCurrentRead()) < 0) {
                throw new BizException("读数不能小于当前读数");
            }
            meter.setCurrentRead(dto.getCurrentRead());
        }
        if (dto.getBalanceAmount() != null) {
            meter.setBalanceAmount(dto.getBalanceAmount());
        }
        meterMapper.updateById(meter);
        auditLogUtil.record(CommonConst.MODULE_WATER_ELEC, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(meter.getId()), before, meter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        WaterElecMeter meter = getMeter(id);
        meterMapper.deleteById(id);
        auditLogUtil.record(CommonConst.MODULE_WATER_ELEC, CommonConst.OPER_TYPE_DELETE,
                String.valueOf(id), meter, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void read(WaterElecReadDTO dto) {
        WaterElecMeter meter = getMeter(dto.getMeterId());
        if (meter.getCurrentRead() != null && dto.getCurrentRead().compareTo(meter.getCurrentRead()) < 0) {
            throw new BizException("本次读数不能小于当前读数");
        }
        WaterElecMeter before = copyBefore(meter);
        meter.setCurrentRead(dto.getCurrentRead());
        meterMapper.updateById(meter);
        auditLogUtil.record(CommonConst.MODULE_WATER_ELEC, CommonConst.OPER_TYPE_READ,
                String.valueOf(meter.getId()), before, meter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchPower(WaterElecSwitchDTO dto) {
        WaterElecMeter meter = getMeter(dto.getMeterId());
        if (Objects.equals(meter.getStatus(), dto.getStatus())) {
            return;
        }
        WaterElecMeter before = copyBefore(meter);
        meter.setStatus(dto.getStatus());
        meterMapper.updateById(meter);
        // 合闸/断电属于高危第三方操作，强制审计留痕
        auditLogUtil.record(CommonConst.MODULE_WATER_ELEC, CommonConst.OPER_TYPE_SWITCH,
                String.valueOf(meter.getId()), before, meter);
    }

    /**
     * 绑定摊位归属校验（跨模块调用租赁摊位 Service 接口）
     * 防止手输/篡改摊位ID绑定他公司摊位，查询结果受 company_id 拦截器约束
     */
    private void checkStallBindable(Long stallId) {
        Map<Long, StallOptionVO> map = leaseStallService.getOptionsByIds(
                Collections.singletonList(stallId));
        if (map.isEmpty()) {
            throw new BizException("绑定摊位不存在或已删除");
        }
    }

    /**
     * 查询设备（多租户隔离由拦截器自动追加 company_id，查不到抛出业务异常）
     */
    private WaterElecMeter getMeter(Long id) {
        WaterElecMeter meter = meterMapper.selectById(id);
        if (meter == null) {
            throw new BizException("设备不存在或已删除");
        }
        return meter;
    }

    /**
     * 同公司设备编号唯一校验
     */
    private void checkMeterNoUnique(Long companyId, String meterNo, Long excludeId) {
        LambdaQueryWrapper<WaterElecMeter> wrapper = new LambdaQueryWrapper<WaterElecMeter>()
                .eq(WaterElecMeter::getCompanyId, companyId)
                .eq(WaterElecMeter::getMeterNo, meterNo);
        if (excludeId != null) {
            wrapper.ne(WaterElecMeter::getId, excludeId);
        }
        Long count = meterMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BizException("设备编号已存在：" + meterNo);
        }
    }

    /**
     * 操作前快照
     */
    private WaterElecMeter copyBefore(WaterElecMeter meter) {
        WaterElecMeter before = new WaterElecMeter();
        cn.hutool.core.bean.BeanUtil.copyProperties(meter, before);
        return before;
    }

    /**
     * 组装 VO（状态文本 + 绑定摊位信息）
     */
    private WaterElecMeterVO toVO(WaterElecMeter meter, Map<Long, StallOptionVO> stallMap) {
        WaterElecMeterVO vo = new WaterElecMeterVO();
        vo.setId(meter.getId());
        vo.setCompanyId(meter.getCompanyId());
        vo.setStallId(meter.getStallId());
        StallOptionVO stall = stallMap.get(meter.getStallId());
        vo.setStallNumber(stall == null ? null : stall.getStallNumber());
        vo.setStallName(stall == null ? null : stall.getStallName());
        vo.setStallMarketId(stall == null ? null : stall.getMarketId());
        vo.setStallMarketName(stall == null ? null : stall.getMarketName());
        vo.setStallCategoryId(stall == null ? null : stall.getStallCategoryId());
        vo.setCategoryName(stall == null ? null : stall.getCategoryName());
        vo.setMeterNo(meter.getMeterNo());
        vo.setMeterType(meter.getMeterType());
        vo.setMeterTypeText(meter.getMeterType() == null ? null
                : (meter.getMeterType() == CommonConst.METER_TYPE_WATER ? "水表" : "电表"));
        vo.setGatewayCode(meter.getGatewayCode());
        vo.setCurrentRead(meter.getCurrentRead());
        vo.setBalanceAmount(meter.getBalanceAmount());
        vo.setStatus(meter.getStatus());
        vo.setStatusText(meter.getStatus() == null ? null
                : (meter.getStatus() == CommonConst.METER_STATUS_POWER_ON ? "通电" : "断电"));
        vo.setCreateTime(meter.getCreateTime());
        return vo;
    }
}