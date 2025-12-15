<template>
  <div class="ai-provider-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="title">AI提供商管理</span>
          <div class="actions">
            <el-button 
              type="info" 
              :icon="Refresh" 
              @click="loadProviders"
            >
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-alert
        title="配置说明"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        AI提供商（名称、API密钥、API地址）从 application.yml 读取，在应用启动时注入。可以编辑每个提供商的模型列表和默认模型。
      </el-alert>

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

        <el-table-column prop="baseUrl" label="API地址（只读）" min-width="250">
          <template #default="scope">
            <el-text size="small" truncated>
              <el-icon><Lock /></el-icon>
              {{ scope.row.baseUrl || '-' }}
            </el-text>
          </template>
        </el-table-column>

        <el-table-column prop="models" label="可用模型" width="120">
          <template #default="scope">
            <el-popover
              placement="top"
              :width="300"
              trigger="hover"
            >
              <template #reference>
                <el-tag size="small" type="info" style="cursor: pointer">
                  {{ scope.row.models.length }} 个模型
                </el-tag>
              </template>
              <div>
                <div v-for="model in scope.row.models" :key="model.modelId" style="margin-bottom: 8px">
                  <el-text><strong>{{ model.modelId }}</strong></el-text><br/>
                  <el-text size="small" type="info">{{ model.modelName }}</el-text>
                </div>
              </div>
            </el-popover>
          </template>
        </el-table-column>

        <el-table-column prop="defaultModel" label="默认模型" width="150">
          <template #default="scope">
            <el-text size="small" type="primary">
              {{ scope.row.defaultModel }}
            </el-text>
          </template>
        </el-table-column>

        <el-table-column prop="enabled" label="启用状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.enabled"
              @change="toggleProvider(scope.row)"
              :loading="scope.row.toggling"
            />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              :icon="Edit"
              @click="editProviderModels(scope.row)"
            >
              编辑模型
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Edit Models Dialog -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑AI提供商模型"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-alert
        title="提示"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        只能编辑模型列表和默认模型，提供商名称、API地址等其他配置为只读（从application.yml读取）。
      </el-alert>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="提供商">
          <el-input :value="formData.provider" disabled />
        </el-form-item>

        <el-form-item label="显示名称">
          <el-input :value="formData.displayName" disabled />
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
                placeholder="模型ID"
                style="width: 28%"
              />
              <el-input
                v-model="model.modelName"
                placeholder="显示名称"
                style="width: 28%"
              />
              <el-input
                v-model="model.description"
                placeholder="描述（可选）"
                style="width: 28%"
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
              :label="`${model.modelId} (${model.modelName})`"
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
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, FormInstance, FormRules } from 'element-plus';
import { Refresh, Lock, Edit, Plus, Delete } from '@element-plus/icons-vue';
import { AIProviderConfig, AIProviderConfigRequest } from '@/api/backend-api';
import backendApi from '@/api/backend-api';

// State
const loading = ref(false);
const providers = ref<AIProviderConfig[]>([]);
const dialogVisible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const currentEditId = ref<string>('');

// Form data
const formData = reactive<AIProviderConfigRequest>({
  provider: '',
  displayName: '',
  description: '',
  enabled: true,
  baseUrl: '',
  models: [],
  defaultModel: ''
});

// Form validation rules
const formRules: FormRules = {
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

const editProviderModels = (provider: AIProviderConfig) => {
  currentEditId.value = provider.id!;
  
  // Populate form data - only editable fields
  formData.provider = provider.provider;
  formData.displayName = provider.displayName;
  formData.description = provider.description || '';
  formData.enabled = provider.enabled;
  formData.baseUrl = provider.baseUrl || '';
  formData.models = JSON.parse(JSON.stringify(provider.models)); // Deep copy
  formData.defaultModel = provider.defaultModel;
  
  dialogVisible.value = true;
};

const addModel = () => {
  formData.models.push({
    modelId: '',
    modelName: '',
    description: ''
  });
};

const removeModel = (index: number) => {
  formData.models.splice(index, 1);
  
  // If removed model was the default, clear default
  const removedModel = formData.models[index];
  if (removedModel && removedModel.modelId === formData.defaultModel) {
    formData.defaultModel = '';
  }
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
      
      const response = await backendApi.updateAIProviderModels(currentEditId.value, request);
      if (response.data.success) {
        ElMessage.success('更新模型配置成功');
        dialogVisible.value = false;
        await loadProviders();
      }
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '操作失败');
    } finally {
      submitting.value = false;
    }
  });
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
