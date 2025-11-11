<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Link, Upload, Download } from '@element-plus/icons-vue'
import aiApi, { UserStoryResponse } from '@/api/backend-api'
import * as XLSX from 'xlsx'

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

// Data loaded from backend
const projects = ref<Array<{ id: string; name: string }>>([])

const users = ref<Array<{ id: string; username: string }>>([])

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
    }
}

// Load users from backend
const loadUsers = async () => {
    try {
        const response = await aiApi.getUsers()
        if (response.data.success) {
            users.value = response.data.data.map((user: any) => ({
                id: user.id,
                username: user.username
            }))
        }
    } catch (error) {
        console.error('Failed to load users:', error)
    }
}

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

// Batch import dialog
const batchImportDialogVisible = ref(false)
const importLoading = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)

const openBatchImportDialog = () => {
    batchImportDialogVisible.value = true
    selectedFile.value = null
}

const handleFileSelect = (event: Event) => {
    const target = event.target as HTMLInputElement
    if (target.files && target.files.length > 0) {
        selectedFile.value = target.files[0]
    }
}

const downloadTemplate = async () => {
    try {
        const response = await aiApi.downloadBatchImportTemplate()
        
        // Create a blob from the response
        const blob = new Blob([response.data], { 
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
        })
        
        // Create a download link
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = 'user_story_template.xlsx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        ElMessage.success('模板下载成功')
    } catch (error) {
        console.error('Failed to download template:', error)
        ElMessage.error('模板下载失败')
    }
}

const uploadBatchUserStories = async () => {
    if (!selectedFile.value) {
        ElMessage.warning('请选择要上传的 Excel 文件')
        return
    }
    
    importLoading.value = true
    
    try {
        // Read Excel file
        const data = await readExcelFile(selectedFile.value)
        
        if (!data || data.length === 0) {
            ElMessage.error('Excel 文件为空或格式不正确')
            importLoading.value = false
            return
        }
        
        // Validate and transform data
        const userStories = parseExcelData(data)
        
        if (userStories.length === 0) {
            ElMessage.error('没有有效的用户故事数据')
            importLoading.value = false
            return
        }
        
        // Call backend API to batch create user stories
        const response = await aiApi.batchImportUserStories(userStories)
        
        if (response.data.success) {
            ElMessage.success(`成功导入 ${userStories.length} 个 User Story`)
            batchImportDialogVisible.value = false
            selectedFile.value = null
            loadUserStories()
        } else {
            ElMessage.error(response.data.message || '批量导入失败')
        }
    } catch (error: any) {
        console.error('Batch import error:', error)
        ElMessage.error(error.response?.data?.message || '批量导入失败')
    } finally {
        importLoading.value = false
    }
}

const readExcelFile = (file: File): Promise<any[]> => {
    return new Promise((resolve, reject) => {
        const reader = new FileReader()
        
        reader.onload = (e) => {
            try {
                const data = e.target?.result
                const workbook = XLSX.read(data, { type: 'binary' })
                const firstSheetName = workbook.SheetNames[0]
                const worksheet = workbook.Sheets[firstSheetName]
                const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })
                resolve(jsonData)
            } catch (error) {
                reject(error)
            }
        }
        
        reader.onerror = (error) => {
            reject(error)
        }
        
        reader.readAsBinaryString(file)
    })
}

const parseExcelData = (data: any[]): any[] => {
    const userStories: any[] = []
    
    console.log('Excel data:', data)
    // Skip header row (index 0)
    for (let i = 1; i < data.length; i++) {
        const row = data[i]
        
        // Skip empty rows
        if (!row || row.length === 0 || !row[0]) {
            continue
        }
        
        // Extract fields based on column index
        const product = row[0]?.toString().trim() || ''          // 所属产品
        const platform = row[1]?.toString().trim() || ''         // 平台/分支
        const module = row[2]?.toString().trim() || ''           // 所属模块
        const plan = row[3]?.toString().trim() || ''             // 所属计划
        const source = row[4]?.toString().trim() || ''           // 来源
        const sourceNote = row[5]?.toString().trim() || ''       // 来源备注
        const title = row[6]?.toString().trim() || ''            // 研发需求名称
        const description = row[7]?.toString().trim() || ''      // 描述
        const acceptanceCriteria = row[8]?.toString().trim() || '' // 验收标准
        const keywords = row[9]?.toString().trim() || ''         // 关键词
        
        // Validate required fields: product, title, description
        if (!product) {
            console.warn(`Row ${i + 1}: Missing required field '所属产品', skipping...`)
            continue
        }
        if (!title) {
            console.warn(`Row ${i + 1}: Missing required field '研发需求名称', skipping...`)
            continue
        }
        if (!description) {
            console.warn(`Row ${i + 1}: Missing required field '描述', skipping...`)
            continue
        }
        
        // Find project ID by product name
        const project = projects.value.find(p => p.name === product)
        if (!project) {
            console.warn(`Row ${i + 1}: Product '${product}' not found, skipping...`)
            continue
        }
        
        // Build tags array
        const tags: string[] = []
        if (platform) tags.push(`平台:${platform}`)
        if (module) tags.push(`模块:${module}`)
        if (plan) tags.push(`计划:${plan}`)
        if (source) tags.push(`来源:${source}`)
        if (keywords) {
            keywords.split(',').forEach((k: string) => tags.push(k.trim()))
        }
        
        // Create user story object
        const userStory = {
            projectId: project.id,
            title: title,
            originalRequirement: description,
            acceptanceCriteria: acceptanceCriteria || undefined,
            tags: tags.length > 0 ? tags : undefined,
            status: 'BACKLOG',
            priority: 'MEDIUM'
        }
        
        // Add source note if provided
        if (sourceNote) {
            userStory.originalRequirement += `\n\n来源备注: ${sourceNote}`
        }
        
        userStories.push(userStory)
    }
    
    return userStories
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
    loadProjects()
    loadUsers()
    loadUserStories()
})
</script>

<template>
    <div class="user-story-list">
        <div class="page-header">
            <h1>User Stories</h1>
            <div class="header-buttons">
                <el-button type="success" @click="openBatchImportDialog">
                    <el-icon><Upload /></el-icon>
                    批量导入
                </el-button>
                <el-button type="primary" @click="createUserStory">
                    <el-icon><Plus /></el-icon>
                    创建 User Story
                </el-button>
            </div>
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

        <!-- Batch Import Dialog -->
        <el-dialog
            v-model="batchImportDialogVisible"
            title="批量导入 User Stories"
            width="600px"
            :close-on-click-modal="false"
        >
            <div class="batch-import-content">
                <el-alert
                    title="导入说明"
                    type="info"
                    :closable="false"
                    style="margin-bottom: 20px;"
                >
                    <p>1. 请先下载 Excel 模板</p>
                    <p>2. 按照模板格式填写数据（所属产品、研发需求名称、描述为必填项）</p>
                    <p>3. 上传填写好的 Excel 文件</p>
                    <p>4. 系统将自动创建 User Stories，后续可在列表中逐一优化</p>
                </el-alert>

                <div class="template-download">
                    <el-button type="primary" plain @click="downloadTemplate">
                        <el-icon><Download /></el-icon>
                        下载 Excel 模板
                    </el-button>
                </div>

                <el-divider />

                <div class="file-upload">
                    <input
                        ref="fileInputRef"
                        type="file"
                        accept=".xlsx,.xls"
                        style="display: none"
                        @change="handleFileSelect"
                    />
                    <el-button @click="fileInputRef?.click()">
                        选择文件
                    </el-button>
                    <span v-if="selectedFile" style="margin-left: 10px; color: #67c23a;">
                        {{ selectedFile.name }}
                    </span>
                </div>
            </div>

            <template #footer>
                <el-button @click="batchImportDialogVisible = false">取消</el-button>
                <el-button
                    type="primary"
                    :loading="importLoading"
                    :disabled="!selectedFile"
                    @click="uploadBatchUserStories"
                >
                    开始导入
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>

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

.header-buttons {
    display: flex;
    gap: 10px;
}

.batch-import-content {
    padding: 10px 0;
}

.batch-import-content p {
    margin: 8px 0;
    line-height: 1.6;
}

.template-download {
    margin: 20px 0;
    text-align: center;
}

.file-upload {
    margin: 20px 0;
    display: flex;
    align-items: center;
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