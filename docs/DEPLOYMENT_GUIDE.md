# DevFlow 部署指南 (MongoDB 版)

## 系统要求

### 必需组件
- **Java 17** 或更高版本
- **MongoDB 4.4+** (推荐 5.0 或 6.0)
- **至少 2GB 可用内存**
- **至少 1GB 磁盘空间**

### 可选组件
- **Ollama** (如果使用本地 AI 模型)
- **Docker & Docker Compose** (容器化部署)

---

## MongoDB 安装与配置

### Windows 安装

```powershell
# 下载 MongoDB Community Server
# 访问: https://www.mongodb.com/try/download/community

# 或使用 Chocolatey 安装
choco install mongodb

# 启动 MongoDB 服务
net start MongoDB

# 验证安装
mongo --version
```

### Linux 安装

```bash
# Ubuntu/Debian
wget -qO - https://www.mongodb.org/static/pgp/server-6.0.asc | sudo apt-key add -
echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu focal/mongodb-org/6.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-6.0.list
sudo apt-get update
sudo apt-get install -y mongodb-org

# 启动 MongoDB
sudo systemctl start mongod
sudo systemctl enable mongod

# 验证安装
mongod --version
```

### macOS 安装

```bash
# 使用 Homebrew
brew tap mongodb/brew
brew install mongodb-community@6.0

# 启动 MongoDB
brew services start mongodb-community@6.0

# 验证安装
mongod --version
```

---

## MongoDB 数据库初始化

### 方式一：使用 MongoDB Shell

```javascript
// 连接到 MongoDB
mongosh

// 切换到 admin 数据库
use admin

// 创建管理员用户（如果启用了认证）
db.createUser({
  user: "admin",
  pwd: "admin123",  // 生产环境请使用强密码
  roles: [ { role: "userAdminAnyDatabase", db: "admin" } ]
})

// 创建 DevFlow 应用数据库和用户
use devflow
db.createUser({
  user: "devflow",
  pwd: "devflow123",  // 生产环境请使用强密码
  roles: [
    { role: "readWrite", db: "devflow" },
    { role: "dbAdmin", db: "devflow" }
  ]
})

// 创建集合（可选，应用启动时会自动创建）
db.createCollection("users")
db.createCollection("projects")
db.createCollection("user_stories")
db.createCollection("images")

// 创建索引以提高性能
db.users.createIndex({ "email": 1 }, { unique: true })
db.users.createIndex({ "username": 1 }, { unique: true })
db.projects.createIndex({ "owner": 1 })
db.projects.createIndex({ "createdAt": -1 })
db.user_stories.createIndex({ "projectId": 1 })
db.user_stories.createIndex({ "createdAt": -1 })
```

### 方式二：使用初始化脚本

运行提供的初始化脚本：

```bash
# Windows (PowerShell)
mongosh < scripts\mongodb\init.js

# Linux/macOS
mongosh < scripts/mongodb/init.js
```

---

## 环境变量配置

### 必需的环境变量

```bash
# ========================================
# MongoDB 配置
# ========================================
export MONGODB_URI=mongodb://devflow:devflow123@localhost:27017/devflow
export MONGODB_DATABASE_NAME=devflow

# 如果启用了 MongoDB 认证
export MONGODB_URI=mongodb://devflow:devflow123@localhost:27017/devflow?authSource=devflow

# 如果使用 MongoDB Atlas (云服务)
export MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/devflow?retryWrites=true&w=majority

# ========================================
# 数据加密配置 (重要：生产环境必须修改)
# ========================================
export MONGODB_ENCRYPT_KEYS_VERSION_1=1
export MONGODB_ENCRYPT_KEYS_KEY_1=V21acTR0N3cheiVDKkYtSmFOZFJnVWtYcDJyNXU4eC8=
export CRYPTO_SECRET_KEY=YourSecretKey32CharactersLong!
export JWT_SECRET_KEY_BASE64=Ti8kN3pwUlB2QklxKG5uRGlAL2w/NX0xP3hsUnpTd3o=

# ========================================
# AI 配置 (选择其一)
# ========================================
# 方案一：使用 Qwen (通义千问 - 云端)
export AI_PROVIDER=qwen
export DASHSCOPE_API_KEY=your-dashscope-api-key

# 方案二：使用 Ollama (本地模型)
export AI_PROVIDER=ollama
export OLLAMA_BASE_URL=http://localhost:11434
export OLLAMA_MODEL=qwen2

# ========================================
# OAuth2 配置 (可选)
# ========================================
export GITHUB_CLIENT_ID=your-github-client-id
export GITHUB_CLIENT_SECRET=your-github-client-secret

# ========================================
# 邮件配置 (可选，用于密码重置等功能)
# ========================================
export MAIL_HOST=smtp.163.com
export MAIL_PORT=465
export MAIL_USERNAME=your-email@163.com
export MAIL_PASSWORD=your-app-password
```

### Windows PowerShell 设置环境变量

```powershell
$env:MONGODB_URI="mongodb://localhost:27017/devflow"
$env:MONGODB_DATABASE_NAME="devflow"
$env:AI_PROVIDER="qwen"
$env:DASHSCOPE_API_KEY="your-api-key"
```

---

## 快速启动

### 方式一：默认配置（开发环境）

```bash
# 确保 MongoDB 正在运行
# Windows: net start MongoDB
# Linux: sudo systemctl status mongod

# 启动应用
java -jar backend\target\backend-0.0.1.jar
```

### 方式二：使用环境变量

```bash
# Windows PowerShell
$env:MONGODB_URI="mongodb://localhost:27017/devflow"; `
$env:DASHSCOPE_API_KEY="your-api-key"; `
java -jar backend\target\backend-0.0.1.jar

# Linux/macOS
MONGODB_URI=mongodb://localhost:27017/devflow \
DASHSCOPE_API_KEY=your-api-key \
java -jar backend/target/backend-0.0.1.jar
```

### 方式三：使用本地 Ollama 模型

```bash
# 1. 安装并启动 Ollama
# 下载: https://ollama.com/download
ollama serve

# 2. 下载模型
ollama pull qwen2

# 3. 启动应用
java -jar backend\target\backend-0.0.1.jar --spring.profiles.active=ollama
```

### 方式四：指定配置文件

```bash
# 使用 office 配置
java -jar backend\target\backend-0.0.1.jar --spring.profiles.active=office

# 使用 local 配置
java -jar backend\target\backend-0.0.1.jar --spring.profiles.active=local
```

---

## 访问应用

应用启动后，访问以下地址：

- **主页面**: http://localhost:8099
- **健康检查**: http://localhost:8099/api/health
- **API 文档**: http://localhost:8099/swagger-ui.html (如已启用)

### 默认管理员账号

**重要：第一个注册的用户将自动成为管理员**


---

## 健康检查与监控

### 检查应用状态

```bash
# 综合健康检查
curl http://localhost:8099/api/health

# 检查 MongoDB 连接
curl http://localhost:8099/api/health/database

# 检查 AI 服务配置
curl http://localhost:8099/api/health/ai

# 检查系统资源
curl http://localhost:8099/api/health/system
```

### 预期响应示例

```json
{
  "status": "UP",
  "timestamp": 1699123456789,
  "database": {
    "status": "UP",
    "type": "MongoDB",
    "version": "6.0.12",
    "collections": ["users", "projects", "user_stories", "images"]
  },
  "ai": {
    "provider": "qwen",
    "status": "CONFIGURED"
  },
  "system": {
    "processors": 8,
    "freeMemory": "512 MB",
    "totalMemory": "1024 MB",
    "javaVersion": "17.0.9"
  }
}
```

---

## MongoDB 连接字符串格式

### 标准连接（无认证）

```
mongodb://localhost:27017/devflow
```

### 带认证的连接

```
mongodb://username:password@localhost:27017/devflow?authSource=devflow
```

### 复制集连接

```
mongodb://host1:27017,host2:27017,host3:27017/devflow?replicaSet=rs0
```

### MongoDB Atlas (云服务)

```
mongodb+srv://username:password@cluster.mongodb.net/devflow?retryWrites=true&w=majority
```

### 常用参数说明

- `authSource`: 认证数据库名称
- `replicaSet`: 复制集名称
- `retryWrites`: 启用重试写入
- `w=majority`: 写入确认级别
- `maxPoolSize`: 最大连接池大小

---

## 生产环境部署检查清单

### 安全配置

- [ ] 修改默认管理员密码
- [ ] 更换所有加密密钥（CRYPTO_SECRET_KEY, JWT_SECRET_KEY, MONGODB_ENCRYPT_KEYS）
- [ ] 启用 MongoDB 认证
- [ ] 配置 MongoDB 访问控制（bindIp, 防火墙规则）
- [ ] 使用 HTTPS/SSL 连接
- [ ] 定期备份数据库

### 性能优化

- [ ] 创建必要的数据库索引
- [ ] 配置 MongoDB 连接池大小
- [ ] 启用 MongoDB 慢查询日志
- [ ] 配置 JVM 内存参数（-Xms, -Xmx）

### 监控配置

- [ ] 配置日志收集
- [ ] 设置健康检查端点
- [ ] 配置告警通知
- [ ] 监控 MongoDB 性能指标

### 备份策略

- [ ] 配置定期数据备份
- [ ] 测试数据恢复流程
- [ ] 存储备份到安全位置

---

## 故障排查

如遇到问题，请参考 [TROUBLESHOOTING.md](TROUBLESHOOTING.md) 文档。

常见问题快速检查：

```bash
# 1. 检查 MongoDB 是否运行
# Windows
net start | findstr MongoDB

# Linux
sudo systemctl status mongod

# 2. 测试 MongoDB 连接
mongosh mongodb://localhost:27017/devflow

# 3. 查看应用日志
# Windows
type logs\devflow.log | findstr ERROR

# Linux
tail -f logs/devflow.log | grep ERROR

# 4. 检查端口占用
# Windows
netstat -ano | findstr :8099

# Linux
lsof -i :8099
```

---

## Docker 部署

参考 [docker-compose.yml](docker-compose.yml) 文件进行容器化部署。

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f devflow

# 停止服务
docker-compose down
```

---

## 扩展阅读

- [MongoDB 官方文档](https://docs.mongodb.com/)
- [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb)
- [故障排查指南](TROUBLESHOOTING.md)
- [用户手册](USER_MANUAL.md)
- [AI Provider 切换指南](AI_PROVIDER_GUIDE.md)
