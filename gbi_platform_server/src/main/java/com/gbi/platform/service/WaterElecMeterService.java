package com.gbi.platform.service;

import com.gbi.platform.dto.WaterElecMeterAddDTO;
import com.gbi.platform.dto.WaterElecMeterQueryDTO;
import com.gbi.platform.dto.WaterElecMeterUpdateDTO;
import com.gbi.platform.dto.WaterElecReadDTO;
import com.gbi.platform.dto.WaterElecSwitchDTO;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.WaterElecMeterVO;

/**
 * 智能水电表设备服务：设备台账 + 远程抄表 + 合闸断电
 *
 * @author gbi
 */
public interface WaterElecMeterService {

    /**
     * 设备分页（自动 company_id 隔离）
     */
    PageVO<WaterElecMeterVO> page(WaterElecMeterQueryDTO dto);

    /**
     * 新增设备（company_id 从登录上下文获取）
     */
    void add(WaterElecMeterAddDTO dto);

    /**
     * 编辑设备
     */
    void update(WaterElecMeterUpdateDTO dto);

    /**
     * 删除设备（逻辑删除）
     */
    void delete(Long id);

    /**
     * 远程抄表：提交新读数，校验不小于当前读数，更新设备读数
     */
    void read(WaterElecReadDTO dto);

    /**
     * 远程合闸/断电（高危第三方操作，强制审计）
     */
    void switchPower(WaterElecSwitchDTO dto);
}
