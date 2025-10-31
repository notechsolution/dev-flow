<template>
    <div class="user-story-detail">
        <el-card v-loading="loading" class="detail-card">
            <!-- Header -->
            <template #header>
                <div class="card-header">
                    <div class="header-left">
                        <el-button
                            type="text"
                            :icon="ArrowLeft"
                            @click="goBack"
                        >
                            返回
                        </el-button>
                        <h1>{{ userStory?.title || 'Loading...' }}</h1>
                    </div>
                    <div class="header-right">
                        <el-button
                            type="primary"
                            @click="editUserStory"
                            v-if="userStory"
                        >
                            <el-icon><Edit /></el-icon>
                            编辑
                        </el-button>
                        <el-dropdown @command="handleCommand">
                            <el-button>
                                更多操作
                                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                            </el-button>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item command="export">导出</el-dropdown-item>
                                    <el-dropdown-item command="duplicate">复制</el-dropdown-item>
                                    <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </div>
                </div>
            </template>

            <!-- Meta Information -->
            <div v-if="userStory" class="meta-section">
                <el-descriptions :column="3" border>
                    <el-descriptions-item label="Story ID">
                        <el-tag size="small">{{ userStory.id.substring(0, 8) }}</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="外部系统ID" v-if="userStory.storyId">
                        <el-tag type="success" size="small">{{ userStory.storyId }}</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="项目">
                        <el-tag type="info">{{ getProjectName(userStory.projectId || '') }}</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="状态">
                        <el-select
                            v-model="userStory.status"
                            size="small"
                            style="width: 120px"
                            @change="updateStatus"
                        >
                            <el-option
                                v-for="status in statusOptions"
                                :key="status.value"
                                :label="status.label"
                                :value="status.value"
                            />
                        </el-select>
                    </el-descriptions-item>
                    <el-descriptions-item label="优先级">
                        <el-tag :type="getPriorityType(userStory.priority || 'MEDIUM')">
                            {{ userStory.priority || 'MEDIUM' }}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="创建人">
                        {{ userStory.createdBy }}
                    </el-descriptions-item>
                    <el-descriptions-item label="创建时间">
                        {{ formatDate(userStory.createdAt) }}
                    </el-descriptions-item>
                </el-descriptions>
            </div>

            <!-- Content Tabs -->
            <div v-if="userStory" class="content-section">
                <el-tabs v-model="activeTab" type="card">
                    <!-- Original Requirement -->
                    <el-tab-pane label="原始需求" name="original">
                        <div class="content-panel">
                            <MilkdownProvider>
                                <MilkdownEditor
                                    :content="userStory.originalRequirement"
                                    :readonly="true"
                                />
                            </MilkdownProvider>
                        </div>
                    </el-tab-pane>

                    <!-- Clarification Q&A -->
                    <el-tab-pane label="需求澄清" name="clarification">
                        <div class="content-panel">
                            <div
                                v-for="(qa, index) in userStory.clarificationQAs"
                                :key="qa.questionId"
                                class="qa-item"
                            >
                                <div class="qa-header">
                                    <el-tag :type="getQuestionTagType(qa.category)" size="small">
                                        {{ qa.category }}
                                    </el-tag>
                                    <span class="qa-number">问题 {{ index + 1 }}</span>
                                </div>
                                <div class="qa-question">
                                    <strong>Q:</strong> {{ qa.question }}
                                </div>
                                <div class="qa-answer">
                                    <strong>A:</strong> {{ qa.answer }}
                                </div>
                            </div>
                            <el-empty
                                v-if="!userStory.clarificationQAs || userStory.clarificationQAs.length === 0"
                                description="暂无澄清问题"
                            />
                        </div>
                    </el-tab-pane>

                    <!-- Optimized Requirement -->
                    <el-tab-pane label="优化后的需求" name="optimized" v-if="userStory.optimizedRequirement">
                        <div class="content-panel">
                            <MilkdownProvider>
                                <MilkdownEditor
                                    :content="userStory.optimizedRequirement"
                                    :readonly="true"
                                />
                            </MilkdownProvider>
                        </div>
                    </el-tab-pane>

                    <!-- User Story -->
                    <el-tab-pane label="User Story" name="userStory" v-if="userStory.userStory">
                        <div class="content-panel">
                            <MilkdownProvider>
                                <MilkdownEditor
                                    :content="userStory.userStory"
                                    :readonly="true"
                                />
                            </MilkdownProvider>
                        </div>
                    </el-tab-pane>

                    <!-- Acceptance Criteria -->
                    <el-tab-pane label="验收标准" name="acceptance" v-if="userStory.acceptanceCriteria">
                        <div class="content-panel">
                            <MilkdownProvider>
                                <MilkdownEditor
                                    :content="userStory.acceptanceCriteria"
                                    :readonly="true"
                                />
                            </MilkdownProvider>
                        </div>
                    </el-tab-pane>

                    <!-- Technical Notes -->
                    <el-tab-pane label="技术说明" name="technical" v-if="userStory.technicalNotes">
                        <div class="content-panel">
                            <MilkdownProvider>
                                <MilkdownEditor
                                    :content="userStory.technicalNotes"
                                    :readonly="true"
                                />
                            </MilkdownProvider>
                        </div>
                    </el-tab-pane>
                </el-tabs>
            </div>
        </el-card>
    </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Edit, ArrowDown } from '@element-plus/icons-vue'
import MilkdownEditor from '@/components/MilkdownEditor.vue'
import { MilkdownProvider } from "@milkdown/vue"
import aiApi, { UserStoryResponse } from '@/api/backend-api'

const router = useRouter()
const route = useRoute()

// State
const loading = ref(false)
const userStory = ref<UserStoryResponse | null>(null)
const activeTab = ref('original')

// Mock data
const projects = ref<Array<{ id: string; name: string }>>([])

const statusOptions = [
    { label: '待办', value: 'BACKLOG' },
    { label: '进行中', value: 'IN_PROGRESS' },
    { label: '已完成', value: 'DONE' },
    { label: '已取消', value: 'CANCELLED' }
]

// Methods
const loadUserStory = async () => {
    const id = route.params.id as string
    if (!id) {
        ElMessage.error('无效的 User Story ID')
        goBack()
        return
    }

    loading.value = true
    try {
        const response = await aiApi.getUserStory(id)
        if (response.data.success) {
            userStory.value = response.data.data
        } else {
            ElMessage.error('加载 User Story 失败')
            goBack()
        }
    } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '加载 User Story 失败')
        console.error('Load error:', error)
        goBack()
    } finally {
        loading.value = false
    }
}

// Load projects from backend
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
        ElMessage.error('加载项目列表失败')
    }
}
const updateStatus = async () => {
    if (!userStory.value) return

    try {
        await aiApi.updateUserStoryStatus(userStory.value.id, userStory.value.status)
        ElMessage.success('状态更新成功')
    } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '更新状态失败')
        console.error('Update status error:', error)
        loadUserStory() // Reload to revert
    }
}

const editUserStory = () => {
    if (userStory.value) {
        router.push(`/requirement/UserStoryEdit/${userStory.value.id}`)
    }
}

const handleCommand = async (command: string) => {
    if (!userStory.value) return

    switch (command) {
        case 'export':
            exportUserStory()
            break
        case 'duplicate':
            duplicateUserStory()
            break
        case 'delete':
            await deleteUserStory()
            break
    }
}

const exportUserStory = () => {
    ElMessage.info('导出功能开发中...')
    // TODO: Implement export functionality
}

const duplicateUserStory = () => {
    ElMessage.info('复制功能开发中...')
    // TODO: Implement duplicate functionality
}

const deleteUserStory = async () => {
    if (!userStory.value) return

    try {
        await ElMessageBox.confirm(
            `确定要删除 User Story "${userStory.value.title}" 吗？`,
            '确认删除',
            {
                confirmButtonText: '删除',
                cancelButtonText: '取消',
                type: 'warning'
            }
        )

        await aiApi.deleteUserStory(userStory.value.id)
        ElMessage.success('删除成功')
        goBack()
    } catch (error: any) {
        if (error !== 'cancel') {
            ElMessage.error(error.response?.data?.message || '删除失败')
            console.error('Delete error:', error)
        }
    }
}

const goBack = () => {
    router.push('/dashboard')
}

// Helper methods
const getProjectName = (projectId: string) => {
    if (!projectId) return '-'
    return projects.value.find(p => p.id === projectId)?.name || projectId
}


const getPriorityType = (priority: string) => {
    const types: Record<string, any> = {
        'HIGH': 'danger',
        'MEDIUM': 'warning',
        'LOW': 'info'
    }
    return types[priority] || 'info'
}

const getQuestionTagType = (category: string) => {
    const tagTypes: Record<string, any> = {
        'Users & Roles': 'success',
        'Goals & Outcomes': 'warning',
        'Business Rules': 'danger',
        'Data & Interface': 'info',
        'Non-functional Requirements': '',
        'General': 'info'
    }
    return tagTypes[category] || 'info'
}

const formatDate = (date: string) => {
    return new Date(date).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    })
}

// Initialize
onMounted(async () => {
    await loadProjects()
    await loadUserStory()
})
</script>

<style scoped>
.user-story-detail {
    padding: 20px;
    width: 100%;
    max-width: 1400px;
    margin: 0 auto;
}

.detail-card {
    margin-bottom: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 10px;
}

.header-left h1 {
    margin: 0;
    font-size: 22px;
    font-weight: 600;
    color: #303133;
}

.header-right {
    display: flex;
    gap: 10px;
}

.meta-section {
    margin-bottom: 30px;
}

.content-section {
    min-height: 500px;
}

.content-panel {
    padding: 20px;
    min-height: 400px;
}

/* Q&A Styles */
.qa-item {
    background: #f5f7fa;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 20px;
}

.qa-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.qa-number {
    font-size: 14px;
    color: #909399;
    font-weight: 500;
}

.qa-question {
    font-size: 15px;
    color: #303133;
    margin-bottom: 12px;
    line-height: 1.6;
}

.qa-question strong {
    color: #409EFF;
    margin-right: 8px;
}

.qa-answer {
    font-size: 15px;
    color: #606266;
    line-height: 1.6;
    padding-left: 20px;
}

.qa-answer strong {
    color: #67C23A;
    margin-right: 8px;
}

:deep(.el-card__body) {
    padding: 20px;
}

:deep(.el-descriptions) {
    margin-top: 20px;
}

:deep(.el-tabs__content) {
    padding: 0;
}

@media (max-width: 768px) {
    .user-story-detail {
        padding: 10px;
    }

    .card-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
    }

    .header-left,
    .header-right {
        width: 100%;
    }

    .header-right {
        justify-content: flex-start;
    }

    .meta-section :deep(.el-descriptions) {
        font-size: 14px;
    }

    .qa-item {
        padding: 15px;
    }
}
</style>
