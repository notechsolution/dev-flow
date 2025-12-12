<template>
  <div class="ai-provider-selector">
    <div class="form-group">
      <label for="ai-provider">AI提供商</label>
      <select 
        id="ai-provider" 
        v-model="selectedProvider" 
        class="form-control"
        :disabled="loading || disabled"
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
        {{ getProviderDescription(selectedProvider || defaultProvider) }}
      </small>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, computed } from 'vue';
import axios from 'axios';

interface ProviderInfo {
  name: string;
  description: string;
}

interface ProvidersResponse {
  availableProviders: string[];
  defaultProvider: string;
  providers: Record<string, ProviderInfo>;
}

export default defineComponent({
  name: 'AIProviderSelector',
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const loading = ref(false);
    const availableProviders = ref<string[]>([]);
    const defaultProvider = ref('');
    const providerInfo = ref<Record<string, ProviderInfo>>({});
    
    const selectedProvider = computed({
      get: () => props.modelValue,
      set: (value) => emit('update:modelValue', value)
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
      getProviderDisplayName,
      getProviderDescription
    };
  }
});
</script>

<style scoped>
.ai-provider-selector {
  margin-bottom: 1rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
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
}

.text-muted {
  color: #6c757d;
}
</style>
