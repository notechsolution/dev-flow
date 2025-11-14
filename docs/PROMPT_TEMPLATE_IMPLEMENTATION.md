# 提示词模板管理功能实现说明

## 功能概述

本功能实现了三级提示词模板管理系统，用户可以在需求澄清和需求优化时选择和自定义提示词模板。

## 三级提示词层级

1. **系统级（SYSTEM）**
   - 存储位置：`src/main/resources/prompts/*.st`
   - 特点：系统默认提供，应用启动时自动导入数据库
   - 用途：提供通用的提示词模板

2. **项目级（PROJECT）**
   - 配置位置：项目设置中配置
   - 特点：针对特定项目定制
   - 优先级：高于系统级

3. **用户级（USER）**
   - 配置位置：用户个人保存
   - 特点：用户可自由创建、修改、保存
   - 优先级：最高

## 优先级规则

模板选择遵循以下优先级（从高到低）：
1. 用户指定的模板ID（`promptTemplateId`参数）
2. 用户级模板
3. 项目级模板
4. 系统级模板
5. 资源文件中的默认模板

## 后端实现

### 核心类

#### 1. 实体类
- `PromptTemplate.java` - 提示词模板实体
  ```java
  @Document(collection = "prompt_templates")
  @CompoundIndexes({
      @CompoundIndex(name = "type_level_idx", def = "{'type': 1, 'level': 1}"),
      @CompoundIndex(name = "type_level_project_idx", def = "{'type': 1, 'level': 1, 'projectId': 1}"),
      @CompoundIndex(name = "type_level_user_idx", def = "{'type': 1, 'level': 1, 'userId': 1}")
  })
  ```

#### 2. 枚举类
- `PromptType.java` - 提示词类型
  - `REQUIREMENT_CLARIFICATION` - 需求澄清
  - `REQUIREMENT_OPTIMIZATION` - 需求优化

- `PromptLevel.java` - 提示词级别
  - `SYSTEM` - 系统级
  - `PROJECT` - 项目级
  - `USER` - 用户级

#### 3. Repository
- `PromptTemplateRepository.java` - MongoDB仓库接口
  - 提供11个查询方法
  - 支持按类型、级别、项目ID、用户ID查询

#### 4. Service
- `PromptTemplateService.java` - 服务接口
- `PromptTemplateServiceImpl.java` - 服务实现
  - `getEffectivePromptTemplate()` - 获取有效的提示词模板（实现优先级逻辑）
  - CRUD操作方法
  - `initializeSystemTemplates()` - 初始化系统模板

#### 5. Controller
- `PromptTemplateController.java` - REST API控制器
  - `GET /api/prompt-templates/effective` - 获取有效模板
  - `GET /api/prompt-templates/system/default` - 获取系统默认模板
  - `GET /api/prompt-templates/system` - 获取所有系统模板
  - `GET /api/prompt-templates/project/{projectId}` - 获取项目模板
  - `GET /api/prompt-templates/my` - 获取个人模板
  - `GET /api/prompt-templates/{id}` - 获取指定模板
  - `POST /api/prompt-templates` - 创建模板
  - `PUT /api/prompt-templates/{id}` - 更新模板
  - `DELETE /api/prompt-templates/{id}` - 删除模板
  - `POST /api/prompt-templates/initialize` - 初始化系统模板

#### 6. Initializer
- `PromptTemplateInitializer.java` - 应用启动初始化器
  - 实现`ApplicationRunner`接口
  - 应用启动时自动导入系统级提示词

### AI服务集成

#### UnifiedAIServiceImpl.java 修改

1. 注入 `PromptTemplateService`
2. 添加 `getEffectivePromptTemplate()` 方法
3. 修改 `generateClarificationQuestions()` 方法
   - 支持 `promptTemplateId` 参数
   - 使用自定义提示词模板
4. 修改 `optimizeRequirementWithClarification()` 方法
   - 支持 `promptTemplateId` 参数
   - 使用自定义提示词模板

### 请求对象修改

#### RequirementClarificationRequest.java
```java
private String projectId;           // 项目ID（用于获取项目级模板）
private String promptTemplateId;    // 自定义提示词模板ID
```

#### RequirementOptimizationRequest.java
```java
private String projectId;           // 项目ID（用于获取项目级模板）
private String promptTemplateId;    // 自定义提示词模板ID
```

### 数据库初始化

#### init-mongo.js 修改
```javascript
db.createCollection('prompt_templates');

db.prompt_templates.createIndex({ type: 1, level: 1 });
db.prompt_templates.createIndex({ type: 1, level: 1, projectId: 1 });
db.prompt_templates.createIndex({ type: 1, level: 1, userId: 1 });
```

## 前端实现

### API接口

#### backend-api.ts 新增接口

```typescript
// 提示词模板相关接口
export interface PromptTemplateRequest {
    type: 'REQUIREMENT_CLARIFICATION' | 'REQUIREMENT_OPTIMIZATION';
    level: 'SYSTEM' | 'PROJECT' | 'USER';
    name: string;
    description?: string;
    template: string;
    projectId?: string;
    enabled?: boolean;
}

export interface PromptTemplateResponse {
    id: string;
    type: string;
    level: string;
    name: string;
    description?: string;
    template: string;
    projectId?: string;
    userId?: string;
    enabled: boolean;
    createdAt: string;
    updatedAt: string;
}

export const promptTemplateApi = {
    // 获取有效的提示词模板（按优先级）
    getEffectiveTemplate: (type: string, projectId?: string) => 
        api.get<PromptTemplateResponse>('/api/prompt-templates/effective', { params: { type, projectId } }),
    
    // 获取系统默认模板
    getSystemDefaultTemplate: (type: string) => 
        api.get<PromptTemplateResponse>('/api/prompt-templates/system/default', { params: { type } }),
    
    // 获取所有系统模板
    getSystemTemplates: (type?: string) => 
        api.get<PromptTemplateResponse[]>('/api/prompt-templates/system', { params: { type } }),
    
    // 获取项目模板
    getProjectTemplates: (projectId: string, type?: string) => 
        api.get<PromptTemplateResponse[]>(`/api/prompt-templates/project/${projectId}`, { params: { type } }),
    
    // 获取个人模板
    getMyTemplates: (type?: string) => 
        api.get<PromptTemplateResponse[]>('/api/prompt-templates/my', { params: { type } }),
    
    // 获取指定模板
    getTemplateById: (id: string) => 
        api.get<PromptTemplateResponse>(`/api/prompt-templates/${id}`),
    
    // 创建模板
    createTemplate: (data: PromptTemplateRequest) => 
        api.post<PromptTemplateResponse>('/api/prompt-templates', data),
    
    // 更新模板
    updateTemplate: (id: string, data: PromptTemplateRequest) => 
        api.put<PromptTemplateResponse>(`/api/prompt-templates/${id}`, data),
    
    // 删除模板
    deleteTemplate: (id: string) => 
        api.delete(`/api/prompt-templates/${id}`)
};
```

#### RequirementClarificationRequest 和 RequirementOptimizationRequest 修改

添加了 `projectId` 和 `promptTemplateId` 字段，用于支持自定义提示词模板。

### 组件实现

#### PromptTemplateSelector.vue

新创建的提示词模板选择器组件，提供以下功能：

**Props:**
- `type`: 提示词类型（'REQUIREMENT_CLARIFICATION' | 'REQUIREMENT_OPTIMIZATION'）
- `projectId`: 项目ID（可选）
- `title`: 选择器标题（可选）

**功能特性:**
1. **模板加载**
   - 自动加载有效的提示词模板（按优先级）
   - 显示模板来源（系统/项目/用户）

2. **模板选择**
   - 下拉框选择系统、项目或个人模板
   - 实时显示模板信息（名称、描述、级别）

3. **模板编辑**
   - 复选框控制是否允许编辑
   - 文本域显示和编辑模板内容

4. **保存个人模板**
   - "保存为我的模板"按钮
   - 输入模板名称和描述
   - 保存后自动刷新模板列表

5. **事件**
   - `@template-selected`: 当模板选择或内容改变时触发
   - 参数：`(templateId: string | null, content: string)`

**样式特点:**
- 使用Element Plus组件库
- 卡片式布局
- 响应式设计
- 清晰的视觉层次

#### UserStoryCreation.vue 集成

**新增状态:**
```typescript
const clarificationTemplateId = ref<string | null>(null)
const clarificationPromptContent = ref<string>('')
const optimizationTemplateId = ref<string | null>(null)
const optimizationPromptContent = ref<string>('')
```

**新增方法:**
```typescript
// 提示词模板选择处理器
const onClarificationTemplateSelected = (templateId: string | null, content: string) => {
    clarificationTemplateId.value = templateId
    clarificationPromptContent.value = content
}

const onOptimizationTemplateSelected = (templateId: string | null, content: string) => {
    optimizationTemplateId.value = templateId
    optimizationPromptContent.value = content
}

// 生成澄清问题
const generateClarificationQuestions = async () => {
    loadingClarification.value = true
    
    try {
        const response = await aiApi.clarifyRequirement({
            originalRequirement: userStory.originalRequirement,
            title: userStory.title,
            projectContext: userStory.projectContext || '',
            projectId: userStory.projectId || undefined,
            promptTemplateId: clarificationTemplateId.value || undefined,
        })
        
        if (response.data.success) {
            clarificationQuestions.value = response.data.questions
            ElMessage.success('澄清问题已生成')
        } else {
            ElMessage.error(response.data.message || '生成澄清问题失败')
        }
    } catch (error) {
        ElMessage.error('生成澄清问题失败')
        console.error('Clarification error:', error)
    } finally {
        loadingClarification.value = false
    }
}

// 生成优化需求
const generateOptimization = async () => {
    loadingOptimization.value = true
    generatingOptimization.value = true

    try {
        const clarificationAnswers: QuestionAnswer[] = clarificationQuestions.value.map(q => ({
            questionId: q.id,
            question: q.question,
            answer: q.answer || '',
            category: q.category
        }))

        const response = await aiApi.optimizeRequirement({
            originalRequirement: userStory.originalRequirement,
            title: userStory.title,
            projectContext: userStory.projectContext || '',
            clarificationAnswers,
            projectId: userStory.projectId || undefined,
            promptTemplateId: optimizationTemplateId.value || undefined,
        })

        if (response.data.success) {
            optimizationResult.optimizedRequirement = response.data.optimizedRequirement
            optimizationResult.userStory = response.data.userStory
            optimizationResult.acceptanceCriteria = response.data.acceptanceCriteria
            optimizationResult.technicalNotes = response.data.technicalNotes
            userStory.description = response.data.optimizedRequirement
            ElMessage.success('需求优化完成')
        } else {
            ElMessage.error(response.data.message || '需求优化失败')
        }
    } catch (error) {
        ElMessage.error('需求优化失败')
        console.error('Optimization error:', error)
    } finally {
        loadingOptimization.value = false
        generatingOptimization.value = false
    }
}
```

**修改的方法:**
```typescript
const goToClarification = async () => {
    // 只负责导航到步骤2，不再自动生成澄清问题
    if (!userStory.title.trim()) {
        ElMessage.warning('请输入标题')
        return
    }
    if (!userStory.originalRequirement.trim()) {
        ElMessage.warning('请输入需求描述')
        return
    }

    currentStep.value = 2

    if (clarificationQuestions.value.length > 0) {
        ElMessage.info('已有澄清问题，继续填写')
        return
    }
}

const goToOptimization = async () => {
    // 只负责导航到步骤3，不再自动生成优化需求
    if (!allQuestionsAnswered.value) {
        ElMessage.warning('请回答所有问题后再继续')
        return
    }

    if (hasExistingOptimization.value) {
        try {
            await ElMessageBox.confirm(
                '您已有优化后的需求，重新优化将覆盖现有内容。确定要继续吗？',
                '确认重新优化',
                {
                    confirmButtonText: '重新优化',
                    cancelButtonText: '取消',
                    type: 'warning'
                }
            )
        } catch {
            return
        }
    }

    currentStep.value = 3
}
```

**UI集成:**

步骤2（需求澄清）：
```vue
<!-- Prompt Template Selector for Clarification -->
<PromptTemplateSelector
    v-if="!loadingClarification && clarificationQuestions.length === 0"
    type="REQUIREMENT_CLARIFICATION"
    :project-id="userStory.projectId"
    title="需求澄清提示词"
    @template-selected="onClarificationTemplateSelected"
/>

<!-- Generate Button -->
<div v-if="!loadingClarification && clarificationQuestions.length === 0" class="generate-button-container">
    <el-button type="primary" @click="generateClarificationQuestions">
        生成澄清问题
    </el-button>
</div>
```

步骤3（需求优化）：
```vue
<!-- Prompt Template Selector for Optimization -->
<PromptTemplateSelector
    v-if="!loadingOptimization && !optimizationResult.optimizedRequirement"
    type="REQUIREMENT_OPTIMIZATION"
    :project-id="userStory.projectId"
    title="需求优化提示词"
    @template-selected="onOptimizationTemplateSelected"
/>

<!-- Generate Optimization Button -->
<div v-if="!loadingOptimization && !optimizationResult.optimizedRequirement" class="generate-button-container">
    <el-button type="primary" @click="generateOptimization">
        开始需求优化
    </el-button>
</div>
```

**新增样式:**
```css
.generate-button-container {
    margin: 20px 0;
    text-align: center;
}
```

## 使用流程

### 需求澄清流程

1. 用户填写需求标题和描述
2. 点击"下一步：需求澄清"
3. 系统显示提示词模板选择器
4. 用户可以：
   - 选择系统/项目/个人模板
   - 查看模板内容
   - 勾选"允许编辑"修改模板内容
   - 点击"保存为我的模板"保存自定义模板
5. 点击"生成澄清问题"按钮
6. AI使用选定的提示词模板生成澄清问题

### 需求优化流程

1. 用户回答所有澄清问题
2. 点击"下一步：需求优化"
3. 系统显示提示词模板选择器
4. 用户可以：
   - 选择系统/项目/个人模板
   - 查看模板内容
   - 勾选"允许编辑"修改模板内容
   - 点击"保存为我的模板"保存自定义模板
5. 点击"开始需求优化"按钮
6. AI使用选定的提示词模板优化需求

## 测试要点

### 后端测试

1. **模板优先级测试**
   - 测试用户级 > 项目级 > 系统级的优先级
   - 测试指定模板ID时的优先级

2. **CRUD操作测试**
   - 创建、读取、更新、删除模板
   - 测试权限控制（用户只能修改自己的模板）

3. **系统初始化测试**
   - 测试应用启动时自动导入系统模板
   - 测试重复导入的处理

4. **AI服务集成测试**
   - 测试使用自定义模板生成澄清问题
   - 测试使用自定义模板优化需求

### 前端测试

1. **组件功能测试**
   - 测试模板加载和显示
   - 测试模板选择和内容编辑
   - 测试保存个人模板功能

2. **集成测试**
   - 测试需求澄清流程
   - 测试需求优化流程
   - 测试模板选择对AI生成结果的影响

3. **UI/UX测试**
   - 测试响应式布局
   - 测试加载状态显示
   - 测试错误处理和提示

## 注意事项

1. **系统模板文件**
   - 确保 `src/main/resources/prompts/` 目录下有对应的 `.st` 文件
   - 文件命名规范：`{type}.st`（如 `REQUIREMENT_CLARIFICATION.st`）

2. **权限控制**
   - 用户只能修改和删除自己创建的模板
   - 系统级和项目级模板需要相应的管理权限

3. **模板内容**
   - 模板应使用StringTemplate格式
   - 确保模板中的变量与AI服务中的参数匹配

4. **性能考虑**
   - 模板查询使用了复合索引
   - 避免频繁加载大量模板

5. **错误处理**
   - 提供友好的错误提示
   - 记录详细的错误日志便于排查问题

## 扩展建议

1. **模板共享**
   - 允许用户将个人模板分享给团队
   - 支持模板的导入和导出

2. **模板版本控制**
   - 记录模板的修改历史
   - 支持回退到之前的版本

3. **模板评价**
   - 用户可以对模板进行评分和评论
   - 根据评价推荐高质量模板

4. **模板统计**
   - 统计模板的使用频率
   - 分析模板的效果

5. **批量操作**
   - 支持批量导入模板
   - 支持批量启用/禁用模板

## 文件清单

### 后端文件

#### 新增文件
1. `backend/src/main/java/com/ygn/devflow/model/enums/PromptType.java`
2. `backend/src/main/java/com/ygn/devflow/model/enums/PromptLevel.java`
3. `backend/src/main/java/com/ygn/devflow/model/PromptTemplate.java`
4. `backend/src/main/java/com/ygn/devflow/repository/PromptTemplateRepository.java`
5. `backend/src/main/java/com/ygn/devflow/dto/PromptTemplateRequest.java`
6. `backend/src/main/java/com/ygn/devflow/dto/PromptTemplateResponse.java`
7. `backend/src/main/java/com/ygn/devflow/service/PromptTemplateService.java`
8. `backend/src/main/java/com/ygn/devflow/service/impl/PromptTemplateServiceImpl.java`
9. `backend/src/main/java/com/ygn/devflow/controller/PromptTemplateController.java`
10. `backend/src/main/java/com/ygn/devflow/initializer/PromptTemplateInitializer.java`

#### 修改文件
1. `backend/src/main/java/com/ygn/devflow/service/impl/UnifiedAIServiceImpl.java`
2. `backend/src/main/java/com/ygn/devflow/dto/RequirementClarificationRequest.java`
3. `backend/src/main/java/com/ygn/devflow/dto/RequirementOptimizationRequest.java`
4. `scripts/mongodb/init-mongo.js`

### 前端文件

#### 新增文件
1. `frontend/src/components/PromptTemplateSelector.vue`

#### 修改文件
1. `frontend/src/api/backend-api.ts`
2. `frontend/src/views/requirement/UserStoryCreation.vue`

## 总结

本功能实现了完整的三级提示词模板管理系统，包括：

- ✅ 后端API完整实现（11个新文件，4个修改文件）
- ✅ 数据库集合和索引配置
- ✅ 前端API接口封装
- ✅ 可复用的提示词选择器组件
- ✅ 需求澄清和优化流程集成
- ✅ 用户友好的UI/UX设计
- ✅ 完善的错误处理和提示

用户现在可以在需求澄清和优化过程中：
1. 选择系统/项目/个人提示词模板
2. 查看和编辑模板内容
3. 保存自定义模板供以后使用
4. 使用自定义模板调用AI生成内容

这大大提高了系统的灵活性和可定制性，满足了不同项目和用户的个性化需求。
