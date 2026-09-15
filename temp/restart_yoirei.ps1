Get-Process | Where-Object { $_.ProcessName -like '*go*' } | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2
Remove-Item "D:\Office\Project\Java\gbi_platform\gbi_platform_go_yoirei\yoirei.lock" -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 1
Start-Process -FilePath "D:\Office\Project\Java\gbi_platform\gbi_platform_go_yoirei\go_yoirei.exe" -WindowStyle Hidden
Write-Host "Go Yoirei restarted with new config"
