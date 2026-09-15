package com.gbi.platform.service;

import com.gbi.platform.dto.ClientDeviceAuthDTO;
import com.gbi.platform.vo.ClientDeviceAuthVO;
import com.gbi.platform.vo.PageVO;

/**
 * 客户端设备授权服务
 *
 * @author gbi
 */
public interface SysClientDeviceAuthService {

    /**
     * 分页查询授权设备
     */
    PageVO<ClientDeviceAuthVO> page(Integer pageNum, Integer pageSize, String motherboardSn, String cpuId);

    /**
     * 新增授权设备
     */
    void add(ClientDeviceAuthDTO dto);

    /**
     * 编辑授权设备
     */
    void update(ClientDeviceAuthDTO dto);

    /**
     * 删除授权设备（逻辑删除）
     */
    void delete(Long id);

    /**
     * 校验硬件信息是否在授权清单
     *
     * @param motherboardSn 主板SN
     * @param cpuId CPU编号
     * @return true=已授权 false=未授权
     */
    boolean checkAuth(String motherboardSn, String cpuId);

    /**
     * 按硬件信息注册或更新设备授权记录（客户端自动上报用，无鉴权）
     * 存在则更新时间戳和硬盘SN，不存在则新增
     *
     * @param motherboardSn 主板SN
     * @param cpuId CPU编号
     * @param diskSn 硬盘序列号（可选）
     * @return 设备ID，null表示未找到
     */
    Long upsertByHardware(String motherboardSn, String cpuId, String diskSn);

    /**
     * 切换设备授权状态（启用/禁用）
     *
     * @param id 设备ID
     */
    void toggleStatus(Long id);
}
