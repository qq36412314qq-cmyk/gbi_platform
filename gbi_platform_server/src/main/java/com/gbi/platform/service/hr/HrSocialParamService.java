package com.gbi.platform.service.hr;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gbi.platform.dto.hr.HrSocialParamDTO;
import com.gbi.platform.entity.hr.HrSocialParamConfig;
import com.gbi.platform.vo.hr.HrSocialParamVO;

/**
 * 社保公积金参数配置服务接口
 */
public interface HrSocialParamService extends IService<HrSocialParamConfig> {

    /**
     * 分页查询
     */
    IPage<HrSocialParamVO> page(Integer pageNum, Integer pageSize, String cityCode, String insuranceCode, Long companyId);

    /**
     * 新增参数配置
     */
    void add(HrSocialParamDTO dto, Long companyId);

    /**
     * 编辑参数配置
     */
    void edit(HrSocialParamDTO dto, Long companyId);

    /**
     * 激活参数配置（停用同城市同险种其他生效记录）
     */
    void activate(Long id, Long companyId);

    /**
     * 切换参数配置状态（启用/停用）
     */
    void toggle(Long id, Long companyId);

    /**
     * 删除参数配置
     */
    void delete(Long id, Long companyId);
}
