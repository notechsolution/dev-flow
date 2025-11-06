# DevFlow 版本化管理方案

## 📋 概述

本方案实现了完整的版本化管理功能，包括：

- ✅ Maven 自动版本注入
- ✅ 构建时间戳记录
- ✅ Git 信息集成（可选）
- ✅ 后端版本API
- ✅ 前端UI版本显示
- ✅ 版本信息悬浮提示

## 🎯 功能特性

### 1. 版本号来源

- **主版本号**：来自 `pom.xml` 的 `<version>` 标签
- **构建时间**：Maven 自动生成
- **Git 信息**：来自环境变量（可选）

### 2. API 端点

#### `/api/version` - 完整版本信息

```json
{
  "application": "DevFlow",
  "version": "0.0.1",
  "buildTime": "2025-11-06 14:30:00",
  "gitCommitId": "abc1234",
  "gitBranch": "main",
  "serverTime": "2025-11-06T14:35:00"
}
```

#### `/api/version/simple` - 简化版本信息

```json
{
  "version": "0.0.1",
  "buildTime": "2025-11-06 14:30:00"
}
```

### 3. UI 显示

- **位置**：右下角固定位置
- **内容**：`v0.0.1`
- **交互**：鼠标悬停显示详细信息
- **样式**：半透明圆角卡片

## 🏗️ 实现架构

### 后端组件

```
backend/
├── src/main/java/com/lz/devflow/controller/
│   └── VersionController.java          # 版本信息API
└── src/main/resources/
    └── application.properties          # 版本配置
```

### 前端组件

```
frontend/
└── src/
    ├── components/
    │   └── VersionInfo.vue             # 版本显示组件
    └── views/
        └── Home.vue                     # 主页面（集成版本组件）
```

## 📦 版本更新流程

### 方式一：修改版本号

1. **更新主版本号**

编辑 `pom.xml`：

```xml
<groupId>com.lz</groupId>
<artifactId>devflow</artifactId>
<version>1.0.0</version>  <!-- 修改这里 -->
<packaging>pom</packaging>
```

2. **重新构建**

```bash
mvn clean package
```

3. **构建时间自动更新**

Maven 会自动更新构建时间戳。

### 方式二：使用Maven Versions插件

```bash
# 设置新版本
mvn versions:set -DnewVersion=1.0.0

# 确认更改
mvn versions:commit

# 或回滚更改
mvn versions:revert
```

### 方式三：CI/CD 自动版本

在 CI/CD 管道中自动生成版本：

```bash
# Jenkins/GitLab CI 示例
mvn versions:set -DnewVersion=${BUILD_NUMBER}
mvn clean package
```

## 🚀 部署指南

### 1. 本地开发环境

```bash
# 前端开发
cd frontend
npm run dev

# 后端开发
cd backend
mvn spring-boot:run
```

版本信息会自动显示在右下角。

### 2. 生产环境打包

#### 方式一：打包整个项目

```bash
# 在项目根目录执行
mvn clean package -DskipTests

# 输出位置
# backend/target/devflow.jar
# frontend/target/dist/
```

#### 方式二：只打包后端

```bash
cd backend
mvn clean package -DskipTests

# 输出：backend/target/devflow.jar
```

#### 方式三：使用脚本打包

创建 `build.sh`：

```bash
#!/bin/bash

echo "=========================================="
echo "DevFlow 项目打包脚本"
echo "=========================================="

# 设置版本号（可选）
if [ -n "$1" ]; then
  echo "设置版本号为: $1"
  mvn versions:set -DnewVersion=$1
  mvn versions:commit
fi

# 清理旧文件
echo "清理旧文件..."
mvn clean

# 打包项目
echo "打包项目..."
mvn package -DskipTests

# 显示构建结果
echo "=========================================="
echo "打包完成！"
echo "后端JAR: backend/target/devflow.jar"
echo "前端文件: frontend/target/dist/"
echo "=========================================="

# 显示版本信息
echo "版本信息:"
cat backend/target/classes/application.properties | grep "application.version"
cat backend/target/classes/application.properties | grep "application.build.time"
```

使用方式：

```bash
# 使用当前版本打包
./build.sh

# 指定新版本打包
./build.sh 1.0.0
```

### 3. 运维环境部署

#### 步骤 1：打包

```bash
# 在开发机器上执行
mvn clean package -DskipTests

# 生成的文件
# backend/target/devflow.jar
```

#### 步骤 2：传输到服务器

```bash
# 使用 scp 传输
scp backend/target/devflow.jar user@server:/app/

# 或使用 rsync
rsync -avz backend/target/devflow.jar user@server:/app/
```

#### 步骤 3：在服务器上启动

```bash
# 创建启动脚本 start.sh
#!/bin/bash

export AI_PROVIDER=qwen
export DASHSCOPE_API_KEY=your-key
export MONGODB_URI=mongodb://user:pass@host:27017/devflow
export MAIL_USERNAME=your-email@163.com
export MAIL_PASSWORD=your-password
export MAIL_HOST=smtp.163.com
export MAIL_PORT=465

java -jar /app/devflow.jar \
  --server.port=8099 \
  --spring.profiles.active=prod
```

```bash
# 启动应用
chmod +x start.sh
./start.sh
```

#### 步骤 4：验证版本

```bash
# 检查版本信息
curl http://localhost:8099/api/version

# 访问UI查看右下角版本号
```

### 4. Docker 部署（可选）

创建 `Dockerfile`：

```dockerfile
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制jar文件
COPY backend/target/devflow.jar /app/devflow.jar

# 暴露端口
EXPOSE 8099

# 启动命令
ENTRYPOINT ["java", "-jar", "/app/devflow.jar"]
```

构建和运行：

```bash
# 构建镜像
docker build -t devflow:1.0.0 .

# 运行容器
docker run -d \
  -p 8099:8099 \
  -e AI_PROVIDER=qwen \
  -e DASHSCOPE_API_KEY=your-key \
  -e MONGODB_URI=mongodb://host:27017/devflow \
  --name devflow \
  devflow:1.0.0
```

## 🔧 Git 集成（可选）

### 方式一：手动设置环境变量

```bash
# 设置 Git 信息
export GIT_COMMIT_ID=$(git rev-parse HEAD)
export GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

# 打包
mvn clean package -DskipTests
```

### 方式二：使用 Git Commit ID Plugin

在 `backend/pom.xml` 中添加：

```xml
<plugin>
    <groupId>pl.project13.maven</groupId>
    <artifactId>git-commit-id-plugin</artifactId>
    <version>4.9.10</version>
    <executions>
        <execution>
            <id>get-the-git-infos</id>
            <goals>
                <goal>revision</goal>
            </goals>
            <phase>initialize</phase>
        </execution>
    </executions>
    <configuration>
        <generateGitPropertiesFile>true</generateGitPropertiesFile>
        <generateGitPropertiesFilename>${project.build.outputDirectory}/git.properties</generateGitPropertiesFilename>
    </configuration>
</plugin>
```

## 📊 版本信息使用场景

### 1. 问题排查

当用户报告问题时，可以要求提供版本号：

```bash
# 用户在浏览器中查看右下角版本号
# 或通过 API 获取
curl http://your-server:8099/api/version
```

### 2. 功能验证

验证用户使用的是否是最新版本：

```bash
# 比较版本号
CURRENT_VERSION=$(curl -s http://server:8099/api/version | jq -r '.version')
LATEST_VERSION="1.0.0"

if [ "$CURRENT_VERSION" != "$LATEST_VERSION" ]; then
  echo "需要升级"
fi
```

### 3. 监控告警

在监控系统中集成版本信息：

```bash
# Prometheus metric
devflow_version{version="1.0.0",build_time="2025-11-06"} 1
```

### 4. 审计追溯

记录每次操作时的系统版本：

```java
logger.info("用户操作 - 版本: {}, 时间: {}", version, LocalDateTime.now());
```

## 🎨 UI 样式自定义

### 修改位置

编辑 `frontend/src/components/VersionInfo.vue`：

```css
.version-info {
  /* 修改位置 */
  bottom: 10px;  /* 距底部距离 */
  right: 10px;   /* 距右侧距离 */
  
  /* 左下角 */
  /* bottom: 10px; */
  /* left: 10px; */
}
```

### 修改样式

```css
.version-info {
  /* 修改颜色 */
  background-color: rgba(64, 158, 255, 0.9);
  color: white;
  
  /* 修改大小 */
  font-size: 14px;
  padding: 6px 16px;
}
```

### 隐藏版本信息

如果不想显示，注释掉 `Home.vue` 中的：

```vue
<!-- <VersionInfo /> -->
```

## 📝 版本命名规范

建议使用 [语义化版本](https://semver.org/lang/zh-CN/)：

```
主版本号.次版本号.修订号

例如：
1.0.0  - 第一个正式版本
1.1.0  - 添加新功能
1.1.1  - 修复bug
2.0.0  - 重大更新（不向下兼容）
```

### 开发阶段版本

```
0.1.0-alpha    - Alpha 版本
0.1.0-beta     - Beta 版本
0.1.0-rc.1     - Release Candidate
1.0.0          - 正式发布
```

### 示例版本历史

```
0.0.1  - 2025-11-01  初始版本
0.1.0  - 2025-11-10  添加 AI 功能
0.2.0  - 2025-11-20  添加用户管理
1.0.0  - 2025-12-01  正式发布
1.0.1  - 2025-12-05  修复登录问题
1.1.0  - 2025-12-15  添加项目管理
```

## 🔍 故障排查

### 问题1：版本号显示为 unknown

**原因**：Maven 资源过滤未生效

**解决**：

1. 检查 `backend/pom.xml` 中的 `<resources>` 配置
2. 清理并重新构建：

```bash
mvn clean
mvn package -DskipTests
```

### 问题2：前端不显示版本号

**原因**：API 调用失败

**解决**：

1. 检查后端是否运行
2. 检查浏览器控制台错误
3. 验证 API 可访问：

```bash
curl http://localhost:8099/api/version
```

### 问题3：构建时间为空

**原因**：时间戳格式配置缺失

**解决**：

在 `pom.xml` 中添加：

```xml
<properties>
    <maven.build.timestamp.format>yyyy-MM-dd HH:mm:ss</maven.build.timestamp.format>
</properties>
```

## 📚 相关文件清单

### 后端文件

- `backend/pom.xml` - Maven 构建配置
- `backend/src/main/resources/application.properties` - 版本配置
- `backend/src/main/java/com/lz/devflow/controller/VersionController.java` - 版本API

### 前端文件

- `frontend/src/components/VersionInfo.vue` - 版本显示组件
- `frontend/src/views/Home.vue` - 主页面（集成版本组件）

### 根文件

- `pom.xml` - 根项目配置

## 🎯 总结

本方案提供了：

✅ **自动化版本管理** - 从 Maven 自动读取
✅ **UI 实时显示** - 右下角固定显示
✅ **详细版本信息** - 包含构建时间和 Git 信息
✅ **灵活部署** - 支持多种部署方式
✅ **易于维护** - 集中式配置管理

**下一步**：

1. 更新 `pom.xml` 中的版本号为实际版本
2. 执行 `mvn clean package -DskipTests` 打包
3. 部署到运维环境
4. 验证版本信息显示正常
