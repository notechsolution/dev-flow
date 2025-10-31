# AI 提供商配置对比

## 配置参数对比表

| 配置项 | Qwen (默认) | Ollama | 说明 |
|--------|------------|---------|------|
| `ai.provider` | `qwen` (默认) | `ollama` | AI 提供商选择 |
| `ai.service.implementation` | `unified` (默认) | `unified` | 服务实现类型 |
| 服务地址 | 云端 API | `http://localhost:11434` | API endpoint |
| 认证方式 | API Key | 无需认证 | 安全机制 |
| 模型配置 | `qwen3-max` | `qwen2` | 默认模型 |
| 温度参数 | `0.2` | `0.7` | 创造性程度 |
| 最大 Token | `10000` | 自动 | 响应长度限制 |

## 配置文件示例

### 1. Qwen (通义千问) - application.properties

```properties
# AI Service Configuration
ai.service.implementation=unified
ai.provider=qwen

# Spring AI Alibaba - Qwen Configuration
spring.ai.dashscope.api-key=${DASHSCOPE_API_KEY:sk-xxxxx}
spring.ai.dashscope.chat.options.model=qwen3-max
spring.ai.dashscope.chat.options.temperature=0.2
spring.ai.dashscope.chat.options.max-tokens=10000
```

### 2. Ollama - application-ollama.properties

```properties
# AI Service Configuration
ai.service.implementation=unified
ai.provider=ollama

# Spring AI Ollama Configuration
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=qwen2
spring.ai.ollama.chat.options.temperature=0.7
```

## 环境变量对比

### Qwen 环境变量

```bash
# Windows PowerShell
$env:AI_PROVIDER="qwen"
$env:DASHSCOPE_API_KEY="sk-xxxxx"
$env:QWEN_MODEL="qwen3-max"

# Linux/Mac
export AI_PROVIDER=qwen
export DASHSCOPE_API_KEY=sk-xxxxx
export QWEN_MODEL=qwen3-max
```

### Ollama 环境变量

```bash
# Windows PowerShell
$env:AI_PROVIDER="ollama"
$env:OLLAMA_BASE_URL="http://localhost:11434"
$env:OLLAMA_MODEL="qwen2"
$env:OLLAMA_TEMPERATURE="0.7"

# Linux/Mac
export AI_PROVIDER=ollama
export OLLAMA_BASE_URL=http://localhost:11434
export OLLAMA_MODEL=qwen2
export OLLAMA_TEMPERATURE=0.7
```

## 启动命令对比

### 使用 Maven 启动

```bash
# Qwen (默认)
mvn spring-boot:run -pl backend

# Ollama
mvn spring-boot:run -pl backend -Dspring-boot.run.profiles=ollama

# 或使用环境变量
export SPRING_PROFILES_ACTIVE=ollama
mvn spring-boot:run -pl backend
```

### 使用 JAR 启动

```bash
# Qwen (默认)
java -jar backend/target/devflow.jar

# Ollama
java -jar backend/target/devflow.jar --spring.profiles.active=ollama
```

### 使用 Docker 启动

```dockerfile
# Qwen
docker run -e AI_PROVIDER=qwen \
           -e DASHSCOPE_API_KEY=sk-xxxxx \
           devflow:latest

# Ollama
docker run -e AI_PROVIDER=ollama \
           -e OLLAMA_BASE_URL=http://host.docker.internal:11434 \
           devflow:latest
```

## 可用模型对比

### Qwen 模型列表

| 模型名称 | 说明 | 适用场景 |
|---------|------|---------|
| `qwen-turbo` | 快速响应 | 简单任务 |
| `qwen-plus` | 平衡性能 | 一般任务 |
| `qwen-max` | 最高性能 | 复杂任务 |
| `qwen3-max` | 最新版本 | 推荐使用 ⭐ |

### Ollama 模型列表

| 模型名称 | 大小 | 说明 | 适用场景 |
|---------|------|------|---------|
| `qwen2` | ~4GB | 通义千问2 | 推荐使用 ⭐ |
| `llama2` | ~3.8GB | Meta Llama 2 | 通用任务 |
| `llama3` | ~4.7GB | Meta Llama 3 | 高质量输出 |
| `mistral` | ~4.1GB | Mistral AI | 多语言支持 |
| `codellama` | ~3.8GB | 代码专用 | 代码生成 |

## 性能参数调优

### 温度参数 (Temperature)

| 值 | 效果 | 适用场景 |
|----|------|---------|
| 0.0 - 0.3 | 确定性输出 | 技术文档、代码生成 |
| 0.4 - 0.7 | 平衡 | 需求分析、测试用例 |
| 0.8 - 1.0 | 创造性输出 | 头脑风暴、创意任务 |

**Qwen 默认**: 0.2 (更保守)
**Ollama 默认**: 0.7 (更灵活)

### 其他参数

```properties
# Qwen 额外参数
spring.ai.dashscope.chat.options.top-p=0.8
spring.ai.dashscope.chat.options.repetition-penalty=1.0

# Ollama 额外参数
spring.ai.ollama.chat.options.top-k=40
spring.ai.ollama.chat.options.top-p=0.9
spring.ai.ollama.chat.options.num-predict=2048
```

## 切换场景建议

### 开发环境

```properties
# 推荐使用 Ollama
ai.provider=ollama
spring.ai.ollama.chat.options.model=qwen2
spring.ai.ollama.chat.options.temperature=0.7
```

**优点**:
- 免费使用
- 无需网络
- 数据隐私
- 快速迭代

### 测试环境

```properties
# 可使用任意提供商
ai.provider=qwen
spring.ai.dashscope.chat.options.model=qwen-plus
```

**优点**:
- 接近生产环境
- 性能稳定
- 易于监控

### 生产环境

```properties
# 推荐使用 Qwen
ai.provider=qwen
spring.ai.dashscope.chat.options.model=qwen3-max
spring.ai.dashscope.chat.options.temperature=0.2
```

**优点**:
- 高可用性
- 专业支持
- 性能保障
- 弹性扩展

## 成本对比

### Qwen (通义千问)

| 模型 | 输入价格 | 输出价格 | 免费额度 |
|------|---------|---------|---------|
| qwen-turbo | ¥0.003/千token | ¥0.006/千token | 有 |
| qwen-plus | ¥0.008/千token | ¥0.02/千token | 有 |
| qwen-max | ¥0.04/千token | ¥0.12/千token | 有 |

**估算**: 每次需求优化约 2000 tokens，成本 ¥0.01-0.1

### Ollama

- **成本**: 免费 ✅
- **硬件要求**:
  - 内存: 8GB+ (推荐 16GB)
  - 磁盘: 10GB+ (每个模型 3-5GB)
  - CPU: 多核 (或 GPU 更佳)

## 混合使用场景

### 方案 1: 按环境切换

```bash
# 开发
export SPRING_PROFILES_ACTIVE=ollama

# 生产
export SPRING_PROFILES_ACTIVE=production
```

### 方案 2: 按功能切换

```java
// 简单任务用 Ollama
if (task.isSimple()) {
    provider = "ollama";
} else {
    provider = "qwen";
}
```

### 方案 3: 降级策略

```properties
# 主提供商
ai.provider=qwen

# 备用提供商（当主提供商不可用时）
ai.provider.fallback=ollama
```

## 监控指标建议

### Qwen 监控

- API 调用成功率
- 平均响应时间
- Token 使用量
- 费用统计

### Ollama 监控

- 本地服务可用性
- 模型加载时间
- 内存使用率
- CPU/GPU 使用率

## 故障排查清单

### Qwen 常见问题

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| API Key 无效 | 配置错误 | 检查环境变量 |
| 响应超时 | 网络问题 | 检查网络连接 |
| 配额不足 | 超出限制 | 充值或等待重置 |
| 模型不存在 | 拼写错误 | 检查模型名称 |

### Ollama 常见问题

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| 连接失败 | 服务未启动 | `ollama serve` |
| 模型未找到 | 未下载 | `ollama pull qwen2` |
| 内存不足 | 硬件限制 | 升级硬件或换小模型 |
| 响应慢 | CPU 性能 | 使用 GPU 或优化参数 |

## 总结

| 对比项 | Qwen | Ollama | 推荐 |
|--------|------|--------|------|
| 部署难度 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | Qwen |
| 运行成本 | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Ollama |
| 响应速度 | ⭐⭐⭐⭐ | ⭐⭐⭐ | Qwen |
| 数据隐私 | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Ollama |
| 稳定性 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | Qwen |
| 开发体验 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | Qwen |

**综合建议**:
- 开发阶段: Ollama (免费 + 隐私)
- 生产环境: Qwen (稳定 + 性能)
- 混合使用: 根据场景动态切换
