# DevFlow - 需求管理与开发流程工具

## 功能特性

- 🎯 智能需求优化 - AI 辅助优化用户故事
- 🤖 多 AI 模型支持 - 支持 Qwen (通义千问) 和 Ollama 本地模型
- 📝 需求澄清 - 自动生成澄清问题
- ✅ 测试用例生成 - AI 自动生成测试用例
- 🔐 OAuth2 集成 - 支持 GitHub 等第三方登录
- 📊 项目管理 - 完整的项目和需求管理
- 🗄️ MongoDB 存储 - 灵活的 NoSQL 数据库
- 📦 版本管理 - 自动化版本追踪和 UI 显示

## 快速开始

### 📚 文档导航

#### 部署文档
- 📦 [部署目录](deploy/) - **新！** 所有部署脚本和文档的集中位置
- 🚀 [快速部署指南](deploy/docs/DEPLOYMENT_QUICK_START.md) - **新！** 5分钟快速部署到生产环境
- 📦 [版本管理指南](deploy/docs/VERSION_MANAGEMENT_GUIDE.md) - **新！** 版本化管理完整方案
- 🪟 [Windows 脚本指南](deploy/docs/WINDOWS_SCRIPTS_GUIDE.md) - **新！** Windows PowerShell 脚本完整说明
- � [打包脚本指南](deploy/docs/BUILD_SCRIPTS_GUIDE.md) - **新！** 跨平台打包脚本使用说明
- 📘 [完整部署指南](DEPLOYMENT_GUIDE.md) - 详细的部署文档，包含 MongoDB 配置

#### 核心文档
- 🔧 [故障排查](TROUBLESHOOTING.md) - 常见问题解决方案
- 📖 [用户手册](USER_MANUAL.md) - 详细的使用说明
- 🤖 [AI Provider 指南](AI_PROVIDER_GUIDE.md) - AI 模型切换指南
- 📋 [配置说明](APPLICATION_PROPERTIES_GUIDE.md) - 配置文件详解

## 🎯 版本信息

当前版本：**v0.0.1**

版本信息会自动显示在 UI 右下角，包含：
- 版本号
- 构建时间
- Git 提交信息（如果可用）

详见 [版本管理指南](VERSION_MANAGEMENT_GUIDE.md)

## AI 模型支持

DevFlow 支持多种 AI 模型提供商，可通过配置轻松切换：

- **Qwen (通义千问)** - 阿里云 DashScope，云端托管 (默认)
- **Ollama** - 本地部署的开源模型，支持隐私保护

详见 [AI Provider 切换指南](AI_PROVIDER_GUIDE.md)

### 快速切换 AI 模型

```bash
# 使用 Ollama (本地模型)
java -jar backend/target/backend-0.0.1.jar --spring.profiles.active=ollama

# 使用 Qwen (云端模型 - 默认)
java -jar backend/target/backend-0.0.1.jar --spring.profiles.active=office
```

## 本地开发部署

### 前置要求

- Java 17+
- MongoDB 4.4+
- Maven 3.6+
- Node.js 16+ (用于前端构建)

### 构建步骤

```bash
# 1. 确保 MongoDB 正在运行
# Windows: net start MongoDB
# Linux: sudo systemctl start mongod

# 2. 克隆项目
git clone https://github.com/notechsolution/dev-flow.git
cd dev-flow

# 3. 配置环境变量（可选）
# Windows PowerShell
$env:MONGODB_URI="mongodb://localhost:27017/devflow"
$env:DASHSCOPE_API_KEY="your-api-key"

# Linux/macOS
export MONGODB_URI="mongodb://localhost:27017/devflow"
export DASHSCOPE_API_KEY="your-api-key"

# 4. 构建项目
# 方式 1: 使用快速启动脚本（推荐）
.\devflow.ps1 build         # Windows
./devflow.sh build          # Linux/Mac

# 方式 2: 直接使用部署脚本
deploy\scripts\build.ps1    # Windows
deploy/scripts/build.sh     # Linux/Mac

# 方式 3: 手动构建
mvn clean package -DskipTests

# 5. 运行应用
# 方式 1: 使用快速启动脚本（推荐）
.\devflow.ps1 start         # Windows
./devflow.sh start          # Linux/Mac

# 方式 2: 直接使用部署脚本
deploy\scripts\start-prod.ps1   # Windows
deploy/scripts/start-prod.sh    # Linux/Mac

# 方式 3: 手动启动
java -jar backend/target/devflow.jar

# 6. 访问应用
# http://localhost:8099
```

## 📦 快速打包部署

### 方式 1: 使用快速启动脚本（推荐）

项目根目录提供了便捷的启动脚本：

```powershell
# Windows
.\devflow.ps1 build                    # 打包
.\devflow.ps1 build -Version 1.0.0     # 指定版本打包
.\devflow.ps1 start                    # 启动
.\devflow.ps1 status                   # 状态检查
.\devflow.ps1 stop                     # 停止
.\devflow.ps1 restart                  # 重启
.\devflow.ps1 help                     # 帮助

# Linux/Mac
chmod +x devflow.sh                    # 添加执行权限（首次）
./devflow.sh build                     # 打包
./devflow.sh build 1.0.0               # 指定版本打包
./devflow.sh start                     # 启动
./devflow.sh stop                      # 停止
./devflow.sh help                      # 帮助
```

### 方式 2: 直接使用部署脚本

所有脚本位于 `deploy/scripts/` 目录：

#### Windows
```powershell
# 进入脚本目录
cd deploy\scripts

# 使用当前版本打包
.\build.ps1

# 指定版本号打包
.\build.ps1 -Version 1.0.0
```

#### Linux/Mac
```bash
# 进入脚本目录
cd deploy/scripts

# 添加执行权限（首次）
chmod +x *.sh

# 打包
./build.sh

# 指定版本号
./build.sh 1.0.0
```

详见 [打包脚本使用指南](deploy/docs/BUILD_SCRIPTS_GUIDE.md)

## 🚀 生产环境部署

### Windows
```powershell
# 进入脚本目录
cd deploy\scripts

# 编辑配置（首次使用）
notepad start-prod.ps1

# 启动应用
.\start-prod.ps1

# 检查状态
.\status.ps1

# 停止应用
.\stop.ps1
```

### Linux/Mac
```bash
# 进入脚本目录
cd deploy/scripts

# 编辑配置（首次使用）
vi start-prod.sh

# 添加执行权限
chmod +x *.sh

# 启动应用
./start-prod.sh

# 停止应用
./stop.sh
```

详见 [快速部署指南](deploy/docs/DEPLOYMENT_QUICK_START.md)

## Docker 快速部署

```bash
# 1. 克隆项目
git clone https://github.com/notechsolution/dev-flow.git
cd dev-flow

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env 文件，设置必要的配置

# 3. 启动所有服务
docker-compose up -d

# 4. 访问应用
# DevFlow: http://localhost:8099
# MongoDB 管理: http://localhost:8081
```

详细说明请查看 [Docker 部署指南](DOCKER_DEPLOYMENT.md)

## 默认登录

- **用户名**: admin
- **密码**: admin123

⚠️ **重要**: 首次登录后请立即修改密码！

## 健康检查

```bash
# 检查应用状态
curl http://localhost:8099/api/ping

# 综合健康检查
curl http://localhost:8099/api/health

# 检查 MongoDB 连接
curl http://localhost:8099/api/health/database

# 检查 AI 服务
curl http://localhost:8099/api/health/ai
```

## 项目结构

```
dev-flow/
├── backend/              # Spring Boot 后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── frontend/             # Vue.js 前端
│   ├── src/
│   ├── public/
│   └── pom.xml
├── scripts/
│   └── mongodb/         # MongoDB 初始化脚本
├── logs/                # 应用日志
├── docker-compose.yml   # Docker 编排文件
├── Dockerfile          # Docker 镜像定义
└── README.md
```

## 技术栈

### 后端
- Spring Boot 3.5.4
- Spring Data MongoDB
- Spring Security
- Spring AI (Alibaba DashScope / Ollama)
- JWT Authentication

### 前端
- Vue.js 3
- Vite
- TailwindCSS
- Milkdown Editor

### 数据库
- MongoDB 6.0+

### AI 服务
- 阿里云通义千问 (Qwen)
- Ollama (可选)

## 开发指南

### 开发环境配置

```bash
# 启动 MongoDB
mongod --dbpath ./data/db

# 后端开发（使用 IDE 或命令行）
cd backend
mvn spring-boot:run

# 前端开发
cd frontend
npm install
npm run dev
```

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=TestClassName
```

## 贡献

欢迎贡献代码！请遵循以下步骤：

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 [MIT License](LICENSE) 许可证。

## 支持

- 📧 Email: support@devflow.com
- 🐛 Issues: [GitHub Issues](https://github.com/notechsolution/dev-flow/issues)
- 📖 文档: [Wiki](https://github.com/notechsolution/dev-flow/wiki)

## 致谢

- Spring Boot
- Vue.js
- MongoDB
- Alibaba Cloud DashScope
- Ollama


