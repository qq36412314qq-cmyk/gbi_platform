package com.gbi.platform.service;

import com.gbi.platform.dto.ConfigDTO;
import com.gbi.platform.vo.ConfigVO;
import com.gbi.platform.vo.PageVO;

import java.util.List;
import java.util.Map;

/**
 * 系统参数服务：sys_config（修改走审计）
 *
 * @author gbi
 */
public interface ConfigService {

    /**
     * 参数分页
     */
    PageVO<ConfigVO> page(Integer pageNum, Integer pageSize, String configName, String configKey);

    /**
     * 新增参数
     */
    void add(ConfigDTO dto);

    /**
     * 编辑参数
     */
    void update(ConfigDTO dto);

    /**
     * 删除参数（逻辑删除）
     */
    void delete(Long id);

    /**
     * 按配置 key 读取参数值（集团全局 company_id=0 优先，无则返回 null）
     * 供水电计费等跨模块业务读取集团统一配置
     *
     * @param configKey 参数 key
     * @return 参数值，未配置返回 null
     */
    String getValueByKey(String configKey);

    /**
     * 批量读取集团全局参数值（company_id=0 优先，未配置返回空 Map）
     * 供前端页面按参数控制字段可写性（合同金额/优惠参数开关等）
     *
     * @param configKeys 参数 key 集合
     * @return key -> value 映射
     */
    Map<String, String> getValuesByKeys(List<String> configKeys);
}
