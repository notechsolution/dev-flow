<template>
  <div class="project-management">
    <div class="header-section">
      <h1>项目管理</h1>
      <el-button 
        v-if="canCreateProject" 
        type="primary" 
        @click="createProject"
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
        <el-form-item label="状态" style="width: 150px;">
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
      <el-table-column label="操作" width="220" fixed="right">
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
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import api from '../api/backend-api';
import type { 
  ProjectResponse, 
  UserManagementResponse 
} from '../api/backend-api';
import { userStore as useUserStore } from '../store/user';

const userStore = useUserStore();
const router = useRouter();

// State
const loading = ref(false);
const showViewDialog = ref(false);
const projects = ref<ProjectResponse[]>([]);
const currentProject = ref<ProjectResponse | null>(null);

// Filters
const filters = reactive({
  name: '',
  status: 'ACTIVE' 
});

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

const resetFilters = () => {
  filters.name = '';
  filters.status = '';
  loadProjects();
};

const createProject = () => {
  router.push('/projects/new');
};

const viewProject = (project: ProjectResponse) => {
  currentProject.value = project;
  showViewDialog.value = true;
};

const editProject = (project: ProjectResponse) => {
  router.push(`/projects/edit/${project.id}`);
};

const editFromView = () => {
  showViewDialog.value = false;
  if (currentProject.value) {
    router.push(`/projects/edit/${currentProject.value.id}`);
  }
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
