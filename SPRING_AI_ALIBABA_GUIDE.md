# Spring AI Alibaba 集成说明

## 概述

本项目已集成 Spring AI Alibaba，使用阿里云通义千问（Qwen）大模型来提供 AI 能力。主要用于：

1. **需求澄清**：分析用户输入的需求，自动生成澄清问题
2. **需求优化**：基于用户的澄清回答，优化并生成完整的需求文档
3. **测试用例生成**：基于需求自动生成测试用例

## 配置步骤

### 1. 获取阿里云 DashScope API Key

1. 访问 [阿里云 DashScope 控制台](https://dashscope.console.aliyun.com/)
2. 注册并登录阿里云账号
3. 开通 DashScope 服务
4. 创建 API Key

### 2. 配置环境变量

在环境变量中设置 DashScope API Key：

```bash
export DASHSCOPE_API_KEY=your-actual-api-key
```

或者在 `application.properties` 中直接配置（不推荐，建议使用环境变量）：

```properties
spring.ai.dashscope.api-key=your-actual-api-key
```

### 3. 配置模型参数（可选）

默认配置在 `backend/src/main/resources/application.properties`：

```properties
# Spring AI Alibaba - Qwen Configuration
spring.ai.dashscope.api-key=${DASHSCOPE_API_KEY:your-dashscope-api-key}
spring.ai.dashscope.chat.options.model=${QWEN_MODEL:qwen-max}
spring.ai.dashscope.chat.options.temperature=0.7
spring.ai.dashscope.chat.options.max-tokens=4000
```

可用的模型选项：
- `qwen-max`：最强大的模型，适合复杂任务
- `qwen-plus`：平衡性能和成本
- `qwen-turbo`：快速响应，适合简单任务

## 架构说明

### 核心组件

1. **QwenAIServiceImpl**
   - 位置：`backend/src/main/java/com/lz/devflow/service/impl/QwenAIServiceImpl.java`
   - 功能：实现 AIService 接口，使用 Spring AI Alibaba 调用 Qwen 模型
   - 标记为 `@Primary`，作为默认的 AI 服务实现

2. **Prompt 模板**
   - 需求澄清：`backend/src/main/resources/prompts/requirement-clarification.st`
   - 需求优化：`backend/src/main/resources/prompts/requirement-optimization.st`

3. **DifyAIServiceImpl**
   - 位置：`backend/src/main/java/com/lz/devflow/service/impl/DifyAIServiceImpl.java`
   - 功能：原有的 Dify AI 实现，作为备选方案保留

### 工作流程

```
用户输入需求
    ↓
调用 generateClarificationQuestions()
    ↓
使用 requirement-clarification.st 模板
    ↓
调用 Qwen 模型生成澄清问题
    ↓
用户回答问题
    ↓
调用 optimizeRequirementWithClarification()
    ↓
使用 requirement-optimization.st 模板
    ↓
调用 Qwen 模型生成优化的需求文档
```

## Prompt 模板说明

### 需求澄清模板 (requirement-clarification.st)

该模板指导 AI 分析需求并生成 5-8 个澄清问题，涵盖以下类别：
- Users & Roles（用户和角色）
- Goals & Outcomes（目标和结果）
- Business Rules（业务规则）
- Data & Interface（数据和接口）
- Non-functional Requirements（非功能性需求）

返回格式：JSON 数组

### 需求优化模板 (requirement-optimization.st)

该模板指导 AI 基于原始需求和澄清回答，生成完整的需求文档，包括：
- 优化后的需求描述
- User Story（用户故事）
- Acceptance Criteria（验收标准）
- Technical Notes（技术说明）

返回格式：JSON 对象

## API 使用示例

### 1. 生成澄清问题

**请求：**
```http
POST /api/ai/clarify-requirement
Content-Type: application/json

{
  "originalRequirement": "我需要一个用户登录功能",
  "title": "用户登录",
  "projectContext": "Web应用"
}
```

**响应：**
```json
{
  "success": true,
  "message": "Clarification questions generated successfully",
  "questions": [
    {
      "id": "q1",
      "question": "需要支持哪些登录方式？（账号密码、第三方登录等）",
      "category": "Users & Roles",
      "answer": ""
    },
    ...
  ]
}
```

### 2. 优化需求

**请求：**
```http
POST /api/ai/optimize-requirement
Content-Type: application/json

{
  "originalRequirement": "我需要一个用户登录功能",
  "title": "用户登录",
  "projectContext": "Web应用",
  "clarificationAnswers": [
    {
      "questionId": "q1",
      "question": "需要支持哪些登录方式？",
      "answer": "账号密码和微信登录",
      "category": "Users & Roles"
    },
    ...
  ]
}
```

**响应：**
```json
{
  "success": true,
  "message": "Requirement optimized successfully",
  "optimizedRequirement": "# 用户登录功能\n\n...",
  "userStory": "作为一个用户，我希望能够...",
  "acceptanceCriteria": "## 验收标准\n\n...",
  "technicalNotes": "## 技术说明\n\n..."
}
```

## 依赖说明

### Maven 依赖

```xml
<!-- Spring AI Alibaba for Qwen -->
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-starter</artifactId>
    <version>1.0.0-M3.2</version>
</dependency>

<!-- Spring AI Core -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-core</artifactId>
    <version>1.0.0-M5</version>
</dependency>
```

## 故障排查

### 问题 1：API Key 无效

**症状**：收到 401 或 403 错误

**解决方案**：
1. 确认 API Key 是否正确
2. 检查 API Key 是否已激活
3. 确认账号余额是否充足

### 问题 2：响应解析失败

**症状**：生成的问题或优化结果为空

**解决方案**：
1. 检查日志中的原始响应内容
2. 确认 Prompt 模板是否正确
3. 系统会自动降级使用默认问题

### 问题 3：响应超时

**症状**：请求长时间没有响应

**解决方案**：
1. 检查网络连接
2. 尝试使用更轻量的模型（qwen-turbo）
3. 减少 max-tokens 配置

## 成本优化建议

1. **选择合适的模型**：
   - 简单任务使用 qwen-turbo
   - 复杂需求分析使用 qwen-max

2. **控制 Token 使用**：
   - 合理设置 max-tokens
   - 优化 Prompt 模板，减少不必要的描述

3. **缓存策略**：
   - 对于相似的需求，可以考虑缓存结果
   - 实现请求去重机制

## 参考链接

- [Spring AI Alibaba 官方文档](https://spring.io/projects/spring-ai-alibaba)
- [阿里云 DashScope 文档](https://help.aliyun.com/zh/dashscope/)
- [通义千问模型介绍](https://help.aliyun.com/zh/dashscope/developer-reference/model-introduction)

## 支持与反馈

如有问题或建议，请联系开发团队或提交 Issue。
