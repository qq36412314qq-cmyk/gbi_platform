package com.gbi.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import com.gbi.platform.dto.ThemeDTO;
import com.gbi.platform.entity.SysUiTheme;
import cn.hutool.core.bean.BeanUtil;
import com.gbi.platform.mapper.SysUiThemeMapper;
import com.gbi.platform.service.ThemeService;
import com.gbi.platform.util.AuditLogUtil;
import com.gbi.platform.vo.ThemeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UI 主题服务实现：sys_ui_theme（按公司隔离，保存实时生效并审计）
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThemeServiceImpl implements ThemeService {

    /** 默认主题（与前端 store/theme.ts DEFAULT_THEME 保持一致） */
    private static final String DEFAULT_PRIMARY_COLOR = "#2f6bff";

    private static final String DEFAULT_LAYOUT_MODE = "side";

    private static final int DEFAULT_CARD_RADIUS = 6;

    private static final int DEFAULT_DARK_MODE = 0;

    private final SysUiThemeMapper themeMapper;

    private final AuditLogUtil auditLogUtil;

    @Override
    public ThemeVO get() {
        Long companyId = UserContext.getLoginUser().getCompanyId();
        SysUiTheme theme = themeMapper.selectOne(new LambdaQueryWrapper<SysUiTheme>()
                .eq(SysUiTheme::getCompanyId, companyId)
                .last("LIMIT 1"));
        if (theme == null) {
            // 无配置返回默认主题
            ThemeVO vo = new ThemeVO();
            vo.setCompanyId(companyId);
            vo.setPrimaryColor(DEFAULT_PRIMARY_COLOR);
            vo.setLayoutMode(DEFAULT_LAYOUT_MODE);
            vo.setCardRadius(DEFAULT_CARD_RADIUS);
            vo.setDarkMode(DEFAULT_DARK_MODE);
            return vo;
        }
        return toVO(theme);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(ThemeDTO dto) {
        LoginUser loginUser = UserContext.getLoginUser();
        Long companyId = loginUser.isSuperAdmin() ? dto.getCompanyId() : loginUser.getCompanyId();
        SysUiTheme theme = themeMapper.selectOne(new LambdaQueryWrapper<SysUiTheme>()
                .eq(SysUiTheme::getCompanyId, companyId)
                .last("LIMIT 1"));
        SysUiTheme before = null;
        if (theme != null) {
            before = new SysUiTheme();
            cn.hutool.core.bean.BeanUtil.copyProperties(theme, before);
            theme.setPrimaryColor(dto.getPrimaryColor());
            theme.setLayoutMode(dto.getLayoutMode());
            theme.setCardRadius(dto.getCardRadius());
            theme.setDarkMode(dto.getDarkMode());
            themeMapper.updateById(theme);
        } else {
            theme = new SysUiTheme();
            theme.setCompanyId(companyId);
            theme.setPrimaryColor(dto.getPrimaryColor());
            theme.setLayoutMode(dto.getLayoutMode());
            theme.setCardRadius(dto.getCardRadius());
            theme.setDarkMode(dto.getDarkMode());
            themeMapper.insert(theme);
        }
        auditLogUtil.record(CommonConst.MODULE_SYS, "保存主题",
                String.valueOf(theme.getId()), before, theme);
    }

    /**
     * 实体转 VO
     */
    private ThemeVO toVO(SysUiTheme theme) {
        ThemeVO vo = new ThemeVO();
        vo.setId(theme.getId());
        vo.setCompanyId(theme.getCompanyId());
        vo.setPrimaryColor(theme.getPrimaryColor());
        vo.setLayoutMode(theme.getLayoutMode());
        vo.setCardRadius(theme.getCardRadius());
        vo.setDarkMode(theme.getDarkMode());
        return vo;
    }
}