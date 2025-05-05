#!/bin/bash
# ========================================================
# 微信公众号爬虫系统 - Schema目录清理脚本
# ========================================================
# 更新时间：2025年5月5日
# 用途：整理schema目录，删除冗余SQL文件，保留核心文件
# ========================================================

echo "开始整理Schema目录..."

# 创建备份目录
mkdir -p backup_schemas
echo "创建备份目录: backup_schemas"

# 保留的核心文件列表
KEEP_FILES=("optimized_schema.sql" "README_SCHEMA.md" "cleanup_script.sh" "cleanup_script.bat")

# 移动其他SQL文件到备份目录
for file in *.sql; do
    if [[ "$file" == "optimized_schema.sql" ]]; then
        echo "[保留] $file"
    else
        mv "$file" backup_schemas/
        echo "[移动到备份] $file"
    fi
done

# 移动其他Markdown文件到备份目录
for file in *.md; do
    if [[ "$file" == "README_SCHEMA.md" ]]; then
        echo "[保留] $file"
    else
        mv "$file" backup_schemas/
        echo "[移动到备份] $file"
    fi
done

echo
echo "目录整理完成。"
echo "- 保留的文件: optimized_schema.sql, README_SCHEMA.md, cleanup_script.sh, cleanup_script.bat"
echo "- 其他文件已移动到备份目录: backup_schemas/"
echo
echo "提示：请查看 README_SCHEMA.md 获取数据库结构和操作指南。"
echo

# 添加执行权限
chmod +x cleanup_script.sh 