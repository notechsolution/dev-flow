# AI Provider 切换指南

本项目已支持多种 AI 模型提供商，可以通过配置文件轻松切换。

## 支持的 AI 提供商

1. **Qwen (通义千问)** - 阿里云 DashScope (默认)
2. **Ollama** - 本地部署的开源模型

## 快速切换方法

### 方法 1: 使用 Spring Profile (推荐)

#### 切换到 Ollama:
```bash
# 启动时指定 profile
java -jar devflow.jar --spring.profiles.active=ollama

# 或设置环境变量
export SPRING_PROFILES_ACTIVE=ollama
mvn spring-boot:run
```

#### 继续使用 Qwen (默认):
```bash
java -jar devflow.jar --spring.profiles.active=office
# 或不指定 profile，默认使用 Qwen
```

### 方法 2: 通过环境变量切换

```bash
# 切换到 Ollama
export AI_PROVIDER=ollama
export OLLAMA_BASE_URL=http://localhost:11434
export OLLAMA_MODEL=qwen2

# 切换到 Qwen
export AI_PROVIDER=qwen
export DASHSCOPE_API_KEY=your-api-key
export QWEN_MODEL=qwen3-max
```

### 方法 3: 直接修改 application.properties

编辑 `backend/src/main/resources/application.properties`:

```properties
# 切换到 Ollama
ai.provider=ollama
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=qwen2

# 或保持 Qwen (默认)
ai.provider=qwen
spring.ai.dashscope.api-key=your-api-key
```

## Ollama 使用指南

### 1. 安装 Ollama

```bash
# macOS / Linux
curl -fsSL https://ollama.com/install.sh | sh

# Windows
# 从 https://ollama.com/download 下载安装程序
```

### 2. 启动 Ollama 服务

```bash
ollama serve
```

### 3. 下载模型

```bash
# 推荐使用 Qwen2 模型 (与通义千问同系列)
ollama pull qwen2

# 其他可用模型
ollama pull llama2
ollama pull llama3
ollama pull mistral
ollama pull codellama
```

### 4. 查看已下载的模型

```bash
ollama list
```

### 5. 测试模型

```bash
ollama run qwen2 "Hello, how are you?"
```

## 配置说明

### Qwen (DashScope) 配置

```properties
# API Key (必需)
spring.ai.dashscope.api-key=${DASHSCOPE_API_KEY}

# 模型名称
spring.ai.dashscope.chat.options.model=qwen3-max
# 可选: qwen-turbo, qwen-plus, qwen-max, qwen3-max

# 温度参数 (0.0-1.0, 越高越随机)
spring.ai.dashscope.chat.options.temperature=0.2

# 最大 token 数
spring.ai.dashscope.chat.options.max-tokens=10000
```

### Ollama 配置

```properties
# Ollama 服务地址
spring.ai.ollama.base-url=http://localhost:11434

# 模型名称 (需要先通过 ollama pull 下载)
spring.ai.ollama.chat.options.model=qwen2
# 可选: llama2, llama3, mistral, codellama, etc.

# 温度参数 (0.0-1.0, 越高越随机)
spring.ai.ollama.chat.options.temperature=0.7
```

**注意**: Spring AI Ollama 使用自动配置，无需手动创建 Bean。配置文件中的参数会自动生效。

## 模型推荐

| 场景 | 推荐模型 | 说明 |
|------|---------|------|
| 生产环境 | Qwen (qwen3-max) | 云端托管，性能稳定 |
| 开发测试 | Ollama (qwen2) | 本地运行，无需网络 |
| 代码生成 | Ollama (codellama) | 专门优化代码生成 |
| 多语言 | Ollama (llama3) | 支持多种语言 |

## 架构说明

### 核心组件

1. **UnifiedAIServiceImpl** - 统一的 AI 服务实现
   - 使用 Spring AI 的 `ChatModel` 接口
   - 支持多种 AI 提供商
   - 默认激活 (`@Primary`)

2. **AIConfiguration** - AI 配置类
   - 根据 `ai.provider` 配置自动选择提供商
   - `ai.provider=qwen`: 使用 DashScope
   - `ai.provider=ollama`: 使用 Ollama

3. **QwenAIServiceImpl** - 旧版实现（遗留）
   - 仅在 `ai.service.implementation=qwen-legacy` 时激活
   - 不推荐使用

### 依赖关系

```
Spring Boot Application
    ↓
AIConfiguration
    ↓
┌─────────────┬──────────────┐
│  Qwen       │   Ollama     │
│  (默认)      │              │
└─────────────┴──────────────┘
    ↓
UnifiedAIServiceImpl
    ↓
AIService Interface
```

## 故障排查

### Ollama 连接失败

```bash
# 检查 Ollama 是否运行
curl http://localhost:11434/api/tags

# 重启 Ollama 服务
ollama serve
```

### 模型未找到

```bash
# 查看已下载的模型
ollama list

# 下载缺失的模型
ollama pull qwen2
```

### Qwen API 调用失败

1. 检查 API Key 是否正确设置
2. 检查网络连接
3. 确认 API Key 有足够的配额

## 性能对比

| 指标 | Qwen (云端) | Ollama (本地) |
|------|------------|---------------|
| 响应速度 | 快 (取决于网络) | 中等 (取决于硬件) |
| 稳定性 | 高 | 高 |
| 成本 | 按调用收费 | 免费 |
| 隐私 | 数据上传云端 | 数据本地处理 |
| 配置难度 | 简单 | 中等 |

## 进阶配置

### 使用远程 Ollama 服务器

```properties
spring.ai.ollama.base-url=http://192.168.1.100:11434
```

### 自定义温度参数

```properties
# Qwen: 更保守的输出
spring.ai.dashscope.chat.options.temperature=0.1

# Ollama: 更有创意的输出
spring.ai.ollama.chat.options.temperature=0.9
```

## 参考资料

- [Spring AI 文档](https://docs.spring.io/spring-ai/reference/)
- [Ollama 官网](https://ollama.com/)
- [阿里云 DashScope](https://dashscope.aliyun.com/)
- [Qwen 模型文档](https://github.com/QwenLM/Qwen)
