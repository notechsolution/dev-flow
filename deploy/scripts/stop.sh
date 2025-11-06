#!/bin/bash

# DevFlow 停止脚本

APP_NAME="DevFlow"
PID_FILE="/app/devflow.pid"

echo "=========================================="
echo "       停止 $APP_NAME"
echo "=========================================="
echo ""

# 检查 PID 文件是否存在
if [ ! -f "$PID_FILE" ]; then
    echo "警告: 未找到 PID 文件: $PID_FILE"
    echo "尝试通过进程名查找..."
    
    # 通过进程名查找
    PID=$(ps aux | grep "devflow.jar" | grep -v grep | awk '{print $2}')
    
    if [ -z "$PID" ]; then
        echo "未找到运行中的 $APP_NAME 进程"
        exit 0
    fi
else
    # 从文件读取 PID
    PID=$(cat $PID_FILE)
fi

# 检查进程是否存在
if ! ps -p $PID > /dev/null 2>&1; then
    echo "进程 $PID 不存在"
    rm -f $PID_FILE
    exit 0
fi

echo "找到进程: $PID"
echo "正在停止..."

# 优雅关闭
kill $PID

# 等待进程结束
for i in {1..30}; do
    if ! ps -p $PID > /dev/null 2>&1; then
        echo ""
        echo "✓ 应用已停止"
        rm -f $PID_FILE
        exit 0
    fi
    echo -n "."
    sleep 1
done

# 如果还未停止，强制杀死
echo ""
echo "进程未响应，强制停止..."
kill -9 $PID

sleep 2

if ! ps -p $PID > /dev/null 2>&1; then
    echo "✓ 应用已强制停止"
    rm -f $PID_FILE
else
    echo "✗ 无法停止进程 $PID"
    exit 1
fi
