# 提示词管理功能说明

## 功能概述

提示词管理系统支持三级提示词配置：**系统级** > **项目级** > **用户级**，允许用户在不同层次自定义AI提示词模板。

## 三级提示词体系

### 1. 系统级提示词（SYSTEM）
- **来源**：存储在 `/src/main/resources/prompts/` 目录下
- **特点**：
  - 系统默认提供
  - 应用启动时自动导入数据库
  - 只能由管理员初始化，不可通过API修改或删除
  - 作为最基础的默认模板
- **文件**：
  - `requirement-clarification.st` - 需求澄清提示词
  - `requirement-optimization.st` - 需求优化提示词

### 2. 项目级提示词（PROJECT）
- **配置方式**：在项目配置页面设置
- **特点**：
  - 项目管理员可自定义
  - 针对特定项目的需求定制
  - 优先级高于系统级
  - 可被项目成员使用
- **使用场景**：项目有特殊的需求模板规范

### 3. 用户级提示词（USER）
- **配置方式**：用户个人保存
- **特点**：
  - 用户可以保存多个自定义提示词
  - 在需求澄清或优化前选择使用
  - 优先级最高
  - 仅用户本人可见和使用
- **使用场景**：
  - 用户有个人偏好的提示词风格
  - 针对特定场景保存优化的提示词

## 提示词类型

当前支持两种提示词类型（可扩展）：

1. **REQUIREMENT_CLARIFICATION** - 需求澄清
2. **REQUIREMENT_OPTIMIZATION** - 需求优化

## API接口

### 1. 获取有效提示词（按优先级）
```http
GET /api/prompt-templates/effective?type={type}&projectId={projectId}
```
- 自动按优先级返回：用户级 > 项目级 > 系统级

### 2. 获取系统默认提示词
```http
GET /api/prompt-templates/system/default?type={type}
```

### 3. 获取所有系统提示词
```http
GET /api/prompt-templates/system?type={type}
```

### 4. 获取项目提示词列表
```http
GET /api/prompt-templates/project/{projectId}?type={type}
```

### 5. 获取用户提示词列表
```http
GET /api/prompt-templates/user/my-templates?type={type}
```

### 6. 创建提示词
```http
POST /api/prompt-templates
Content-Type: application/json

{
  "name": "我的自定义提示词",
  "type": "REQUIREMENT_CLARIFICATION",
  "level": "USER",
  "content": "提示词内容...",
  "description": "描述信息",
  "userId": "当前用户ID",
  "enabled": true
}
```

### 7. 更新提示词
```http
PUT /api/prompt-templates/{id}
```

### 8. 删除提示词
```http
DELETE /api/prompt-templates/{id}
```

### 9. 初始化系统模板（管理员）
```http
POST /api/prompt-templates/system/initialize
```

## 使用流程

### 需求澄清流程

1. **前端发送请求**时可指定：
```json
{
  "originalRequirement": "需求内容",
  "title": "需求标题",
  "projectContext": "项目背景",
  "projectId": "项目ID（可选）",
  "promptTemplateId": "自定义模板ID（可选）"
}
```

2. **后端处理优先级**：
   - 如果指定了 `promptTemplateId`，使用该模板
   - 否则按优先级查找：用户级 > 项目级 > 系统级
   - 最终回退到资源文件中的默认模板

### 需求优化流程

与需求澄清流程相同，支持相同的提示词选择机制。

## 前端集成建议

### 1. 需求澄清页面
- 添加提示词选择下拉框
- 选项包括：
  - 系统默认
  - 项目预设（如果有）
  - 我的提示词（用户保存的）
  - 自定义（输入新的）
- 用户可以编辑选中的提示词
- 提供"保存为我的提示词"按钮

### 2. 项目配置页面
- 项目管理员可以设置项目级提示词
- 支持为不同类型的提示词设置不同模板
- 可以基于系统默认模板进行修改

### 3. 用户个人设置页面
- 管理用户保存的提示词
- 支持编辑、删除操作
- 可以查看提示词使用历史

## 数据库设计

### PromptTemplate 集合
```javascript
{
  _id: ObjectId,
  name: String,                    // 提示词名称
  type: String,                    // 类型：REQUIREMENT_CLARIFICATION, REQUIREMENT_OPTIMIZATION
  level: String,                   // 级别：SYSTEM, PROJECT, USER
  content: String,                 // 提示词内容
  description: String,             // 描述
  projectId: String,              // 项目ID（level=PROJECT时）
  userId: String,                 // 用户ID（level=USER时）
  isDefault: Boolean,             // 是否为系统默认
  enabled: Boolean,               // 是否启用
  createdAt: Date,
  updatedAt: Date,
  createdBy: String,
  updatedBy: String
}
```

### 索引
```javascript
// 复合索引
{ type: 1, level: 1 }
{ type: 1, level: 1, projectId: 1 }
{ type: 1, level: 1, userId: 1 }
```

## 权限控制

- **系统级**：只能由系统初始化，不可修改删除
- **项目级**：需要项目管理员权限
- **用户级**：用户只能管理自己的提示词

## 注意事项

1. 系统级提示词在应用启动时自动初始化
2. 不允许通过API创建系统级提示词
3. 删除操作不会删除系统默认提示词
4. 提示词内容支持变量占位符，如 `{title}`, `{requirement}` 等
5. 建议提供提示词预览功能，方便用户查看效果

## 扩展性

系统设计支持添加新的提示词类型，只需：

1. 在 `PromptType` 枚举中添加新类型
2. 在 `resources/prompts/` 下添加对应的模板文件
3. 重启应用自动初始化新模板

## 测试建议

1. 测试三级提示词的优先级是否正确
2. 测试提示词的CRUD操作
3. 测试权限控制是否生效
4. 测试系统启动时的自动初始化
5. 测试提示词在AI调用中的实际效果
