# AI提供商选择功能 - 快速入门

## 概述

DevFlow现在支持三种AI提供商：**DashScope (通义千问)**、**Ollama** 和 **OpenAI**。用户可以在生成需求澄清问题、优化User Story等操作时自由选择使用哪个AI平台。

## 5分钟快速开始

### 步骤1：查看可用的AI提供商

```bash
curl http://localhost:8080/api/ai/providers
```

响应示例：
```json
{
  "availableProviders": ["dashscope", "ollama", "openai"],
  "defaultProvider": "dashscope"
}
```

### 步骤2：在API请求中指定provider

#### 生成澄清问题
```bash
curl -X POST http://localhost:8080/api/ai/clarify-requirement \
  -H "Content-Type: application/json" \
  -d '{
    "originalRequirement": "实现用户登录功能",
    "title": "用户登录",
    "projectContext": "电商平台",
    "projectId": "proj-123",
    "provider": "ollama"
  }'
```

#### 优化User Story
```bash
curl -X POST http://localhost:8080/api/ai/optimize-user-story \
  -H "Content-Type: application/json" \
  -d '{
    "description": "作为用户，我想要登录系统",
    "title": "用户登录",
    "projectContext": "电商平台",
    "projectId": "proj-123",
    "provider": "openai"
  }'
```

### 步骤3：在前端中使用

```vue
<template>
  <div>
    <!-- 添加AI提供商选择器 -->
    <AIProviderSelector v-model="selectedProvider" />
    
    <!-- 你的表单 -->
    <button @click="generateQuestions">生成问题</button>
  </div>
</template>

<script>
import AIProviderSelector from '@/components/AIProviderSelector.vue';

export default {
  components: { AIProviderSelector },
  data() {
    return {
      selectedProvider: ''  // 空字符串表示使用默认provider
    };
  },
  methods: {
    async generateQuestions() {
      const response = await this.$axios.post('/api/ai/clarify-requirement', {
        originalRequirement: this.requirement,
        title: this.title,
        provider: this.selectedProvider  // 传递用户选择
      });
      console.log(response.data);
    }
  }
};
</script>
```

## 常见问题

### Q: 如果不指定provider会怎样？
A: 系统会使用配置文件中设置的默认provider（通常是dashscope）。

### Q: 如何更改默认provider？
A: 在`application.yml`中修改：
```yaml
ai:
  provider: ollama  # 改为你想要的默认provider
```

### Q: 如果指定的provider不可用怎么办？
A: 系统会自动回退到默认provider，并在日志中记录警告。

### Q: 不同provider的响应有区别吗？
A: 是的，不同的AI模型可能产生不同质量和风格的响应。建议根据实际效果选择。

### Q: 所有API都支持provider参数吗？
A: 是的，以下所有端点都支持：
- `/api/ai/clarify-requirement` - 生成澄清问题
- `/api/ai/optimize-requirement` - 优化需求
- `/api/ai/optimize-user-story` - 优化User Story
- `/api/ai/generate-test-cases` - 生成测试用例

## 最佳实践

### 1. 保持provider一致性
在同一个需求优化流程中，建议使用相同的provider：
```javascript
const provider = 'dashscope';

// 步骤1：生成澄清问题
await generateQuestions({ provider });

// 步骤2：优化需求（使用相同的provider）
await optimizeRequirement({ provider });

// 步骤3：生成测试用例（使用相同的provider）
await generateTestCases({ provider });
```

### 2. 处理错误
```javascript
try {
  const response = await generateQuestions({ 
    ...requestData,
    provider: selectedProvider 
  });
  
  if (!response.data.success) {
    // 处理业务错误
    console.error(response.data.message);
  }
} catch (error) {
  // 处理网络或系统错误
  console.error('请求失败:', error);
}
```

### 3. 提供友好的UI反馈
```vue
<template>
  <div>
    <AIProviderSelector v-model="selectedProvider" />
    <p v-if="selectedProvider">
      将使用 {{ getProviderName(selectedProvider) }} 生成
    </p>
    <p v-else>
      将使用默认AI提供商生成
    </p>
  </div>
</template>
```

## 性能建议

### Provider选择建议

| Provider | 适用场景 | 优点 | 缺点 |
|----------|---------|------|------|
| **DashScope** | 生产环境、中文场景 | 响应速度快、中文理解好 | 需要API密钥 |
| **Ollama** | 开发测试、隐私敏感 | 本地运行、无成本 | 需要本地部署、速度较慢 |
| **OpenAI** | 生产环境、英文场景 | 质量高、功能强大 | 成本较高、需要API密钥 |

### 成本考虑

- **Ollama**: 免费（本地运行）
- **DashScope**: 按调用计费
- **OpenAI**: 按Token计费

建议在开发阶段使用Ollama，生产环境根据预算和需求选择DashScope或OpenAI。

## 下一步

- 查看[完整文档](./AI_PROVIDER_SELECTION_GUIDE.md)了解更多细节
- 查看[实现总结](./AI_PROVIDER_SELECTION_IMPLEMENTATION.md)了解技术细节
- 查看[示例组件](../frontend/src/components/RequirementClarificationExample.vue)了解完整实现

## 需要帮助？

如果遇到问题，请检查：
1. AI provider的配置是否正确
2. API密钥是否有效
3. 网络连接是否正常
4. 查看应用日志获取详细错误信息

---

更新日期：2025-12-12
