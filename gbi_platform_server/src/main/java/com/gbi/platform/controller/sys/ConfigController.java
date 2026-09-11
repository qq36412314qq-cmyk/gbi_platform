package com.gbi.platform.controller.sys;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.ConfigDTO;
import com.gbi.platform.service.ConfigService;
import com.gbi.platform.vo.ConfigVO;
import com.gbi.platform.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 系统参数接口（对齐前端 api/sys.ts config 部分）
 *
 * @author gbi
 */
@Tag(name = "系统参数")
@RestController
@RequestMapping("/sys/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    @Operation(summary = "参数分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).CONFIG_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<ConfigVO>> page(@RequestParam Integer pageNum,
                                         @RequestParam Integer pageSize,
                                         @RequestParam(required = false) String configName,
                                         @RequestParam(required = false) String configKey) {
        return Result.success(configService.page(pageNum, pageSize, configName, configKey));
    }

    @Operation(summary = "按 key 批量读取集团参数值（子公司只读共享，供页面控制可写性）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).CONFIG_LIST,'')")
    @GetMapping("/values")
    public Result<Map<String, String>> values(@RequestParam List<String> keys) {
        return Result.success(configService.getValuesByKeys(keys));
    }

    @Operation(summary = "新增参数")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).CONFIG_ADD,'')")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody ConfigDTO dto) {
        configService.add(dto);
        return Result.success();
    }

    @Operation(summary = "编辑参数")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).CONFIG_EDIT,'')")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody ConfigDTO dto) {
        configService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除参数")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).CONFIG_DELETE,'')")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        configService.delete(id);
        return Result.success();
    }
}