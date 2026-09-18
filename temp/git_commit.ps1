# Git 提交脚本
Set-Location "D:\Office\Project\Java\gbi_platform"

Write-Host "=== 查看当前状态 ===" -ForegroundColor Cyan
git status

Write-Host "`n=== 查看远程配置 ===" -ForegroundColor Cyan
git remote -v

Write-Host "`n=== 添加所有变更 ===" -ForegroundColor Cyan
git add .

Write-Host "`n=== 提交变更 ===" -ForegroundColor Cyan
git commit -m "fix: 补全入职申请工作经历/学业经历表格，修复hr模块前端功能，优化组织岗位管理

- transfer/index.vue: 补全工作经历和学业经历动态表格
- hr.ts: 补充缺失的人事异动API接口
- HrOrgServiceImpl.java: 修复SysOrg导入，优化组织路径构建
- org/index.vue: 优化所属组织下拉选择和岗位联动
- 修复编译错误和前端运行时错误"

Write-Host "`n=== 提交结果 ===" -ForegroundColor Cyan
git log --oneline -3

Write-Host "`n=== 推送远程 ===" -ForegroundColor Cyan
# 尝试推送到origin main
git push origin main

Write-Host "`n完成!" -ForegroundColor Green
