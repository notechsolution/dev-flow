<template>
  <div class="ai-provider-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="title">AI提供商管理</span>
          <div class="actions">
            <el-button 
              type="primary" 
              :icon="Plus" 
              @click="showCreateDialog"
            >
              添加提供商
            </el-button>
            <el-button 
              type="info" 
              :icon="Refresh" 
              @click="initializeDefaults"
              :loading="initializingDefaults"
            >
              初始化默认配置
            </el-button>
          </div>
        </div>
      </template>

      <!-- Provider List -->
      <el-table 
        v-loading="loading" 
        :data="providers" 
        style="width: 100%"
        :default-sort="{ prop: 'provider', order: 'ascending' }"
      >
        <el-table-column prop="provider" label="提供商" width="120">
          <template #default="scope">
            <el-tag :type="getProviderTagType(scope.row.provider)">
              {{ scope.row.provider }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="displayName" label="显示名称" width="150" />

        <el-table-column prop="description" label="描述" min-width="200" />

        <el-table-column prop="baseUrl" label="API地址" min-width="200">
          <template #default="scope">
            <el-text size="small" truncated>
              {{ scope.row.baseUrl || '-' }}
            </el-text>
          </template>
        </el-table-column>

        <el-table-column prop="models" label="可用模型" width="150">
          <template #default="scope">
            <el-tag size="small" type="info">
              {{ scope.row.models.length }} 个模型
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="defaultModel" label="默认模型" width="150">
          <template #default="scope">
            <el-text size="small" type="primary">
              {{ scope.row.defaultModel }}
            </el-text>
          </template>
        </el-table-column>

        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.enabled"
              @change="toggleProvider(scope.row)"
              :loading="scope.row.toggling"
            />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              :icon="Edit"
              @click="editProvider(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              :icon="Delete"
              @click="deleteProvider(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '添加AI提供商' : '编辑AI提供商'"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="提供商代码" prop="provider">
          <el-input
            v-model="formData.provider"
            placeholder="例如: dashscope, ollama, openai"
            :disabled="dialogMode === 'edit'"
          />
        </el-form-item>

        <el-form-item label="显示名称" prop="displayName">
          <el-input
            v-model="formData.displayName"
            placeholder="例如: 阿里云DashScope"
          />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="2"
            placeholder="AI提供商的简短描述"
          />
        </el-form-item>

        <el-form-item label="API地址" prop="baseUrl">
          <el-input
            v-model="formData.baseUrl"
            placeholder="例如: https://dashscope.aliyuncs.com/api/v1"
          />
        </el-form-item>

        <el-form-item label="API密钥" prop="apiKey">
          <el-input
            v-model="formData.apiKey"
            type="password"
            show-password
            placeholder="请输入API密钥"
          />
          <el-text size="small" type="info">
            密钥将被加密存储，留空表示不修改
          </el-text>
        </el-form-item>

        <el-form-item label="启用状态" prop="enabled">
          <el-switch v-model="formData.enabled" />
        </el-form-item>

        <el-divider />

        <el-form-item label="模型配置">
          <div class="models-config">
            <div 
              v-for="(model, index) in formData.models" 
              :key="index" 
              class="model-item"
            >
              <el-input
                v-model="model.modelId"
                placeholder="模型代码"
                style="width: 30%"
              />
              <el-input
                v-model="model.modelName"
                placeholder="显示名称"
                style="width: 30%"
              />
              <el-input
                v-model="model.description"
                placeholder="描述（可选）"
                style="width: 30%"
              />
              <el-button
                type="danger"
                :icon="Delete"
                circle
                size="small"
                @click="removeModel(index)"
              />
            </div>
            <el-button
              type="primary"
              :icon="Plus"
              size="small"
              @click="addModel"
            >
              添加模型
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="默认模型" prop="defaultModel">
          <el-select
            v-model="formData.defaultModel"
            placeholder="选择默认模型"
            style="width: 100%"
          >
            <el-option
              v-for="model in formData.models"
              :key="model.modelId"
              :label="model.modelId + '(' + model.modelName + ')'"
              :value="model.modelId"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="submitForm"
          :loading="submitting"
        >
          {{ dialogMode === 'create' ? '创建' : '更新' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus';
import { Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue';
import { 
  AIProviderConfig, 
  ModelConfig,
  AIProviderConfigRequest
} from '@/api/backend-api';
import backendApi from '@/api/backend-api';

// State
const loading = ref(false);
const providers = ref<AIProviderConfig[]>([]);
const dialogVisible = ref(false);
const dialogMode = ref<'create' | 'edit'>('create');
const submitting = ref(false);
const initializingDefaults = ref(false);
const formRef = ref<FormInstance>();

// Form data
const formData = reactive<AIProviderConfigRequest>({
  provider: '',
  displayName: '',
  description: '',
  enabled: true,
  apiKey: '',
  baseUrl: '',
  models: [],
  defaultModel: ''
});

const currentEditId = ref<string>('');

// Form validation rules
const formRules: FormRules = {
  provider: [
    { required: true, message: '请输入提供商代码', trigger: 'blur' }
  ],
  displayName: [
    { required: true, message: '请输入显示名称', trigger: 'blur' }
  ],
  defaultModel: [
    { required: true, message: '请选择默认模型', trigger: 'change' }
  ]
};

// Methods
const loadProviders = async () => {
  loading.value = true;
  try {
    const response = await backendApi.getAllAIProviders();
    if (response.data.success) {
      providers.value = response.data.data.map(p => ({
        ...p,
        toggling: false
      }));
    }
  } catch (error: any) {
    ElMessage.error('加载AI提供商列表失败: ' + error.message);
  } finally {
    loading.value = false;
  }
};

const getProviderTagType = (provider: string) => {
  const typeMap: Record<string, any> = {
    'dashscope': 'success',
    'ollama': 'warning',
    'openai': 'primary'
  };
  return typeMap[provider.toLowerCase()] || 'info';
};

const showCreateDialog = () => {
  dialogMode.value = 'create';
  resetForm();
  dialogVisible.value = true;
};

const editProvider = (provider: AIProviderConfig) => {
  dialogMode.value = 'edit';
  currentEditId.value = provider.id!;
  
  // Populate form data
  formData.provider = provider.provider;
  formData.displayName = provider.displayName;
  formData.description = provider.description || '';
  formData.enabled = provider.enabled;
  formData.apiKey = ''; // Don't show existing API key
  formData.baseUrl = provider.baseUrl || '';
  formData.models = JSON.parse(JSON.stringify(provider.models));
  formData.defaultModel = provider.defaultModel;
  
  dialogVisible.value = true;
};

const resetForm = () => {
  formData.provider = '';
  formData.displayName = '';
  formData.description = '';
  formData.enabled = true;
  formData.apiKey = '';
  formData.baseUrl = '';
  formData.models = [];
  formData.defaultModel = '';
  currentEditId.value = '';
  formRef.value?.clearValidate();
};

const addModel = () => {
  formData.models.push({
    name: '',
    displayName: '',
    description: ''
  });
};

const removeModel = (index: number) => {
  formData.models.splice(index, 1);
};

const submitForm = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    
    submitting.value = true;
    try {
      const request: AIProviderConfigRequest = {
        provider: formData.provider,
        displayName: formData.displayName,
        description: formData.description,
        enabled: formData.enabled,
        baseUrl: formData.baseUrl,
        models: formData.models,
        defaultModel: formData.defaultModel
      };
      
      // Only include apiKey if it's not empty
      if (formData.apiKey) {
        request.apiKey = formData.apiKey;
      }
      
      if (dialogMode.value === 'create') {
        const response = await backendApi.createAIProvider(request);
        if (response.data.success) {
          ElMessage.success('创建成功');
          dialogVisible.value = false;
          await loadProviders();
        }
      } else {
        const response = await backendApi.updateAIProvider(currentEditId.value, request);
        if (response.data.success) {
          ElMessage.success('更新成功');
          dialogVisible.value = false;
          await loadProviders();
        }
      }
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '操作失败');
    } finally {
      submitting.value = false;
    }
  });
};

const toggleProvider = async (provider: any) => {
  provider.toggling = true;
  try {
    const response = await backendApi.toggleAIProvider(provider.id, provider.enabled);
    if (response.data.success) {
      ElMessage.success(response.data.message);
    }
  } catch (error: any) {
    // Revert on error
    provider.enabled = !provider.enabled;
    ElMessage.error('操作失败: ' + error.message);
  } finally {
    provider.toggling = false;
  }
};

const deleteProvider = async (provider: AIProviderConfig) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除提供商 "${provider.displayName}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    const response = await backendApi.deleteAIProvider(provider.id!);
    if (response.data.success) {
      ElMessage.success('删除成功');
      await loadProviders();
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message);
    }
  }
};

const initializeDefaults = async () => {
  try {
    await ElMessageBox.confirm(
      '这将初始化三个默认的AI提供商配置（DashScope、Ollama、OpenAI）。如果已存在，将不会重复创建。',
      '确认初始化',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    );
    
    initializingDefaults.value = true;
    const response = await backendApi.initializeDefaultAIProviders();
    if (response.data.success) {
      ElMessage.success(response.data.message);
      await loadProviders();
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('初始化失败: ' + error.message);
    }
  } finally {
    initializingDefaults.value = false;
  }
};

// Lifecycle
onMounted(() => {
  loadProviders();
});
</script>

<style scoped>
.ai-provider-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  font-size: 18px;
  font-weight: bold;
}

.card-header .actions {
  display: flex;
  gap: 10px;
}

.models-config {
  width: 100%;
}

.model-item {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  align-items: center;
}
</style>
