@echo off
set PATH=C:\Program Files\Go\bin;%PATH%
cd /d D:\Office\Project\Java\gbi_platform\gbi_platform_go_yoirei
echo 开始编译
go build -ldflags="-H windowsgui -w -s" -o go_yoirei.exe .
if %ERRORLEVEL% NEQ 0 (
    echo 【错误】编译失败
    pause
) else (
    echo 【成功】编译完成
    pause
)
