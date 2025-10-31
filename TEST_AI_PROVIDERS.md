# AI Provider 测试脚本

## 测试 Ollama 配置

### 1. 确保 Ollama 已安装并运行
```powershell
# 检查 Ollama 服务
Invoke-RestMethod -Uri "http://localhost:11434/api/tags" -Method Get
```

### 2. 下载并测试 Qwen2 模型
```powershell
# 下载模型
ollama pull qwen2

# 测试模型
ollama run qwen2 "你好，请用中文回答：1+1等于几？"
```

### 3. 使用 Ollama 启动应用
```powershell
# 方法 1: 使用 profile
$env:SPRING_PROFILES_ACTIVE="ollama"
mvn spring-boot:run -pl backend

# 方法 2: 使用环境变量
$env:AI_PROVIDER="ollama"
$env:OLLAMA_MODEL="qwen2"
mvn spring-boot:run -pl backend
```

## 测试 Qwen 配置

### 1. 设置 API Key
```powershell
$env:DASHSCOPE_API_KEY="your-api-key-here"
```

### 2. 使用 Qwen 启动应用
```powershell
# 默认就是 Qwen，直接启动
mvn spring-boot:run -pl backend

# 或明确指定
$env:AI_PROVIDER="qwen"
mvn spring-boot:run -pl backend
```

## 验证配置

### 查看日志确认使用的 AI 提供商
启动后查看日志中的这些信息：
```
UnifiedAIServiceImpl initialized with provider: ollama, model: OllamaChatModel
# 或
UnifiedAIServiceImpl initialized with provider: qwen, model: DashScopeChatModel
```

### 测试 API 调用
```powershell
# 生成需求澄清问题
$body = @{
    title = "用户登录功能"
    originalRequirement = "实现一个用户登录系统"
    projectContext = "Web应用"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8099/api/requirements/clarification" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

## 切换提供商测试

### 测试脚本
```powershell
# test-ai-providers.ps1

Write-Host "=== 测试 AI 提供商切换 ===" -ForegroundColor Green

# 测试 Ollama
Write-Host "`n1. 测试 Ollama..." -ForegroundColor Yellow
$env:AI_PROVIDER="ollama"
$env:OLLAMA_MODEL="qwen2"
Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run -pl backend" -NoNewWindow

Start-Sleep -Seconds 30  # 等待启动

# 调用 API 测试
Write-Host "调用 API..." -ForegroundColor Cyan
# ... API 测试代码 ...

# 停止服务
Stop-Process -Name "java" -Force

# 测试 Qwen
Write-Host "`n2. 测试 Qwen..." -ForegroundColor Yellow
$env:AI_PROVIDER="qwen"
$env:DASHSCOPE_API_KEY="your-api-key"
Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run -pl backend" -NoNewWindow

Start-Sleep -Seconds 30

# 调用 API 测试
Write-Host "调用 API..." -ForegroundColor Cyan
# ... API 测试代码 ...

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green
```

## 常见问题

### Ollama 连接失败
```powershell
# 检查 Ollama 是否运行
Get-Process ollama -ErrorAction SilentlyContinue

# 启动 Ollama
Start-Process "ollama" -ArgumentList "serve" -NoNewWindow
```

### 端口被占用
```powershell
# 查看 8099 端口占用
netstat -ano | findstr :8099

# 杀死进程
taskkill /PID <PID> /F
```

### Maven 构建失败
```powershell
# 清理并重新构建
mvn clean install -DskipTests
```
