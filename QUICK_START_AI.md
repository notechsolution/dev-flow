# 快速开始 - AI 模型切换

## 当前配置（Qwen - 默认）

项目默认使用阿里云通义千问（Qwen）模型。

### 环境变量配置

```powershell
# 设置 DashScope API Key
$env:DASHSCOPE_API_KEY="your-api-key-here"

# 启动应用
mvn spring-boot:run -pl backend
```

---

## 切换到 Ollama（本地模型）

### 步骤 1: 安装 Ollama

Windows: 从 https://ollama.com/download 下载安装程序

### 步骤 2: 启动 Ollama 并下载模型

```powershell
# 启动 Ollama 服务（后台运行）
Start-Process "ollama" -ArgumentList "serve" -WindowStyle Hidden

# 下载 Qwen2 模型（推荐，与通义千问同系列）
ollama pull qwen2

# 查看已下载的模型
ollama list
```

### 步骤 3: 使用 Ollama Profile 启动应用

```powershell
# 方法 1: 使用 Spring Profile（推荐）
$env:SPRING_PROFILES_ACTIVE="ollama"
mvn spring-boot:run -pl backend

# 方法 2: 设置环境变量
$env:AI_PROVIDER="ollama"
$env:OLLAMA_MODEL="qwen2"
mvn spring-boot:run -pl backend
```

### 步骤 4: 验证

查看启动日志，确认看到：
```
UnifiedAIServiceImpl initialized with provider: ollama, model: OllamaChatModel
```

---

## 常用 Ollama 命令

```powershell
# 查看所有可用模型
ollama list

# 下载其他模型
ollama pull llama3
ollama pull mistral
ollama pull codellama

# 测试模型
ollama run qwen2 "你好"

# 删除模型
ollama rm modelname

# 停止 Ollama 服务
Stop-Process -Name "ollama"
```

---

## 配置文件对比

### application.properties (Qwen)
```properties
ai.provider=qwen
spring.ai.dashscope.api-key=${DASHSCOPE_API_KEY}
spring.ai.dashscope.chat.options.model=qwen3-max
```

### application-ollama.properties (Ollama)
```properties
ai.provider=ollama
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=qwen2
```

---

## 故障排查

### Ollama 无法连接

```powershell
# 检查 Ollama 是否运行
Get-Process ollama -ErrorAction SilentlyContinue

# 测试连接
Invoke-RestMethod -Uri "http://localhost:11434/api/tags"

# 重启 Ollama
Stop-Process -Name "ollama" -Force
Start-Process "ollama" -ArgumentList "serve"
```

### 模型未找到

确保已下载模型：
```powershell
ollama pull qwen2
```

### 端口冲突

检查 8099 端口是否被占用：
```powershell
netstat -ano | findstr :8099
```

---

## 性能建议

- **开发环境**: 推荐使用 Ollama (本地，免费，隐私保护)
- **生产环境**: 推荐使用 Qwen (云端，高性能，稳定)
- **代码生成**: 可尝试 `codellama` 模型
- **多语言**: 可尝试 `llama3` 模型

---

详细文档请参考: [AI_PROVIDER_GUIDE.md](AI_PROVIDER_GUIDE.md)
