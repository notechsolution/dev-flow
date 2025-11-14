<template>
    <div class="user-prompt-settings">
        <el-card>
            <template #header>
                <div class="card-header">
                    <span>我的提示词</span>
                    <div>
                        <el-button size="small" @click="handleCreate">
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

            <!-- 快速统计 -->
            <div class="quick-stats">
                <el-statistic title="总数" :value="templates.length" />
                <el-statistic 
                    title="需求澄清" 
                    :value="templates.filter(t => t.type === 'REQUIREMENT_CLARIFICATION').length" 
                />
                <el-statistic 
                    title="需求优化" 
                    :value="templates.filter(t => t.type === 'REQUIREMENT_OPTIMIZATION').length" 
                />
            </div>

            <!-- 提示词列表 -->
            <el-table
                :data="templates"
                v-loading="loading"
                stripe
                style="margin-top: 20px"
            >
                <el-table-column prop="name" label="名称" min-width="150" />
                
                <el-table-column prop="type" label="类型" width="120">
                    <template #default="{ row }">
                        <el-tag size="small">
                            {{ row.type === 'REQUIREMENT_CLARIFICATION' ? '需求澄清' : '需求优化' }}
                        </el-tag>
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

                <el-table-column label="操作" width="150">
                    <template #default="{ row }">
                        <el-button link @click="handleEdit(row)">编辑</el-button>
                        <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <!-- 编辑器 -->
        <TemplateEditor
            v-model="editorVisible"
            :template="currentTemplate"
            @success="handleEditorSuccess"
        />
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import TemplateEditor from '@/views/prompt-templates/components/TemplateEditor.vue'
import { useTemplateManagement } from '@/composables/useTemplateManagement'
import { PromptTemplateResponse } from '@/api/backend-api'

const router = useRouter()

// State
const editorVisible = ref(false)
const currentTemplate = ref<PromptTemplateResponse | null>(null)

// Use composable
const {
    loading,
    templates,
    loadMyTemplates,
    deleteTemplate: deleteTemplateAPI
} = useTemplateManagement()

// Methods
const handleCreate = () => {
    currentTemplate.value = null
    editorVisible.value = true
}

const handleEdit = (template: PromptTemplateResponse) => {
    currentTemplate.value = template
    editorVisible.value = true
}

const handleDelete = async (template: PromptTemplateResponse) => {
    try {
        await ElMessageBox.confirm(
            `确定要删除提示词"${template.name}"吗？`,
            '确认删除',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        )
        
        await deleteTemplateAPI(template.id)
        loadMyTemplates()
    } catch (error) {
        // User cancelled
    }
}

const handleEditorSuccess = () => {
    loadMyTemplates()
}

const goToManagementCenter = () => {
    router.push('/prompt-templates')
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

// Lifecycle
onMounted(() => {
    loadMyTemplates()
})
</script>

<style scoped lang="scss">
.user-prompt-settings {
    padding: 20px;

    .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-weight: 600;
    }

    .quick-stats {
        display: flex;
        gap: 40px;
        padding: 20px;
        background: #f5f7fa;
        border-radius: 4px;
    }
}
</style>
