@echo off
chcp 65001 >nul
echo ========================================
echo  编译前端...
echo ========================================
cd /d "D:\Office\Project\Java\gbi_platform\gbi_platform_admin"
npm run build
if %errorlevel% neq 0 (
    echo 前端编译失败！
    pause
    exit /b 1
)
echo 前端编译成功！
pause
