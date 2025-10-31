# AI 提供商集成总结

## 改造完成 ✅

DevFlow 项目已成功支持多 AI 提供商切换，可通过配置文件灵活切换不同的 AI 模型。

## 改造内容

### 1. 新增文件

#### 核心代码
- `backend/src/main/java/com/lz/devflow/service/impl/UnifiedAIServiceImpl.java`
  - 统一的 AI 服务实现，使用 `ChatModel` 接口
  - 支持所有实现 `ChatModel` 接口的 AI 模型
  - 标记为 `@Primary`，默认使用

- `backend/src/main/java/com/lz/devflow/configuration/AIConfiguration.java`
  - AI 配置类，根据 `ai.provider` 参数选择提供商
  - 利用 Spring Boot 自动配置机制

#### 配置文件
- `backend/src/main/resources/application-ollama.properties`
  - Ollama 专用配置文件
  - 通过 `--spring.profiles.active=ollama` 快速切换

#### 文档
- `AI_PROVIDER_GUIDE.md` - 完整的 AI 提供商切换指南
- `TEST_AI_PROVIDERS.md` - 测试脚本和验证方法
- `QUICK_START_AI.md` - 快速开始指南
- `SPRING_AI_INTEGRATION_SUMMARY.md` - 本文档

### 2. 修改文件

#### Maven 依赖
- `backend/pom.xml`
  - 新增 `spring-ai-ollama-spring-boot-starter` 依赖

#### 配置文件
- `backend/src/main/resources/application.properties`
  - 新增 `ai.service.implementation` 配置项
  - 新增 `ai.provider` 配置项
  - 新增 Ollama 相关配置项

#### 服务实现
- `backend/src/main/java/com/lz/devflow/service/impl/QwenAIServiceImpl.java`
  - 添加 `@ConditionalOnProperty` 注解
  - 仅在 `ai.service.implementation=qwen-legacy` 时激活
  - 标记为遗留实现

#### 项目文档
- `README.md`
  - 添加 AI 模型支持说明
  - 添加快速切换示例

## 使用方式

### 方法 1: 使用 Spring Profile（推荐）

```bash
# 使用 Ollama
java -jar devflow.jar --spring.profiles.active=ollama

# 使用 Qwen（默认）
java -jar devflow.jar --spring.profiles.active=office
```

### 方法 2: 环境变量

```bash
# 使用 Ollama
export AI_PROVIDER=ollama
export OLLAMA_MODEL=qwen2

# 使用 Qwen
export AI_PROVIDER=qwen
export DASHSCOPE_API_KEY=your-key
```

### 方法 3: 修改配置文件

编辑 `application.properties`:
```properties
# 切换提供商
ai.provider=ollama  # 或 qwen

# Ollama 配置
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=qwen2

# Qwen 配置
spring.ai.dashscope.api-key=your-key
spring.ai.dashscope.chat.options.model=qwen3-max
```

## 架构说明

### 设计模式

采用 **策略模式** + **Spring 条件配置**：

```
AIService (接口)
    ↑
    ├── UnifiedAIServiceImpl (@Primary, 默认)
    │       ↓ 依赖
    │   ChatModel (接口)
    │       ↑
    │       ├── DashScopeChatModel (ai.provider=qwen)
    │       └── OllamaChatModel (ai.provider=ollama)
    │
    └── QwenAIServiceImpl (遗留，ai.service.implementation=qwen-legacy)
```

### 关键技术点

1. **接口抽象**: `ChatModel` 接口统一不同 AI 模型的调用方式
2. **条件配置**: `@ConditionalOnProperty` 根据配置动态激活对应的实现
3. **自动配置**: 利用 Spring Boot 的自动配置机制，无需手动创建 Bean
4. **配置外部化**: 所有参数都可通过配置文件或环境变量修改

## 支持的 AI 模型

### Qwen (通义千问) - 云端
- qwen-turbo
- qwen-plus
- qwen-max
- **qwen3-max** (推荐)

### Ollama - 本地
- **qwen2** (推荐，与通义千问同系列)
- llama2
- llama3
- mistral
- codellama
- 其他开源模型

## 优势对比

| 特性 | Qwen (云端) | Ollama (本地) |
|------|------------|---------------|
| 部署难度 | 简单 (只需 API Key) | 中等 (需安装 Ollama) |
| 运行成本 | 按调用付费 | 免费 (硬件成本) |
| 响应速度 | 快 (取决于网络) | 中等 (取决于硬件) |
| 数据隐私 | 数据上传云端 | 数据本地处理 ✅ |
| 模型更新 | 自动 | 手动下载 |
| 网络要求 | 需要 | 不需要 ✅ |

## 测试验证

### 1. 编译项目
```bash
mvn clean compile -DskipTests
```

### 2. 启动测试（Ollama）
```bash
# 确保 Ollama 已运行
ollama serve

# 下载模型
ollama pull qwen2

# 启动应用
export SPRING_PROFILES_ACTIVE=ollama
mvn spring-boot:run -pl backend
```

### 3. 验证日志
查看启动日志中的这行：
```
UnifiedAIServiceImpl initialized with provider: ollama, model: OllamaChatModel
```

### 4. 测试 API
```bash
curl -X POST http://localhost:8099/api/requirements/clarification \
  -H "Content-Type: application/json" \
  -d '{
    "title": "用户登录",
    "originalRequirement": "实现用户登录功能",
    "projectContext": "Web应用"
  }'
```

## 扩展性

### 添加新的 AI 提供商

只需 3 步：

1. **添加 Maven 依赖**
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>
```

2. **添加配置类**（可选，如果需要自定义配置）
```java
@Configuration
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public static class OpenAIConfiguration {
    // OpenAI 会自动配置，也可以自定义
}
```

3. **添加配置文件**
```properties
# application-openai.properties
ai.provider=openai
spring.ai.openai.api-key=your-key
spring.ai.openai.chat.options.model=gpt-4
```

## 注意事项

### 1. Spring AI 版本
- Ollama: `spring-ai-ollama-spring-boot-starter:1.0.0-M6`
- 使用自动配置，API 可能在不同版本有变化

### 2. 配置优先级
```
环境变量 > application-{profile}.properties > application.properties
```

### 3. Bean 冲突
- `UnifiedAIServiceImpl` 标记为 `@Primary`
- `QwenAIServiceImpl` 仅在特定条件下激活
- 确保同时只有一个 `AIService` Bean 处于激活状态

### 4. Ollama 模型大小
- qwen2: ~4GB
- llama3: ~4.7GB
- codellama: ~3.8GB

确保有足够的磁盘空间和内存。

## 后续优化建议

1. **监控和日志**
   - 添加 AI 调用的性能监控
   - 记录每次调用的耗时和 token 使用量

2. **缓存机制**
   - 对相同请求的 AI 响应进行缓存
   - 减少 API 调用次数

3. **降级策略**
   - 当主 AI 服务不可用时，自动切换到备用服务
   - 实现请求队列和重试机制

4. **成本控制**
   - 实现 token 使用量统计
   - 设置每日调用限额

5. **A/B 测试**
   - 支持同时调用多个 AI 模型
   - 对比不同模型的响应质量

## 相关文档

- [AI Provider 切换指南](AI_PROVIDER_GUIDE.md)
- [测试指南](TEST_AI_PROVIDERS.md)
- [快速开始](QUICK_START_AI.md)
- [Spring AI 文档](https://docs.spring.io/spring-ai/reference/)
- [Ollama 文档](https://ollama.com/docs)

## 维护者

如有问题，请参考文档或提交 Issue。

---

**改造完成时间**: 2025-10-31
**版本**: v1.0
**状态**: ✅ 已完成并测试
