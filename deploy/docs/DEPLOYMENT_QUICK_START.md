# DevFlow 快速部署指南

## 📦 开发环境打包

### Windows 环境

```powershell
# 在项目根目录执行
.\build.ps1

# 或指定版本号
.\build.ps1 -Version 1.0.0
```

### Linux/Mac 环境

```bash
# 添加执行权限
chmod +x build.sh

# 执行打包
./build.sh

# 或指定版本号
./build.sh 1.0.0
```

## 🚀 部署到生产环境

### 步骤 1: 打包应用

在开发机器上执行：

```bash
mvn clean package -DskipTests
```

生成文件位置：
- `backend/target/devflow.jar` - 后端可执行 JAR
- `frontend/target/dist/` - 前端静态文件（已包含在 JAR 中）

### 步骤 2: 上传到服务器

#### Linux/Mac 服务器
```bash
# 使用 scp 上传
scp backend/target/devflow.jar user@your-server:/app/

# 上传启动脚本
scp start-prod.sh user@your-server:/app/
scp stop.sh user@your-server:/app/
```

#### Windows 服务器
```powershell
# 使用 RDP 或共享文件夹复制
# 或使用 PowerShell 远程复制
Copy-Item backend\target\devflow.jar -Destination "\\server\share\app\"
Copy-Item start-prod.ps1 -Destination "\\server\share\app\"
Copy-Item stop.ps1 -Destination "\\server\share\app\"
```

### 步骤 3: 配置环境

#### Linux/Mac 服务器

SSH 登录到服务器：

```bash
ssh user@your-server
cd /app
```

编辑启动脚本 `start-prod.sh`，修改以下配置：

```bash
# AI 配置
export DASHSCOPE_API_KEY="your-actual-api-key"

# 数据库配置
export MONGODB_URI="mongodb://user:pass@host:27017/devflow"

# 邮件配置
export MAIL_USERNAME="your-email@163.com"
export MAIL_PASSWORD="your-authorization-code"
```

添加执行权限：

```bash
chmod +x start-prod.sh
chmod +x stop.sh
```

#### Windows 服务器

远程桌面登录到服务器，编辑 `start-prod.ps1`：

```powershell
# AI 配置
$env:DASHSCOPE_API_KEY = "your-actual-api-key"

# 数据库配置
$env:MONGODB_URI = "mongodb://user:pass@host:27017/devflow"

# 邮件配置
$env:MAIL_USERNAME = "your-email@163.com"
$env:MAIL_PASSWORD = "your-authorization-code"
```

### 步骤 4: 启动应用

#### Linux/Mac
```bash
./start-prod.sh
```

#### Windows
```powershell
.\start-prod.ps1
```

启动成功后会显示：
- 进程 ID
- 日志文件位置
- 版本信息

### 步骤 5: 验证部署

#### Linux/Mac

**检查进程**
```bash
ps aux | grep devflow.jar
```

**检查进程**
```bash
ps aux | grep devflow.jar
```

**查看日志**
```bash
tail -f /app/logs/devflow.log
```

**测试 API**
```bash
# 健康检查
curl http://localhost:8099/api/health

# 版本信息
curl http://localhost:8099/api/version

# 预期输出:
# {
#   "application": "DevFlow",
#   "version": "0.0.1",
#   "buildTime": "2025-11-06 14:30:00",
#   ...
# }
```

#### Windows

**检查进程**
```powershell
# 查看进程
Get-Process -Name java | Where-Object {$_.CommandLine -like "*devflow.jar*"}

# 或使用状态脚本
.\status.ps1
```

**查看日志**
```powershell
Get-Content logs\devflow.log -Wait -Tail 50
```

**测试 API**
```powershell
# 健康检查
Invoke-RestMethod -Uri http://localhost:8099/api/health

# 版本信息
Invoke-RestMethod -Uri http://localhost:8099/api/version | ConvertTo-Json
```

#### 访问 UI

打开浏览器访问：`http://your-server-ip:8099`

查看右下角是否显示版本号：`v0.0.1`

## 🔄 更新应用

### Linux/Mac

**步骤 1: 停止当前版本**
```bash
./stop.sh
```

**步骤 2: 备份旧版本**
```bash
mv devflow.jar devflow.jar.backup.$(date +%Y%m%d_%H%M%S)
```

**步骤 3: 上传新版本**
```bash
# 在开发机器上
scp backend/target/devflow.jar user@your-server:/app/
```

**步骤 4: 启动新版本**
```bash
./start-prod.sh
```

**步骤 5: 验证更新**
```bash
# 检查版本号是否更新
curl http://localhost:8099/api/version | grep version
```

### Windows

**步骤 1: 停止当前版本**
```powershell
.\stop.ps1
```

**步骤 2: 备份旧版本**
```powershell
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
Move-Item backend\target\devflow.jar "devflow.jar.backup.$timestamp"
```

**步骤 3: 上传新版本**
```powershell
# 从开发机器复制
Copy-Item backend\target\devflow.jar -Destination "\\server\share\app\"
```

**步骤 4: 启动新版本**
```powershell
.\start-prod.ps1
```

**步骤 5: 验证更新**
```powershell
# 检查版本号是否更新
(Invoke-RestMethod -Uri http://localhost:8099/api/version).version
```

## 🛠️ 常用运维命令

### Linux/Mac

**查看应用状态**
```bash
# 检查进程
ps -ef | grep devflow

# 查看端口
netstat -tlnp | grep 8099

# 或使用 lsof
lsof -i :8099
```

**查看日志**
```bash
# 实时日志
tail -f /app/logs/devflow.log

# 最近 100 行
tail -n 100 /app/logs/devflow.log

# 错误日志
grep "ERROR" /app/logs/devflow.log

# 今天的日志
grep "$(date +%Y-%m-%d)" /app/logs/devflow.log
# 错误日志
grep "ERROR" /app/logs/devflow.log

# 今天的日志
grep "$(date +%Y-%m-%d)" /app/logs/devflow.log
```

**重启应用**
```bash
./stop.sh && ./start-prod.sh

# 或使用专用重启脚本（如果有）
./restart.sh
```

**内存使用情况**
```bash
# 查看 Java 进程内存
ps aux | grep devflow.jar

# 详细堆信息
jmap -heap $(cat /app/devflow.pid)
```

### Windows

**查看应用状态**
```powershell
# 完整状态检查
.\status.ps1

# 检查进程
Get-Process -Name java | Where-Object {$_.CommandLine -like "*devflow.jar*"}

# 查看端口
Get-NetTCPConnection -LocalPort 8099 -State Listen
```

**查看日志**
```powershell
# 实时日志
Get-Content logs\devflow.log -Wait -Tail 50

# 最近 100 行
Get-Content logs\devflow.log -Tail 100

# 错误日志
Get-Content logs\devflow.log | Select-String "ERROR"

# 今天的日志
$today = Get-Date -Format "yyyy-MM-dd"
Get-Content logs\devflow.log | Select-String $today
```

**重启应用**
```powershell
# 停止再启动
.\stop.ps1
.\start-prod.ps1

# 或使用重启脚本
.\restart.ps1
```

**内存使用情况**
```powershell
# 查看进程内存
Get-Process -Name java | Where-Object {$_.CommandLine -like "*devflow.jar*"} | 
  Select-Object Id, ProcessName, @{N='Memory(MB)';E={[math]::Round($_.WS/1MB,2)}}
```

## 🐳 Docker 部署（可选）

### 创建 Dockerfile

在项目根目录创建 `Dockerfile`：

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY backend/target/devflow.jar /app/devflow.jar

EXPOSE 8099

ENTRYPOINT ["java", "-jar", "/app/devflow.jar"]
```

### 构建镜像

```bash
# 先打包应用
mvn clean package -DskipTests

# 构建 Docker 镜像
docker build -t devflow:1.0.0 .
```

### 运行容器

```bash
docker run -d \
  --name devflow \
  -p 8099:8099 \
  -e AI_PROVIDER=qwen \
  -e DASHSCOPE_API_KEY=your-key \
  -e MONGODB_URI=mongodb://host:27017/devflow \
  -e MAIL_USERNAME=your-email@163.com \
  -e MAIL_PASSWORD=your-password \
  -v /app/logs:/app/logs \
  devflow:1.0.0
```

### 查看容器日志

```bash
docker logs -f devflow
```

### 停止容器

```bash
docker stop devflow
docker rm devflow
```

## 📊 监控和告警

### 健康检查端点

```bash
# 系统健康
curl http://localhost:8099/api/health

# 系统信息
curl http://localhost:8099/api/system/info
```

### 设置定时健康检查

添加到 crontab：

```bash
crontab -e

# 每 5 分钟检查一次
*/5 * * * * curl -f http://localhost:8099/api/health || echo "DevFlow is down!" | mail -s "Alert: DevFlow Down" admin@example.com
```

## 🔧 故障排查

### 问题 1: 应用无法启动

**检查日志**：

```bash
tail -100 /app/logs/devflow.log
```

**常见原因**：
- 端口被占用：`lsof -i :8099`
- MongoDB 连接失败：检查 MONGODB_URI
- 环境变量未设置：检查 start-prod.sh

### 问题 2: 内存溢出

**调整 JVM 参数**：

编辑 `start-prod.sh`：

```bash
JAVA_OPTS="-Xms1g -Xmx4g -XX:+UseG1GC"
```

### 问题 3: 版本号显示 unknown

**原因**：Maven 资源过滤未生效

**解决**：

```bash
# 重新打包
mvn clean package -DskipTests

# 验证
unzip -p backend/target/devflow.jar BOOT-INF/classes/application.properties | grep version
```

## 📝 部署检查清单

部署前检查：

- [ ] 已修改 `pom.xml` 中的版本号
- [ ] 已执行 `mvn clean package -DskipTests`
- [ ] 已备份生产环境数据
- [ ] 已准备好环境变量配置
- [ ] 已测试数据库连接
- [ ] 已测试 AI API 密钥

部署后验证：

- [ ] 应用进程正常运行
- [ ] 日志无ERROR级别错误
- [ ] `/api/health` 返回正常
- [ ] `/api/version` 返回正确版本
- [ ] UI 可以正常访问
- [ ] UI 右下角显示版本号
- [ ] 核心功能测试通过

## 🔐 安全建议

1. **不要在启动脚本中硬编码敏感信息**

使用环境变量或配置文件：

```bash
# 从文件加载配置
source /etc/devflow/config.env
```

2. **限制文件权限**

```bash
chmod 600 /app/start-prod.sh
chmod 400 /etc/devflow/config.env
```

3. **使用防火墙**

```bash
# 只允许特定 IP 访问
iptables -A INPUT -p tcp --dport 8099 -s 192.168.1.0/24 -j ACCEPT
iptables -A INPUT -p tcp --dport 8099 -j DROP
```

4. **启用 HTTPS**

使用 Nginx 反向代理并配置 SSL 证书。

## 📞 支持

如有问题，请查看：
- 详细文档：`VERSION_MANAGEMENT_GUIDE.md`
- 故障排查：`TROUBLESHOOTING.md`
- 日志文件：`/app/logs/devflow.log`
