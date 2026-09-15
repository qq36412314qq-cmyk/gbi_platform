@echo off
chcp 65001 >nul
echo ========================================
echo  停止后端进程...
echo ========================================
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    taskkill /F /PID %%a >nul 2>&1
)
timeout /t 2 /nobreak >nul

echo.
echo ========================================
echo  编译后端...
echo ========================================
cd /d "D:\Office\Project\Java\gbi_platform\gbi_platform_server"
call mvn clean package -DskipTests -s "C:\Users\Admin\.m2\settings.xml" "-Dmaven.repo.local=D:\Office\Project\Java\gbi_platform\m2_repo_tmp"
if %errorlevel% neq 0 (
    echo 后端编译失败！
    pause
    exit /b 1
)
echo 后端编译成功！

echo.
echo ========================================
echo  启动后端...
echo ========================================
start /b "gbi_platform_server" "C:\Users\Admin\jdk-21.0.12+8\bin\java.exe" -jar target\out\gbi_platform_server.jar --spring.profiles.active=dev
echo 后端启动中...
timeout /t 15 /nobreak >nul

echo.
echo ========================================
echo  验证后端...
echo ========================================
curl -s -X POST http://localhost:8080/base/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"123456\"}"
echo.
echo 后端启动完成！
