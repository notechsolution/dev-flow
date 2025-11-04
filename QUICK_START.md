# DevFlow 快速开始指南

5 分钟快速部署 DevFlow！

---

## 方式一：Docker 快速部署（推荐）

### 步骤 1: 安装 Docker

确保已安装 Docker 和 Docker Compose：
- Windows/Mac: [Docker Desktop](https://www.docker.com/products/docker-desktop)
- Linux: [Docker Engine](https://docs.docker.com/engine/install/)

### 步骤 2: 获取代码

```bash
git clone <your-repo-url>
cd dev-flow
```

### 步骤 3: 配置环境

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，至少设置以下项：
# DASHSCOPE_API_KEY=your-api-key-here
```

### 步骤 4: 启动服务

```bash
docker-compose up -d
```

### 步骤 5: 访问应用

- **应用地址**: http://localhost:8099
- **默认账号**: 第一个注册的用户

🎉 完成！

---

## 方式二：本地直接部署

### 前置要求

- Java 17+
- MongoDB 6.0+
- Maven 3.6+

### 步骤 1: 安装 MongoDB

**Windows:**
```powershell
choco install mongodb
net start MongoDB
```

**Linux:**
```bash
sudo apt-get install mongodb-org
sudo systemctl start mongod
```

**macOS:**
```bash
brew install mongodb-community@6.0
brew services start mongodb-community@6.0
```

### 步骤 2: 初始化数据库

```bash
mongosh < scripts/mongodb/init-mongo.js
```

### 步骤 3: 配置环境变量

**Windows PowerShell:**
```powershell
$env:MONGODB_URI="mongodb://localhost:27017/devflow"
$env:DASHSCOPE_API_KEY="your-api-key"
```

**Linux/macOS:**
```bash
export MONGODB_URI="mongodb://localhost:27017/devflow"
export DASHSCOPE_API_KEY="your-api-key"
```

### 步骤 4: 构建并运行

```bash
# 构建
mvn clean package

# 运行
java -jar backend/target/backend-0.0.1.jar
```

### 步骤 5: 访问应用

- **应用地址**: http://localhost:8099
- **默认账号**: 第一个注册的用户

🎉 完成！

---

## 获取 Qwen API Key

1. 访问 [阿里云 DashScope](https://dashscope.console.aliyun.com/)
2. 注册/登录账号
3. 创建 API Key
4. 复制 API Key 到配置中

---

## 使用 Ollama（本地 AI）

如果不想使用云端 AI 服务，可以使用本地 Ollama：

### 安装 Ollama

1. 下载：https://ollama.com/download
2. 安装并启动服务

### 下载模型

```bash
ollama pull qwen2
# 或
ollama pull llama2
```

### 配置应用

修改 `.env` 文件：
```bash
AI_PROVIDER=ollama
OLLAMA_BASE_URL=http://localhost:11434
```

或使用 Docker，取消注释 docker-compose.yml 中的 ollama 服务。

---

## 验证部署

### 健康检查

```bash
# 应用状态
curl http://localhost:8099/api/ping

# 详细健康检查
curl http://localhost:8099/api/health

# MongoDB 连接
curl http://localhost:8099/api/health/database

# AI 服务
curl http://localhost:8099/api/health/ai
```

### 预期响应

```json
{
  "status": "UP",
  "timestamp": 1699123456789,
  "database": {
    "status": "UP",
    "type": "MongoDB"
  },
  "ai": {
    "provider": "qwen",
    "status": "CONFIGURED"
  }
}
```

---

## 首次使用

### 1. 登录系统

- 访问 http://localhost:8099
- 用户名：`admin`
- 密码：`admin123`

### 2. 修改密码（重要！）

- 点击右上角头像
- 选择"个人设置"
- 修改密码

### 3. 创建项目

- 点击"项目管理"
- 点击"新建项目"
- 填写项目信息

### 4. 创建需求

- 进入项目
- 点击"AI 创建用户故事"
- 输入需求描述
- 跟随 AI 引导完成

---

## 常见问题

### Q: 端口 8099 被占用

```bash
# 修改端口
java -jar backend/target/backend-0.0.1.jar --server.port=8100

# 或在 .env 中设置
SERVER_PORT=8100
```

### Q: MongoDB 连接失败

```bash
# 检查 MongoDB 是否运行
# Windows
net start MongoDB

# Linux
sudo systemctl status mongod

# 测试连接
mongosh mongodb://localhost:27017/devflow
```

### Q: AI 功能不可用

检查：
1. DASHSCOPE_API_KEY 是否正确设置
2. 网络连接是否正常
3. 查看日志：`logs/ai-service.log`

---

## 下一步

- 📖 阅读[用户手册](USER_MANUAL.md)了解详细功能
- 🔧 查看[部署指南](DEPLOYMENT_GUIDE.md)了解完整配置
- 🐛 遇到问题查看[故障排查](TROUBLESHOOTING.md)

---

## 获取帮助

- 📧 技术支持: support@devflow.com
- 🐛 问题反馈: GitHub Issues
- 📚 完整文档: 项目 Wiki

---

**开始使用 DevFlow，让 AI 助力您的需求管理！** ✨
