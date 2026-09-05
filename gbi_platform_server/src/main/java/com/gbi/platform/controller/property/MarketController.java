package com.gbi.platform.controller.property;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.MarketDTO;
import com.gbi.platform.dto.MarketQueryDTO;
import com.gbi.platform.service.MarketService;
import com.gbi.platform.vo.MarketVO;
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

/**
 * 市场管理接口（物业管理模块，铺位/市场地图统一关联，前端 api/market.ts）
 *
 * @author gbi
 */
@Tag(name = "物业管理-市场管理")
@RestController
@RequestMapping("/property/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;

    @Operation(summary = "市场分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).MARKET_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<MarketVO>> page(@Valid MarketQueryDTO dto) {
        return Result.success(marketService.page(dto));
    }

    @Operation(summary = "市场全量列表（下拉选择用，仅启用）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).MARKET_LIST,'')")
    @GetMapping("/list")
    public Result<List<MarketVO>> list() {
        return Result.success(marketService.list());
    }

    @Operation(summary = "新增市场")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).MARKET_ADD,'')")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody MarketDTO dto) {
        marketService.add(dto);
        return Result.success();
    }

    @Operation(summary = "编辑市场")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).MARKET_EDIT,'')")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody MarketDTO dto) {
        marketService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除市场（市场下有铺位禁止删除，逻辑删除）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).MARKET_DELETE,'')")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        marketService.delete(id);
        return Result.success();
    }
}