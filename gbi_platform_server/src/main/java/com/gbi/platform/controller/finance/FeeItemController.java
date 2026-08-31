package com.gbi.platform.controller.finance;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.FeeItemDTO;
import com.gbi.platform.dto.FeeItemQueryDTO;
import com.gbi.platform.service.FeeItemService;
import com.gbi.platform.vo.FeeItemVO;
import com.gbi.platform.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 自定义收费类型管理：租金/物业费/水费/电费/押金/其他等，子公司可配置增删
 * 权限标识 fee:item:*（集团账号仅 list 只读，无新增/编辑/删除入口）
 *
 * @author gbi
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/finance/feeItem")
@Tag(name = "自定义收费类型管理")
public class FeeItemController {

    private final FeeItemService feeItemService;

    @Operation(summary = "收费类型全量列表（供收费规则下拉选择）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FEE_ITEM_LIST,'')")
    @GetMapping("/list")
    public Result<List<FeeItemVO>> list(FeeItemQueryDTO dto) {
        return Result.success(feeItemService.list(dto));
    }

    @Operation(summary = "收费类型分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FEE_ITEM_LIST,'')")
    @GetMapping("/page")
    public Result<PageVO<FeeItemVO>> page(FeeItemQueryDTO dto) {
        if (dto.getPageNum() == null || dto.getPageSize() == null) {
            return Result.error(400, "分页参数不能为空");
        }
        return Result.success(feeItemService.page(dto));
    }

    @Operation(summary = "新增收费类型")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FEE_ITEM_ADD,'')")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody FeeItemDTO dto) {
        feeItemService.add(dto);
        return Result.success();
    }

    @Operation(summary = "编辑收费类型")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FEE_ITEM_EDIT,'')")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody FeeItemDTO dto) {
        feeItemService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除收费类型（被收费规则引用禁止删除）")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).FEE_ITEM_DELETE,'')")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam("id") Long id) {
        feeItemService.delete(id);
        return Result.success();
    }
}