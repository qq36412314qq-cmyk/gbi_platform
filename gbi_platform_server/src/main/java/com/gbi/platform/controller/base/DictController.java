package com.gbi.platform.controller.base;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.DictDataDTO;
import com.gbi.platform.dto.DictTypeDTO;
import com.gbi.platform.service.DictService;
import com.gbi.platform.vo.DictItemVO;
import com.gbi.platform.vo.DictTypeVO;
import com.gbi.platform.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 字典接口：登录用户均可读取，维护需对应权限（对齐前端 api/base.ts）
 *
 * @author gbi
 */
@Tag(name = "字典")
@RestController
@RequestMapping("/base")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @Operation(summary = "按编码查启用字典数据（登录即可用）")
    @GetMapping("/dict/data")
    public Result<List<DictItemVO>> dictData(@RequestParam String dictCode) {
        return Result.success(dictService.getByCode(dictCode));
    }

    @Operation(summary = "字典类型分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DICT_LIST,'')")
    @GetMapping("/dictType/page")
    public Result<PageVO<DictTypeVO>> pageTypes(@RequestParam Integer pageNum,
                                                @RequestParam Integer pageSize,
                                                @RequestParam(required = false) String dictName,
                                                @RequestParam(required = false) String dictCode) {
        return Result.success(dictService.pageTypes(pageNum, pageSize, dictName, dictCode));
    }

    @Operation(summary = "新增字典类型")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DICT_ADD,'')")
    @PostMapping("/dictType/add")
    public Result<Void> addType(@Valid @RequestBody DictTypeDTO dto) {
        dictService.addType(dto);
        return Result.success();
    }

    @Operation(summary = "编辑字典类型")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DICT_EDIT,'')")
    @PostMapping("/dictType/update")
    public Result<Void> updateType(@Valid @RequestBody DictTypeDTO dto) {
        dictService.updateType(dto);
        return Result.success();
    }

    @Operation(summary = "删除字典类型")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DICT_DELETE,'')")
    @DeleteMapping("/dictType/delete")
    public Result<Void> deleteType(@RequestParam Long id) {
        dictService.deleteType(id);
        return Result.success();
    }

    @Operation(summary = "按类型查字典数据")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DICT_LIST,'')")
    @GetMapping("/dictData/list")
    public Result<List<DictItemVO>> listData(@RequestParam Long dictTypeId) {
        return Result.success(dictService.listData(dictTypeId));
    }

    @Operation(summary = "新增字典数据")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DICT_ADD,'')")
    @PostMapping("/dictData/add")
    public Result<Void> addData(@Valid @RequestBody DictDataDTO dto) {
        dictService.addData(dto);
        return Result.success();
    }

    @Operation(summary = "编辑字典数据")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DICT_EDIT,'')")
    @PostMapping("/dictData/update")
    public Result<Void> updateData(@Valid @RequestBody DictDataDTO dto) {
        dictService.updateData(dto);
        return Result.success();
    }

    @Operation(summary = "删除字典数据")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).DICT_DELETE,'')")
    @DeleteMapping("/dictData/delete")
    public Result<Void> deleteData(@RequestParam Long id) {
        dictService.deleteData(id);
        return Result.success();
    }
}