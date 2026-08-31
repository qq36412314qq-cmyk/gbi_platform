package com.gbi.platform.controller.org;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.OrgDTO;
import com.gbi.platform.service.OrgService;
import com.gbi.platform.vo.OrgTreeVO;
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
 * 组织接口（对齐前端 api/org.ts）
 *
 * @author gbi
 */
@Tag(name = "组织")
@RestController
@RequestMapping("/org")
@RequiredArgsConstructor
public class OrgController {

    private final OrgService orgService;

    @Operation(summary = "组织树")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).ORG_LIST,'')")
    @GetMapping("/tree")
    public Result<List<OrgTreeVO>> tree() {
        return Result.success(orgService.tree());
    }

    @Operation(summary = "新增组织")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).ORG_ADD,'')")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody OrgDTO dto) {
        orgService.add(dto);
        return Result.success();
    }

    @Operation(summary = "编辑组织")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).ORG_EDIT,'')")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody OrgDTO dto) {
        orgService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除组织")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).ORG_DELETE,'')")
    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        orgService.delete(id);
        return Result.success();
    }
}