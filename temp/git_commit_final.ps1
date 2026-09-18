# Git 提交脚本 - 修复 ghproxy 代理问题
Set-Location "D:\Office\Project\Java\gbi_platform"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Git 提交脚本" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 1. 查看当前状态
Write-Host "[1/6] 查看当前Git状态..." -ForegroundColor Yellow
git status --short

# 2. 添加所有变更
Write-Host "`n[2/6] 添加所有变更文件..." -ForegroundColor Yellow
git add .

# 3. 提交变更
Write-Host "`n[3/6] 提交变更..." -ForegroundColor Yellow
$commitMessage = @"
fix: 补全入职申请工作经历/学业经历表格，修复hr模块前端功能

- transfer/index.vue: 补全工作经历和学业经历动态表格
- hr.ts: 补充缺失的人事异动API接口(getPostPageApi等)
- HrOrgServiceImpl.java: 修复SysOrg导入，优化组织路径构建
- org/index.vue: 优化所属组织下拉选择(只能选部门)和岗位联动
- 修复编译错误和前端运行时错误

 closes #1
"@
git commit -m $commitMessage

# 4. 查看提交历史
Write-Host "`n[4/6] 查看最近提交..." -ForegroundColor Yellow
git log --oneline -5

# 5. 检查远程配置
Write-Host "`n[5/6] 检查远程仓库配置..." -ForegroundColor Yellow
$remoteUrl = git remote get-url origin
Write-Host "当前远程URL: $remoteUrl" -ForegroundColor White

# 6. 尝试推送（可能失败）
Write-Host "`n[6/6] 尝试推送远程..." -ForegroundColor Yellow
Write-Host "注意: ghproxy.com 代理可能连接超时" -ForegroundColor Red
git push origin main 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "`n========================================" -ForegroundColor Red
    Write-Host "  推送失败！建议操作：" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "方法1: 修改远程仓库URL为原始GitHub地址" -ForegroundColor Yellow
    Write-Host "  git remote set-url origin https://github.com/qq36412314qq-cmyk/gbi_platform.git" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "方法2: 使用 SSH 方式推送" -ForegroundColor Yellow
    Write-Host "  git remote set-url origin git@github.com:qq36412314qq-cmyk/gbi_platform.git" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "方法3: 手动复制以下命令执行" -ForegroundColor Yellow
    Write-Host "  git push https://github.com/qq36412314qq-cmyk/gbi_platform.git main" -ForegroundColor Cyan
} else {
    Write-Host "`n========================================" -ForegroundColor Green
    Write-Host "  提交并推送成功！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
}

Write-Host "`n完成!" -ForegroundColor Green
