# Windows PowerShell 脚本使用指南

## 📋 脚本列表

DevFlow 提供了完整的 Windows PowerShell 管理脚本：

| 脚本 | 功能 | 用途 |
|------|------|------|
| `start-prod.ps1` | 启动应用 | 在生产环境启动 DevFlow |
| `stop.ps1` | 停止应用 | 优雅地停止运行中的应用 |
| `restart.ps1` | 重启应用 | 停止后重新启动应用 |
| `status.ps1` | 状态检查 | 查看应用运行状态和详细信息 |
| `build.ps1` | 打包构建 | 构建项目并打包 JAR 文件 |
| `build-zh.ps1` | 打包构建（中文） | 中文界面的打包脚本 |

## 🚀 快速开始

### 1. 启动应用

```powershell
# 首次启动前，先编辑 start-prod.ps1 配置环境变量
notepad start-prod.ps1

# 启动应用
.\start-prod.ps1
```

**输出示例：**
```
==========================================
       Starting DevFlow
==========================================

JAR file: backend\target\devflow.jar
Port: 8099
Profile: prod
Log file: logs\devflow.log

Application started successfully!
Process ID: 12345

Application is running!

Version Information:
  Application: DevFlow
  Version: 0.0.1
  Build Time: 2025-11-06 14:30:00

Access application at: http://localhost:8099
```

### 2. 检查状态

```powershell
.\status.ps1
```

**输出示例：**
```
==========================================
       DevFlow Status
==========================================

PID File: Found (12345)
Process Status: Running

Process Details:
  Process ID: 12345
  Process Name: java
  Start Time: 11/06/2025 14:30:00
  Uptime: 0d 2h 15m 30s
  Memory Usage: 512.50 MB
  CPU Time: 00:05:23

Port Check:
  Port 8099: In Use (Listening)
  Process: java (PID: 12345)

Health Check:
  Health API: Responding
  Status: UP

Version Information:
  Application: DevFlow
  Version: 0.0.1
  Build Time: 2025-11-06 14:30:00

==========================================
Overall Status: RUNNING

Access application at: http://localhost:8099
==========================================
```

### 3. 停止应用

```powershell
.\stop.ps1
```

**输出示例：**
```
==========================================
       Stopping DevFlow
==========================================

Found process: 12345
Stopping process...
.....

Application stopped successfully
PID file cleaned up
```

### 4. 重启应用

```powershell
.\restart.ps1
```

## 📝 详细使用说明

### start-prod.ps1

**功能：** 启动 DevFlow 应用

**配置项：**

在使用前，需要修改脚本中的以下配置：

```powershell
# AI 配置
$env:AI_PROVIDER = "qwen"                    # AI 提供商：qwen 或 ollama
$env:DASHSCOPE_API_KEY = "your-api-key"      # 通义千问 API Key

# 数据库配置
$env:MONGODB_URI = "mongodb://user:pass@host:27017/devflow"

# 邮件配置
$env:MAIL_HOST = "smtp.163.com"              # SMTP 服务器
$env:MAIL_PORT = "465"                       # SMTP 端口
$env:MAIL_USERNAME = "your-email@163.com"    # 邮箱账号
$env:MAIL_PASSWORD = "your-auth-code"        # 授权码（非密码）

# JVM 配置
$JAVA_OPTS = "-Xms512m -Xmx2048m -XX:+UseG1GC"
```

**特性：**
- ✅ 自动创建日志目录
- ✅ 检查 JAR 文件是否存在
- ✅ 检查端口是否被占用
- ✅ 后台运行应用
- ✅ 日志自动重定向到文件
- ✅ 保存进程 ID 到 PID 文件
- ✅ 自动获取 Git 信息
- ✅ 启动后健康检查
- ✅ 显示版本信息

**日志位置：** `logs\devflow.log`

**PID 文件：** `devflow.pid`

### stop.ps1

**功能：** 停止 DevFlow 应用

**停止策略：**
1. 尝试读取 PID 文件
2. 如果 PID 文件不存在，通过进程名查找
3. 先尝试优雅关闭（30秒超时）
4. 如果优雅关闭失败，强制终止
5. 清理 PID 文件

**使用示例：**
```powershell
# 基本使用
.\stop.ps1

# 如果进程卡住，会自动强制终止
```

### restart.ps1

**功能：** 重启 DevFlow 应用

**流程：**
1. 调用 `stop.ps1` 停止应用
2. 等待 3 秒
3. 调用 `start-prod.ps1` 启动应用

**使用示例：**
```powershell
.\restart.ps1
```

### status.ps1

**功能：** 检查应用运行状态

**检查项：**
- ✅ PID 文件状态
- ✅ 进程运行状态
- ✅ 进程详细信息（ID、名称、启动时间、运行时长）
- ✅ 内存使用情况
- ✅ CPU 时间
- ✅ 端口监听状态
- ✅ 健康检查 API
- ✅ 版本信息 API
- ✅ 综合状态评估

**使用示例：**
```powershell
# 完整状态检查
.\status.ps1

# 只查看进程信息
Get-Process -Name java | Where-Object {$_.CommandLine -like "*devflow.jar*"}
```

### build.ps1

**功能：** 构建打包项目

**使用示例：**
```powershell
# 使用当前版本打包
.\build.ps1

# 指定新版本打包
.\build.ps1 -Version 1.0.0
```

详见 [打包脚本使用指南](BUILD_SCRIPTS_GUIDE.md)

## 🛠️ 常见操作

### 查看实时日志

```powershell
# 查看最新 50 行并持续监控
Get-Content logs\devflow.log -Wait -Tail 50

# 查看最近 100 行
Get-Content logs\devflow.log -Tail 100
```

### 搜索日志

```powershell
# 搜索错误日志
Get-Content logs\devflow.log | Select-String "ERROR"

# 搜索特定关键词
Get-Content logs\devflow.log | Select-String "关键词"

# 搜索今天的日志
$today = Get-Date -Format "yyyy-MM-dd"
Get-Content logs\devflow.log | Select-String $today

# 统计错误数量
(Get-Content logs\devflow.log | Select-String "ERROR").Count
```

### 检查端口占用

```powershell
# 查看端口 8099 是否被占用
Get-NetTCPConnection -LocalPort 8099 -State Listen

# 查看占用端口的进程
Get-Process -Id (Get-NetTCPConnection -LocalPort 8099).OwningProcess

# 强制释放端口
Stop-Process -Id (Get-NetTCPConnection -LocalPort 8099).OwningProcess -Force
```

### 手动启动（不用脚本）

```powershell
# 设置环境变量
$env:AI_PROVIDER = "qwen"
$env:DASHSCOPE_API_KEY = "your-key"
$env:MONGODB_URI = "mongodb://localhost:27017/devflow"

# 启动应用
java -jar backend\target\devflow.jar --server.port=8099
```

### API 测试

```powershell
# 健康检查
Invoke-RestMethod -Uri http://localhost:8099/api/health

# 版本信息（格式化输出）
Invoke-RestMethod -Uri http://localhost:8099/api/version | ConvertTo-Json -Depth 10

# 版本信息（只显示版本号）
(Invoke-RestMethod -Uri http://localhost:8099/api/version).version

# 系统信息
Invoke-RestMethod -Uri http://localhost:8099/api/system/info
```

## ⚠️ 常见问题

### Q1: 执行脚本时提示"无法加载，因为在此系统上禁止运行脚本"

**原因：** PowerShell 执行策略限制

**解决方案：**

```powershell
# 查看当前策略
Get-ExecutionPolicy

# 临时允许（仅当前会话）
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass

# 永久允许（需要管理员权限）
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
```

### Q2: 端口被占用

**错误信息：**
```
Error: Port 8099 is already in use
```

**解决方案：**

```powershell
# 方法 1: 查找并停止占用端口的进程
$process = Get-Process -Id (Get-NetTCPConnection -LocalPort 8099).OwningProcess
Stop-Process -Id $process.Id

# 方法 2: 使用 stop.ps1 停止 DevFlow
.\stop.ps1

# 方法 3: 修改应用端口
# 编辑 start-prod.ps1，修改 $APP_PORT 变量
```

### Q3: JAR 文件未找到

**错误信息：**
```
Error: JAR file not found: backend\target\devflow.jar
```

**解决方案：**

```powershell
# 先构建项目
.\build.ps1

# 或手动构建
mvn clean package -DskipTests
```

### Q4: 应用启动后立即退出

**检查步骤：**

1. 查看日志：
```powershell
Get-Content logs\devflow.log -Tail 100
```

2. 检查配置：
```powershell
# 验证环境变量
$env:MONGODB_URI
$env:DASHSCOPE_API_KEY
```

3. 测试数据库连接：
```powershell
# 如果安装了 MongoDB 客户端
mongo $env:MONGODB_URI
```

### Q5: 找不到 Java 命令

**错误信息：**
```
java : 无法将"java"项识别为 cmdlet、函数、脚本文件或可运行程序的名称
```

**解决方案：**

1. 安装 JDK 17 或更高版本
2. 配置环境变量：
   - 系统变量 `JAVA_HOME`: `C:\Program Files\Java\jdk-17`
   - 系统变量 `Path`: 添加 `%JAVA_HOME%\bin`

3. 验证：
```powershell
java -version
```

## 📚 脚本开发指南

### 修改脚本

所有脚本都是纯文本文件，可以用任何文本编辑器修改：

```powershell
# 使用记事本编辑
notepad start-prod.ps1

# 使用 VS Code 编辑
code start-prod.ps1

# 使用 PowerShell ISE 编辑
ise start-prod.ps1
```

### 脚本调试

```powershell
# 显示详细执行过程
$VerbosePreference = "Continue"
.\start-prod.ps1

# 显示调试信息
$DebugPreference = "Continue"
.\start-prod.ps1
```

### 添加日志

在脚本中添加日志输出：

```powershell
# 信息日志（绿色）
Write-Host "操作成功" -ForegroundColor Green

# 警告日志（黄色）
Write-Host "注意事项" -ForegroundColor Yellow

# 错误日志（红色）
Write-Host "发生错误" -ForegroundColor Red
```

## 🎯 最佳实践

### 1. 首次部署

```powershell
# 1. 构建项目
.\build.ps1

# 2. 编辑配置
notepad start-prod.ps1

# 3. 启动应用
.\start-prod.ps1

# 4. 检查状态
.\status.ps1

# 5. 查看日志
Get-Content logs\devflow.log -Wait -Tail 50
```

### 2. 日常运维

```powershell
# 每天检查一次状态
.\status.ps1

# 定期查看日志
Get-Content logs\devflow.log | Select-String "ERROR"

# 定期备份日志
$date = Get-Date -Format "yyyyMMdd"
Copy-Item logs\devflow.log "logs\devflow.log.$date.backup"
```

### 3. 版本更新

```powershell
# 1. 停止应用
.\stop.ps1

# 2. 备份当前版本
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
Copy-Item backend\target\devflow.jar "devflow.jar.backup.$timestamp"

# 3. 更新 JAR 文件
# 将新的 JAR 文件复制到 backend\target\

# 4. 启动新版本
.\start-prod.ps1

# 5. 验证版本
(Invoke-RestMethod -Uri http://localhost:8099/api/version).version
```

### 4. 自动化任务

创建计划任务自动检查应用状态：

```powershell
# 创建状态检查脚本
$script = @"
cd C:\app\devflow
.\status.ps1 > logs\status-check.log
"@

$script | Out-File -FilePath "C:\app\devflow\auto-check.ps1" -Encoding UTF8

# 创建计划任务（需要管理员权限）
$action = New-ScheduledTaskAction -Execute "powershell.exe" -Argument "-File C:\app\devflow\auto-check.ps1"
$trigger = New-ScheduledTaskTrigger -Daily -At 9am
Register-ScheduledTask -TaskName "DevFlow-StatusCheck" -Action $action -Trigger $trigger
```

## 📞 获取帮助

- **完整部署指南**：[DEPLOYMENT_QUICK_START.md](DEPLOYMENT_QUICK_START.md)
- **版本管理**：[VERSION_MANAGEMENT_GUIDE.md](VERSION_MANAGEMENT_GUIDE.md)
- **打包脚本**：[BUILD_SCRIPTS_GUIDE.md](BUILD_SCRIPTS_GUIDE.md)
- **故障排查**：[TROUBLESHOOTING.md](TROUBLESHOOTING.md)
