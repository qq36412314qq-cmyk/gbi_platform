Write-Host "=== 编译后端 ===" -ForegroundColor Cyan
Set-Location "D:\Office\Project\Java\gbi_platform\gbi_platform_server"
mvn clean package -DskipTests -s "C:\Users\Admin\.m2\settings.xml" "-Dmaven.repo.local=D:\Office\Project\Java\gbi_platform\m2_repo_tmp" 2>&1 | Select-Object -Last 10
if ($LASTEXITCODE -ne 0) { Write-Host "后端编译失败！" -ForegroundColor Red; exit 1 }
Write-Host "后端编译成功！" -ForegroundColor Green

Write-Host "`n=== 启动后端 ===" -ForegroundColor Cyan
Stop-Process -Name java -Force -ErrorAction SilentlyContinue
Start-Process -FilePath "C:\Users\Admin\jdk-21.0.12+8\bin\java.exe" `
  -ArgumentList "-jar","target\out\gbi_platform_server.jar","--spring.profiles.active=dev" `
  -NoNewWindow
Write-Host "后端启动中..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

Write-Host "`n=== 验证后端 ===" -ForegroundColor Cyan
try {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/base/login" -Method POST `
      -ContentType "application/json" -Body '{"username":"admin","password":"123456"}' -TimeoutSec 5
    Write-Host "后端启动成功！code=$($result.code)" -ForegroundColor Green
} catch {
    Write-Host "后端验证失败：$_" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host " 现在请手动编译前端：" -ForegroundColor Yellow
Write-Host "   cd D:\Office\Project\Java\gbi_platform\gbi_platform_admin" -ForegroundColor Gray
Write-Host "   npm run build" -ForegroundColor Gray
Write-Host " 或启动开发服务器：" -ForegroundColor Gray
Write-Host "   npm run dev" -ForegroundColor Gray
Write-Host "========================================" -ForegroundColor Cyan
