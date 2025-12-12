<template>
  <div class="requirement-clarification">
    <h2>需求澄清</h2>
    
    <!-- AI提供商选择器 -->
    <AIProviderSelector 
      v-model="selectedProvider"
      :disabled="loading"
    />
    
    <!-- 需求输入表单 -->
    <div class="form-group">
      <label for="title">需求标题</label>
      <input 
        id="title"
        v-model="requirementTitle" 
        type="text" 
        class="form-control"
        placeholder="输入需求标题"
        :disabled="loading"
      />
    </div>
    
    <div class="form-group">
      <label for="requirement">需求描述</label>
      <textarea 
        id="requirement"
        v-model="requirementDescription" 
        class="form-control"
        rows="6"
        placeholder="详细描述你的需求..."
        :disabled="loading"
      ></textarea>
    </div>
    
    <!-- 操作按钮 -->
    <div class="action-buttons">
      <button 
        @click="generateQuestions" 
        :disabled="loading || !canGenerateQuestions"
        class="btn btn-primary"
      >
        <span v-if="loading">生成中...</span>
        <span v-else>生成澄清问题</span>
      </button>
    </div>
    
    <!-- 澄清问题列表 -->
    <div v-if="clarificationQuestions.length > 0" class="questions-section">
      <h3>澄清问题</h3>
      <p class="provider-info">
        使用 {{ getProviderDisplayName() }} 生成
      </p>
      
      <div 
        v-for="question in clarificationQuestions" 
        :key="question.id"
        class="question-item"
      >
        <div class="question-header">
          <span class="question-category">{{ question.category }}</span>
          <span class="question-text">{{ question.question }}</span>
        </div>
        <textarea 
          v-model="answers[question.id]"
          class="form-control answer-input"
          rows="2"
          placeholder="输入你的答案..."
          :disabled="loading"
        ></textarea>
      </div>
      
      <div class="action-buttons">
        <button 
          @click="optimizeRequirement" 
          :disabled="loading || !canOptimize"
          class="btn btn-success"
        >
          <span v-if="loading">优化中...</span>
          <span v-else>优化需求</span>
        </button>
      </div>
    </div>
    
    <!-- 优化结果 -->
    <div v-if="optimizationResult" class="optimization-result">
      <h3>优化结果</h3>
      <p class="provider-info">
        使用 {{ getProviderDisplayName() }} 生成
      </p>
      
      <div class="result-section">
        <h4>优化后的需求</h4>
        <div class="result-content">{{ optimizationResult.optimizedRequirement }}</div>
      </div>
      
      <div class="result-section">
        <h4>用户故事</h4>
        <div class="result-content">{{ optimizationResult.userStory }}</div>
      </div>
      
      <div class="result-section">
        <h4>验收标准</h4>
        <div class="result-content">{{ optimizationResult.acceptanceCriteria }}</div>
      </div>
      
      <div class="result-section" v-if="optimizationResult.technicalNotes">
        <h4>技术说明</h4>
        <div class="result-content">{{ optimizationResult.technicalNotes }}</div>
      </div>
    </div>
    
    <!-- 错误提示 -->
    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue';
import AIProviderSelector from './AIProviderSelector.vue';
import axios from 'axios';

interface ClarificationQuestion {
  id: string;
  question: string;
  category: string;
}

interface OptimizationResult {
  optimizedRequirement: string;
  userStory: string;
  acceptanceCriteria: string;
  technicalNotes?: string;
}

export default defineComponent({
  name: 'RequirementClarificationExample',
  components: {
    AIProviderSelector
  },
  props: {
    projectId: {
      type: String,
      required: true
    },
    projectContext: {
      type: String,
      default: ''
    }
  },
  setup(props) {
    const loading = ref(false);
    const error = ref('');
    const selectedProvider = ref('');
    const requirementTitle = ref('');
    const requirementDescription = ref('');
    const clarificationQuestions = ref<ClarificationQuestion[]>([]);
    const answers = ref<Record<string, string>>({});
    const optimizationResult = ref<OptimizationResult | null>(null);
    const currentProvider = ref('');

    const canGenerateQuestions = computed(() => {
      return requirementTitle.value.trim() !== '' && 
             requirementDescription.value.trim() !== '';
    });

    const canOptimize = computed(() => {
      return clarificationQuestions.value.every(q => answers.value[q.id]?.trim());
    });

    const getProviderDisplayName = () => {
      const providerNames: Record<string, string> = {
        'dashscope': 'DashScope (通义千问)',
        'ollama': 'Ollama',
        'openai': 'OpenAI'
      };
      return providerNames[currentProvider.value] || '默认提供商';
    };

    const generateQuestions = async () => {
      loading.value = true;
      error.value = '';
      currentProvider.value = selectedProvider.value || 'default';
      
      try {
        const response = await axios.post('/api/ai/clarify-requirement', {
          originalRequirement: requirementDescription.value,
          title: requirementTitle.value,
          projectContext: props.projectContext,
          projectId: props.projectId,
          provider: selectedProvider.value || undefined
        });
        
        if (response.data.success) {
          clarificationQuestions.value = response.data.questions;
          answers.value = {};
          optimizationResult.value = null;
        } else {
          error.value = response.data.message || '生成澄清问题失败';
        }
      } catch (err: any) {
        error.value = err.response?.data?.message || '生成澄清问题时发生错误';
        console.error('Error generating clarification questions:', err);
      } finally {
        loading.value = false;
      }
    };

    const optimizeRequirement = async () => {
      loading.value = true;
      error.value = '';
      
      try {
        const clarificationAnswers = clarificationQuestions.value.map(q => ({
          questionId: q.id,
          question: q.question,
          answer: answers.value[q.id],
          category: q.category
        }));
        
        const response = await axios.post('/api/ai/optimize-requirement', {
          originalRequirement: requirementDescription.value,
          title: requirementTitle.value,
          projectContext: props.projectContext,
          projectId: props.projectId,
          provider: selectedProvider.value || undefined,
          clarificationAnswers
        });
        
        if (response.data.success) {
          optimizationResult.value = {
            optimizedRequirement: response.data.optimizedRequirement,
            userStory: response.data.userStory,
            acceptanceCriteria: response.data.acceptanceCriteria,
            technicalNotes: response.data.technicalNotes
          };
        } else {
          error.value = response.data.message || '优化需求失败';
        }
      } catch (err: any) {
        error.value = err.response?.data?.message || '优化需求时发生错误';
        console.error('Error optimizing requirement:', err);
      } finally {
        loading.value = false;
      }
    };

    return {
      loading,
      error,
      selectedProvider,
      requirementTitle,
      requirementDescription,
      clarificationQuestions,
      answers,
      optimizationResult,
      canGenerateQuestions,
      canOptimize,
      getProviderDisplayName,
      generateQuestions,
      optimizeRequirement
    };
  }
});
</script>

<style scoped>
.requirement-clarification {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

h2 {
  margin-bottom: 1.5rem;
  color: #333;
}

h3 {
  margin-top: 2rem;
  margin-bottom: 1rem;
  color: #555;
}

h4 {
  margin-top: 1rem;
  margin-bottom: 0.5rem;
  color: #666;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #333;
}

.form-control {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  font-family: inherit;
}

.form-control:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.action-buttons {
  margin-top: 1rem;
  margin-bottom: 1rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #0056b3;
}

.btn-success {
  background-color: #28a745;
  color: white;
}

.btn-success:hover:not(:disabled) {
  background-color: #218838;
}

.provider-info {
  color: #666;
  font-size: 0.9rem;
  font-style: italic;
  margin-bottom: 1rem;
}

.questions-section {
  margin-top: 2rem;
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.question-item {
  margin-bottom: 1.5rem;
  padding: 1rem;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.question-header {
  margin-bottom: 0.5rem;
}

.question-category {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  background-color: #007bff;
  color: white;
  border-radius: 3px;
  font-size: 0.85rem;
  margin-right: 0.5rem;
}

.question-text {
  font-weight: 500;
  color: #333;
}

.answer-input {
  margin-top: 0.5rem;
}

.optimization-result {
  margin-top: 2rem;
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.result-section {
  margin-bottom: 1.5rem;
  padding: 1rem;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.result-content {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #333;
}

.alert {
  padding: 1rem;
  margin-top: 1rem;
  border-radius: 4px;
}

.alert-danger {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}
</style>
