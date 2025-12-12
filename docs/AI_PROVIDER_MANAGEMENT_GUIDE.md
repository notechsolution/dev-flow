# AI提供商管理指南

## 概述

AI提供商管理功能允许**运维人员(OPERATOR角色)**配置和管理DevFlow系统中可用的AI服务提供商。系统支持以下AI平台:

- **DashScope** (阿里云通义千问)
- **Ollama** (本地部署)
- **OpenAI** (GPT系列)

## 功能特性

### 1. 提供商配置管理

- ✅ 启用/禁用AI提供商
- ✅ 配置API密钥和端点
- ✅ 管理可用模型列表
- ✅ 设置默认模型
- ✅ 完整的CRUD操作

### 2. 安全特性

- 🔐 **角色限制**: 仅OPERATOR角色可访问管理页面
- 🔐 **API密钥加密**: 使用CryptoUtil加密存储API密钥
- 🔐 **密钥脱敏**: API响应中API密钥仅显示后4位

### 3. 自动初始化

系统启动时自动初始化三个默认提供商配置(如果不存在):

| 提供商 | 默认模型 | 可用模型 |
|--------|----------|----------|
| DashScope | qwen-turbo | qwen-turbo, qwen-plus, qwen-max |
| Ollama | llama2 | llama2, mistral, codellama |
| OpenAI | gpt-3.5-turbo | gpt-3.5-turbo, gpt-4, gpt-4-turbo |

## 访问管理页面

### 前置条件

- 用户必须具有 **OPERATOR** 角色
- 系统管理员需要在用户管理中为特定用户分配OPERATOR角色

### 访问路径

1. 登录DevFlow系统
2. 访问URL: `/admin/ai-providers`
3. 如果角色不是OPERATOR,将显示权限错误并重定向

## 操作指南

### 查看提供商列表

管理页面显示所有已配置的AI提供商,包括:

- 提供商标识 (provider)
- 显示名称
- 描述信息
- API地址
- 可用模型数量
- 默认模型
- 启用状态

### 添加新提供商

1. 点击 **"添加提供商"** 按钮
2. 填写提供商信息:
   - **提供商代码**: 唯一标识符 (例如: `dashscope`, `ollama`, `openai`)
   - **显示名称**: 用户友好的名称 (例如: "阿里云DashScope")
   - **描述**: 可选的详细说明
   - **API地址**: 提供商的API端点 (例如: `https://dashscope.aliyuncs.com/api/v1`)
   - **API密钥**: 访问提供商服务的密钥 (加密存储)
   - **启用状态**: 是否启用该提供商
3. 配置模型列表:
   - 点击 **"添加模型"** 添加可用模型
   - 填写模型信息:
     - **模型代码**: 模型标识符 (例如: `qwen-turbo`)
     - **显示名称**: 用户友好的模型名称 (例如: "通义千问Turbo")
     - **描述**: 可选的模型说明
   - 可添加多个模型
4. 选择 **默认模型** (从已添加的模型列表中选择)
5. 点击 **"创建"** 保存

### 编辑提供商

1. 在提供商列表中找到要编辑的提供商
2. 点击 **"编辑"** 按钮
3. 修改配置信息:
   - **注意**: 提供商代码不可修改
   - **API密钥**: 留空表示保持原密钥不变
   - 其他字段可自由修改
4. 点击 **"更新"** 保存更改

### 启用/禁用提供商

- 直接切换提供商列表中的开关按钮
- 禁用的提供商不会出现在用户的AI提供商选择列表中

### 删除提供商

1. 在提供商列表中找到要删除的提供商
2. 点击 **"删除"** 按钮
3. 确认删除操作
4. **注意**: 删除操作不可恢复,请谨慎操作

### 初始化默认配置

如果需要恢复或重新创建默认的三个提供商配置:

1. 点击 **"初始化默认配置"** 按钮
2. 确认操作
3. 系统将创建DashScope、Ollama、OpenAI的默认配置
4. **注意**: 如果提供商已存在,不会重复创建

## API端点说明

所有管理API端点都需要OPERATOR角色权限:

### 1. 获取所有提供商

```http
GET /api/admin/ai-providers
```

**响应示例**:
```json
{
  "success": true,
  "data": [
    {
      "id": "507f1f77bcf86cd799439011",
      "provider": "dashscope",
      "displayName": "阿里云DashScope",
      "description": "阿里云通义千问大模型服务",
      "enabled": true,
      "apiKey": "sk-****1234",
      "baseUrl": "https://dashscope.aliyuncs.com/api/v1",
      "models": [
        {
          "name": "qwen-turbo",
          "displayName": "通义千问Turbo",
          "description": "快速响应模型"
        }
      ],
      "defaultModel": "qwen-turbo",
      "createdAt": "2024-01-14T12:00:00Z",
      "updatedAt": "2024-01-14T12:00:00Z"
    }
  ],
  "message": "获取AI提供商配置成功"
}
```

### 2. 获取特定提供商

```http
GET /api/admin/ai-providers/{id}
```

### 3. 创建提供商

```http
POST /api/admin/ai-providers
Content-Type: application/json

{
  "provider": "dashscope",
  "displayName": "阿里云DashScope",
  "description": "阿里云通义千问大模型服务",
  "enabled": true,
  "apiKey": "sk-xxxxxxxxxxxx",
  "baseUrl": "https://dashscope.aliyuncs.com/api/v1",
  "models": [
    {
      "name": "qwen-turbo",
      "displayName": "通义千问Turbo",
      "description": "快速响应模型"
    }
  ],
  "defaultModel": "qwen-turbo"
}
```

### 4. 更新提供商

```http
PUT /api/admin/ai-providers/{id}
Content-Type: application/json

{
  "provider": "dashscope",
  "displayName": "阿里云DashScope",
  "enabled": true,
  "apiKey": "",  // 留空表示不修改
  "models": [...],
  "defaultModel": "qwen-plus"
}
```

### 5. 切换启用状态

```http
PATCH /api/admin/ai-providers/{id}/toggle
Content-Type: application/json

{
  "enabled": true
}
```

### 6. 删除提供商

```http
DELETE /api/admin/ai-providers/{id}
```

### 7. 初始化默认配置

```http
POST /api/admin/ai-providers/initialize
```

## 技术架构

### 后端架构

```
Controller Layer (AIProviderManagementController)
    ↓
Service Layer (AIProviderConfigService)
    ↓
Repository Layer (AIProviderConfigRepository)
    ↓
MongoDB (ai_provider_configs collection)
```

### 数据模型

**AIProviderConfig Entity**:
```java
{
    String id;                    // MongoDB ObjectId
    String provider;              // 提供商代码 (唯一)
    String displayName;           // 显示名称
    String description;           // 描述
    boolean enabled;              // 启用状态
    String apiKey;                // API密钥 (加密存储)
    String baseUrl;               // API基础URL
    List<ModelConfig> models;     // 可用模型列表
    String defaultModel;          // 默认模型
    LocalDateTime createdAt;      // 创建时间
    LocalDateTime updatedAt;      // 更新时间
    String createdBy;             // 创建人
    String updatedBy;             // 更新人
}
```

**ModelConfig**:
```java
{
    String name;           // 模型代码
    String displayName;    // 显示名称
    String description;    // 描述
}
```

### 前端架构

**组件**: `AIProviderManagement.vue`

**功能**:
- 提供商列表展示 (el-table)
- 创建/编辑对话框 (el-dialog)
- 表单验证 (el-form)
- 启用/禁用切换 (el-switch)
- 删除确认 (el-message-box)

**状态管理**:
- 使用Vue 3 Composition API
- 响应式数据 (ref, reactive)
- 异步操作处理

## 安全考虑

### 1. 角色权限控制

- **后端**: `@PreAuthorize("hasRole('OPERATOR')")` 注解保护所有管理端点
- **前端**: 路由守卫检查用户角色,非OPERATOR用户无法访问管理页面

### 2. API密钥保护

- **加密存储**: 使用 `CryptoUtil.encrypt()` 加密API密钥后存储到MongoDB
- **解密使用**: 在实际调用AI服务时使用 `CryptoUtil.decrypt()` 解密
- **脱敏显示**: API响应中仅显示密钥后4位 (例如: `sk-****1234`)

### 3. 审计日志

每个提供商配置记录以下审计信息:
- `createdAt`: 创建时间
- `createdBy`: 创建用户
- `updatedAt`: 最后更新时间
- `updatedBy`: 最后更新用户

## 常见问题

### Q1: 如何获取OPERATOR角色?

**A**: 联系系统管理员在用户管理中为您的账户分配OPERATOR角色。

### Q2: API密钥是否安全?

**A**: 是的,API密钥在存储时使用加密算法加密,在数据库中以密文形式保存,只在实际调用AI服务时解密使用。

### Q3: 可以添加自定义的AI提供商吗?

**A**: 可以,但需要确保:
1. 后端AIConfiguration类支持该提供商的ChatClient创建
2. 提供商的API兼容Spring AI的接口规范

### Q4: 禁用提供商后会影响现有用户吗?

**A**: 是的,禁用提供商后:
- 该提供商不会出现在用户的AI提供商选择列表中
- 正在使用该提供商的操作可能会失败
- 建议在禁用前通知用户

### Q5: 初始化默认配置会覆盖现有配置吗?

**A**: 不会,初始化操作会检查提供商是否已存在,只创建不存在的提供商配置。

### Q6: 如何更新API密钥?

**A**: 编辑提供商配置,在API密钥字段输入新密钥并保存。留空则保持原密钥不变。

## 最佳实践

### 1. 定期更新API密钥

- 建议每季度更新一次API密钥
- 更新后测试所有依赖该提供商的功能

### 2. 监控使用情况

- 定期检查各提供商的使用频率
- 根据使用情况优化默认提供商选择

### 3. 模型配置优化

- 为每个提供商配置合适的模型列表
- 选择性能和成本平衡的模型作为默认模型

### 4. 备份配置

- 定期导出提供商配置
- 在重要变更前创建配置快照

## 相关文档

- [AI提供商选择功能指南](./AI_PROVIDER_SELECTION_GUIDE.md)
- [User Story创建AI集成文档](./USER_STORY_AI_PROVIDER_INTEGRATION.md)
- [Spring AI集成总结](./SPRING_AI_INTEGRATION_SUMMARY.md)

## 更新日志

### v1.0.0 (2024-01-14)

- ✨ 初始版本发布
- ✨ 支持DashScope、Ollama、OpenAI三个提供商
- ✨ 完整的CRUD管理功能
- ✨ API密钥加密存储
- ✨ OPERATOR角色权限控制
- ✨ 自动初始化默认配置
- ✨ 前端管理界面
