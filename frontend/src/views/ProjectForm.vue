<template>
  <div class="project-form-page">
    <div class="form-header">
      <el-page-header @back="goBack">
        <template #content>
          <h1>{{ isEditMode ? '编辑项目' : '创建项目' }}</h1>
        </template>
      </el-page-header>
    </div>

    <el-card class="form-card">
      <el-form 
        ref="projectFormRef" 
        :model="projectForm" 
        :rules="projectRules" 
        label-width="140px"
        label-position="left"
      >
        <!-- 基本信息 -->
        <el-divider content-position="left">
          <el-icon><InfoFilled /></el-icon>
          <span style="margin-left: 8px;">基本信息</span>
        </el-divider>

        <el-form-item label="项目名称" prop="name">
          <el-input 
            v-model="projectForm.name" 
            placeholder="请输入项目名称"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="项目描述" prop="description">
          <el-input 
            v-model="projectForm.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入项目描述"
            clearable
          />
        </el-form-item>

        <el-form-item label="状态" prop="status" v-if="isEditMode">
          <el-select v-model="projectForm.status" placeholder="选择项目状态">
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="归档" value="ARCHIVED" />
            <el-option label="暂停" value="PAUSED" />
          </el-select>
        </el-form-item>

        <!-- Git 仓库配置 -->
        <el-divider content-position="left">
          <el-icon><Connection /></el-icon>
          <span style="margin-left: 8px;">Git 仓库配置</span>
        </el-divider>
        
        <el-form-item label="Git 类型">
          <el-select 
            v-model="projectForm.gitRepository.type" 
            placeholder="选择 Git 类型"
            clearable
          >
            <el-option label="GitLab" value="GITLAB" />
            <el-option label="GitHub" value="GITHUB" />
          </el-select>
        </el-form-item>

        <el-form-item label="Base URL" v-if="projectForm.gitRepository.type">
          <el-input 
            v-model="projectForm.gitRepository.baseUrl" 
            placeholder="例如: https://github.com 或 https://gitlab.com"
            clearable
          />
        </el-form-item>

        <el-form-item label="仓库 ID" v-if="projectForm.gitRepository.type">
          <el-select
            v-model="projectForm.gitRepository.repositoryIds"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入仓库 ID 并按回车添加，支持多个仓库"
            style="width: 100%"
          >
          </el-select>
          <div class="form-item-tip">
            可以添加多个仓库 ID，例如: owner/repo-name 或项目 ID
          </div>
        </el-form-item>

        <el-form-item label="Access Token" v-if="projectForm.gitRepository.type">
          <el-input 
            v-model="projectForm.gitRepository.accessToken" 
            type="password" 
            show-password
            placeholder="Git 访问令牌"
            clearable
          />
          <div class="form-item-tip">
            用于访问 Git 仓库的个人访问令牌（Personal Access Token）
          </div>
        </el-form-item>

        <!-- 项目管理系统配置 -->
        <el-divider content-position="left">
          <el-icon><Tickets /></el-icon>
          <span style="margin-left: 8px;">项目管理系统配置</span>
        </el-divider>

        <el-form-item label="系统类型">
          <el-select 
            v-model="projectForm.projectManagementSystem.type" 
            placeholder="选择项目管理系统类型"
            clearable
          >
            <el-option label="禅道" value="ZENTAO" />
            <el-option label="Trello" value="TRELLO" />
          </el-select>
        </el-form-item>

        <el-form-item label="系统 ID" v-if="projectForm.projectManagementSystem.type">
          <el-input 
            v-model="projectForm.projectManagementSystem.systemId" 
            placeholder="项目 ID 或 Board ID"
            clearable
          />
          <div class="form-item-tip">
            项目在管理系统中的唯一标识符
          </div>
        </el-form-item>

        <el-form-item label="Base URL" v-if="projectForm.projectManagementSystem.type">
          <el-input 
            v-model="projectForm.projectManagementSystem.baseUrl" 
            placeholder="例如: https://zentao.example.com 或 https://trello.com"
            clearable
          />
        </el-form-item>

        <el-form-item label="Access Token" v-if="projectForm.projectManagementSystem.type">
          <el-input 
            v-model="projectForm.projectManagementSystem.accessToken" 
            type="password" 
            show-password
            placeholder="系统访问令牌"
            clearable
          />
          <div class="form-item-tip">
            用于访问项目管理系统 API 的访问令牌
          </div>
        </el-form-item>

        <!-- 团队成员 -->
        <el-divider content-position="left">
          <el-icon><User /></el-icon>
          <span style="margin-left: 8px;">团队成员</span>
        </el-divider>

        <el-form-item label="项目管理员">
          <el-select 
            v-model="projectForm.adminIds" 
            multiple 
            placeholder="选择项目管理员"
            style="width: 100%"
            filterable
          >
            <el-option 
              v-for="user in availableUsers" 
              :key="user.id" 
              :label="`${user.username} (${user.email})`" 
              :value="user.id"
            />
          </el-select>
          <div class="form-item-tip">
            管理员拥有项目的完全管理权限
          </div>
        </el-form-item>

        <el-form-item label="项目成员">
          <el-select 
            v-model="projectForm.memberIds" 
            multiple 
            placeholder="选择项目成员"
            style="width: 100%"
            filterable
          >
            <el-option 
              v-for="user in availableUsers" 
              :key="user.id" 
              :label="`${user.username} (${user.email})`" 
              :value="user.id"
            />
          </el-select>
          <div class="form-item-tip">
            成员可以参与项目协作
          </div>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button @click="goBack" size="large">取消</el-button>
          <el-button 
            type="primary" 
            @click="submitProject" 
            :loading="submitting"
            size="large"
          >
            {{ isEditMode ? '保存修改' : '创建项目' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, FormInstance } from 'element-plus';
import { InfoFilled, Connection, Tickets, User } from '@element-plus/icons-vue';
import api from '../api/backend-api';
import type { 
  CreateProjectRequest, 
  UpdateProjectRequest,
  UserManagementResponse 
} from '../api/backend-api';

const route = useRoute();
const router = useRouter();

// State
const submitting = ref(false);
const availableUsers = ref<UserManagementResponse[]>([]);
const projectFormRef = ref<FormInstance>();

// Check if edit mode
const isEditMode = computed(() => !!route.params.id);
const projectId = computed(() => route.params.id as string);

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
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '描述不能超过 500 个字符', trigger: 'blur' }
  ]
};

// Methods
const loadUsers = async () => {
  try {
    const response = await api.getUsers();
    availableUsers.value = response.data.data;
  } catch (error: any) {
    console.error('Failed to load users:', error);
    ElMessage.error('加载用户列表失败');
  }
};

const loadProject = async () => {
  if (!isEditMode.value) return;
  
  try {
    const response = await api.getProject(projectId.value);
    const project = response.data.data;
    
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
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '加载项目信息失败');
    router.push('/projects');
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

      if (isEditMode.value) {
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

      if (isEditMode.value) {
        await api.updateProject(projectId.value, payload as UpdateProjectRequest);
        ElMessage.success('项目更新成功');
      } else {
        await api.createProject(payload as CreateProjectRequest);
        ElMessage.success('项目创建成功');
      }

      router.push('/projects');
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '操作失败');
    } finally {
      submitting.value = false;
    }
  });
};

const goBack = () => {
  router.push('/projects');
};

// Lifecycle
onMounted(() => {
  loadUsers();
  if (isEditMode.value) {
    loadProject();
  }
});
</script>

<style scoped>
.project-form-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.form-header {
  margin-bottom: 20px;
}

.form-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 500;
}

.form-card {
  margin-top: 20px;
}

.form-item-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  line-height: 1.4;
}

:deep(.el-divider__text) {
  display: flex;
  align-items: center;
  font-weight: 500;
  font-size: 16px;
  color: #303133;
}

:deep(.el-form-item) {
  margin-bottom: 28px;
}

:deep(.el-form-item:last-child) {
  margin-top: 40px;
  margin-bottom: 0;
}

:deep(.el-page-header__content) {
  display: flex;
  align-items: center;
}
</style>
