# ========= 1. 停止旧进程 =========
$pid = (netstat -ano | Select-String ":8080 " | Select-String "LISTENING" | ForEach-Object { $_ -split '\s+' | Select-Object -Last 1 } | Select-Object -First 1)
if ($pid) { Stop-Process -Id $pid -Force; Write-Host "端口 8080 已释放" } else { Write-Host "端口 8080 未被占用" }

# ========= 2. Maven 编译后端 =========
Set-Location "D:\Office\Project\Java\gbi_platform\gbi_platform_server"
mvn clean package -DskipTests -s "C:\Users\Admin\.m2\settings.xml" "-Dmaven.repo.local=D:\Office\Project\Java\gbi_platform\m2_repo_tmp" 2>&1 | Select-Object -Last 5
if ($LASTEXITCODE -ne 0) { Write-Host "后端编译失败，请检查错误"; exit 1 }
Write-Host "后端编译成功"

# ========= 3. 后台启动后端 =========
Write-Host "后端启动中..."
Start-Process -FilePath "C:\Users\Admin\jdk-21.0.12+8\bin\java.exe" `
  -ArgumentList "-jar","target\out\gbi_platform_server.jar","--spring.profiles.active=dev" `
  -NoNewWindow `
  -RedirectStandardOutput "server_run.log" `
  -RedirectStandardError "server_err.log"

# ========= 4. 等待启动 =========
Write-Host "等待后端启动..."
Start-Sleep -Seconds 15

# ========= 5. 验证后端 =========
$login = Invoke-RestMethod -Uri "http://localhost:8080/base/login" -Method POST `
  -ContentType "application/json" -Body '{"username":"admin","password":"123456"}' -TimeoutSec 5
Write-Host "后端登录验证: code=$($login.code)"
