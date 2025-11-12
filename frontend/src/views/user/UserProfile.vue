<template>
  <div class="user-profile">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h2>
            <el-icon><User /></el-icon>
            <span style="margin-left: 10px;">个人信息</span>
          </h2>
        </div>
      </template>

      <el-descriptions :column="1" border size="large" v-loading="loading">
        <el-descriptions-item label="用户名">
          <div class="info-value">
            <el-icon><UserFilled /></el-icon>
            <span>{{ userInfo.username || '-' }}</span>
          </div>
        </el-descriptions-item>
        
        <el-descriptions-item label="邮箱">
          <div class="info-value">
            <el-icon><Message /></el-icon>
            <span>{{ userInfo.email || '-' }}</span>
            <el-button 
              type="primary" 
              link 
              @click="showEditEmailDialog = true"
              style="margin-left: 10px;"
            >
              修改邮箱
            </el-button>
          </div>
        </el-descriptions-item>
        
        <el-descriptions-item label="密码">
          <div class="info-value">
            <el-icon><Lock /></el-icon>
            <span>••••••••</span>
            <el-button 
              type="primary" 
              link 
              @click="goToChangePassword"
              style="margin-left: 10px;"
            >
              修改密码
            </el-button>
          </div>
        </el-descriptions-item>
        
        <el-descriptions-item label="角色">
          <div class="info-value">
            <el-icon><Stamp /></el-icon>
            <el-tag :type="getRoleType(userInfo.role)">
              {{ getRoleLabel(userInfo.role) }}
            </el-tag>
          </div>
        </el-descriptions-item>
        
        <el-descriptions-item label="所在项目">
          <div class="info-value" v-if="projects.length > 0">
            <el-icon><FolderOpened /></el-icon>
            <div class="projects-list">
              <el-tag 
                v-for="project in projects" 
                :key="project.id"
                type="info"
                style="margin: 4px;"
              >
                {{ project.name }}
              </el-tag>
            </div>
          </div>
          <div class="info-value" v-else>
            <el-icon><FolderOpened /></el-icon>
            <span class="text-muted">暂无项目</span>
          </div>
        </el-descriptions-item>
        
        <el-descriptions-item label="创建时间">
          <div class="info-value">
            <el-icon><Calendar /></el-icon>
            <span>{{ formatDate(userInfo.createdAt) }}</span>
          </div>
        </el-descriptions-item>
        
        <el-descriptions-item label="最后更新">
          <div class="info-value">
            <el-icon><Clock /></el-icon>
            <span>{{ formatDate(userInfo.updatedAt) }}</span>
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- Edit Email Dialog -->
    <el-dialog 
      title="修改邮箱" 
      v-model="showEditEmailDialog"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form 
        ref="emailFormRef" 
        :model="emailForm" 
        :rules="emailRules" 
        label-width="100px"
      >
        <el-form-item label="当前邮箱">
          <el-input :value="userInfo.email" disabled />
        </el-form-item>
        
        <el-form-item label="新邮箱" prop="email">
          <el-input 
            v-model="emailForm.email" 
            placeholder="请输入新邮箱地址"
            clearable
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showEditEmailDialog = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="updateEmail" 
          :loading="submitting"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, FormInstance } from 'element-plus';
import { 
  User, 
  UserFilled, 
  Message, 
  Lock, 
  Stamp, 
  FolderOpened, 
  Calendar, 
  Clock 
} from '@element-plus/icons-vue';
import api from '../../api/backend-api';
import userApi from '../../api/user-api';
import type { UserManagementResponse, ProjectResponse } from '../../api/backend-api';
import { userStore as useUserStore } from '../../store/user';

const router = useRouter();
const userStore = useUserStore();

// State
const loading = ref(false);
const submitting = ref(false);
const showEditEmailDialog = ref(false);
const userInfo = ref<UserManagementResponse>({
  id: '',
  username: '',
  email: '',
  role: '',
  projectIds: [],
  createdBy: '',
  createdAt: '',
  updatedBy: '',
  updatedAt: ''
});
const projects = ref<ProjectResponse[]>([]);
const emailFormRef = ref<FormInstance>();

// Email form
const emailForm = reactive({
  email: ''
});

// Validation rules
const emailRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: ['blur', 'change'] }
  ]
};

// Methods
const loadUserInfo = async () => {
  loading.value = true;
  try {
    // Get current user info from backend
    const response = await userApi.getLoggedInUser();
    if (response.status === 200 && response.data) {
      const userData = response.data;
      userInfo.value = {
        id: userData.id,
        username: userData.username,
        email: userData.email,
        role: userData.role,
        projectIds: userData.projectIds || [],
        createdBy: userData.createdBy || '',
        createdAt: userData.createdAt || '',
        updatedBy: userData.updatedBy || '',
        updatedAt: userData.updatedAt || ''
      };
      
      // Load projects if user has project IDs
      if (userInfo.value.projectIds && userInfo.value.projectIds.length > 0) {
        await loadProjects();
      }
    }
  } catch (error: any) {
    console.error('Failed to load user info:', error);
    ElMessage.error('加载用户信息失败');
  } finally {
    loading.value = false;
  }
};

const loadProjects = async () => {
  try {
    // Load all projects and filter by user's project IDs
    const response = await api.getProjects({});
    const allProjects = response.data.data;
    
    // Filter projects that user belongs to
    projects.value = allProjects.filter(project => 
      userInfo.value.projectIds?.includes(project.id)
    );
  } catch (error: any) {
    console.error('Failed to load projects:', error);
  }
};

const updateEmail = async () => {
  if (!emailFormRef.value) return;
  
  await emailFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    submitting.value = true;
    try {
      // Update user email
      await api.updateUser(userInfo.value.id, {
        username: userInfo.value.username,
        email: emailForm.email,
        role: userInfo.value.role,
        projectIds: userInfo.value.projectIds
      });
      
      ElMessage.success('邮箱修改成功');
      
      // Update local state
      userInfo.value.email = emailForm.email;
      userStore.updateProfile(emailForm.email);
      
      showEditEmailDialog.value = false;
      emailForm.email = '';
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '修改邮箱失败');
    } finally {
      submitting.value = false;
    }
  });
};

const goToChangePassword = () => {
  router.push('/change-password');
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString('zh-CN');
};

const getRoleType = (role: string) => {
  const map: Record<string, any> = {
    'OPERATOR': 'danger',
    'ADMIN': 'warning',
    'USER': 'success'
  };
  return map[role] || 'info';
};

const getRoleLabel = (role: string) => {
  const map: Record<string, string> = {
    'OPERATOR': '运维人员',
    'ADMIN': '管理员',
    'USER': '普通用户'
  };
  return map[role] || role;
};

// Lifecycle
onMounted(() => {
  loadUserInfo();
});
</script>

<style scoped>
.user-profile {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.profile-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.info-value {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-value .el-icon {
  color: #409eff;
  font-size: 16px;
}

.projects-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.text-muted {
  color: #909399;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
  color: #606266;
  width: 120px;
}

:deep(.el-descriptions__content) {
  color: #303133;
}

:deep(.el-descriptions-item) {
  padding: 16px 20px;
}
</style>
