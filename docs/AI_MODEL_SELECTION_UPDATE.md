# AI提供商模型选择功能更新

## 更新日期
2025-12-15

## 功能描述
用户在生成需求澄清问题和优化User Story时，除了可以选择AI平台（DashScope、Ollama、OpenAI）外，现在还可以选择该平台支持的具体模型。

## 改动内容

### 后端改动

#### 1. API返回模型信息 (`AIController.java`)
- 修改 `/api/ai/providers` 端点，从数据库读取AI提供商配置
- 返回每个提供商支持的模型列表和默认模型信息

#### 2. DTO添加model字段
为所有AI相关的请求DTO添加 `model` 字段：
- `RequirementClarificationRequest` - 需求澄清请求
- `RequirementOptimizationRequest` - 需求优化请求  
- `UserStoryOptimizationRequest` - User Story优化请求
- `TestCaseGenerationRequest` - 测试用例生成请求

#### 3. AI配置支持模型参数 (`AIConfiguration.java`)
- 新增 `getChatClientForModel(String provider, String model)` 方法
- 目前返回默认ChatClient，模型参数预留用于未来扩展

#### 4. AI服务使用模型参数 (`UnifiedAIServiceImpl.java`)
- 所有AI调用方法记录选择的模型
- 将模型参数传递给ChatClient（为未来支持做准备）

### 前端改动

#### 1. AIProviderSelector组件重构
**位置**: `frontend/src/components/AIProviderSelector.vue`

**改动**:
- 将单个提供商选择器改为提供商+模型双选择器
- 两个选择器并排显示（响应式布局）
- Props改为 `provider` 和 `model` 双向绑定：
  ```vue
  v-model:provider="selectedProvider"
  v-model:model="selectedModel"
  ```
- 根据选择的提供商动态加载可用模型列表
- 显示每个模型的描述信息
- 切换提供商时自动清空模型选择

#### 2. UserStoryCreation.vue更新
**位置**: `frontend/src/views/requirement/UserStoryCreation.vue`

**改动**:
- 添加模型状态变量：
  - `selectedClarificationModel` - 需求澄清模型
  - `selectedOptimizationModel` - 需求优化模型
- 更新所有AIProviderSelector使用（4处）：
  ```vue
  <AIProviderSelector
      v-model:provider="selectedClarificationProvider"
      v-model:model="selectedClarificationModel"
      :disabled="loadingClarification"
  />
  ```
- 所有AI API调用添加model参数传递

#### 3. TypeScript接口更新
**位置**: `frontend/src/api/backend-api.ts`

为所有请求接口添加 `model` 可选字段：
```typescript
export interface RequirementClarificationRequest {
    // ... existing fields
    provider?: string;
    model?: string;  // 新增
}
```

## 用户界面变化

### 之前
```
┌─────────────────────────────┐
│ AI提供商                     │
│ [DashScope (阿里百炼) ▼]    │
│ 阿里云百炼平台               │
└─────────────────────────────┘
```

### 现在
```
┌───────────────────────┬────────────────────────┐
│ AI提供商              │ AI模型                  │
│ [DashScope ▼]         │ [通义千问Turbo ▼]      │
│ 阿里云百炼平台        │ 快速响应模型            │
└───────────────────────┴────────────────────────┘
```

## 支持的模型

### DashScope（阿里云）
- **qwen-turbo** - 通义千问Turbo（默认）
- **qwen-plus** - 通义千问Plus
- **qwen-max** - 通义千问Max

### Ollama（本地）
- **llama2** - Llama 2（默认）
- **mistral** - Mistral
- **codellama** - Code Llama

### OpenAI
- **gpt-3.5-turbo** - GPT-3.5 Turbo（默认）
- **gpt-4** - GPT-4
- **gpt-4-turbo** - GPT-4 Turbo

## 使用说明

### 1. 生成需求澄清问题时
在步骤2"需求澄清"中：
1. 选择AI提供商（或使用默认）
2. 选择该提供商支持的模型（或使用默认）
3. 点击"生成澄清问题"

### 2. 优化User Story时
在步骤3"需求优化"中：
1. 选择AI提供商（或使用默认）
2. 选择该提供商支持的模型（或使用默认）
3. 点击"优化需求"

### 3. 默认行为
- 如果不选择提供商，使用系统配置的默认提供商
- 如果不选择模型，使用该提供商的默认模型
- 切换提供商时，模型选择会自动清空，需要重新选择

## 技术说明

### 模型选择流程
1. 页面加载时，从 `/api/ai/providers` 获取所有启用的提供商及其模型列表
2. 用户选择提供商后，自动筛选出该提供商支持的模型
3. 用户选择模型（或留空使用默认）
4. 调用AI服务时，将 `provider` 和 `model` 参数传递给后端
5. 后端记录使用的模型，为未来的模型级别控制做准备

### 未来扩展
当前实现为模型选择预留了接口，未来可以：
- 在ChatClient级别指定具体模型
- 为不同模型配置不同的参数（温度、最大token等）
- 实现模型级别的使用统计和配额管理
- 支持用户自定义模型参数

## 相关文件

### 后端
- `backend/src/main/java/com/lz/devflow/controller/AIController.java`
- `backend/src/main/java/com/lz/devflow/configuration/AIConfiguration.java`
- `backend/src/main/java/com/lz/devflow/service/impl/UnifiedAIServiceImpl.java`
- `backend/src/main/java/com/lz/devflow/dto/RequirementClarificationRequest.java`
- `backend/src/main/java/com/lz/devflow/dto/RequirementOptimizationRequest.java`
- `backend/src/main/java/com/lz/devflow/dto/UserStoryOptimizationRequest.java`
- `backend/src/main/java/com/lz/devflow/dto/TestCaseGenerationRequest.java`

### 前端
- `frontend/src/components/AIProviderSelector.vue`
- `frontend/src/views/requirement/UserStoryCreation.vue`
- `frontend/src/api/backend-api.ts`

## 测试建议

1. **基本功能测试**
   - 选择不同提供商，验证模型列表正确切换
   - 选择具体模型，验证请求正确传递参数
   - 不选择模型，验证使用默认模型

2. **边界情况测试**
   - 切换提供商，验证模型选择被清空
   - 禁用状态下，验证选择器不可操作
   - 加载中状态，验证UI正确显示

3. **集成测试**
   - 使用不同模型生成澄清问题，对比结果质量
   - 使用不同模型优化需求，对比输出风格
   - 验证日志正确记录模型选择

## 注意事项

1. 模型列表来自AI提供商管理配置，确保已初始化默认配置
2. 运维人员可通过管理页面调整可用模型列表
3. 当前版本模型参数不影响实际调用，主要用于日志记录
4. 未来版本将实现真正的模型级别控制

## 兼容性

- **向后兼容**: 所有model参数都是可选的，不传递时使用默认模型
- **前端兼容**: 旧版本前端不传model参数时，后端自动使用默认模型
- **配置兼容**: 如果AI提供商配置未初始化，API会返回空列表，前端显示默认选项
