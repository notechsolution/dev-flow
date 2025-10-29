<template>
  <div class="project-management">
    <div class="header-section">
      <h1>项目管理</h1>
      <el-button 
        v-if="canCreateProject" 
        type="primary" 
        @click="showCreateDialog = true"
      >
        <el-icon><Plus /></el-icon>
        创建项目
      </el-button>
    </div>

    <!-- Filters -->
    <div class="filter-section">
      <el-form :inline="true">
        <el-form-item label="项目名称">
          <el-input 
            v-model="filters.name" 
            placeholder="搜索项目名称" 
            clearable
            @change="loadProjects"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="filters.status" 
            placeholder="选择状态" 
            clearable
            @change="loadProjects"
          >
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="归档" value="ARCHIVED" />
            <el-option label="暂停" value="PAUSED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadProjects">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Project List Table -->
    <el-table 
      :data="projects" 
      v-loading="loading" 
      style="width: 100%"
      stripe
    >
      <el-table-column prop="name" label="项目名称" min-width="150" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Git仓库" min-width="120">
        <template #default="scope">
          <span v-if="scope.row.gitRepository?.type">
            {{ scope.row.gitRepository.type }}
          </span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="项目管理系统" min-width="140">
        <template #default="scope">
          <span v-if="scope.row.projectManagementSystem?.type">
            {{ scope.row.projectManagementSystem.type }}
          </span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="成员数" width="100">
        <template #default="scope">
          {{ (scope.row.adminIds?.length || 0) + (scope.row.memberIds?.length || 0) }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="160">
        <template #default="scope">
          {{ formatDate(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button 
            type="primary" 
            size="small" 
            @click="viewProject(scope.row)"
          >
            查看
          </el-button>
          <el-button 
            v-if="canModifyProject(scope.row)" 
            type="warning" 
            size="small" 
            @click="editProject(scope.row)"
          >
            编辑
          </el-button>
          <el-button 
            v-if="canDeleteProject" 
            type="danger" 
            size="small" 
            @click="deleteProject(scope.row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Create/Edit Dialog -->
    <el-dialog 
      :title="dialogMode === 'create' ? '创建项目' : '编辑项目'" 
      v-model="showCreateDialog"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form 
        ref="projectFormRef" 
        :model="projectForm" 
        :rules="projectRules" 
        label-width="120px"
      >
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="projectForm.name" placeholder="请输入项目名称" />
        </el-form-item>
        
        <el-form-item label="项目描述" prop="description">
          <el-input 
            v-model="projectForm.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入项目描述"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status" v-if="dialogMode === 'edit'">
          <el-select v-model="projectForm.status" placeholder="选择项目状态">
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="归档" value="ARCHIVED" />
            <el-option label="暂停" value="PAUSED" />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">Git 仓库配置</el-divider>
        
        <el-form-item label="Git类型">
          <el-select 
            v-model="projectForm.gitRepository.type" 
            placeholder="选择Git类型"
            clearable
          >
            <el-option label="GitHub" value="GITHUB" />
            <el-option label="GitLab" value="GITLAB" />
            <el-option label="Bitbucket" value="BITBUCKET" />
            <el-option label="Azure DevOps" value="AZURE_DEVOPS" />
          </el-select>
        </el-form-item>

        <el-form-item label="Base URL">
          <el-input 
            v-model="projectForm.gitRepository.baseUrl" 
            placeholder="例如: https://github.com"
          />
        </el-form-item>

        <el-form-item label="仓库ID">
          <el-select
            v-model="projectForm.gitRepository.repositoryIds"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入仓库ID并按回车添加"
            style="width: 100%"
          >
          </el-select>
        </el-form-item>

        <el-form-item label="Access Token">
          <el-input 
            v-model="projectForm.gitRepository.accessToken" 
            type="password" 
            show-password
            placeholder="Git访问令牌"
          />
        </el-form-item>

        <el-divider content-position="left">项目管理系统配置</el-divider>

        <el-form-item label="系统类型">
          <el-select 
            v-model="projectForm.projectManagementSystem.type" 
            placeholder="选择项目管理系统"
            clearable
          >
            <el-option label="Jira" value="JIRA" />
            <el-option label="Azure DevOps" value="AZURE_DEVOPS" />
            <el-option label="GitHub Issues" value="GITHUB_ISSUES" />
            <el-option label="Trello" value="TRELLO" />
          </el-select>
        </el-form-item>

        <el-form-item label="系统ID">
          <el-input 
            v-model="projectForm.projectManagementSystem.systemId" 
            placeholder="项目或Board ID"
          />
        </el-form-item>

        <el-form-item label="Base URL">
          <el-input 
            v-model="projectForm.projectManagementSystem.baseUrl" 
            placeholder="例如: https://yourcompany.atlassian.net"
          />
        </el-form-item>

        <el-form-item label="Access Token">
          <el-input 
            v-model="projectForm.projectManagementSystem.accessToken" 
            type="password" 
            show-password
            placeholder="系统访问令牌"
          />
        </el-form-item>

        <el-divider content-position="left">团队成员</el-divider>

        <el-form-item label="管理员">
          <el-select 
            v-model="projectForm.adminIds" 
            multiple 
            placeholder="选择项目管理员"
            style="width: 100%"
          >
            <el-option 
              v-for="user in availableUsers" 
              :key="user.id" 
              :label="user.username" 
              :value="user.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="成员">
          <el-select 
            v-model="projectForm.memberIds" 
            multiple 
            placeholder="选择项目成员"
            style="width: 100%"
          >
            <el-option 
              v-for="user in availableUsers" 
              :key="user.id" 
              :label="user.username" 
              :value="user.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitProject" :loading="submitting">
          {{ dialogMode === 'create' ? '创建' : '保存' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- View Project Dialog -->
    <el-dialog 
      title="项目详情" 
      v-model="showViewDialog"
      width="700px"
    >
      <el-descriptions :column="2" border v-if="currentProject">
        <el-descriptions-item label="项目名称">{{ currentProject.name }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentProject.status)">
            {{ getStatusLabel(currentProject.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentProject.description || '-' }}</el-descriptions-item>
        
        <el-descriptions-item label="Git类型">
          {{ currentProject.gitRepository?.type || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="Git Base URL">
          {{ currentProject.gitRepository?.baseUrl || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="仓库ID" :span="2">
          <el-tag 
            v-for="repoId in currentProject.gitRepository?.repositoryIds" 
            :key="repoId" 
            style="margin-right: 5px"
          >
            {{ repoId }}
          </el-tag>
          <span v-if="!currentProject.gitRepository?.repositoryIds?.length">-</span>
        </el-descriptions-item>
        
        <el-descriptions-item label="项目管理系统">
          {{ currentProject.projectManagementSystem?.type || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="系统ID">
          {{ currentProject.projectManagementSystem?.systemId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="系统URL" :span="2">
          {{ currentProject.projectManagementSystem?.baseUrl || '-' }}
        </el-descriptions-item>
        
        <el-descriptions-item label="管理员数量">
          {{ currentProject.adminIds?.length || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="成员数量">
          {{ currentProject.memberIds?.length || 0 }}
        </el-descriptions-item>
        
        <el-descriptions-item label="创建时间">
          {{ formatDate(currentProject.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ formatDate(currentProject.updatedAt) }}
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="showViewDialog = false">关闭</el-button>
        <el-button 
          v-if="currentProject && canModifyProject(currentProject)" 
          type="primary" 
          @click="editFromView"
        >
          编辑
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import api from '../api/backend-api';
import type { 
  ProjectResponse, 
  CreateProjectRequest, 
  UpdateProjectRequest,
  UserManagementResponse 
} from '../api/backend-api';
import { userStore as useUserStore } from '../store/user';

const userStore = useUserStore();

// State
const loading = ref(false);
const submitting = ref(false);
const showCreateDialog = ref(false);
const showViewDialog = ref(false);
const dialogMode = ref<'create' | 'edit'>('create');
const projects = ref<ProjectResponse[]>([]);
const currentProject = ref<ProjectResponse | null>(null);
const availableUsers = ref<UserManagementResponse[]>([]);
const projectFormRef = ref<FormInstance>();

// Filters
const filters = reactive({
  name: '',
  status: ''
});

// Form
const defaultProjectForm = {
  name: '',
  description: '',
  status: 'ACTIVE',
  adminIds: [] as string[],
  memberIds: [] as string[],
  gitRepository: {
    type: '',
    baseUrl: '',
    repositoryIds: [] as string[],
    accessToken: ''
  },
  projectManagementSystem: {
    type: '',
    systemId: '',
    baseUrl: '',
    accessToken: ''
  }
};

const projectForm = reactive({ ...defaultProjectForm });

// Validation rules
const projectRules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' }
  ]
};

// Computed
const canCreateProject = computed(() => {
  return userStore.role === 'OPERATOR';
});

const canDeleteProject = computed(() => {
  return userStore.role === 'OPERATOR';
});

const canModifyProject = (project: ProjectResponse) => {
  if (!userStore.username) return false;
  if (userStore.role === 'OPERATOR') return true;
  // Check if current user is in project's admin list
  // Note: This is a simplified check. In production, you'd need the user's ID
  return false; // For now, only OPERATOR can modify
};

// Methods
const loadProjects = async () => {
  loading.value = true;
  try {
    const response = await api.getProjects({
      name: filters.name || undefined,
      status: filters.status || undefined
    });
    projects.value = response.data.data;
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '加载项目列表失败');
  } finally {
    loading.value = false;
  }
};

const loadUsers = async () => {
  try {
    const response = await api.getUsers();
    availableUsers.value = response.data.data;
  } catch (error: any) {
    console.error('Failed to load users:', error);
  }
};

const resetFilters = () => {
  filters.name = '';
  filters.status = '';
  loadProjects();
};

const viewProject = (project: ProjectResponse) => {
  currentProject.value = project;
  showViewDialog.value = true;
};

const editProject = (project: ProjectResponse) => {
  dialogMode.value = 'edit';
  currentProject.value = project;
  
  Object.assign(projectForm, {
    name: project.name,
    description: project.description || '',
    status: project.status,
    adminIds: project.adminIds || [],
    memberIds: project.memberIds || [],
    gitRepository: {
      type: project.gitRepository?.type || '',
      baseUrl: project.gitRepository?.baseUrl || '',
      repositoryIds: project.gitRepository?.repositoryIds || [],
      accessToken: '' // Don't populate token for security
    },
    projectManagementSystem: {
      type: project.projectManagementSystem?.type || '',
      systemId: project.projectManagementSystem?.systemId || '',
      baseUrl: project.projectManagementSystem?.baseUrl || '',
      accessToken: '' // Don't populate token for security
    }
  });
  
  showCreateDialog.value = true;
};

const editFromView = () => {
  showViewDialog.value = false;
  if (currentProject.value) {
    editProject(currentProject.value);
  }
};

const submitProject = async () => {
  if (!projectFormRef.value) return;
  
  await projectFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    submitting.value = true;
    try {
      const payload: CreateProjectRequest | UpdateProjectRequest = {
        name: projectForm.name,
        description: projectForm.description || undefined,
        adminIds: projectForm.adminIds.length > 0 ? projectForm.adminIds : undefined,
        memberIds: projectForm.memberIds.length > 0 ? projectForm.memberIds : undefined
      };

      if (dialogMode.value === 'edit') {
        (payload as UpdateProjectRequest).status = projectForm.status;
      }

      // Add Git Repository if configured
      if (projectForm.gitRepository.type) {
        payload.gitRepository = {
          type: projectForm.gitRepository.type,
          baseUrl: projectForm.gitRepository.baseUrl || undefined,
          repositoryIds: projectForm.gitRepository.repositoryIds.length > 0 
            ? projectForm.gitRepository.repositoryIds 
            : undefined,
          accessToken: projectForm.gitRepository.accessToken || undefined
        };
      }

      // Add Project Management System if configured
      if (projectForm.projectManagementSystem.type) {
        payload.projectManagementSystem = {
          type: projectForm.projectManagementSystem.type,
          systemId: projectForm.projectManagementSystem.systemId || undefined,
          baseUrl: projectForm.projectManagementSystem.baseUrl || undefined,
          accessToken: projectForm.projectManagementSystem.accessToken || undefined
        };
      }

      if (dialogMode.value === 'create') {
        await api.createProject(payload as CreateProjectRequest);
        ElMessage.success('项目创建成功');
      } else {
        await api.updateProject(currentProject.value!.id, payload as UpdateProjectRequest);
        ElMessage.success('项目更新成功');
      }

      showCreateDialog.value = false;
      resetForm();
      loadProjects();
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '操作失败');
    } finally {
      submitting.value = false;
    }
  });
};

const deleteProject = (project: ProjectResponse) => {
  ElMessageBox.confirm(
    `确定要删除项目 "${project.name}" 吗？此操作不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await api.deleteProject(project.id);
      ElMessage.success('项目删除成功');
      loadProjects();
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '删除失败');
    }
  }).catch(() => {
    // User cancelled
  });
};

const resetForm = () => {
  Object.assign(projectForm, defaultProjectForm);
  projectFormRef.value?.resetFields();
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString('zh-CN');
};

const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    'ACTIVE': 'success',
    'ARCHIVED': 'info',
    'PAUSED': 'warning'
  };
  return map[status] || 'info';
};

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    'ACTIVE': '活跃',
    'ARCHIVED': '归档',
    'PAUSED': '暂停'
  };
  return map[status] || status;
};

// Lifecycle
onMounted(() => {
  loadProjects();
  loadUsers();
});
</script>

<style scoped>
.project-management {
  padding: 20px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-section h1 {
  margin: 0;
  font-size: 24px;
}

.filter-section {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.text-muted {
  color: #909399;
}
</style>
