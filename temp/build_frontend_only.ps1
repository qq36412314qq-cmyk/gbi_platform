Write-Host "=== 编译前端 ===" -ForegroundColor Cyan
Set-Location "D:\Office\Project\Java\gbi_platform\gbi_platform_admin"
npm run build
if ($LASTEXITCODE -ne 0) { Write-Host "前端编译失败！" -ForegroundColor Red; exit 1 }
Write-Host "前端编译成功！" -ForegroundColor Green

Write-Host "`n访问地址：" -ForegroundColor Cyan
Write-Host "  前端开发服务器: http://localhost:5173" -ForegroundColor White
Write-Host "  社保参数配置: http://localhost:5173/platform/socialParam" -ForegroundColor White
Write-Host "  城市列表: http://localhost:5173/platform/city" -ForegroundColor White
