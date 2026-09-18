@echo off
chcp 65001 >nul
cd /d D:\Office\Project\Java\gbi_platform

echo.
echo ========================================
echo   Git 提交脚本 - 修复代理问题
echo ========================================
echo.

:: 1. 查看当前状态
echo [1/7] 查看当前Git状态...
git status --short
echo.

:: 2. 修改远程仓库URL（移除ghproxy代理）
echo [2/7] 修改远程仓库URL...
git remote set-url origin https://github.com/qq36412314qq-cmyk/gbi_platform.git
echo 远程URL已修改为: https://github.com/qq36412314qq-cmyk/gbi_platform.git
echo.

:: 3. 验证远程配置
echo [3/7] 验证远程配置...
git remote -v
echo.

:: 4. 添加所有变更文件
echo [4/7] 添加所有变更文件...
git add .
echo.

:: 5. 提交变更
echo [5/7] 提交变更...
git commit -m "fix: 补全入职申请工作经历/学业经历表格，修复hr模块前端功能

- transfer/index.vue: 补全工作经历和学业经历动态表格
- hr.ts: 补充缺失的人事异动API接口(getPostPageApi等)
- HrOrgServiceImpl.java: 修复SysOrg导入，优化组织路径构建
- org/index.vue: 优化所属组织下拉选择(只能选部门)和岗位联动
- 修复编译错误和前端运行时错误"
echo.

:: 6. 查看提交历史
echo [6/7] 查看最近提交...
git log --oneline -5
echo.

:: 7. 推送远程
echo [7/7] 推送远程...
git push origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   提交并推送成功！
    echo ========================================
) else (
    echo.
    echo ========================================
    echo   推送失败，尝试使用 SSH 方式...
    echo ========================================
    echo.
    echo 如果仍然失败，请检查：
    echo   1. 网络连接是否正常
    echo   2. GitHub 账号权限是否正确
    echo   3. 是否需要配置 SSH Key
    echo.
    echo SSH 推送命令：
    echo   git remote set-url origin git@github.com:qq36412314qq-cmyk/gbi_platform.git
    echo   git push origin main
    echo.
)

echo.
echo 完成!
pause
