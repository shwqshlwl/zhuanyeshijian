@echo off
REM 前端 Vite 缓存清理脚本（Windows）
REM 用于解决绝对路径问题

echo 🧹 开始清理 Vite 缓存...
echo.

cd frontend

echo 📦 删除 node_modules\.vite...
if exist "node_modules\.vite" (
    rmdir /s /q "node_modules\.vite"
)

echo 📦 删除 .vite...
if exist ".vite" (
    rmdir /s /q ".vite"
)

echo 📦 删除 dist...
if exist "dist" (
    rmdir /s /q "dist"
)

echo.
echo ✅ 清理完成！
echo.
echo 现在运行以下命令启动项目：
echo   npm run dev
echo.
echo 如果问题仍然存在，运行：
echo   npm run reinstall
echo.

pause
