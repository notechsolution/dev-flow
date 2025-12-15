# AI提供商管理改为只读模式 - 迁移文档

## 变更概述

根据需求，AI提供商管理功能已从可编辑模式改为只读模式。所有配置现在从 `application.yml` 读取，在应用启动时自动初始化。

## 主要变更

### 1. 后端变更

#### 1.1 Controller层 (`AIProviderManagementController.java`)

**删除的API端点:**
- ❌ `POST /api/admin/ai-providers` - 创建提供商
- ❌ `PUT /api/admin/ai-providers/{id}` - 更新提供商  
- ❌ `DELETE /api/admin/ai-providers/{id}` - 删除提供商
- ❌ `POST /api/admin/ai-providers/initialize` - 初始化默认配置

**保留的API端点:**
- ✅ `GET /api/admin/ai-providers` - 获取所有提供商（只读）
- ✅ `GET /api/admin/ai-providers/{id}` - 获取特定提供商（只读）
- ✅ `PATCH /api/admin/ai-providers/{id}/toggle` - 切换启用状态

#### 1.2 Service层 (`AIProviderConfigService.java` & `AIProviderConfigServiceImpl.java`)

**删除的方法:**
- ❌ `createProvider(AIProviderConfigDTO)` - 创建提供商
- ❌ `updateProvider(String, AIProviderConfigDTO)` - 更新提供商
- ❌ `deleteProvider(String)` - 删除提供商
- ❌ `initializeDefaultProviders()` - 旧的初始化方法

**新增/修改的方法:**
- ✅ `initializeProvidersFromConfig()` - 从 application.yml 初始化配置
- ✅ 使用 `@PostConstruct` 在启动时自动初始化

**配置读取:**
```java
// DashScope 配置
@Value("${spring.ai.dashscope.chat.options.model:qwen3-max}")
private String dashscopeModel;

// Ollama 配置  
@Value("${spring.ai.ollama.base-url:http://localhost:11434}")
private String ollamaBaseUrl;

@Value("${spring.ai.ollama.chat.options.model:llama2}")
private String ollamaModel;

// OpenAI 配置
@Value("${spring.ai.openai.base-url:https://api.openai.com}")
private String openaiBaseUrl;
```

#### 1.3 Entity层 (`AIProviderConfig.java`)

**移除字段:**
- ❌ `private String apiKey;` - API密钥字段（现在从application.yml读取）
- ❌ `getApiKey()` / `setApiKey()` 方法

**保留字段:**
- ✅ `baseUrl` - 只读，从application.yml同步
- ✅ 其他所有字段保持不变

#### 1.4 DTO层 (`AIProviderConfigDTO.java`)

**移除字段:**
- ❌ `private String apiKey;` - 不再暴露API密钥
- ❌ `getApiKey()` / `setApiKey()` 方法

### 2. 前端变更

#### 2.1 API接口 (`backend-api.ts`)

**删除的API方法:**
- ❌ `createAIProvider(config)` - 创建提供商
- ❌ `updateAIProvider(id, config)` - 更新提供商
- ❌ `deleteAIProvider(id)` - 删除提供商
- ❌ `initializeDefaultAIProviders()` - 初始化默认配置

**保留的API方法:**
- ✅ `getAllAIProviders()` - 获取所有提供商
- ✅ `getAIProvider(id)` - 获取特定提供商
- ✅ `toggleAIProvider(id, enabled)` - 切换启用状态

#### 2.2 Vue组件 (`AIProviderManagement.vue`)

**删除的功能:**
- ❌ 添加提供商按钮和对话框
- ❌ 编辑提供商按钮和对话框
- ❌ 删除提供商功能
- ❌ 初始化默认配置按钮
- ❌ API密钥输入框
- ❌ 模型配置编辑功能

**保留的功能:**
- ✅ 提供商列表展示（只读）
- ✅ 启用/禁用开关
- ✅ 刷新按钮
- ✅ API地址只读显示（带锁图标）
- ✅ 模型信息悬停显示

**新增内容:**
- ✅ 配置说明提示框，告知用户如何修改配置

## 配置示例

在 `application.yml` 中配置AI提供商：

```yaml
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY:dummy-api-key}
      chat:
        options:
          model: ${QWEN_MODEL:qwen3-max}
          temperature: 0.2
          max-tokens: 10000
    
    ollama:
      base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
      chat:
        options:
          model: ${OLLAMA_MODEL:llama2}
          temperature: ${OLLAMA_TEMPERATURE:0.2}
    
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://api.openai-hk.com
```

## 启动时行为

1. **应用启动时**：`AIProviderConfigServiceImpl` 的 `@PostConstruct` 方法会自动执行
2. **配置初始化**：读取 `application.yml` 中的配置
3. **数据库同步**：
   - 如果提供商不存在，创建新记录
   - 如果提供商已存在，更新 `baseUrl` 和 `defaultModel`
4. **ChatModel注入**：`AIConfiguration` 在启动时注入三个AI提供商的 `ChatModel`

## 用户操作

### 运维人员 (OPERATOR角色)

**可以执行的操作:**
- ✅ 查看所有AI提供商配置
- ✅ 查看特定提供商详情
- ✅ 切换提供商的启用/禁用状态

**不能执行的操作:**
- ❌ 添加新的AI提供商
- ❌ 修改提供商配置（baseUrl、模型等）
- ❌ 删除AI提供商
- ❌ 修改或查看API密钥

### 修改配置的步骤

如果需要修改AI提供商配置：

1. 修改 `application.yml` 或环境变量
2. 重启应用
3. 配置会自动同步到数据库

## 安全性提升

1. **API密钥保护**：API密钥不再存储在数据库中，只在 `application.yml` 配置
2. **环境变量**：通过环境变量注入敏感信息
3. **只读UI**：前端界面不再暴露修改入口
4. **启动时注入**：AI提供商在应用启动时注入，确保配置一致性

## 迁移检查清单

- [x] 后端Controller删除创建/更新/删除端点
- [x] 后端Service删除相关方法
- [x] 后端Entity移除apiKey字段
- [x] 后端DTO移除apiKey字段
- [x] 后端Service实现从application.yml读取配置
- [x] 后端添加@PostConstruct初始化
- [x] 前端API删除创建/更新/删除方法
- [x] 前端Vue组件移除编辑功能
- [x] 前端Vue组件添加只读说明
- [x] 前端显示API地址为只读（带锁图标）
- [x] 测试启动时配置初始化

## 测试建议

1. **启动测试**：确认应用启动时能正确初始化三个AI提供商
2. **配置同步测试**：修改application.yml后重启，验证配置已更新
3. **权限测试**：确认OPERATOR角色只能执行允许的操作
4. **UI测试**：确认前端正确显示只读模式，无法添加/编辑/删除
5. **切换测试**：验证启用/禁用开关功能正常

## 注意事项

1. 首次启动时，确保环境变量已正确配置（特别是API密钥）
2. 数据库中已存在的provider记录不会被删除，只会更新配置
3. 如果需要添加新的AI提供商类型，需要修改代码并重新部署
4. API密钥在 `AIConfiguration` 中注入时使用，不经过数据库

## 文件修改清单

### 后端文件
1. `AIProviderManagementController.java` - 删除端点，添加注释
2. `AIProviderConfigService.java` - 删除方法定义
3. `AIProviderConfigServiceImpl.java` - 重写初始化逻辑
4. `AIProviderConfig.java` - 移除apiKey字段
5. `AIProviderConfigDTO.java` - 移除apiKey字段

### 前端文件
1. `backend-api.ts` - 删除API方法
2. `AIProviderManagement.vue` - 简化为只读视图

## 版本信息

- 迁移日期：2025-12-15
- 影响版本：v0.2.0+
- 兼容性：需要重新部署后端和前端

---

如有问题，请联系开发团队。
