# AI提供商管理 - 模型可编辑功能更新

## 更新日期
2025-12-15

## 功能说明

AI提供商管理现在支持**部分可编辑**模式：

### ✅ 可编辑内容
- **模型列表**：可以添加、编辑、删除提供商下的模型
- **默认模型**：可以修改提供商的默认模型
- **启用状态**：可以切换提供商的启用/禁用状态

### ❌ 不可编辑内容（从application.yml读取）
- 提供商名称（provider）
- 提供商显示名称（displayName）
- 提供商描述（description）
- API密钥（apiKey）
- API地址（baseUrl）

## API端点

### 新增端点

```http
PUT /api/admin/ai-providers/{id}/models
Content-Type: application/json

{
  "models": [
    {
      "modelId": "qwen3-max",
      "modelName": "通义千问-Max",
      "description": "最强性能模型"
    },
    {
      "modelId": "qwen3-plus",
      "modelName": "通义千问-Plus",
      "description": "高性价比模型"
    }
  ],
  "defaultModel": "qwen3-max"
}
```

**说明：**
- 只接受 `models` 和 `defaultModel` 字段
- 其他字段会被忽略
- 需要OPERATOR角色权限

### 保留的端点

1. **GET /api/admin/ai-providers** - 获取所有提供商
2. **GET /api/admin/ai-providers/{id}** - 获取特定提供商
3. **PATCH /api/admin/ai-providers/{id}/toggle** - 切换启用状态
4. **PUT /api/admin/ai-providers/{id}/models** - 更新模型配置（新增）

## 后端变更

### 1. Controller层
新增方法：
```java
@PutMapping("/{id}/models")
public ResponseEntity<Map<String, Object>> updateProviderModels(
        @PathVariable String id,
        @RequestBody AIProviderConfigDTO providerDTO)
```

### 2. Service层
新增接口方法：
```java
AIProviderConfigDTO updateProviderModels(String id, AIProviderConfigDTO providerDTO);
```

实现逻辑：
- 只更新 `models` 和 `defaultModel` 字段
- 其他字段保持不变
- 自动记录更新时间和更新人

### 3. 数据模型
保持不变，使用现有的：
- `AIProviderConfig` (Entity)
- `AIProviderConfigDTO` (DTO)
- `ModelConfig` (内部类)

## 前端变更

### 1. API接口 (backend-api.ts)
新增方法：
```typescript
updateAIProviderModels(id: string, config: AIProviderConfigRequest): 
  Promise<AxiosResponse<AIProviderResponse>>
```

### 2. Vue组件 (AIProviderManagement.vue)

**新增功能：**
- ✅ "编辑模型"按钮（每行一个）
- ✅ 模型编辑对话框
- ✅ 添加模型按钮
- ✅ 删除模型按钮（每个模型一个）
- ✅ 默认模型选择器

**对话框功能：**
- 提供商名称和显示名称显示为禁用状态（只读）
- 模型列表可以增删改
- 每个模型包含：
  - 模型ID (modelId)
  - 显示名称 (modelName)
  - 描述 (description)
- 默认模型下拉选择
- 保存按钮提交更改

**用户界面：**
```
┌─────────────────────────────────────────────────┐
│ AI提供商管理                          [刷新]    │
├─────────────────────────────────────────────────┤
│ ⓘ AI提供商（名称、API密钥、API地址）从        │
│   application.yml 读取，可编辑模型列表         │
├─────────────────────────────────────────────────┤
│ 提供商 │ 名称 │ ... │ 模型 │ 状态 │ 操作      │
├─────────┼──────┼─────┼──────┼──────┼───────────┤
│ dashscope│DashScope│...│3个模型│ ☑ │[编辑模型]│
│ ollama  │Ollama │...│2个模型│ ☑ │[编辑模型]│
│ openai  │OpenAI │...│3个模型│ ☐ │[编辑模型]│
└─────────────────────────────────────────────────┘
```

## 使用示例

### 场景1：添加新模型

1. 点击提供商行的"编辑模型"按钮
2. 在对话框中点击"添加模型"
3. 填写新模型的ID、名称和描述
4. 点击"保存"

### 场景2：修改默认模型

1. 点击提供商行的"编辑模型"按钮
2. 在"默认模型"下拉列表中选择新的默认模型
3. 点击"保存"

### 场景3：删除模型

1. 点击提供商行的"编辑模型"按钮
2. 点击要删除的模型右侧的删除按钮（🗑️）
3. 如果删除的是默认模型，需要重新选择默认模型
4. 点击"保存"

### 场景4：修改提供商配置（API地址等）

1. 编辑 `application.yml` 文件
2. 重启应用
3. 配置会自动同步到数据库

## 权限要求

所有操作都需要 **OPERATOR** 角色权限。

## 数据验证

### 后端验证
- 提供商ID必须存在
- 模型列表不能为空
- 默认模型必须在模型列表中

### 前端验证
- 默认模型为必选项
- 模型ID和显示名称建议填写（虽然不是必填）

## 安全性

1. **API密钥保护**：API密钥仍然不存储在数据库，只在application.yml配置
2. **只读字段保护**：提供商名称、API地址等字段在后端强制只读
3. **权限控制**：所有操作需要OPERATOR角色
4. **审计日志**：自动记录更新时间和更新人

## 测试建议

1. **模型添加测试**：
   - 添加新模型
   - 验证模型出现在列表中
   - 可以选择为默认模型

2. **模型删除测试**：
   - 删除非默认模型，验证成功
   - 删除默认模型，验证需要重新选择默认模型

3. **默认模型切换测试**：
   - 切换默认模型
   - 验证新默认模型生效

4. **只读字段测试**：
   - 尝试修改提供商名称（应该禁用）
   - 尝试修改API地址（应该禁用）

5. **权限测试**：
   - 非OPERATOR角色无法访问
   - OPERATOR角色可以正常操作

## 数据库影响

- 模型配置存储在 `ai_provider_configs` 集合的 `models` 字段（数组）
- 每次更新会自动更新 `updatedAt` 和 `updatedBy` 字段
- 不影响其他字段

## 兼容性说明

- ✅ 向后兼容：现有的AI提供商配置不受影响
- ✅ API兼容：保留了所有现有的查询API
- ✅ 数据兼容：数据库结构没有变化

## 文件变更清单

### 后端
1. ✅ `AIProviderManagementController.java` - 新增 `updateProviderModels` 端点
2. ✅ `AIProviderConfigService.java` - 新增接口方法
3. ✅ `AIProviderConfigServiceImpl.java` - 实现更新模型逻辑

### 前端
1. ✅ `backend-api.ts` - 新增 `updateAIProviderModels` 方法
2. ✅ `AIProviderManagement.vue` - 添加编辑对话框和相关逻辑

## 后续优化建议

1. **模型验证**：添加模型ID格式验证（例如不允许重复）
2. **批量操作**：支持批量导入模型配置
3. **模型测试**：添加"测试模型"功能，验证模型是否可用
4. **版本管理**：记录模型配置的历史版本
5. **模板管理**：为常见AI提供商提供模型配置模板

---

如有问题，请联系开发团队。
