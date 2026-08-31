package com.gbi.platform.controller.sys;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.ThemeDTO;
import com.gbi.platform.service.ThemeService;
import com.gbi.platform.vo.ThemeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UI 主题接口（对齐前端 api/sys.ts theme 部分）
 *
 * @author gbi
 */
@Tag(name = "UI主题")
@RestController
@RequestMapping("/sys/theme")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    @Operation(summary = "查询当前公司主题")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).THEME_LIST,'')")
    @GetMapping("/get")
    public Result<ThemeVO> get() {
        return Result.success(themeService.get());
    }

    @Operation(summary = "保存主题")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).THEME_EDIT,'')")
    @PostMapping("/save")
    public Result<Void> save(@Valid @RequestBody ThemeDTO dto) {
        themeService.save(dto);
        return Result.success();
    }
}