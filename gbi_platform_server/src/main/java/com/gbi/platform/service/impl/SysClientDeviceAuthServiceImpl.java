package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.dto.ClientDeviceAuthDTO;
import com.gbi.platform.entity.SysClientDeviceAuth;
import com.gbi.platform.mapper.SysClientDeviceAuthMapper;
import com.gbi.platform.service.SysClientDeviceAuthService;
import com.gbi.platform.vo.ClientDeviceAuthVO;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 客户端设备授权服务实现
 *
 * @author gbi
 */
@Service
@RequiredArgsConstructor
public class SysClientDeviceAuthServiceImpl implements SysClientDeviceAuthService {

    private final SysClientDeviceAuthMapper deviceAuthMapper;

    @Override
    public PageVO<ClientDeviceAuthVO> page(Integer pageNum, Integer pageSize, String motherboardSn, String cpuId) {
        LambdaQueryWrapper<SysClientDeviceAuth> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(motherboardSn)) {
            wrapper.like(SysClientDeviceAuth::getMotherboardSn, motherboardSn);
        }
        if (StringUtils.hasText(cpuId)) {
            wrapper.like(SysClientDeviceAuth::getCpuId, cpuId);
        }
        wrapper.orderByDesc(SysClientDeviceAuth::getCreateTime);

        Page<SysClientDeviceAuth> page = deviceAuthMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);

        return new PageVO<>(
                page.getRecords()
                        .stream()
                        .map(this::convertToVO)
                        .collect(Collectors.toList()),
                page.getTotal(),
                (long) pageNum,
                (long) pageSize,
                page.getPages());
    }

    @Override
    public void add(ClientDeviceAuthDTO dto) {
        SysClientDeviceAuth entity = new SysClientDeviceAuth();
        entity.setMotherboardSn(dto.getMotherboardSn());
        entity.setCpuId(dto.getCpuId());
        entity.setDiskSn(dto.getDiskSn());
        entity.setDeviceName(dto.getDeviceName());
        entity.setExpireTime(dto.getExpireTime());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        deviceAuthMapper.insert(entity);
    }

    @Override
    public void update(ClientDeviceAuthDTO dto) {
        SysClientDeviceAuth entity = deviceAuthMapper.selectById(dto.getId());
        if (entity == null) {
            throw new RuntimeException("授权设备不存在");
        }
        entity.setMotherboardSn(dto.getMotherboardSn());
        entity.setCpuId(dto.getCpuId());
        entity.setDiskSn(dto.getDiskSn());
        entity.setDeviceName(dto.getDeviceName());
        entity.setExpireTime(dto.getExpireTime());
        entity.setStatus(dto.getStatus());
        deviceAuthMapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        deviceAuthMapper.deleteById(id);
    }

    @Override
    public boolean checkAuth(String motherboardSn, String cpuId) {
        SysClientDeviceAuth auth = deviceAuthMapper.selectOne(
                new LambdaQueryWrapper<SysClientDeviceAuth>()
                        .eq(SysClientDeviceAuth::getMotherboardSn, motherboardSn)
                        .eq(SysClientDeviceAuth::getCpuId, cpuId)
                        .eq(SysClientDeviceAuth::getStatus, 1)
                        .last("LIMIT 1"));
        if (auth == null) {
            return false;
        }
        // 检查有效期
        if (auth.getExpireTime() != null && auth.getExpireTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        return true;
    }

    @Override
    public Long upsertByHardware(String motherboardSn, String cpuId, String diskSn) {
        SysClientDeviceAuth existing = deviceAuthMapper.selectOne(
                new LambdaQueryWrapper<SysClientDeviceAuth>()
                        .eq(SysClientDeviceAuth::getMotherboardSn, motherboardSn)
                        .eq(SysClientDeviceAuth::getCpuId, cpuId)
                        .last("LIMIT 1"));
        if (existing != null) {
            // 存在则更新硬盘SN，触发update_time
            if (StringUtils.hasText(diskSn)) {
                existing.setDiskSn(diskSn);
            }
            deviceAuthMapper.updateById(existing);
            return existing.getId();
        }
        // 不存在则新增（状态默认启用）
        SysClientDeviceAuth entity = new SysClientDeviceAuth();
        entity.setMotherboardSn(motherboardSn);
        entity.setCpuId(cpuId);
        entity.setDiskSn(diskSn);
        entity.setStatus(0);
        deviceAuthMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void toggleStatus(Long id) {
        SysClientDeviceAuth entity = deviceAuthMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("授权设备不存在");
        }
        entity.setStatus(entity.getStatus() == 1 ? 0 : 1);
        deviceAuthMapper.updateById(entity);
    }

    private ClientDeviceAuthVO convertToVO(SysClientDeviceAuth entity) {
        ClientDeviceAuthVO vo = new ClientDeviceAuthVO();
        vo.setId(entity.getId());
        vo.setMotherboardSn(entity.getMotherboardSn());
        vo.setCpuId(entity.getCpuId());
        vo.setDiskSn(entity.getDiskSn());
        vo.setDeviceName(entity.getDeviceName());
        vo.setAuthorizedBy(entity.getAuthorizedBy());
        vo.setExpireTime(entity.getExpireTime());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
