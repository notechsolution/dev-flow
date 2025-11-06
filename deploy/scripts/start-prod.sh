#!/bin/bash

# DevFlow 生产环境启动脚本
# 请根据实际环境修改以下配置

# ==================== 配置区 ====================

# 应用配置
APP_NAME="DevFlow"
APP_JAR="/app/devflow.jar"
APP_PORT=8099
SPRING_PROFILE="prod"

# JVM 配置
JAVA_OPTS="-Xms512m -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# AI 配置
export AI_PROVIDER="qwen"
export DASHSCOPE_API_KEY="your-dashscope-api-key-here"

# 数据库配置
export MONGODB_URI="mongodb://username:password@10.222.39.208:27017/devflow"

# 邮件配置
export MAIL_HOST="smtp.163.com"
export MAIL_PORT="465"
export MAIL_USERNAME="your-email@163.com"
export MAIL_PASSWORD="your-email-authorization-code"

# Git 信息（可选）
export GIT_COMMIT_ID=$(git rev-parse HEAD 2>/dev/null || echo "unknown")
export GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")

# 日志配置
LOG_DIR="/app/logs"
LOG_FILE="$LOG_DIR/devflow.log"

# ==================== 脚本逻辑 ====================

# 创建日志目录
mkdir -p $LOG_DIR

# 检查 JAR 文件是否存在
if [ ! -f "$APP_JAR" ]; then
    echo "错误: JAR 文件不存在: $APP_JAR"
    exit 1
fi

# 检查端口是否被占用
if lsof -Pi :$APP_PORT -sTCP:LISTEN -t >/dev/null ; then
    echo "错误: 端口 $APP_PORT 已被占用"
    echo "请先停止占用该端口的进程"
    exit 1
fi

echo "=========================================="
echo "       启动 $APP_NAME"
echo "=========================================="
echo ""
echo "JAR 文件: $APP_JAR"
echo "端口: $APP_PORT"
echo "配置: $SPRING_PROFILE"
echo "日志: $LOG_FILE"
echo ""

# 启动应用
nohup java $JAVA_OPTS \
    -jar $APP_JAR \
    --server.port=$APP_PORT \
    --spring.profiles.active=$SPRING_PROFILE \
    > $LOG_FILE 2>&1 &

# 获取进程 ID
PID=$!
echo $PID > /app/devflow.pid

echo "应用已启动，进程 ID: $PID"
echo ""

# 等待几秒，检查是否成功启动
sleep 5

if ps -p $PID > /dev/null; then
    echo "✓ 应用启动成功！"
    echo ""
    echo "查看日志: tail -f $LOG_FILE"
    echo "检查状态: ps -p $PID"
    echo "停止应用: kill $PID"
    echo ""
    
    # 尝试获取版本信息
    sleep 10
    VERSION_INFO=$(curl -s http://localhost:$APP_PORT/api/version 2>/dev/null)
    if [ -n "$VERSION_INFO" ]; then
        echo "版本信息:"
        echo "$VERSION_INFO" | python3 -m json.tool 2>/dev/null || echo "$VERSION_INFO"
        echo ""
    fi
else
    echo "✗ 应用启动失败！"
    echo ""
    echo "请查看日志文件: $LOG_FILE"
    exit 1
fi
