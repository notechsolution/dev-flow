# DevFlow - 需求管理与开发流程工具

## 功能特性

- 🎯 智能需求优化 - AI 辅助优化用户故事
- 🤖 多 AI 模型支持 - 支持 Qwen (通义千问) 和 Ollama 本地模型
- 📝 需求澄清 - 自动生成澄清问题
- ✅ 测试用例生成 - AI 自动生成测试用例
- 🔐 OAuth2 集成 - 支持 GitHub 等第三方登录
- 📊 项目管理 - 完整的项目和需求管理
- 🗄️ MongoDB 存储 - 灵活的 NoSQL 数据库

## 快速开始

### 文档导航

- 📘 [部署指南](DEPLOYMENT_GUIDE.md) - 完整的部署文档，包含 MongoDB 配置
- 🐳 [Docker 部署](DOCKER_DEPLOYMENT.md) - 使用 Docker Compose 快速部署
- 🔧 [故障排查](TROUBLESHOOTING.md) - 常见问题解决方案
- 📖 [用户手册](USER_MANUAL.md) - 详细的使用说明
- 📋 [配置说明](APPLICATION_PROPERTIES_GUIDE.md) - 配置文件详解
- 🤖 [AI Provider 指南](AI_PROVIDER_GUIDE.md) - AI 模型切换指南

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

# 4. 在父 pom 目录执行构建
mvn clean package

# 5. 运行应用
java -jar backend/target/backend-0.0.1.jar

# 6. 访问应用
# http://localhost:8099
```

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


