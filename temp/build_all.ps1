# ========= 1. 停止旧进程 =========
Write-Host "=== 停止后端进程 ===" -ForegroundColor Cyan
$pid = (netstat -ano | Select-String ":8080 " | Select-String "LISTENING" | ForEach-Object { $_ -split '\s+' | Select-Object -Last 1 } | Select-Object -First 1)
if ($pid) { Stop-Process -Id $pid -Force; Write-Host "端口 8080 已释放" -ForegroundColor Green } else { Write-Host "端口 8080 未被占用" -ForegroundColor Yellow }

# ========= 2. Maven 编译后端 =========
Write-Host "`n=== 编译后端 ===" -ForegroundColor Cyan
Set-Location "D:\Office\Project\Java\gbi_platform\gbi_platform_server"
mvn clean package -DskipTests -s "C:\Users\Admin\.m2\settings.xml" "-Dmaven.repo.local=D:\Office\Project\Java\gbi_platform\m2_repo_tmp" 2>&1 | Select-Object -Last 10
if ($LASTEXITCODE -ne 0) { Write-Host "后端编译失败，请检查错误" -ForegroundColor Red; exit 1 }
Write-Host "后端编译成功！" -ForegroundColor Green

# ========= 3. 后台启动 =========
Write-Host "`n=== 启动后端 ===" -ForegroundColor Cyan
Start-Process -FilePath "C:\Users\Admin\jdk-21.0.12+8\bin\java.exe" `
  -ArgumentList "-jar","target\out\gbi_platform_server.jar","--spring.profiles.active=dev" `
  -NoNewWindow `
  -RedirectStandardOutput "server_run.log" `
  -RedirectStandardError "server_err.log"

# ========= 4. 等待并验证 =========
Write-Host "等待后端启动..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

$login = Invoke-RestMethod -Uri "http://localhost:8080/base/login" -Method POST `
  -ContentType "application/json" -Body '{"username":"admin","password":"123456"}' -TimeoutSec 5
Write-Host "后端登录验证: code=$($login.code)" -ForegroundColor Green

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host " 后端启动完成！" -ForegroundColor Green
Write-Host " 访问地址: http://localhost:8080" -ForegroundColor White
Write-Host " 前端地址: http://localhost:5173" -ForegroundColor White
Write-Host " 日志文件: logs\gbi_platform_server.log" -ForegroundColor Gray
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`n请手动编译前端：" -ForegroundColor Yellow
Write-Host "  Set-Location 'D:\Office\Project\Java\gbi_platform\gbi_platform_admin'" -ForegroundColor Gray
Write-Host "  npm run build" -ForegroundColor Gray
Write-Host "  或 npm run dev (开发模式)" -ForegroundColor Gray
