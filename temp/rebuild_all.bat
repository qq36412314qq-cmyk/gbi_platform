@echo off
chcp 65001 >nul
echo ========================================
echo  gbi_platform 前后端编译运行脚本
echo ========================================

echo.
echo [1/4] 停止旧的后端进程...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    taskkill /F /PID %%a >nul 2>&1
    echo 已停止进程 PID=%%a
)

echo.
echo [2/4] 编译后端...
cd /d "D:\Office\Project\Java\gbi_platform\gbi_platform_server"
call mvn clean package -DskipTests -s "C:\Users\Admin\.m2\settings.xml" "-Dmaven.repo.local=D:\Office\Project\Java\gbi_platform\m2_repo_tmp"
if %errorlevel% neq 0 (
    echo 后端编译失败！
    pause
    exit /b 1
)
echo 后端编译成功！

echo.
echo [3/4] 启动后端服务...
start /b "gbi_platform_server" "C:\Users\Admin\jdk-21.0.12+8\bin\java.exe" -jar target\out\gbi_platform_server.jar --spring.profiles.active=dev
timeout /t 15 /nobreak >nul

echo.
echo [4/4] 验证后端启动...
curl -s -X POST http://localhost:8080/base/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"123456\"}"
echo.
echo 后端启动完成！
echo 访问地址: http://localhost:8080
echo 日志文件: logs\gbi_platform_server.log

echo.
echo ========================================
echo  前端编译（在另一个终端执行）
echo ========================================
echo cd /d "D:\Office\Project\Java\gbi_platform\gbi_platform_admin"
echo npm run dev
echo.
pause
