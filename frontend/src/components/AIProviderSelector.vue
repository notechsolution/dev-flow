<template>
  <div class="ai-provider-selector">
    <div class="selector-row">
      <div class="form-group provider-group">
        <label for="ai-provider">AI提供商</label>
        <select 
          id="ai-provider" 
          v-model="selectedProvider" 
          class="form-control"
          :disabled="loading || disabled"
          @change="onProviderChange"
        >
        <option value="">{{ defaultProviderName }} (默认)</option>
          <option 
            v-for="provider in availableProviders" 
            :key="provider" 
            :value="provider"
          >
            {{ getProviderDisplayName(provider) }}
          </option>
        </select>
        <small class="form-text text-muted">
          {{ getProviderDescription(effectiveProvider) }}
        </small>
      </div>
      
      <div class="form-group model-group">
        <label for="ai-model">AI模型</label>
        <select 
          id="ai-model" 
          v-model="selectedModel" 
          class="form-control"
          :disabled="loading || disabled || !availableModels.length"
        >
          <option 
            v-for="model in availableModels" 
            :key="model.modelId" 
            :value="model.modelId"
          >
            {{ model.modelId + '(' + model.modelName + ')' }}
          </option>
        </select>
        <small class="form-text text-muted">
          {{ getModelDescription(selectedModel) }}
        </small>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, computed, watch } from 'vue';
import axios from 'axios';
import { de } from 'element-plus/es/locale';

interface ModelInfo {
  modelId: string;
  modelName: string;
  description?: string;
}

interface ProviderInfo {
  name: string;
  description: string;
  models: ModelInfo[];
  defaultModel: string;
}

interface ProvidersResponse {
  availableProviders: string[];
  defaultProvider: string;
  providers: Record<string, ProviderInfo>;
}

export default defineComponent({
  name: 'AIProviderSelector',
  props: {
    provider: {
      type: String,
      default: ''
    },
    model: {
      type: String,
      default: ''
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:provider', 'update:model'],
  setup(props, { emit }) {
    const loading = ref(false);
    const availableProviders = ref<string[]>([]);
    const defaultProvider = ref('');
    const providerInfo = ref<Record<string, ProviderInfo>>({});
    
    const selectedProvider = computed({
      get: () => props.provider,
      set: (value) => emit('update:provider', value)
    });
    
    const selectedModel = computed({
      get: () => props.model,
      set: (value) => emit('update:model', value)
    });

    const effectiveProvider = computed(() => {
      return selectedProvider.value || defaultProvider.value;
    });

    const availableModels = computed(() => {
      const provider = effectiveProvider.value;
      const providerInfoData = providerInfo.value[provider];
      if (!providerInfoData) return [];
      
      const models = providerInfoData.models || [];
      // find default model and put it first
      const defaultModel = providerInfoData.models.find(m => m.modelId === providerInfoData.defaultModel);

      let filteredModels = models.filter(m => m.modelId !== providerInfoData.defaultModel);
      // set the default model as the first option
      if (defaultModel) {
        filteredModels.unshift({
          modelId: defaultModel.modelId,
          modelName: defaultModel.modelName + ' - 默认',
          description: defaultModel.description
        });
      }
      return filteredModels;
    });

    const defaultProviderName = computed(() => {
      return providerInfo.value[defaultProvider.value]?.name || '默认提供商';
    });

    const getProviderDisplayName = (provider: string): string => {
      return providerInfo.value[provider]?.name || provider;
    };

    const getProviderDescription = (provider: string): string => {
      return providerInfo.value[provider]?.description || '';
    };
    
    const getModelDescription = (modelId: string): string => {
      if (!modelId) {
        return `使用${effectiveProvider.value}的默认模型`;
      }
      const model = availableModels.value.find(m => m.modelId === modelId);
      return model?.description || '';
    };

    const onProviderChange = () => {
      // Set to default model when provider changes
      const provider = effectiveProvider.value;
      const providerData = providerInfo.value[provider];
      selectedModel.value = providerData?.defaultModel || '';
    };

    const loadAvailableProviders = async () => {
      loading.value = true;
      try {
        const response = await axios.get<ProvidersResponse>('/api/ai/providers');
        const data = response.data;
        
        availableProviders.value = data.availableProviders;
        defaultProvider.value = data.defaultProvider;
        // remove default provider from the list to avoid duplication
        availableProviders.value = availableProviders.value.filter(p => p !== defaultProvider.value);
        providerInfo.value = data.providers;
        
        // Set default model on initial load if no model is selected
        if (!props.model) {
          const provider = effectiveProvider.value;
          const providerData = providerInfo.value[provider];
          selectedModel.value = providerData?.defaultModel || '';
        }
      } catch (error) {
        console.error('Failed to load AI providers:', error);
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      loadAvailableProviders();
    });

    return {
      loading,
      availableProviders,
      defaultProvider,
      defaultProviderName,
      selectedProvider,
      selectedModel,
      effectiveProvider,
      availableModels,
      getProviderDisplayName,
      getProviderDescription,
      getModelDescription,
      onProviderChange
    };
  }
});
</script>

<style scoped>
.ai-provider-selector {
  margin-bottom: 1rem;
}

.selector-row {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.form-group {
  flex: 1;
  margin-bottom: 0.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  font-size: 0.95rem;
}

.form-control {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.form-control:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.form-text {
  display: block;
  margin-top: 0.25rem;
  font-size: 0.875rem;
  min-height: 1.2em;
}

.text-muted {
  color: #6c757d;
}

@media (max-width: 768px) {
  .selector-row {
    flex-direction: column;
  }
}
</style>
