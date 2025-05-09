@echo off
REM ========================================================
REM 微信公众号爬虫系统 - Schema目录清理脚本
REM ========================================================
REM 更新时间：2025年5月5日
REM 用途：整理schema目录，删除冗余SQL文件，保留核心文件
REM ========================================================

echo 开始整理Schema目录...

REM 创建备份目录
mkdir backup_schemas 2>nul
echo 创建备份目录: backup_schemas

REM 保留的核心文件列表
set KEEP_FILES=optimized_schema.sql README_SCHEMA.md cleanup_script.bat

REM 移动其他SQL文件到备份目录
for %%f in (*.sql) do (
    set FILENAME=%%f
    set KEEP=false
    
    if "%%f"=="optimized_schema.sql" (
        set KEEP=true
        echo [保留] %%f
    ) else (
        move "%%f" backup_schemas\
        echo [移动到备份] %%f
    )
)

REM 移动其他Markdown文件到备份目录
for %%f in (*.md) do (
    if "%%f"=="README_SCHEMA.md" (
        echo [保留] %%f
    ) else (
        move "%%f" backup_schemas\
        echo [移动到备份] %%f
    )
)

echo.
echo 目录整理完成。
echo - 保留的文件: optimized_schema.sql, README_SCHEMA.md, cleanup_script.bat
echo - 其他文件已移动到备份目录: backup_schemas\
echo.
echo 提示：请查看 README_SCHEMA.md 获取数据库结构和操作指南。
echo.

pause 