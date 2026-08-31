package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.dto.DictDataDTO;
import com.gbi.platform.dto.DictTypeDTO;
import com.gbi.platform.entity.SysDictData;
import cn.hutool.core.bean.BeanUtil;
import com.gbi.platform.entity.SysDictType;
import cn.hutool.core.bean.BeanUtil;
import com.gbi.platform.mapper.SysDictDataMapper;
import com.gbi.platform.mapper.SysDictTypeMapper;
import com.gbi.platform.service.DictService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.DictItemVO;
import com.gbi.platform.vo.DictTypeVO;
import com.gbi.platform.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 字典服务实现：Redis 缓存（gbi:dict:{code}）+ DB 兜底，变更即清缓存
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final SysDictTypeMapper dictTypeMapper;

    private final SysDictDataMapper dictDataMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    private final AuditLogUtil auditLogUtil;

    @Override
    public List<DictItemVO> getByCode(String dictCode) {
        // 1. 优先读 Redis 缓存
        String key = CommonConst.REDIS_KEY_DICT + dictCode;
        try {
            String cached = stringRedisTemplate.opsForValue().get(key);
            if (StringUtils.hasText(cached)) {
                return objectMapper.readValue(cached, new TypeReference<List<DictItemVO>>() {
                });
            }
        } catch (Exception e) {
            // Redis 异常降级查库
            log.warn("字典缓存读取失败，降级查库: dictCode={}", dictCode, e);
        }
        // 2. 查库并写回缓存
        List<DictItemVO> list = queryFromDb(dictCode);
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(list));
        } catch (Exception e) {
            log.warn("字典缓存写入失败: dictCode={}", dictCode, e);
        }
        return list;
    }

    @Override
    public PageVO<DictTypeVO> pageTypes(Integer pageNum, Integer pageSize, String dictName, String dictCode) {
        Page<SysDictType> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<SysDictType>()
                .like(StringUtils.hasText(dictName), SysDictType::getDictName, dictName)
                .like(StringUtils.hasText(dictCode), SysDictType::getDictCode, dictCode)
                .orderByDesc(SysDictType::getId);
        Page<SysDictType> result = dictTypeMapper.selectPage(page, wrapper);
        List<DictTypeVO> voList = result.getRecords().stream().map(t -> {
            DictTypeVO vo = new DictTypeVO();
            vo.setId(t.getId());
            vo.setDictCode(t.getDictCode());
            vo.setDictName(t.getDictName());
            vo.setStatus(t.getStatus());
            vo.setRemark(t.getRemark());
            vo.setCreateTime(t.getCreateTime());
            return vo;
        }).toList();
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addType(DictTypeDTO dto) {
        Long exist = dictTypeMapper.selectCount(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictCode, dto.getDictCode()));
        if (exist != null && exist > 0) {
            throw new BizException("字典编码已存在：" + dto.getDictCode());
        }
        SysDictType type = new SysDictType();
        type.setDictCode(dto.getDictCode());
        type.setDictName(dto.getDictName());
        type.setStatus(dto.getStatus());
        type.setRemark(dto.getRemark());
        dictTypeMapper.insert(type);
        auditLogUtil.record(CommonConst.MODULE_SYS, CommonConst.OPER_TYPE_ADD,
                String.valueOf(type.getId()), null, type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateType(DictTypeDTO dto) {
        SysDictType type = dictTypeMapper.selectById(dto.getId());
        if (type == null) {
            throw new BizException("字典类型不存在");
        }
        Long exist = dictTypeMapper.selectCount(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictCode, dto.getDictCode())
                .ne(SysDictType::getId, dto.getId()));
        if (exist != null && exist > 0) {
            throw new BizException("字典编码已存在：" + dto.getDictCode());
        }
        String oldCode = type.getDictCode();
        type.setDictCode(dto.getDictCode());
        type.setDictName(dto.getDictName());
        type.setStatus(dto.getStatus());
        type.setRemark(dto.getRemark());
        dictTypeMapper.updateById(type);
        clearCache(oldCode);
        clearCache(dto.getDictCode());
        auditLogUtil.record(CommonConst.MODULE_SYS, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(type.getId()), null, type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteType(Long id) {
        SysDictType type = dictTypeMapper.selectById(id);
        if (type == null) {
            throw new BizException("字典类型不存在");
        }
        // 级联逻辑删除数据项
        dictDataMapper.delete(new LambdaQueryWrapper<SysDictData>().eq(SysDictData::getDictTypeId, id));
        dictTypeMapper.deleteById(id);
        clearCache(type.getDictCode());
        auditLogUtil.record(CommonConst.MODULE_SYS, CommonConst.OPER_TYPE_DELETE, String.valueOf(id), type, null);
    }

    @Override
    public List<DictItemVO> listData(Long dictTypeId) {
        return dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                        .eq(SysDictData::getDictTypeId, dictTypeId)
                        .orderByAsc(SysDictData::getSortOrder))
                .stream().map(d -> {
                    DictItemVO vo = new DictItemVO();
                    vo.setId(d.getId());
                    vo.setDictTypeId(d.getDictTypeId());
                    vo.setDictValue(d.getDictValue());
                    vo.setDictKey(d.getDictKey());
                    vo.setSortOrder(d.getSortOrder());
                    vo.setStatus(d.getStatus());
                    return vo;
                }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addData(DictDataDTO dto) {
        SysDictData data = new SysDictData();
        data.setDictTypeId(dto.getDictTypeId());
        data.setDictValue(dto.getDictValue());
        data.setDictKey(dto.getDictKey());
        data.setSortOrder(dto.getSortOrder());
        data.setStatus(dto.getStatus());
        dictDataMapper.insert(data);
        clearCache(dto.getDictTypeId());
        auditLogUtil.record(CommonConst.MODULE_SYS, CommonConst.OPER_TYPE_ADD,
                String.valueOf(data.getId()), null, data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateData(DictDataDTO dto) {
        SysDictData data = dictDataMapper.selectById(dto.getId());
        if (data == null) {
            throw new BizException("字典数据不存在");
        }
        SysDictData before = new SysDictData();
        cn.hutool.core.bean.BeanUtil.copyProperties(data, before);
        data.setDictValue(dto.getDictValue());
        data.setDictKey(dto.getDictKey());
        data.setSortOrder(dto.getSortOrder());
        data.setStatus(dto.getStatus());
        dictDataMapper.updateById(data);
        clearCache(data.getDictTypeId());
        auditLogUtil.record(CommonConst.MODULE_SYS, CommonConst.OPER_TYPE_UPDATE,
                String.valueOf(data.getId()), before, data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteData(Long id) {
        SysDictData data = dictDataMapper.selectById(id);
        if (data == null) {
            throw new BizException("字典数据不存在");
        }
        dictDataMapper.deleteById(id);
        clearCache(data.getDictTypeId());
        auditLogUtil.record(CommonConst.MODULE_SYS, CommonConst.OPER_TYPE_DELETE, String.valueOf(id), data, null);
    }

    /**
     * 查库组装启用数据项
     */
    private List<DictItemVO> queryFromDb(String dictCode) {
        SysDictType type = dictTypeMapper.selectOne(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictCode, dictCode)
                .eq(SysDictType::getStatus, CommonConst.STATUS_ENABLED)
                .last("LIMIT 1"));
        if (type == null) {
            return new ArrayList<>();
        }
        return dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                        .eq(SysDictData::getDictTypeId, type.getId())
                        .eq(SysDictData::getStatus, CommonConst.STATUS_ENABLED)
                        .orderByAsc(SysDictData::getSortOrder))
                .stream().map(d -> {
                    DictItemVO vo = new DictItemVO();
                    vo.setId(d.getId());
                    vo.setDictTypeId(d.getDictTypeId());
                    vo.setDictValue(d.getDictValue());
                    vo.setDictKey(d.getDictKey());
                    vo.setSortOrder(d.getSortOrder());
                    vo.setStatus(d.getStatus());
                    return vo;
                }).toList();
    }

    /**
     * 按类型ID清缓存（查类型编码后删除对应 Redis key）
     */
    private void clearCache(Long dictTypeId) {
        if (dictTypeId == null) {
            return;
        }
        SysDictType type = dictTypeMapper.selectById(dictTypeId);
        if (type != null) {
            clearCache(type.getDictCode());
        }
    }

    /**
     * 按编码清缓存
     */
    private void clearCache(String dictCode) {
        try {
            stringRedisTemplate.delete(CommonConst.REDIS_KEY_DICT + dictCode);
        } catch (Exception e) {
            log.warn("字典缓存清理失败: dictCode={}", dictCode, e);
        }
    }
}