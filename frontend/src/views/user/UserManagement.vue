<template>
    <div class="user-management">
        <div class="page-header">
            <h1>用户管理</h1>
            <el-button type="primary" @click="showCreateDialog">
                <el-icon><Plus /></el-icon>
                创建用户
            </el-button>
        </div>

        <!-- Filters -->
        <el-card class="filter-section">
            <el-form :inline="true" :model="filters">
                <el-form-item label="角色">
                    <el-select
                        v-model="filters.role"
                        placeholder="所有角色"
                        clearable
                        style="width: 150px"
                        @change="loadUsers"
                    >
                        <el-option
                            v-for="role in roleOptions"
                            :key="role.value"
                            :label="role.label"
                            :value="role.value"
                        />
                    </el-select>
                </el-form-item>

                <el-form-item label="项目">
                    <el-select
                        v-model="filters.projectId"
                        placeholder="所有项目"
                        clearable
                        style="width: 200px"
                        @change="loadUsers"
                    >
                        <el-option
                            v-for="project in projects"
                            :key="project.id"
                            :label="project.name"
                            :value="project.id"
                        />
                    </el-select>
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="loadUsers">
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
                :data="users"
                stripe
                style="width: 100%"
            >
                <el-table-column prop="id" label="用户 ID" width="120">
                    <template #default="{ row }">
                        <el-tag size="small">{{ row.id.substring(0, 8) }}</el-tag>
                    </template>
                </el-table-column>

                <el-table-column prop="username" label="用户名" min-width="150" />

                <el-table-column prop="email" label="邮箱" min-width="200" />

                <el-table-column prop="role" label="角色" width="120">
                    <template #default="{ row }">
                        <el-tag :type="getRoleType(row.role)">
                            {{ getRoleLabel(row.role) }}
                        </el-tag>
                    </template>
                </el-table-column>

                <el-table-column prop="projectIds" label="项目" min-width="200">
                    <template #default="{ row }">
                        <template v-if="row.role === 'OPERATOR'">
                            <el-tag size="small" type="success">全部项目</el-tag>
                        </template>
                        <template v-else-if="row.projectIds && row.projectIds.length > 0">
                            <el-tag
                                v-for="(projectId, index) in row.projectIds.slice(0, 2)"
                                :key="projectId"
                                size="small"
                                style="margin-right: 5px"
                            >
                                {{ getProjectName(projectId) }}
                            </el-tag>
                            <el-tag v-if="row.projectIds.length > 2" size="small" type="info">
                                +{{ row.projectIds.length - 2 }}
                            </el-tag>
                        </template>
                        <template v-else>
                            <span class="text-muted">-</span>
                        </template>
                    </template>
                </el-table-column>

                <el-table-column prop="createdAt" label="创建时间" width="180">
                    <template #default="{ row }">
                        {{ formatDate(row.createdAt) }}
                    </template>
                </el-table-column>

                <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            size="small"
                            type="primary"
                            link
                            @click="showEditDialog(row)"
                        >
                            编辑
                        </el-button>
                        <el-button
                            size="small"
                            type="danger"
                            link
                            @click="deleteUser(row)"
                            :disabled="row.role === 'OPERATOR' && currentUserRole !== 'OPERATOR'"
                        >
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <!-- Create/Edit User Dialog -->
        <el-dialog
            v-model="dialogVisible"
            :title="dialogMode === 'create' ? '创建用户' : '编辑用户'"
            width="600px"
        >
            <el-form
                ref="userFormRef"
                :model="userForm"
                :rules="userFormRules"
                label-width="100px"
            >
                <el-form-item label="用户名" prop="username">
                    <el-input v-model="userForm.username" placeholder="请输入用户名" />
                </el-form-item>

                <el-form-item label="邮箱" prop="email">
                    <el-input v-model="userForm.email" placeholder="请输入邮箱" />
                </el-form-item>

                <el-form-item label="密码" prop="password">
                    <el-input
                        v-model="userForm.password"
                        type="password"
                        :placeholder="dialogMode === 'create' ? '请输入密码' : '留空则不修改密码'"
                        show-password
                    />
                </el-form-item>

                <el-form-item label="角色" prop="role">
                    <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%">
                        <el-option
                            v-for="role in roleOptions"
                            :key="role.value"
                            :label="role.label"
                            :value="role.value"
                        />
                    </el-select>
                </el-form-item>

                <el-form-item label="项目" prop="projectIds" v-if="userForm.role !== 'OPERATOR'">
                    <el-select
                        v-model="userForm.projectIds"
                        multiple
                        placeholder="请选择项目"
                        style="width: 100%"
                    >
                        <el-option
                            v-for="project in projects"
                            :key="project.id"
                            :label="project.name"
                            :value="project.id"
                        />
                    </el-select>
                </el-form-item>
            </el-form>

            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSubmit" :loading="submitting">
                    确定
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import aiApi, { UserManagementResponse, CreateUserRequest, UpdateUserRequest } from '@/api/backend-api'

// State
const loading = ref(false)
const users = ref<UserManagementResponse[]>([])
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const submitting = ref(false)
const userFormRef = ref<FormInstance>()
const currentUserRole = ref('OPERATOR') // TODO: Get from auth context

const filters = reactive({
    role: '',
    projectId: ''
})

const userForm = reactive({
    id: '',
    username: '',
    email: '',
    password: '',
    role: 'USER',
    projectIds: [] as string[]
})

// Mock data - replace with actual API calls
const projects = ref([
    { id: '1', name: 'Project Alpha' },
    { id: '2', name: 'Project Beta' },
    { id: '3', name: 'Project Gamma' }
])

const roleOptions = [
    { label: '操作员', value: 'OPERATOR' },
    { label: '管理员', value: 'ADMIN' },
    { label: '普通用户', value: 'USER' }
]

// Form validation rules
const userFormRules: FormRules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' }
    ],
    email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
    ],
    password: [
        { required: dialogMode.value === 'create', message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少 6 个字符', trigger: 'blur' }
    ],
    role: [
        { required: true, message: '请选择角色', trigger: 'change' }
    ]
}

// Methods
const loadUsers = async () => {
    loading.value = true
    try {
        const params: any = {}
        if (filters.role) params.role = filters.role
        if (filters.projectId) params.projectId = filters.projectId

        const response = await aiApi.getUsers(params)
        if (response.data.success) {
            users.value = response.data.data
        } else {
            ElMessage.error('加载用户列表失败')
        }
    } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '加载用户列表失败')
        console.error('Load error:', error)
    } finally {
        loading.value = false
    }
}

const resetFilters = () => {
    filters.role = ''
    filters.projectId = ''
    loadUsers()
}

const showCreateDialog = () => {
    dialogMode.value = 'create'
    resetForm()
    dialogVisible.value = true
}

const showEditDialog = (user: UserManagementResponse) => {
    dialogMode.value = 'edit'
    userForm.id = user.id
    userForm.username = user.username
    userForm.email = user.email
    userForm.password = ''
    userForm.role = user.role
    userForm.projectIds = user.projectIds || []
    dialogVisible.value = true
}

const resetForm = () => {
    userForm.id = ''
    userForm.username = ''
    userForm.email = ''
    userForm.password = ''
    userForm.role = 'USER'
    userForm.projectIds = []
    userFormRef.value?.clearValidate()
}

const handleSubmit = async () => {
    if (!userFormRef.value) return

    await userFormRef.value.validate(async (valid) => {
        if (!valid) return

        submitting.value = true
        try {
            if (dialogMode.value === 'create') {
                const request: CreateUserRequest = {
                    username: userForm.username,
                    email: userForm.email,
                    password: userForm.password,
                    role: userForm.role,
                    projectIds: userForm.role === 'OPERATOR' ? [] : userForm.projectIds
                }
                await aiApi.createUser(request)
                ElMessage.success('创建用户成功')
            } else {
                const request: UpdateUserRequest = {
                    username: userForm.username,
                    email: userForm.email,
                    password: userForm.password || undefined,
                    role: userForm.role,
                    projectIds: userForm.role === 'OPERATOR' ? [] : userForm.projectIds
                }
                await aiApi.updateUser(userForm.id, request)
                ElMessage.success('更新用户成功')
            }

            dialogVisible.value = false
            loadUsers()
        } catch (error: any) {
            ElMessage.error(error.response?.data?.message || '操作失败')
            console.error('Submit error:', error)
        } finally {
            submitting.value = false
        }
    })
}

const deleteUser = async (user: UserManagementResponse) => {
    try {
        await ElMessageBox.confirm(
            `确定要删除用户 "${user.username}" 吗？`,
            '确认删除',
            {
                confirmButtonText: '删除',
                cancelButtonText: '取消',
                type: 'warning'
            }
        )

        await aiApi.deleteUser(user.id)
        ElMessage.success('删除成功')
        loadUsers()
    } catch (error: any) {
        if (error !== 'cancel') {
            ElMessage.error(error.response?.data?.message || '删除失败')
            console.error('Delete error:', error)
        }
    }
}

// Helper methods
const getProjectName = (projectId: string) => {
    return projects.value.find(p => p.id === projectId)?.name || projectId
}

const getRoleType = (role: string) => {
    const types: Record<string, any> = {
        'OPERATOR': 'danger',
        'ADMIN': 'warning',
        'USER': 'info'
    }
    return types[role] || 'info'
}

const getRoleLabel = (role: string) => {
    const labels: Record<string, string> = {
        'OPERATOR': '操作员',
        'ADMIN': '管理员',
        'USER': '普通用户'
    }
    return labels[role] || role
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
    loadUsers()
})
</script>

<style scoped>
.user-management {
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

.text-muted {
    color: #909399;
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

@media (max-width: 768px) {
    .user-management {
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
