<template>
    <div class="user-story-list">
        <div class="page-header">
            <h1>User Stories</h1>
            <el-button type="primary" @click="createUserStory">
                <el-icon><Plus /></el-icon>
                创建 User Story
            </el-button>
        </div>

        <!-- Filters -->
        <el-card class="filter-section">
            <el-form :inline="true" :model="filters">
                <el-form-item label="项目">
                    <el-select
                        v-model="filters.projectId"
                        placeholder="所有项目"
                        clearable
                        style="width: 200px"
                        @change="loadUserStories"
                    >
                        <el-option
                            v-for="project in projects"
                            :key="project.id"
                            :label="project.name"
                            :value="project.id"
                        />
                    </el-select>
                </el-form-item>

                <el-form-item label="状态">
                    <el-select
                        v-model="filters.status"
                        placeholder="所有状态"
                        clearable
                        style="width: 150px"
                        @change="loadUserStories"
                    >
                        <el-option
                            v-for="status in statusOptions"
                            :key="status.value"
                            :label="status.label"
                            :value="status.value"
                        />
                    </el-select>
                </el-form-item>

                <el-form-item label="负责人">
                    <el-select
                        v-model="filters.ownerId"
                        placeholder="所有人"
                        clearable
                        filterable
                        style="width: 150px"
                        @change="loadUserStories"
                    >
                        <el-option
                            v-for="user in users"
                            :key="user.id"
                            :label="user.username"
                            :value="user.id"
                        />
                    </el-select>
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="loadUserStories">
                        <el-icon><Search /></el-icon>
                        查询
                    </el-button>
                    <el-button @click="resetFilters">
                        <el-icon><Refresh /></el-icon>
                        重置
                    </el-button>
                </el-form-item>
            </el-form>
        </el-card>

        <!-- Table -->
        <el-card class="table-section">
            <el-table
                v-loading="loading"
                :data="userStories"
                stripe
                style="width: 100%"
                @row-click="viewUserStory"
                :row-style="{ cursor: 'pointer' }"
            >
                <el-table-column prop="id" label="ID" width="180">
                    <template #default="{ row }">
                        <div class="id-column">
                            <div v-if="row.storyId" class="external-id">
                                <el-tag type="success" size="small">
                                    <el-icon style="margin-right: 4px;"><Link /></el-icon>
                                    {{ row.storyId }}
                                </el-tag>
                                <el-tooltip content="外部系统ID" placement="top">
                                    <span class="id-label">外部ID</span>
                                </el-tooltip>
                            </div>
                            <div v-else class="internal-id">
                                <el-tag size="small">
                                    {{ row.id.substring(0, 8) }}
                                </el-tag>
                                <el-tooltip content="内部数据库ID" placement="top">
                                    <span class="id-label">内部ID</span>
                                </el-tooltip>
                            </div>
                        </div>
                    </template>
                </el-table-column>

                <el-table-column prop="title" label="标题" min-width="300" show-overflow-tooltip />

                <el-table-column prop="projectId" label="项目" width="150">
                    <template #default="{ row }">
                        <el-tag type="info">{{ getProjectName(row.projectId) }}</el-tag>
                    </template>
                </el-table-column>

                <el-table-column prop="status" label="状态" width="120">
                    <template #default="{ row }">
                        <el-tag :type="getStatusType(row.status)">
                            {{ getStatusLabel(row.status) }}
                        </el-tag>
                    </template>
                </el-table-column>

                <el-table-column prop="priority" label="优先级" width="100">
                    <template #default="{ row }">
                        <el-tag :type="getPriorityType(row.priority)" size="small">
                            {{ row.priority }}
                        </el-tag>
                    </template>
                </el-table-column>

                <el-table-column prop="createdBy" label="创建人" width="120">
                    <template #default="{ row }">
                        {{ getUserName(row.createdBy) }}
                    </template>
                </el-table-column>

                <el-table-column prop="createdAt" label="创建时间" width="180">
                    <template #default="{ row }">
                        {{ formatDate(row.createdAt) }}
                    </template>
                </el-table-column>

                <el-table-column label="操作" width="180" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            size="small"
                            type="primary"
                            link
                            @click.stop="viewUserStory(row)"
                        >
                            查看
                        </el-button>
                        <el-button
                            size="small"
                            type="warning"
                            link
                            @click.stop="editUserStory(row)"
                        >
                            编辑
                        </el-button>
                        <el-button
                            size="small"
                            type="danger"
                            link
                            @click.stop="deleteUserStory(row)"
                        >
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- Pagination -->
            <div class="pagination-container">
                <el-pagination
                    v-model:current-page="pagination.page"
                    v-model:page-size="pagination.pageSize"
                    :page-sizes="[10, 20, 50, 100]"
                    :total="pagination.total"
                    layout="total, sizes, prev, pager, next, jumper"
                    @size-change="loadUserStories"
                    @current-change="loadUserStories"
                />
            </div>
        </el-card>
    </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Link } from '@element-plus/icons-vue'
import aiApi, { UserStoryResponse } from '@/api/backend-api'

const router = useRouter()

// State
const loading = ref(false)
const userStories = ref<UserStoryResponse[]>([])

const filters = reactive({
    projectId: '',
    status: '',
    ownerId: ''
})

const pagination = reactive({
    page: 1,
    pageSize: 20,
    total: 0
})

// Mock data - replace with actual API calls
const projects = ref([
    { id: '1', name: 'Project Alpha' },
    { id: '2', name: 'Project Beta' },
    { id: '3', name: 'Project Gamma' }
])

const users = ref([
    { id: '1', username: 'john.doe' },
    { id: '2', username: 'jane.smith' },
    { id: '3', username: 'bob.wilson' }
])

const statusOptions = [
    { label: '待办', value: 'BACKLOG' },
    { label: '进行中', value: 'IN_PROGRESS' },
    { label: '已完成', value: 'DONE' },
    { label: '已取消', value: 'CANCELLED' }
]

// Methods
const loadUserStories = async () => {
    loading.value = true
    try {
        const params: any = {}
        if (filters.projectId) params.projectId = filters.projectId
        if (filters.status) params.status = filters.status
        if (filters.ownerId) params.ownerId = filters.ownerId

        const response = await aiApi.getUserStories(params)
        if (response.data.success) {
            userStories.value = response.data.data
            pagination.total = response.data.total
        } else {
            ElMessage.error('加载 User Stories 失败')
        }
    } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '加载 User Stories 失败')
        console.error('Load error:', error)
    } finally {
        loading.value = false
    }
}

const resetFilters = () => {
    filters.projectId = ''
    filters.status = ''
    filters.ownerId = ''
    loadUserStories()
}

const createUserStory = () => {
    router.push('/requirement/UserStoryCreation')
}

const viewUserStory = (row: UserStoryResponse) => {
    router.push(`/requirement/UserStoryDetail/${row.id}`)
}

const editUserStory = (row: UserStoryResponse) => {
    router.push(`/requirement/UserStoryEdit/${row.id}`)
}

const deleteUserStory = async (row: UserStoryResponse) => {
    try {
        await ElMessageBox.confirm(
            `确定要删除 User Story "${row.title}" 吗？`,
            '确认删除',
            {
                confirmButtonText: '删除',
                cancelButtonText: '取消',
                type: 'warning'
            }
        )

        await aiApi.deleteUserStory(row.id)
        ElMessage.success('删除成功')
        loadUserStories()
    } catch (error: any) {
        if (error !== 'cancel') {
            ElMessage.error(error.response?.data?.message || '删除失败')
            console.error('Delete error:', error)
        }
    }
}

// Helper methods
const getProjectName = (projectId: string) => {
    if (!projectId) return '-'
    return projects.value.find(p => p.id === projectId)?.name || projectId
}

const getUserName = (userId: string) => {
    return users.value.find(u => u.id === userId)?.username || userId
}

const getStatusType = (status: string) => {
    const types: Record<string, any> = {
        'BACKLOG': 'info',
        'IN_PROGRESS': 'warning',
        'DONE': 'success',
        'CANCELLED': 'danger'
    }
    return types[status] || 'info'
}

const getStatusLabel = (status: string) => {
    const labels: Record<string, string> = {
        'BACKLOG': '待办',
        'IN_PROGRESS': '进行中',
        'DONE': '已完成',
        'CANCELLED': '已取消'
    }
    return labels[status] || status
}

const getPriorityType = (priority: string) => {
    const types: Record<string, any> = {
        'HIGH': 'danger',
        'MEDIUM': 'warning',
        'LOW': 'info'
    }
    return types[priority] || 'info'
}

const formatDate = (date: string) => {
    return new Date(date).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    })
}

// Initialize
onMounted(() => {
    loadUserStories()
})
</script>

<style scoped>
.user-story-list {
    padding: 20px;
    width: 100%;
    max-width: 1800px;
    margin: 0 auto;
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.page-header h1 {
    color: #303133;
    font-size: 24px;
    font-weight: 600;
    margin: 0;
}

.filter-section {
    margin-bottom: 20px;
}

.table-section {
    margin-bottom: 20px;
}

.pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}

:deep(.el-table__row) {
    transition: all 0.2s;
}

:deep(.el-table__row:hover) {
    background-color: #f5f7fa;
}

:deep(.el-card__body) {
    padding: 20px;
}

/* ID Column Styles */
.id-column {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.external-id,
.internal-id {
    display: flex;
    align-items: center;
    gap: 6px;
}

.id-label {
    font-size: 11px;
    color: #909399;
    font-weight: 500;
}

.external-id .el-tag {
    background-color: #f0f9ff;
    border-color: #67c23a;
    color: #67c23a;
}

@media (max-width: 768px) {
    .user-story-list {
        padding: 10px;
    }

    .page-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
    }

    .filter-section :deep(.el-form--inline) {
        display: flex;
        flex-direction: column;
    }

    .filter-section :deep(.el-form-item) {
        margin-right: 0;
        width: 100%;
    }
}
</style>
