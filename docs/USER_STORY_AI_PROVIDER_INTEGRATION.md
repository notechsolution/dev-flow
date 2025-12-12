# User Story创建页面 - AI提供商集成说明

## 概述

User Story创建和编辑页面现已集成AI提供商选择功能，用户可以在生成需求澄清问题和优化需求时选择不同的AI平台。

## 功能位置

### 1. 需求澄清阶段（步骤2）

在"需求澄清"步骤中，AI提供商选择器显示在以下两个位置：

- **首次生成**：在生成澄清问题按钮上方显示
- **重新生成**：在已有澄清问题时，重新生成按钮上方显示

### 2. 需求优化阶段（步骤3）

在"需求优化"步骤中，AI提供商选择器显示在以下两个位置：

- **首次优化**：在开始需求优化按钮上方显示
- **重新优化**：在已有优化结果时，重新优化按钮上方显示

## 使用流程

### 完整的需求创建流程

```
步骤1：需求输入
  ↓
  输入需求标题和描述
  ↓
  点击"下一步：需求澄清"
  ↓
步骤2：需求澄清
  ↓
  1. 选择AI提供商（可选，默认使用配置的provider）
  2. 选择提示词模板（可选）
  3. 点击"生成澄清问题"
  ↓
  AI生成澄清问题
  ↓
  回答所有问题
  ↓
  点击"下一步：需求优化"
  ↓
步骤3：需求优化
  ↓
  1. 选择AI提供商（可选，可与步骤2不同）
  2. 选择提示词模板（可选）
  3. 点击"开始需求优化"
  ↓
  AI优化需求并生成User Story
  ↓
  点击"保存 User Story"
```

## UI布局

### 步骤2 - 需求澄清

```
┌─────────────────────────────────────────┐
│         需求澄清                         │
│  已回答 0 / 5                            │
├─────────────────────────────────────────┤
│                                          │
│  【AI提供商选择器】                      │
│  ┌──────────────────────────┐           │
│  │ AI提供商: DashScope (默认)│           │
│  └──────────────────────────┘           │
│                                          │
│  【提示词模板选择器】                    │
│  ┌──────────────────────────┐           │
│  │ 需求澄清提示词: 系统默认  │           │
│  └──────────────────────────┘           │
│                                          │
│  ┌──────────────────────────┐           │
│  │  生成澄清问题             │           │
│  └──────────────────────────┘           │
│                                          │
└─────────────────────────────────────────┘
```

### 步骤3 - 需求优化

```
┌─────────────────────────────────────────┐
│         优化后的需求         [保存]      │
├─────────────────────────────────────────┤
│                                          │
│  【AI提供商选择器】                      │
│  ┌──────────────────────────┐           │
│  │ AI提供商: Ollama          │           │
│  └──────────────────────────┘           │
│                                          │
│  【提示词模板选择器】                    │
│  ┌──────────────────────────┐           │
│  │ 需求优化提示词: 系统默认  │           │
│  └──────────────────────────┘           │
│                                          │
│  ┌──────────────────────────┐           │
│  │  开始需求优化             │           │
│  └──────────────────────────┘           │
│                                          │
└─────────────────────────────────────────┘
```

## 特性说明

### 1. 独立选择

- 澄清阶段和优化阶段的AI提供商可以独立选择
- 例如：澄清使用DashScope，优化使用Ollama

### 2. 默认行为

- 如果不选择AI提供商，系统使用配置的默认provider
- 下拉框会显示"[Provider Name] (默认)"

### 3. 重新生成

- 每次重新生成时，可以重新选择AI提供商
- 允许用户尝试不同provider的效果

### 4. 状态保持

- 当前选择的provider会在重新生成前保持
- 用户可以继续使用相同的provider或切换到其他provider

## 代码集成点

### 修改的文件

1. **UserStoryCreation.vue**
   - 导入AIProviderSelector组件
   - 添加provider状态管理
   - 在步骤2和步骤3中添加provider选择器
   - 更新API调用以传递provider参数

2. **backend-api.ts**
   - 更新接口定义，添加provider字段
   - `RequirementClarificationRequest`
   - `RequirementOptimizationRequest`
   - `UserStoryOptimizationRequest`
   - `TestCaseGenerationRequest`

### 新增的状态

```typescript
// AI Provider state
const selectedClarificationProvider = ref<string>('')
const selectedOptimizationProvider = ref<string>('')
```

### API调用示例

```typescript
// 生成澄清问题
const response = await aiApi.clarifyRequirement({
    originalRequirement: userStory.originalRequirement,
    title: userStory.title,
    projectId: userStory.projectId || undefined,
    promptTemplateId: clarificationTemplateId.value || undefined,
    provider: selectedClarificationProvider.value || undefined,
})

// 优化需求
const response = await aiApi.optimizeRequirement({
    originalRequirement: userStory.originalRequirement,
    title: userStory.title,
    clarificationAnswers,
    projectId: userStory.projectId || undefined,
    promptTemplateId: optimizationTemplateId.value || undefined,
    provider: selectedOptimizationProvider.value || undefined,
})
```

## 用户体验

### 优点

1. **灵活性**：用户可以为不同阶段选择最合适的AI提供商
2. **易用性**：保持原有的操作流程，仅添加可选的provider选择
3. **可见性**：清晰显示使用的AI提供商
4. **一致性**：与提示词模板选择器的UI风格一致

### 使用建议

- **中文需求**：建议使用DashScope（通义千问）
- **测试环境**：可以使用Ollama节省成本
- **英文需求**：可以尝试OpenAI
- **质量对比**：可以针对同一需求尝试不同provider，对比效果

## 常见问题

### Q: 我必须选择AI提供商吗？
A: 不需要。如果不选择，系统会使用默认配置的provider（通常是DashScope）。

### Q: 我可以在澄清和优化阶段使用不同的provider吗？
A: 可以。这两个阶段的provider选择是独立的。

### Q: 如果选择的provider不可用会怎样？
A: 后端会自动回退到默认provider，并在日志中记录。前端会显示错误消息。

### Q: 重新生成时会保留我之前的provider选择吗？
A: 是的，provider选择会保持，但你可以在重新生成前更改它。

### Q: 不同provider生成的结果质量有差异吗？
A: 是的，不同的AI模型可能产生不同质量和风格的结果。建议根据实际需求选择：
- DashScope：中文理解好，响应快
- Ollama：完全本地，隐私安全
- OpenAI：质量高，功能强大

## 相关文档

- [AI Provider Selection Guide](./AI_PROVIDER_SELECTION_GUIDE.md) - 完整功能文档
- [Quick Start Guide](./AI_PROVIDER_SELECTION_QUICKSTART.md) - 快速入门
- [Implementation Summary](./AI_PROVIDER_SELECTION_IMPLEMENTATION.md) - 实现细节

## 更新日期

2025-12-12

## 版本

v1.1.0
