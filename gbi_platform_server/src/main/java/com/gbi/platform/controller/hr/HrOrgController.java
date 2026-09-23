package com.gbi.platform.controller.hr;

import com.gbi.platform.common.constant.PermissionConst;
import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.hr.PostDTO;
import com.gbi.platform.service.hr.HrOrgService;
import com.gbi.platform.vo.hr.HrPostVO;
import com.gbi.platform.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "人力资源-组织岗位")
@RestController
@RequestMapping("/hr/org")
@RequiredArgsConstructor
public class HrOrgController {

    private final HrOrgService orgService;

    @Operation(summary = "岗位分页")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_POST_LIST,'')")
    @GetMapping("/post/page")
    public Result<PageVO<HrPostVO>> pagePost(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(orgService.pagePost(pageNum, pageSize, status));
    }

    @Operation(summary = "新增岗位")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_POST_ADD,'')")
    @PostMapping("/post/add")
    public Result<Void> addPost(@RequestBody PostDTO dto) {
        orgService.addPost(dto);
        return Result.success();
    }

    @Operation(summary = "编辑岗位")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_POST_EDIT,'')")
    @PostMapping("/post/update")
    public Result<Void> updatePost(@RequestBody PostDTO dto) {
        orgService.updatePost(dto);
        return Result.success();
    }

    @Operation(summary = "删除岗位")
    @PreAuthorize("hasPermission(T(com.gbi.platform.common.constant.PermissionConst).HR_POST_DELETE,'')")
    @PostMapping("/post/delete/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        orgService.deletePost(id);
        return Result.success();
    }

    @Operation(summary = "按部门查询岗位列表")
    @GetMapping("/post/byDept/{deptId}")
    public Result<List<HrPostVO>> getPostByDept(@PathVariable Long deptId) {
        return Result.success(orgService.getPostByDept(deptId));
    }
}
