<template>
    <div class="prompt-template-management">
        <div class="page-header">
            <h1>提示词管理</h1>
            <el-button type="primary" @click="handleCreate">
                <el-icon><Plus /></el-icon>
                创建提示词
            </el-button>
        </div>

        <!-- Tab 切换 -->
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="我的提示词" name="user">
                <TemplateTableView
                    :templates="templates"
                    :loading="loading"
                    @view="handleView"
                    @edit="handleEdit"
                    @delete="handleDelete"
                />
            </el-tab-pane>

            <el-tab-pane label="项目提示词" name="project">
                <div class="project-selector">
                    <el-select
                        v-model="selectedProjectId"
                        placeholder="请选择项目"
                        filterable
                        @change="loadProjectTemplates"
                        style="width: 300px"
                    >
                        <el-option
                            v-for="project in projects"
                            :key="project.id"
                            :label="project.name"
                            :value="project.id"
                        />
                    </el-select>
                </div>
                <TemplateTableView
                    v-if="selectedProjectId"
                    :templates="templates"
                    :loading="loading"
                    @view="handleView"
                    @edit="handleEdit"
                    @delete="handleDelete"
                />
                <el-empty v-else description="请选择项目查看提示词" />
            </el-tab-pane>

            <el-tab-pane label="系统提示词" name="system" v-if="isOperator">
                <TemplateTableView
                    :templates="templates"
                    :loading="loading"
                    @view="handleView"
                    @edit="handleEdit"
                    @delete="handleDelete"
                />
            </el-tab-pane>
        </el-tabs>

        <!-- 编辑器对话框 -->
        <TemplateEditor
            v-model="editorVisible"
            :template="currentTemplate"
            :project-id="selectedProjectId"
            @success="handleEditorSuccess"
        />

        <!-- 查看对话框 -->
        <el-dialog
            v-model="viewDialogVisible"
            title="查看提示词"
            width="700px"
        >
            <div v-if="currentTemplate" class="template-detail">
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="名称">
                        {{ currentTemplate.name }}
                    </el-descriptions-item>
                    <el-descriptions-item label="类型">
                        <el-tag>{{ getTypeLabel(currentTemplate.type) }}</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="级别">
                        <el-tag :type="getLevelType(currentTemplate.level)">
                            {{ getLevelLabel(currentTemplate.level) }}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="状态">
                        <el-tag :type="currentTemplate.enabled ? 'success' : 'info'">
                            {{ currentTemplate.enabled ? '启用' : '禁用' }}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="描述" :span="2">
                        {{ currentTemplate.description || '-' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="创建时间">
                        {{ formatDate(currentTemplate.createdAt) }}
                    </el-descriptions-item>
                    <el-descriptions-item label="更新时间">
                        {{ formatDate(currentTemplate.updatedAt) }}
                    </el-descriptions-item>
                </el-descriptions>

                <div class="content-section">
                    <h4>提示词内容</h4>
                    <el-input
                        :model-value="currentTemplate.content"
                        type="textarea"
                        :rows="15"
                        readonly
                        class="content-textarea"
                    />
                </div>
            </div>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import TemplateTableView from './components/TemplateTableView.vue'
import TemplateEditor from './components/TemplateEditor.vue'
import { useTemplateManagement } from '@/composables/useTemplateManagement'
import { PromptTemplateResponse } from '@/api/backend-api'
import { userStore } from '@/store/user'
import aiApi from '@/api/backend-api'

// State
const activeTab = ref('user')
const editorVisible = ref(false)
const viewDialogVisible = ref(false)
const currentTemplate = ref<PromptTemplateResponse | null>(null)
const projects = ref<Array<{ id: string; name: string }>>([])
const selectedProjectId = ref<string>('')

const store = userStore()
const isOperator = computed(() => store.role === 'OPERATOR')

// Use composable
const {
    loading,
    templates,
    loadMyTemplates,
    loadProjectTemplates: loadProjectTemplatesAPI,
    loadSystemTemplates,
    createTemplate,
    updateTemplate,
    deleteTemplate: deleteTemplateAPI
} = useTemplateManagement()

// Methods
const handleTabChange = (tab: string) => {
    currentTemplate.value = null
    selectedProjectId.value = ''
    
    switch (tab) {
        case 'user':
            loadMyTemplates()
            break
        case 'system':
            loadSystemTemplates()
            break
        case 'project':
            // Wait for project selection
            break
    }
}

const loadProjects = async () => {
    try {
        const response = await aiApi.getProjects()
        if (response.data.success) {
            projects.value = response.data.data.map((project: any) => ({
                id: project.id,
                name: project.name
            }))
        }
    } catch (error) {
        console.error('Failed to load projects:', error)
    }
}

const loadProjectTemplates = () => {
    if (selectedProjectId.value) {
        loadProjectTemplatesAPI(selectedProjectId.value)
    }
}

const handleCreate = () => {
    currentTemplate.value = null
    editorVisible.value = true
}

const handleView = (template: PromptTemplateResponse) => {
    currentTemplate.value = template
    viewDialogVisible.value = true
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
        // Reload current view
        handleTabChange(activeTab.value)
    } catch (error) {
        // User cancelled
    }
}

const handleEditorSuccess = () => {
    // Reload current view
    handleTabChange(activeTab.value)
}

// Helper methods
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

// Lifecycle
onMounted(() => {
    loadProjects()
    loadMyTemplates()
})
</script>

<style scoped lang="scss">
.prompt-template-management {
    padding: 20px;
    max-width: 1600px;
    margin: 0 auto;

    .page-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;

        h1 {
            margin: 0;
            font-size: 24px;
            font-weight: 600;
        }
    }

    .project-selector {
        margin-bottom: 20px;
    }

    .template-detail {
        .content-section {
            margin-top: 20px;

            h4 {
                margin-bottom: 10px;
                color: #303133;
            }

            .content-textarea {
                :deep(.el-textarea__inner) {
                    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
                    background-color: #f5f7fa;
                }
            }
        }
    }
}
</style>
