package com.gbi.platform.service;

import com.gbi.platform.dto.ThemeDTO;
import com.gbi.platform.vo.ThemeVO;

/**
 * UI 主题服务：sys_ui_theme（保存走审计，实时生效）
 *
 * @author gbi
 */
public interface ThemeService {

    /**
     * 查询当前公司主题；无配置返回默认主题
     */
    ThemeVO get();

    /**
     * 保存主题（存在则更新，不存在则新增）
     */
    void save(ThemeDTO dto);
}
