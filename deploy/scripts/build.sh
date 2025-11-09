#!/bin/bash

# DevFlow 项目打包脚本 (Bash)
# 使用方式:
#   ./build.sh              # 使用当前版本打包
#   ./build.sh 1.0.0        # 指定新版本打包

echo "=========================================="
echo "       DevFlow 项目打包脚本"
echo "=========================================="
echo ""

# 设置版本号（如果提供）
if [ -n "$1" ]; then
  echo "设置版本号为: $1"
  mvn versions:set -DnewVersion=$1
  mvn versions:commit
  echo ""
fi

# 清理旧文件
echo "清理旧文件..."
mvn clean
echo ""

# 打包项目
echo "打包项目（跳过测试）..."
mvn package -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "         打包完成！"
    echo "=========================================="
    echo ""
    echo "构建产物位置:"
    echo "  后端 JAR: backend/target/devflow.jar"
    echo "  前端文件: frontend/target/dist/"
    echo ""
    
    # 显示版本信息
    echo "版本信息:"
    if [ -f "backend/target/classes/application.properties" ]; then
        grep "application.version\|application.build.time" backend/target/classes/application.properties | sed 's/^/  /'
    fi
    echo ""
    
    # 显示JAR文件大小
    if [ -f "backend/target/devflow.jar" ]; then
        JAR_SIZE=$(du -h "backend/target/devflow.jar" | cut -f1)
        echo "JAR 文件大小: $JAR_SIZE"
    fi
    echo ""
    
    echo "下一步操作:"
    echo "  1. 验证构建产物"
    echo "  2. 上传到服务器: scp backend/target/devflow.jar user@server:/app/"
    echo "  3. 配置环境变量"
    echo "  4. 启动应用: java -jar /app/devflow.jar"
    echo ""
    
else
    echo ""
    echo "=========================================="
    echo "         打包失败！"
    echo "=========================================="
    echo ""
    echo "请检查错误信息并修复问题"
    echo ""
    exit 1
fi
