package com.gbi.platform.service;

import com.gbi.platform.dto.DictDataDTO;
import com.gbi.platform.dto.DictTypeDTO;
import com.gbi.platform.vo.DictItemVO;
import com.gbi.platform.vo.DictTypeVO;
import com.gbi.platform.vo.PageVO;

/**
 * 字典服务：Redis 缓存 + 数据库兜底（对齐《后端编码规范》九）
 *
 * @author gbi
 */
public interface DictService {

    /**
     * 按字典编码查询启用数据（Redis 缓存，未命中查库并写回；Redis 异常自动降级查库）
     */
    java.util.List<DictItemVO> getByCode(String dictCode);

    /**
     * 字典类型分页
     */
    PageVO<DictTypeVO> pageTypes(Integer pageNum, Integer pageSize, String dictName, String dictCode);

    /**
     * 新增字典类型
     */
    void addType(DictTypeDTO dto);

    /**
     * 编辑字典类型
     */
    void updateType(DictTypeDTO dto);

    /**
     * 删除字典类型（级联删除数据项，逻辑删除）
     */
    void deleteType(Long id);

    /**
     * 按类型ID查询字典数据
     */
    java.util.List<DictItemVO> listData(Long dictTypeId);

    /**
     * 新增字典数据
     */
    void addData(DictDataDTO dto);

    /**
     * 编辑字典数据
     */
    void updateData(DictDataDTO dto);

    /**
     * 删除字典数据
     */
    void deleteData(Long id);
}
