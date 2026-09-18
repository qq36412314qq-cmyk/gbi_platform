@echo off
chcp 65001 >nul
cd /d D:\Office\Project\Java\gbi_platform

echo.
echo ========================================
echo   Git 提交脚本
echo ========================================
echo.

echo [1/5] 查看当前Git状态...
git status --short
echo.

echo [2/5] 添加所有变更文件...
git add .
echo.

echo [3/5] 提交变更...
git commit -m "fix: 补全入职申请工作经历/学业经历表格，修复hr模块前端功能

- transfer/index.vue: 补全工作经历和学业经历动态表格
- hr.ts: 补充缺失的人事异动API接口(getPostPageApi等)
- HrOrgServiceImpl.java: 修复SysOrg导入，优化组织路径构建
- org/index.vue: 优化所属组织下拉选择(只能选部门)和岗位联动
- 修复编译错误和前端运行时错误"
echo.

echo [4/5] 查看最近提交...
git log --oneline -5
echo.

echo [5/5] 尝试推送远程...
echo 注意: ghproxy.com 代理可能连接超时
git push origin main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo   推送失败！建议操作：
    echo ========================================
    echo.
    echo 方法1: 修改远程仓库URL为原始GitHub地址
    echo   git remote set-url origin https://github.com/qq36412314qq-cmyk/gbi_platform.git
    echo.
    echo 方法2: 使用 SSH 方式推送
    echo   git remote set-url origin git@github.com:qq36412314qq-cmyk/gbi_platform.git
    echo.
    echo 方法3: 手动复制执行以下命令
    echo   git push https://github.com/qq36412314qq-cmyk/gbi_platform.git main
    echo.
) else (
    echo.
    echo ========================================
    echo   提交并推送成功！
    echo ========================================
)

echo.
echo 完成!
pause
