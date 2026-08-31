package com.gbi.platform.controller.waterElec;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.WaterElecMeterAddDTO;
import com.gbi.platform.dto.WaterElecMeterQueryDTO;
import com.gbi.platform.dto.WaterElecMeterUpdateDTO;
import com.gbi.platform.dto.WaterElecReadDTO;
import com.gbi.platform.dto.WaterElecSwitchDTO;
import com.gbi.platform.service.WaterElecMeterService;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.WaterElecMeterVO;
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

/**
 * 智能水电表设备接口（物业模块，前端 api/waterElec.ts meter 部分）
 *
 * @author gbi
 */
@Tag(name = "水电物业-设备管理")
@RestController
@RequestMapping("/waterElec/meter")
@RequiredArgsConstructor
public class WaterElecMeterController {

    private final WaterElecMeterService waterElecMeterService;

    @Operation(summary = "设备分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WATER_ELEC_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<WaterElecMeterVO>> page(@Valid WaterElecMeterQueryDTO dto) {
        return Result.success(waterElecMeterService.page(dto));
    }

    @Operation(summary = "新增设备")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WATER_ELEC_ADD,'')")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody WaterElecMeterAddDTO dto) {
        waterElecMeterService.add(dto);
        return Result.success();
    }

    @Operation(summary = "编辑设备")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WATER_ELEC_EDIT,'')")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody WaterElecMeterUpdateDTO dto) {
        waterElecMeterService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除设备（逻辑删除）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WATER_ELEC_DELETE,'')")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        waterElecMeterService.delete(id);
        return Result.success();
    }

    @Operation(summary = "远程抄表")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WATER_ELEC_READ,'')")
    @PostMapping("/read")
    public Result<Void> read(@Valid @RequestBody WaterElecReadDTO dto) {
        waterElecMeterService.read(dto);
        return Result.success();
    }

    @Operation(summary = "远程合闸/断电（高危操作，审计留痕）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).WATER_ELEC_SWITCH,'')")
    @PostMapping("/switch")
    public Result<Void> switchPower(@Valid @RequestBody WaterElecSwitchDTO dto) {
        waterElecMeterService.switchPower(dto);
        return Result.success();
    }
}
