# 提示词管理功能实施方案

## 一、设计理念

采用**分层管理 + 统一入口**的混合模式，平衡集中管理的效率和分散访问的便利性。

## 二、功能架构

```
提示词管理系统
├── 统一管理中心（主入口）
│   ├── 系统提示词视图（仅管理员）
│   ├── 我的提示词视图
│   └── 项目提示词视图
├── 上下文快捷入口（辅助入口）
│   ├── 项目设置页
│   └── 用户设置页
└── 使用场景选择器（已实现）
    ├── 需求澄清提示词选择
    └── 需求优化提示词选择
```

## 三、详细设计

### 3.1 统一管理中心

**路由**: `/prompt-templates`

**页面结构**:
```vue
<template>
  <div class="prompt-template-management">
    <!-- 页面头部 -->
    <PageHeader title="提示词管理">
      <el-button type="primary" @click="createTemplate">
        <el-icon><Plus /></el-icon>
        创建提示词
      </el-button>
    </PageHeader>

    <!-- 视图切换 -->
    <el-tabs v-model="activeView" @tab-change="onViewChange">
      <el-tab-pane label="我的提示词" name="user">
        <UserTemplatesView />
      </el-tab-pane>
      <el-tab-pane label="项目提示词" name="project">
        <ProjectTemplatesView />
      </el-tab-pane>
      <el-tab-pane label="系统提示词" name="system" v-if="isAdmin">
        <SystemTemplatesView />
      </el-tab-pane>
    </el-tabs>

    <!-- 统计面板 -->
    <TemplateStatistics />
  </div>
</template>
```

**核心功能**:
1. **多视图管理**
   - 我的提示词：当前用户创建的所有提示词
   - 项目提示词：用户有权限的项目的提示词
   - 系统提示词：系统默认提示词（仅管理员可见）

2. **搜索与筛选**
   ```typescript
   interface FilterOptions {
     type: 'REQUIREMENT_CLARIFICATION' | 'REQUIREMENT_OPTIMIZATION' | 'ALL'
     keyword: string
     level: 'SYSTEM' | 'PROJECT' | 'USER' | 'ALL'
     enabled: boolean | null
     projectId?: string
   }
   ```

3. **批量操作**
   - 批量启用/禁用
   - 批量删除（仅限用户自己的）
   - 批量导出为JSON/Markdown
   - 批量导入

4. **提示词编辑器**
   - 支持Markdown预览
   - 语法高亮（StringTemplate格式）
   - 变量提示和验证
   - 版本历史（可选，后期功能）

5. **使用统计**
   ```typescript
   interface TemplateStats {
     templateId: string
     usageCount: number
     lastUsed: string
     avgRating?: number
     successRate?: number
   }
   ```

### 3.2 项目设置中的提示词管理

**路由**: `/projects/:id/settings/prompts`

**页面特点**:
- 仅显示当前项目的提示词
- 简化的CRUD操作
- 快速链接到统一管理中心

**示例代码**:
```vue
<template>
  <div class="project-prompt-settings">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>项目提示词设置</span>
          <el-button 
            type="primary" 
            link 
            @click="goToManagementCenter"
          >
            在管理中心查看全部 →
          </el-button>
        </div>
      </template>

      <!-- 需求澄清提示词 -->
      <el-form-item label="需求澄清提示词">
        <PromptTemplateCard
          :template="projectClarificationTemplate"
          type="REQUIREMENT_CLARIFICATION"
          :project-id="projectId"
          @edit="editTemplate"
          @delete="deleteTemplate"
        />
      </el-form-item>

      <!-- 需求优化提示词 -->
      <el-form-item label="需求优化提示词">
        <PromptTemplateCard
          :template="projectOptimizationTemplate"
          type="REQUIREMENT_OPTIMIZATION"
          :project-id="projectId"
          @edit="editTemplate"
          @delete="deleteTemplate"
        />
      </el-form-item>
    </el-card>
  </div>
</template>
```

### 3.3 用户设置中的提示词管理

**路由**: `/user/settings/prompts` 或 `/profile/prompts`

**页面特点**:
- 显示用户所有个人提示词
- 快速创建常用提示词
- 标记常用提示词（收藏）

**示例代码**:
```vue
<template>
  <div class="user-prompt-settings">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的提示词</span>
          <div>
            <el-button @click="createTemplate">
              <el-icon><Plus /></el-icon>
              新建
            </el-button>
            <el-button 
              type="primary" 
              link 
              @click="goToManagementCenter"
            >
              查看全部 →
            </el-button>
          </div>
        </div>
      </template>

      <!-- 常用提示词 -->
      <div class="favorite-templates">
        <h3>常用提示词</h3>
        <el-space wrap>
          <PromptTemplateCard
            v-for="template in favoriteTemplates"
            :key="template.id"
            :template="template"
            size="small"
            @click="useTemplate"
          />
        </el-space>
      </div>

      <!-- 全部提示词列表 -->
      <el-table :data="userTemplates" style="margin-top: 20px">
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="type" label="类型">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link @click="editTemplate(row)">编辑</el-button>
            <el-button link @click="toggleFavorite(row)">
              {{ row.isFavorite ? '取消收藏' : '收藏' }}
            </el-button>
            <el-button link type="danger" @click="deleteTemplate(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
```

## 四、实施路线图

### Phase 1: MVP（最小可行产品）- 2周

**优先级**: ⭐⭐⭐⭐⭐

1. ✅ 使用场景选择器（已完成）
2. 🔨 统一管理中心基础版
   - [ ] 我的提示词视图
   - [ ] 基本CRUD操作
   - [ ] 简单搜索和筛选

3. 🔨 用户设置快捷入口
   - [ ] 显示个人提示词列表
   - [ ] 快速创建/编辑

### Phase 2: 增强功能 - 2周

**优先级**: ⭐⭐⭐⭐

1. 项目设置快捷入口
2. 项目提示词视图
3. 批量操作（启用/禁用、删除）
4. 高级搜索和筛选
5. 导入/导出功能

### Phase 3: 高级特性 - 3周

**优先级**: ⭐⭐⭐

1. 系统提示词管理（管理员功能）
2. 使用统计和分析
3. 提示词评分和反馈
4. 版本历史
5. 提示词模板市场（可选）

### Phase 4: 智能化 - 2周

**优先级**: ⭐⭐

1. AI推荐提示词
2. 提示词效果分析
3. 自动优化建议
4. A/B测试功能

## 五、技术实现

### 5.1 前端组件结构

```
src/views/
├── prompt-templates/
│   ├── PromptTemplateManagement.vue      # 统一管理中心主页
│   ├── components/
│   │   ├── UserTemplatesView.vue         # 我的提示词视图
│   │   ├── ProjectTemplatesView.vue      # 项目提示词视图
│   │   ├── SystemTemplatesView.vue       # 系统提示词视图
│   │   ├── TemplateEditor.vue            # 提示词编辑器
│   │   ├── TemplateCard.vue              # 提示词卡片
│   │   ├── TemplateStatistics.vue        # 统计面板
│   │   └── BatchOperations.vue           # 批量操作组件
│   └── composables/
│       ├── useTemplateManagement.ts      # 提示词管理逻辑
│       ├── useTemplateFilter.ts          # 筛选逻辑
│       └── useTemplateStats.ts           # 统计逻辑
├── projects/
│   └── settings/
│       └── ProjectPromptSettings.vue     # 项目提示词设置
└── user/
    └── settings/
        └── UserPromptSettings.vue        # 用户提示词设置
```

### 5.2 后端API扩展

**需要新增的API**:

```typescript
// 统计相关
GET /api/prompt-templates/stats                    // 获取统计信息
GET /api/prompt-templates/{id}/usage               // 获取使用记录

// 批量操作
POST /api/prompt-templates/batch/enable            // 批量启用
POST /api/prompt-templates/batch/disable           // 批量禁用
POST /api/prompt-templates/batch/delete            // 批量删除
POST /api/prompt-templates/import                  // 导入
GET /api/prompt-templates/export                   // 导出

// 收藏功能
POST /api/prompt-templates/{id}/favorite           // 收藏
DELETE /api/prompt-templates/{id}/favorite         // 取消收藏
GET /api/prompt-templates/favorites                // 获取收藏列表

// 评分功能（可选）
POST /api/prompt-templates/{id}/rate               // 评分
GET /api/prompt-templates/{id}/ratings             // 获取评分
```

### 5.3 数据模型扩展

```java
// 在PromptTemplate实体中添加字段
@Document(collection = "prompt_templates")
public class PromptTemplate {
    // ... 现有字段 ...
    
    // 新增字段
    private Integer usageCount = 0;              // 使用次数
    private Date lastUsedAt;                     // 最后使用时间
    private Boolean isFavorite = false;          // 是否收藏（用户级）
    private List<String> tags;                   // 标签
    private Double avgRating;                    // 平均评分
    private Integer ratingCount;                 // 评分次数
    
    // 索引
    @CompoundIndex(name = "user_favorite_idx", 
                   def = "{'userId': 1, 'isFavorite': 1}")
    @CompoundIndex(name = "usage_count_idx", 
                   def = "{'usageCount': -1}")
}

// 使用记录实体
@Document(collection = "prompt_template_usage")
public class PromptTemplateUsage {
    @Id
    private String id;
    private String templateId;
    private String userId;
    private String projectId;
    private PromptType type;
    private Date usedAt;
    private Boolean successful;
    private String feedback;
}
```

## 六、UI/UX 设计建议

### 6.1 信息架构

```
导航栏
├── 项目管理
├── 需求管理
├── 提示词管理 ← 新增主菜单项
│   ├── 我的提示词
│   ├── 项目提示词
│   └── 系统提示词（管理员）
└── 设置
    ├── 个人设置
    │   └── 提示词管理 ← 快捷入口
    └── 项目设置
        └── 提示词配置 ← 快捷入口
```

### 6.2 交互设计原则

1. **渐进式公开**
   - 默认显示常用功能
   - 高级功能折叠或在二级菜单

2. **即时反馈**
   - 操作后立即显示结果
   - 使用Toast提示成功/失败

3. **防止误操作**
   - 删除操作需要确认
   - 批量操作显示影响范围

4. **上下文感知**
   - 在项目页面默认筛选当前项目
   - 在使用场景默认选择相关类型

### 6.3 视觉设计

**颜色语义**:
- 🟦 系统级：蓝色 (#409EFF)
- 🟩 项目级：绿色 (#67C23A)
- 🟨 用户级：橙色 (#E6A23C)
- ⚪ 已禁用：灰色 (#909399)

**卡片布局**:
```
┌─────────────────────────────────┐
│ 🔵 系统 | 需求澄清              │
│                        ⭐ 1250次 │
├─────────────────────────────────┤
│ 需求澄清默认提示词               │
│ 用于生成需求澄清问题...          │
├─────────────────────────────────┤
│ 标签: 通用, 推荐                 │
│ 最后使用: 2小时前                │
├─────────────────────────────────┤
│ [预览] [使用] [编辑]             │
└─────────────────────────────────┘
```

## 七、最佳实践参考

### 7.1 竞品分析

| 产品 | 管理方式 | 优点 | 缺点 |
|------|---------|------|------|
| **Notion** | 统一模板库 + 工作区模板 | 层级清晰，易于发现 | 缺少使用统计 |
| **Jira** | 系统模板 + 项目模板 | 上下文相关性强 | 管理分散 |
| **GitHub Actions** | Marketplace + 仓库 | 集中浏览，便于分享 | 配置复杂 |
| **AWS CloudFormation** | 模板库 + 自定义 | 版本控制，可复用 | 学习曲线陡 |

### 7.2 行业标准

1. **权限分层**
   - 系统级：仅管理员可修改
   - 项目级：项目管理员可修改
   - 用户级：用户自己可修改

2. **命名规范**
   ```
   [层级]-[类型]-[用途]-[版本]
   示例: 
   - SYS-CLARIFY-DEFAULT-v1
   - PRJ-OPTIMIZE-FINANCIAL-v2
   - USER-CLARIFY-CUSTOM-2024
   ```

3. **版本管理**
   - 系统模板使用语义化版本
   - 用户模板记录创建/修改时间
   - 支持回退到历史版本

## 八、成功指标

### 8.1 使用率指标

```typescript
interface SuccessMetrics {
  // 采用率
  userAdoptionRate: number         // 使用自定义提示词的用户比例
  templateCoverageRate: number     // 有项目模板的项目比例
  
  // 活跃度
  avgTemplatesPerUser: number      // 每用户平均创建模板数
  templateUsageFrequency: number   // 模板平均使用频率
  
  // 满意度
  templateRating: number           // 模板平均评分
  customizationRate: number        // 用户修改模板的比例
  
  // 效率
  timeToCreateTemplate: number     // 创建模板平均耗时
  searchSuccessRate: number        // 搜索成功率
}
```

### 8.2 质量指标

- 系统模板覆盖主要场景 > 90%
- 用户自定义模板使用率 > 30%
- 模板查找时间 < 10秒
- 模板编辑成功率 > 95%

## 九、风险与应对

### 9.1 潜在风险

| 风险 | 影响 | 概率 | 应对措施 |
|------|------|------|---------|
| 用户不理解三级层级 | 高 | 中 | 提供详细引导和文档 |
| 模板数量过多难以管理 | 中 | 高 | 实现标签、搜索、筛选 |
| 模板质量参差不齐 | 中 | 中 | 添加评分和推荐机制 |
| 系统性能问题 | 高 | 低 | 分页加载、缓存优化 |

### 9.2 应对策略

1. **用户教育**
   - 新手引导流程
   - 帮助文档和视频教程
   - 最佳实践案例

2. **质量控制**
   - 模板验证机制
   - 用户评分和反馈
   - 管理员审核（可选）

3. **性能优化**
   - 虚拟滚动
   - 懒加载
   - Redis缓存

## 十、总结

### 推荐方案优势

✅ **用户体验优秀**
- 集中管理降低认知负担
- 上下文入口提高效率
- 使用时选择无缝集成

✅ **可扩展性强**
- 易于添加新的提示词类型
- 支持未来的AI功能
- 便于集成第三方模板

✅ **维护成本低**
- 统一的代码结构
- 复用现有组件
- 清晰的职责划分

✅ **符合业界标准**
- 参考成熟产品设计
- 遵循用户习惯
- 易于理解和使用

### 实施建议

1. **分阶段实施**：按优先级逐步推进，MVP先行
2. **用户参与**：邀请早期用户测试和反馈
3. **迭代优化**：根据使用数据持续改进
4. **文档先行**：提供清晰的使用指南

这个方案在**集中管理的效率**和**分散访问的便利性**之间取得了良好平衡，是最适合您系统的提示词管理方式。
