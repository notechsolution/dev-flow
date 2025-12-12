# DevFlow - AI Provider Selection Feature

## 新功能：多AI提供商支持 🎉

DevFlow现在支持在生成需求澄清问题、优化User Story时选择不同的AI平台！

### 支持的AI平台

- 🚀 **DashScope (通义千问)** - 阿里云通义千问大模型，中文场景优化
- 🏠 **Ollama** - 本地运行的开源大模型，隐私安全
- 🌐 **OpenAI** - OpenAI GPT模型，业界领先

### 快速使用

```javascript
// 1. 获取可用的AI提供商
const providers = await fetch('/api/ai/providers').then(r => r.json());
console.log(providers.availableProviders); // ['dashscope', 'ollama', 'openai']

// 2. 使用指定的provider生成澄清问题
const response = await fetch('/api/ai/clarify-requirement', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    originalRequirement: '实现用户登录功能',
    title: '用户登录',
    projectContext: '电商平台',
    provider: 'ollama'  // 指定使用Ollama
  })
});
```

### 前端集成

```vue
<template>
  <div>
    <!-- 使用提供的AI提供商选择器组件 -->
    <AIProviderSelector v-model="selectedProvider" />
    
    <!-- 你的业务逻辑 -->
    <button @click="generate">生成</button>
  </div>
</template>

<script>
import AIProviderSelector from '@/components/AIProviderSelector.vue';

export default {
  components: { AIProviderSelector },
  data: () => ({ selectedProvider: '' }),
  methods: {
    async generate() {
      // provider参数会自动使用用户选择的AI平台
      await this.$api.generateQuestions({
        ...this.formData,
        provider: this.selectedProvider
      });
    }
  }
};
</script>
```

### 文档

- 📖 [快速入门指南](./docs/AI_PROVIDER_SELECTION_QUICKSTART.md)
- 📚 [完整功能文档](./docs/AI_PROVIDER_SELECTION_GUIDE.md)
- 🔧 [实现细节](./docs/AI_PROVIDER_SELECTION_IMPLEMENTATION.md)

### 特性

✅ **向后兼容** - 现有代码无需修改即可工作  
✅ **智能回退** - provider不可用时自动使用默认配置  
✅ **灵活配置** - 可配置默认provider  
✅ **完整日志** - 详细记录AI调用和provider使用情况  

### 配置

在`application.yml`中配置默认的AI提供商：

```yaml
ai:
  provider: dashscope  # 可选: dashscope, ollama, openai
  
# 各provider的具体配置
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
    ollama:
      base-url: http://localhost:11434
    openai:
      api-key: ${OPENAI_API_KEY}
```

### API更新

所有AI相关端点现在都支持可选的`provider`参数：

- `POST /api/ai/clarify-requirement` - 生成需求澄清问题
- `POST /api/ai/optimize-requirement` - 优化需求
- `POST /api/ai/optimize-user-story` - 优化User Story
- `POST /api/ai/generate-test-cases` - 生成测试用例

新增端点：

- `GET /api/ai/providers` - 获取可用的AI提供商列表

### 示例

完整的需求优化流程示例：

```javascript
// 使用DashScope生成问题
const questions = await clarifyRequirement({
  requirement: '...',
  provider: 'dashscope'
});

// 使用Ollama优化需求（可以切换provider）
const optimized = await optimizeRequirement({
  requirement: '...',
  answers: [...],
  provider: 'ollama'
});

// 使用OpenAI生成测试用例
const testCases = await generateTestCases({
  requirement: optimized.requirement,
  provider: 'openai'
});
```

### 贡献者

感谢所有为此功能做出贡献的开发者！

---

更新日期：2025-12-12  
版本：v1.1.0
