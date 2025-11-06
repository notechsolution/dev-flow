#!/bin/bash

# DevFlow 快速启动脚本
# 此脚本位于项目根目录，用于快速调用 deploy 目录中的脚本

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_SCRIPTS_DIR="$SCRIPT_DIR/deploy/scripts"

function show_help() {
    cat << EOF
DevFlow 快速启动工具
==================

用法: ./devflow.sh <action> [options]

操作 (Action):
  build [version]    打包项目（可选指定版本号）
  start              启动应用
  stop               停止应用
  status             查看状态（仅 Windows）
  help               显示此帮助

示例:
  ./devflow.sh build              # 打包项目
  ./devflow.sh build 1.0.0        # 打包并指定版本
  ./devflow.sh start              # 启动应用
  ./devflow.sh stop               # 停止应用

更多信息请查看: deploy/README.md
EOF
}

function invoke_script() {
    local script_name=$1
    shift
    local script_path="$DEPLOY_SCRIPTS_DIR/${script_name}.sh"
    
    if [ ! -f "$script_path" ]; then
        echo "错误: 找不到脚本 $script_path"
        exit 1
    fi
    
    if [ ! -x "$script_path" ]; then
        echo "添加执行权限: $script_path"
        chmod +x "$script_path"
    fi
    
    "$script_path" "$@"
}

# 主逻辑
ACTION=${1:-help}

case "$ACTION" in
    build)
        echo "执行打包..."
        if [ -n "$2" ]; then
            invoke_script "build" "$2"
        else
            invoke_script "build"
        fi
        ;;
    start)
        echo "启动应用..."
        invoke_script "start-prod"
        ;;
    stop)
        echo "停止应用..."
        invoke_script "stop"
        ;;
    status)
        echo "注意: 状态检查功能仅在 Windows 上可用"
        echo "请使用以下命令手动检查:"
        echo "  ps aux | grep devflow"
        echo "  curl http://localhost:8099/api/version"
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo "未知操作: $ACTION"
        echo ""
        show_help
        exit 1
        ;;
esac
