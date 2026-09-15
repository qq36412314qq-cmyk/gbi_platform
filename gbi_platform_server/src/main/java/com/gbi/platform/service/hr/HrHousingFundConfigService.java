package com.gbi.platform.service.hr;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gbi.platform.dto.hr.HrHousingFundDTO;
import com.gbi.platform.entity.hr.HrHousingFundConfig;
import com.gbi.platform.vo.hr.HrHousingFundVO;

/**
 * 公积金参数配置服务接口
 */
public interface HrHousingFundConfigService extends IService<HrHousingFundConfig> {

    /**
     * 分页查询
     */
    IPage<HrHousingFundVO> page(Integer pageNum, Integer pageSize, String cityCode, Long companyId);

    /**
     * 新增参数配置
     */
    void add(HrHousingFundDTO dto, Long companyId);

    /**
     * 激活参数配置（停用同城市其他生效记录）
     */
    void activate(Long id, Long companyId);

    /**
     * 删除参数配置
     */
    void delete(Long id, Long companyId);
}
