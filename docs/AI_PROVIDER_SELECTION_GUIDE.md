# AI Provider Selection Guide

## 概述

DevFlow现在支持用户在生成需求澄清问题、优化User Story和生成测试用例时选择不同的AI平台。

支持的AI平台：
- **DashScope (通义千问)** - 阿里云通义千问大模型
- **Ollama** - 本地运行的开源大模型
- **OpenAI** - OpenAI GPT模型

## API变更

### 1. 获取可用的AI提供商

**端点**: `GET /api/ai/providers`

**响应示例**:
```json
{
  "availableProviders": ["dashscope", "ollama", "openai"],
  "defaultProvider": "dashscope",
  "providers": {
    "dashscope": {
      "name": "DashScope (通义千问)",
      "description": "阿里云通义千问大模型"
    },
    "ollama": {
      "name": "Ollama",
      "description": "本地运行的开源大模型"
    },
    "openai": {
      "name": "OpenAI",
      "description": "OpenAI GPT模型"
    }
  }
}
```

### 2. 生成需求澄清问题

**端点**: `POST /api/ai/clarify-requirement`

**请求体新增字段**:
```json
{
  "originalRequirement": "需求描述",
  "title": "需求标题",
  "projectContext": "项目上下文",
  "projectId": "项目ID",
  "promptTemplateId": "自定义模板ID（可选）",
  "provider": "dashscope"  // 新增：指定AI提供商，可选值: dashscope, ollama, openai
}
```

### 3. 优化需求

**端点**: `POST /api/ai/optimize-requirement`

**请求体新增字段**:
```json
{
  "originalRequirement": "原始需求",
  "title": "需求标题",
  "projectContext": "项目上下文",
  "projectId": "项目ID",
  "promptTemplateId": "自定义模板ID（可选）",
  "provider": "ollama",  // 新增：指定AI提供商
  "clarificationAnswers": [
    {
      "questionId": "q1",
      "question": "问题内容",
      "answer": "回答内容",
      "category": "分类"
    }
  ]
}
```

### 4. 优化User Story

**端点**: `POST /api/ai/optimize-user-story`

**请求体新增字段**:
```json
{
  "description": "User Story描述",
  "title": "标题",
  "projectContext": "项目上下文",
  "projectId": "项目ID",
  "additionalRequirements": "额外需求",
  "provider": "openai"  // 新增：指定AI提供商
}
```

### 5. 生成测试用例

**端点**: `POST /api/ai/generate-test-cases`

**请求体新增字段**:
```json
{
  "description": "需求描述",
  "optimizedDescription": "优化后的描述",
  "projectContext": "项目上下文",
  "provider": "dashscope"  // 新增：指定AI提供商
}
```

## 使用说明

### 前端集成

1. **获取可用的提供商列表**

```typescript
// 获取可用的AI提供商
async function getAvailableProviders() {
  const response = await fetch('/api/ai/providers');
  const data = await response.json();
  return data;
}
```

2. **在表单中添加提供商选择器**

```vue
<template>
  <div>
    <label>选择AI提供商：</label>
    <select v-model="selectedProvider">
      <option value="">使用默认提供商</option>
      <option v-for="provider in availableProviders" :key="provider" :value="provider">
        {{ getProviderDisplayName(provider) }}
      </option>
    </select>
  </div>
</template>

<script>
export default {
  data() {
    return {
      selectedProvider: '',
      availableProviders: [],
      providerInfo: {}
    };
  },
  async mounted() {
    const data = await this.getAvailableProviders();
    this.availableProviders = data.availableProviders;
    this.providerInfo = data.providers;
  },
  methods: {
    getProviderDisplayName(provider) {
      return this.providerInfo[provider]?.name || provider;
    }
  }
};
</script>
```

3. **在API调用中包含provider参数**

```typescript
// 生成澄清问题时指定提供商
async function generateClarificationQuestions(request) {
  const response = await fetch('/api/ai/clarify-requirement', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      ...request,
      provider: selectedProvider  // 添加提供商参数
    })
  });
  return await response.json();
}
```

## 配置说明

### 默认提供商配置

在`application.yml`中配置默认的AI提供商：

```yaml
ai:
  provider: dashscope  # 默认使用DashScope
```

### 提供商特定配置

每个提供商的具体配置请参考：
- DashScope配置：参考现有的`spring.ai.dashscope.*`配置
- Ollama配置：参考现有的`spring.ai.ollama.*`配置
- OpenAI配置：参考现有的`spring.ai.openai.*`配置

## 行为说明

1. **Provider参数为空或未指定**：使用默认配置的AI提供商（通过`ai.provider`配置）

2. **Provider参数指定了不支持的值**：自动回退到默认提供商，并记录警告日志

3. **指定的Provider未配置或不可用**：自动回退到默认提供商

4. **日志记录**：所有AI调用都会记录使用的提供商，方便追踪和调试

## 注意事项

1. 确保在使用特定提供商之前，已正确配置该提供商的凭证和连接信息
2. 不同的AI提供商可能产生不同质量的结果，建议根据实际效果选择合适的提供商
3. 考虑到成本和延迟，本地Ollama适合开发测试，DashScope和OpenAI适合生产环境
4. Provider参数是可选的，如果不指定则使用系统默认配置

## 测试建议

1. 使用`GET /api/ai/providers`验证所有配置的提供商都已正确注册
2. 分别使用不同的provider参数测试各个AI功能
3. 测试provider参数为空时的默认行为
4. 测试provider参数为无效值时的回退行为

## 迁移指南

### 现有前端代码兼容性

所有API都向后兼容。现有的前端代码不需要修改即可正常工作（使用默认提供商）。

### 逐步迁移步骤

1. **第一阶段**：后端部署新版本，所有请求默认使用配置的提供商
2. **第二阶段**：前端添加`GET /api/ai/providers`调用，显示可用的提供商列表
3. **第三阶段**：前端添加提供商选择UI组件
4. **第四阶段**：前端在API调用中包含用户选择的provider参数

## 示例：完整的需求澄清流程

```typescript
// 1. 获取可用的AI提供商
const providersData = await fetch('/api/ai/providers').then(r => r.json());
console.log('可用的提供商:', providersData.availableProviders);

// 2. 用户选择提供商（或使用默认）
const selectedProvider = 'dashscope'; // 用户选择或留空使用默认

// 3. 生成澄清问题
const clarificationResponse = await fetch('/api/ai/clarify-requirement', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    originalRequirement: '实现用户登录功能',
    title: '用户登录',
    projectContext: '电商平台',
    projectId: 'proj-123',
    provider: selectedProvider
  })
}).then(r => r.json());

// 4. 用户回答澄清问题后，优化需求
const optimizationResponse = await fetch('/api/ai/optimize-requirement', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    originalRequirement: '实现用户登录功能',
    title: '用户登录',
    projectContext: '电商平台',
    projectId: 'proj-123',
    provider: selectedProvider, // 使用相同的提供商保持一致性
    clarificationAnswers: [
      {
        questionId: 'q1',
        question: '支持哪些登录方式？',
        answer: '账号密码和手机验证码',
        category: '功能需求'
      }
    ]
  })
}).then(r => r.json());
```

## 相关文件

- `AIConfiguration.java` - AI提供商配置类
- `UnifiedAIServiceImpl.java` - 统一AI服务实现
- `AIController.java` - AI相关的REST API控制器
- `RequirementClarificationRequest.java` - 需求澄清请求DTO
- `RequirementOptimizationRequest.java` - 需求优化请求DTO
- `UserStoryOptimizationRequest.java` - User Story优化请求DTO
- `TestCaseGenerationRequest.java` - 测试用例生成请求DTO
