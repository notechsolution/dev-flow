# DevFlow 故障排查指南 (MongoDB 版)

本指南帮助您快速诊断和解决 DevFlow 部署和运行中的常见问题。

---

## 快速诊断工具

### 健康检查命令

```bash
# 综合健康检查
curl http://localhost:8099/api/health

# MongoDB 连接检查
curl http://localhost:8099/api/health/database

# AI 服务配置检查
curl http://localhost:8099/api/health/ai

# 系统资源检查
curl http://localhost:8099/api/health/system

# 简单 ping 测试
curl http://localhost:8099/api/ping
```

---

## 1. 应用启动问题

### 1.1 端口被占用

**错误信息：**
```
Port 8099 is already in use
```

**Windows 解决方案：**
```powershell
# 查找占用端口的进程
netstat -ano | findstr :8099

# 结束进程（替换 <PID> 为实际进程 ID）
taskkill /PID <PID> /F

# 或者更改应用端口
java -jar backend\target\backend-0.0.1.jar --server.port=8100
```

**Linux/macOS 解决方案：**
```bash
# 查找占用端口的进程
lsof -i :8099

# 结束进程
kill -9 <PID>

# 或者更改应用端口
java -jar backend/target/backend-0.0.1.jar --server.port=8100
```

### 1.2 Java 版本不兼容

**错误信息：**
```
Unsupported major.minor version
```

**解决方案：**
```bash
# 检查 Java 版本（需要 Java 17+）
java -version

# 下载正确版本
# https://adoptium.net/
# 或
# https://www.oracle.com/java/technologies/downloads/
```

### 1.3 缺少依赖包

**错误信息：**
```
ClassNotFoundException
```

**解决方案：**
```bash
# 重新构建项目
cd d:\githome\github\dev-flow
mvn clean package -DskipTests

# 确认 JAR 文件完整
ls -lh backend/target/backend-0.0.1.jar
```

---

## 2. MongoDB 连接问题

### 2.1 无法连接到 MongoDB

**错误信息：**
```
Connection refused
MongoTimeoutException
```

**诊断步骤：**

```bash
# 1. 检查 MongoDB 是否运行
# Windows
net start | findstr MongoDB
sc query MongoDB

# Linux
sudo systemctl status mongod
ps aux | grep mongod

# macOS
brew services list | grep mongodb
```

**启动 MongoDB：**

```bash
# Windows
net start MongoDB

# Linux
sudo systemctl start mongod
sudo systemctl enable mongod

# macOS
brew services start mongodb-community@6.0
```

**测试连接：**
```bash
# 使用 mongosh 测试
mongosh mongodb://localhost:27017/devflow

# 检查监听端口
# Windows
netstat -ano | findstr :27017

# Linux/macOS
netstat -an | grep 27017
```

### 2.2 认证失败

**错误信息：**
```
Authentication failed
MongoSecurityException
```

**解决方案：**

```javascript
// 1. 连接到 MongoDB
mongosh

// 2. 切换到 admin 数据库
use admin

// 3. 验证用户
db.auth("devflow", "your-password")

// 4. 检查用户权限
db.getUser("devflow")

// 5. 如需重置密码
db.changeUserPassword("devflow", "new-password")

// 6. 或重新创建用户
use devflow
db.dropUser("devflow")
db.createUser({
  user: "devflow",
  pwd: "devflow123",
  roles: [
    { role: "readWrite", db: "devflow" },
    { role: "dbAdmin", db: "devflow" }
  ]
})
```

**更新应用配置：**
```bash
# 设置正确的连接字符串
export MONGODB_URI="mongodb://devflow:devflow123@localhost:27017/devflow?authSource=devflow"
```

### 2.3 数据库不存在

**错误信息：**
```
Database 'devflow' does not exist
```

**解决方案：**

```javascript
// MongoDB 会自动创建数据库，但您也可以手动创建
mongosh

use devflow

// 创建集合以初始化数据库
db.createCollection("users")
db.createCollection("projects")
db.createCollection("user_stories")
db.createCollection("images")

// 创建索引
db.users.createIndex({ "email": 1 }, { unique: true })
db.users.createIndex({ "username": 1 }, { unique: true })
db.projects.createIndex({ "owner": 1 })
db.user_stories.createIndex({ "projectId": 1 })
```

### 2.4 查看 MongoDB 日志

**查看 MongoDB 日志文件：**

```bash
# Linux
sudo tail -f /var/log/mongodb/mongod.log

# macOS (Homebrew)
tail -f /usr/local/var/log/mongodb/mongo.log

# Windows
# 日志位置: C:\Program Files\MongoDB\Server\6.0\log\mongod.log
type "C:\Program Files\MongoDB\Server\6.0\log\mongod.log"
```

---

## 3. AI 服务问题

### 3.1 Qwen API 调用失败

**错误信息：**
```
401 Unauthorized
Invalid API key
```

**诊断步骤：**

```bash
# 1. 检查 API Key 是否设置
echo $DASHSCOPE_API_KEY

# 2. 测试 API Key（替换为您的实际 Key）
curl -X POST https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation \
  -H "Authorization: Bearer $DASHSCOPE_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "qwen-max",
    "input": {
      "prompt": "你好"
    }
  }'

# 3. 检查应用配置
curl http://localhost:8099/api/diagnostics/config
```

**解决方案：**

```bash
# 1. 从阿里云 DashScope 获取 API Key
# 访问: https://dashscope.console.aliyun.com/

# 2. 设置环境变量
# Windows PowerShell
$env:DASHSCOPE_API_KEY="your-api-key"

# Linux/macOS
export DASHSCOPE_API_KEY="your-api-key"

# 3. 或在配置文件中设置
# 编辑 backend/src/main/resources/application.properties
spring.ai.dashscope.api-key=your-api-key
```

### 3.2 Ollama 连接失败

**错误信息：**
```
Connection refused to http://localhost:11434
```

**诊断步骤：**

```bash
# 1. 检查 Ollama 是否运行
curl http://localhost:11434/api/tags

# 2. 查看 Ollama 进程
# Windows
tasklist | findstr ollama

# Linux/macOS
ps aux | grep ollama
```

**解决方案：**

```bash
# 1. 安装 Ollama
# 访问: https://ollama.com/download

# 2. 启动 Ollama 服务
ollama serve

# 3. 下载模型
ollama pull qwen2
ollama pull llama2

# 4. 列出已下载的模型
ollama list

# 5. 测试模型
ollama run qwen2 "你好"

# 6. 设置应用使用 Ollama
java -jar backend/target/backend-0.0.1.jar --spring.profiles.active=ollama
```

### 3.3 AI 响应超时

**错误信息：**
```
Read timed out
SocketTimeoutException
```

**解决方案：**

1. **检查网络连接**（Qwen）
2. **检查 Ollama 服务状态**（Ollama）
3. **查看 AI 服务日志**：

```bash
# 查看最近的 AI 调用日志
# Windows
type logs\ai-service.log | findstr ERROR

# Linux/macOS
tail -f logs/ai-service.log | grep ERROR
```

### 3.4 测试 AI 服务

```bash
# 使用诊断接口测试 AI 服务
curl -X POST http://localhost:8099/api/diagnostics/ai/test \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"prompt": "你好，这是一个测试"}'
```

---

## 4. 前端访问问题

### 4.1 404 Not Found

**原因：**
- 应用未启动
- 端口配置错误
- 前端资源未正确打包

**解决方案：**

```bash
# 1. 确认应用已启动
curl http://localhost:8099/api/ping

# 2. 检查前端资源
ls -la backend/target/classes/public/static/

# 3. 重新构建（包含前端）
mvn clean package

# 4. 访问正确的 URL
http://localhost:8099
```

### 4.2 CORS 错误

**错误信息（浏览器控制台）：**
```
Access to XMLHttpRequest blocked by CORS policy
```

**解决方案：**

检查 WebSecurityConfiguration 中的 CORS 配置。如果前端和后端在不同端口运行，需要配置 CORS。

### 4.3 登录失败

**诊断步骤：**

```bash
# 1. 检查用户是否存在
mongosh
use devflow
db.users.findOne({ username: "admin" })

# 2. 查看认证日志
tail -f logs/security.log

# 3. 重置管理员密码（如需要）
# 需要在应用中实现或手动修改数据库
```

---

## 5. 性能问题

### 5.1 响应缓慢

**诊断步骤：**

```bash
# 1. 检查系统资源
# Windows
taskmgr

# Linux
top
htop

# macOS
top

# 2. 检查内存使用
curl http://localhost:8099/api/health/system

# 3. 查看 MongoDB 性能
mongosh
use devflow
db.currentOp()
db.serverStatus()
```

**优化建议：**

1. **增加 JVM 内存：**
```bash
java -Xms512m -Xmx2048m -jar backend/target/backend-0.0.1.jar
```

2. **检查 MongoDB 索引：**
```javascript
mongosh
use devflow
db.users.getIndexes()
db.projects.getIndexes()
db.user_stories.getIndexes()

// 如缺少索引，创建它们
db.users.createIndex({ "email": 1 })
db.projects.createIndex({ "owner": 1 })
db.user_stories.createIndex({ "projectId": 1 })
```

3. **启用查询分析：**
```javascript
// 分析慢查询
db.setProfilingLevel(1, { slowms: 100 })
db.system.profile.find().limit(10).sort({ ts: -1 }).pretty()
```

### 5.2 内存不足

**错误信息：**
```
OutOfMemoryError
GC overhead limit exceeded
```

**解决方案：**

```bash
# 调整 JVM 内存设置
java -Xms1024m -Xmx2048m -XX:+UseG1GC -jar backend/target/backend-0.0.1.jar

# 查看内存使用情况
curl http://localhost:8099/api/health/system
```

---

## 6. 日志位置

```
项目根目录/
├── logs/
│   ├── devflow.log         # 主日志文件
│   ├── error.log           # 错误日志
│   ├── ai-service.log      # AI 服务调用日志
│   ├── mongodb.log         # MongoDB 操作日志
│   └── security.log        # 安全相关日志
```

### 查看日志命令

```bash
# Windows PowerShell
Get-Content logs\devflow.log -Tail 50 -Wait

# Linux/macOS
tail -f logs/devflow.log

# 查看错误日志
tail -f logs/error.log

# 搜索特定错误
grep "ERROR" logs/devflow.log
grep "Exception" logs/devflow.log
```

---

## 7. 数据备份与恢复

### 7.1 备份 MongoDB 数据

```bash
# 备份整个数据库
mongodump --uri="mongodb://devflow:devflow123@localhost:27017/devflow" --out=./backup

# 备份到压缩文件
mongodump --uri="mongodb://localhost:27017/devflow" --archive=devflow-backup.gz --gzip

# 指定集合备份
mongodump --uri="mongodb://localhost:27017/devflow" --collection=users --out=./backup
```

### 7.2 恢复 MongoDB 数据

```bash
# 从备份恢复
mongorestore --uri="mongodb://localhost:27017/devflow" ./backup/devflow

# 从压缩文件恢复
mongorestore --uri="mongodb://localhost:27017/devflow" --archive=devflow-backup.gz --gzip

# 恢复到不同数据库
mongorestore --uri="mongodb://localhost:27017/devflow_restore" ./backup/devflow
```

---

## 8. 环境信息收集

当需要报告问题时，请收集以下信息：

```bash
# 1. 系统信息
java -version
mongod --version

# Windows
systeminfo

# Linux
uname -a
cat /etc/os-release

# 2. 应用健康状态
curl http://localhost:8099/api/health

# 3. 诊断报告
curl http://localhost:8099/api/diagnostics/report

# 4. 最近的错误日志
# Windows
Get-Content logs\error.log -Tail 100

# Linux/macOS
tail -100 logs/error.log

# 5. MongoDB 状态
mongosh --eval "db.serverStatus()" mongodb://localhost:27017/devflow
```

---

## 9. 常见配置错误

### 9.1 加密密钥未设置

**错误信息：**
```
Encryption key not configured
```

**解决方案：**
```bash
# 设置必需的加密密钥
export MONGODB_ENCRYPT_KEYS_KEY_1="your-32-char-base64-key"
export CRYPTO_SECRET_KEY="YourSecretKey32CharactersLong!"
export JWT_SECRET_KEY_BASE64="your-jwt-secret-base64"
```

### 9.2 配置文件路径错误

**确保使用正确的配置文件：**
```bash
# 查看当前激活的配置
curl http://localhost:8099/api/diagnostics/config

# 指定配置文件
java -jar backend/target/backend-0.0.1.jar \
  --spring.config.location=file:./config/application.properties
```

---

## 10. 获取帮助

如果以上方法都无法解决问题，请提供以下信息寻求帮助：

1. **错误描述和复现步骤**
2. **完整的错误堆栈信息**（logs/error.log）
3. **系统环境信息**
   - 操作系统版本
   - Java 版本
   - MongoDB 版本
4. **健康检查结果**
   ```bash
   curl http://localhost:8099/api/health
   ```
5. **诊断报告**
   ```bash
   curl http://localhost:8099/api/diagnostics/report
   ```
6. **配置信息**（脱敏后）
7. **相关日志片段**

---

## 快速参考

| 问题类型 | 快速检查命令 | 日志位置 |
|---------|-------------|---------|
| 应用启动 | `curl http://localhost:8099/api/ping` | logs/devflow.log |
| MongoDB | `mongosh mongodb://localhost:27017/devflow` | logs/mongodb.log |
| AI 服务 | `curl http://localhost:8099/api/health/ai` | logs/ai-service.log |
| 认证问题 | `curl http://localhost:8099/api/health` | logs/security.log |
| 性能 | `curl http://localhost:8099/api/health/system` | logs/devflow.log |
