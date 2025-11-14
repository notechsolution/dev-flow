<template>
    <div class="template-table-view">
        <!-- 搜索和筛选 -->
        <el-row :gutter="20" class="filter-bar">
            <el-col :span="8">
                <el-input
                    v-model="filterOptions.keyword"
                    placeholder="搜索提示词名称、描述或内容..."
                    clearable
                >
                    <template #prefix>
                        <el-icon><Search /></el-icon>
                    </template>
                </el-input>
            </el-col>
            <el-col :span="4">
                <el-select v-model="filterOptions.type" placeholder="类型" clearable>
                    <el-option label="全部类型" value="ALL" />
                    <el-option label="需求澄清" value="REQUIREMENT_CLARIFICATION" />
                    <el-option label="需求优化" value="REQUIREMENT_OPTIMIZATION" />
                </el-select>
            </el-col>
            <el-col :span="4">
                <el-select v-model="filterOptions.enabled" placeholder="状态" clearable>
                    <el-option label="全部状态" :value="null" />
                    <el-option label="已启用" :value="true" />
                    <el-option label="已禁用" :value="false" />
                </el-select>
            </el-col>
            <el-col :span="4">
                <el-button @click="resetFilters">重置</el-button>
            </el-col>
        </el-row>

        <!-- 统计信息 -->
        <div class="statistics-bar">
            <el-tag type="info">共 {{ statistics.total }} 条</el-tag>
            <el-tag type="success">筛选结果: {{ statistics.filtered }} 条</el-tag>
            <el-tag>澄清: {{ statistics.byType.clarification }}</el-tag>
            <el-tag>优化: {{ statistics.byType.optimization }}</el-tag>
        </div>

        <!-- 表格 -->
        <el-table
            :data="paginatedTemplates"
            v-loading="loading"
            stripe
            style="width: 100%"
            @selection-change="handleSelectionChange"
        >
            <el-table-column type="selection" width="55" v-if="selectable" />
            
            <el-table-column prop="name" label="名称" min-width="150">
                <template #default="{ row }">
                    <div class="template-name">
                        <el-tag :type="getLevelType(row.level)" size="small">
                            {{ getLevelLabel(row.level) }}
                        </el-tag>
                        <span class="name-text">{{ row.name }}</span>
                    </div>
                </template>
            </el-table-column>

            <el-table-column prop="type" label="类型" width="120">
                <template #default="{ row }">
                    <el-tag :type="getTypeTagType(row.type)" size="small">
                        {{ getTypeLabel(row.type) }}
                    </el-tag>
                </template>
            </el-table-column>

            <el-table-column prop="description" label="描述" min-width="200">
                <template #default="{ row }">
                    <span class="description-text">{{ row.description || '-' }}</span>
                </template>
            </el-table-column>

            <el-table-column prop="enabled" label="状态" width="80">
                <template #default="{ row }">
                    <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                        {{ row.enabled ? '启用' : '禁用' }}
                    </el-tag>
                </template>
            </el-table-column>

            <el-table-column prop="updatedAt" label="更新时间" width="160">
                <template #default="{ row }">
                    {{ formatDate(row.updatedAt) }}
                </template>
            </el-table-column>

            <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                    <el-button link type="primary" @click="$emit('view', row)">
                        查看
                    </el-button>
                    <el-button 
                        link 
                        @click="$emit('edit', row)"
                        v-if="canEdit(row)"
                    >
                        编辑
                    </el-button>
                    <el-button 
                        link 
                        type="danger" 
                        @click="$emit('delete', row)"
                        v-if="canDelete(row)"
                    >
                        删除
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <!-- 分页 -->
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="filteredTemplates.length"
            layout="total, sizes, prev, pager, next, jumper"
            class="pagination"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
    </div>
</template>

<script setup lang="ts">
import { ref, computed, toRef } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { PromptTemplateResponse } from '@/api/backend-api'
import { useTemplateFilter } from '@/composables/useTemplateFilter'
import { userStore } from '@/store/user'

// Props
const props = defineProps<{
    templates: PromptTemplateResponse[]
    loading?: boolean
    selectable?: boolean
}>()

// Emits
const emit = defineEmits<{
    (e: 'view', template: PromptTemplateResponse): void
    (e: 'edit', template: PromptTemplateResponse): void
    (e: 'delete', template: PromptTemplateResponse): void
    (e: 'selection-change', templates: PromptTemplateResponse[]): void
}>()

// 使用筛选组合式函数 - 传入响应式ref
const templatesRef = toRef(props, 'templates')
const { 
    filterOptions, 
    filteredTemplates, 
    statistics, 
    resetFilters 
} = useTemplateFilter(templatesRef)

// 分页
const currentPage = ref(1)
const pageSize = ref(20)

const paginatedTemplates = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredTemplates.value.slice(start, end)
})

const handleSizeChange = (val: number) => {
    pageSize.value = val
    currentPage.value = 1
}

const handleCurrentChange = (val: number) => {
    currentPage.value = val
}

// 选择
const handleSelectionChange = (selection: PromptTemplateResponse[]) => {
    emit('selection-change', selection)
}

// 权限判断
const currentUserId = userStore().getUserId

const canEdit = (template: PromptTemplateResponse) => {
    if (template.level === 'SYSTEM') return false
    if (template.level === 'USER') return template.userId === currentUserId
    if (template.level === 'PROJECT') return true // 项目管理员权限判断可以后续完善
    return false
}

const canDelete = (template: PromptTemplateResponse) => {
    if (template.level === 'SYSTEM') return false
    if (template.level === 'USER') return template.userId === currentUserId
    if (template.level === 'PROJECT') return true
    return false
}

// 辅助方法
const getLevelType = (level: string) => {
    const types: Record<string, any> = {
        'SYSTEM': 'primary',
        'PROJECT': 'success',
        'USER': 'warning'
    }
    return types[level] || 'info'
}

const getLevelLabel = (level: string) => {
    const labels: Record<string, string> = {
        'SYSTEM': '系统',
        'PROJECT': '项目',
        'USER': '用户'
    }
    return labels[level] || level
}

const getTypeTagType = (type: string) => {
    return type === 'REQUIREMENT_CLARIFICATION' ? '' : 'success'
}

const getTypeLabel = (type: string) => {
    const labels: Record<string, string> = {
        'REQUIREMENT_CLARIFICATION': '需求澄清',
        'REQUIREMENT_OPTIMIZATION': '需求优化'
    }
    return labels[type] || type
}

const formatDate = (dateString: string) => {
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    })
}
</script>

<style scoped lang="scss">
.template-table-view {
    .filter-bar {
        margin-bottom: 16px;
    }

    .statistics-bar {
        margin-bottom: 16px;
        display: flex;
        gap: 8px;
    }

    .template-name {
        display: flex;
        align-items: center;
        gap: 8px;

        .name-text {
            font-weight: 500;
        }
    }

    .description-text {
        color: #606266;
        font-size: 13px;
    }

    .pagination {
        margin-top: 20px;
        justify-content: flex-end;
    }
}
</style>
