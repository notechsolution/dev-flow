<template>
    <div class="user-story-creation">
        <div class="page-header">
            <h1>{{ isEditMode ? '编辑需求' : '创建需求' }}</h1>
        </div>

        <!-- Meta Data Section - Only show in first step -->
        <el-card v-if="currentStep === 1" class="meta-section">
            <el-row :gutter="20">
                <el-col :span="6">
                    <el-form-item label="项目" required>
                        <el-select
                            v-model="userStory.projectId"
                            placeholder="请选择项目"
                            style="width: 100%"
                            filterable
                        >
                            <el-option
                                v-for="project in projects"
                                :key="project.id"
                                :label="project.name"
                                :value="project.id"
                            />
                        </el-select>
                    </el-form-item>
                </el-col>

                <el-col :span="8">
                    <el-form-item label="标签">
                        <el-select
                            v-model="userStory.tags"
                            multiple
                            filterable
                            allow-create
                            default-first-option
                            placeholder="请选择或创建标签"
                            style="width: 100%"
                        >
                            <el-option
                                v-for="tag in availableTags"
                                :key="tag"
                                :label="tag"
                                :value="tag"
                            />
                        </el-select>
                    </el-form-item>
                </el-col>

                <el-col :span="6">
                    <el-form-item label="负责人">
                        <el-select
                            v-model="userStory.ownerId"
                            placeholder="请选择负责人"
                            style="width: 100%"
                            filterable
                        >
                            <el-option
                                v-for="user in users"
                                :key="user.id"
                                :label="user.username"
                                :value="user.id"
                            />
                        </el-select>
                    </el-form-item>
                </el-col>
                
                <el-col :span="4" v-if="isEditMode && userStory.storyId">
                    <el-form-item label="外部系统ID">
                        <el-tag type="success" size="default">{{ userStory.storyId }}</el-tag>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row :gutter="20">
                <el-col :span="20">
                    <el-form-item label="需求标题" required>
                        <el-input
                            v-model="userStory.title"
                            placeholder="请输入用户故事标题"
                            maxlength="100"
                            show-word-limit
                        />
                    </el-form-item>
                </el-col>
            </el-row>
        </el-card>

        <!-- Step Indicator -->
        <el-card class="step-indicator">
            <el-steps :active="currentStep - 1" align-center>
                <el-step title="需求输入" description="输入原始需求" />
                <el-step title="需求澄清" description="AI 提问并确认" />
                <el-step title="需求优化" description="生成优化的需求" />
            </el-steps>
        </el-card>

        <!-- Main Content Section -->
        <el-card class="main-section" shadow="hover">
            <!-- Step 1: Requirement Input -->
            <div v-if="currentStep === 1" class="step-content fullscreen-editor">
                <div class="editor-header">
                    <h2>请输入您的需求</h2>
                </div>
                <MilkdownProvider class="fullscreen-milkdown">
                    <MilkdownEditor
                        ref="requirementEditor"
                        :content="userStory.originalRequirement"
                        placeholder="请在这里详细描述您的需求..."
                        @updated-content="onRequirementUpdate"
                    />
                </MilkdownProvider>
                <div class="step-actions">
                    <el-button size="large" @click="cancel">取消</el-button>
                    <el-button 
                        type="primary" 
                        size="large" 
                        @click="goToClarification"
                        :disabled="!userStory.originalRequirement.trim() || !userStory.title.trim()"
                    >
                        下一步：需求澄清
                    </el-button>
                </div>
            </div>

            <!-- Step 2: Requirement Clarification -->
            <div v-if="currentStep === 2" class="step-content clarification-content">
                <div class="editor-header">
                    <h2>需求澄清</h2>
                    <el-tag v-if="clarificationQuestions.length > 0" type="info">
                        {{ answeredQuestionsCount }} / {{ clarificationQuestions.length }} 已回答
                    </el-tag>
                </div>
                
                <!-- AI Provider Selector for Clarification -->
                <AIProviderSelector
                    v-if="!loadingClarification && clarificationQuestions.length === 0"
                    v-model="selectedClarificationProvider"
                    :disabled="loadingClarification"
                />
                
                <!-- Prompt Template Selector for Clarification -->
                <PromptTemplateSelector
                    v-if="!loadingClarification && clarificationQuestions.length === 0"
                    type="REQUIREMENT_CLARIFICATION"
                    :project-id="userStory.projectId"
                    :user-id="userId"
                    title="需求澄清提示词"
                    @template-selected="onClarificationTemplateSelected"
                />
                
                <!-- Generate Button -->
                <div v-if="!loadingClarification && clarificationQuestions.length === 0" class="generate-button-container">
                    <el-button type="primary" @click="generateClarificationQuestions">
                        生成澄清问题
                    </el-button>
                </div>
                
                <div v-if="loadingClarification" class="loading-container">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    <p>AI 正在分析您的需求并生成需要澄清的问题...</p>
                </div>
                
                <div v-else-if="clarificationQuestions.length > 0" class="questions-container">
                    <!-- Regenerate section for edit mode -->
                    <div class="regenerate-section">
                        
                        <!-- AI Provider Selector for Regeneration -->
                        <AIProviderSelector
                            v-if="!loadingClarification"
                            v-model="selectedClarificationProvider"
                            :disabled="loadingClarification"
                        />
                        
                        <!-- Prompt Template Selector for Regeneration -->
                        <PromptTemplateSelector
                            v-if="!loadingClarification"
                            type="REQUIREMENT_CLARIFICATION"
                            :project-id="userStory.projectId"
                            :user-id="userId"
                            title="需求澄清提示词"
                            @template-selected="onClarificationTemplateSelected"
                        />
                        
                        <div class="generate-button-container">
                            <el-button 
                                type="warning" 
                                @click="regenerateClarificationQuestions"
                                :loading="loadingClarification"
                            >
                                重新生成澄清问题
                            </el-button>
                        </div>
                    </div>
                    
                    <div 
                        v-for="(question, index) in clarificationQuestions" 
                        :key="question.id"
                        class="question-item"
                    >
                        <div class="question-header">
                            <el-tag :type="getQuestionTagType(question.category)" size="small">
                                {{ question.category }}
                            </el-tag>
                            <span class="question-number">问题 {{ index + 1 }}</span>
                        </div>
                        <div class="question-text">
                            {{ question.question }}
                        </div>
                        <el-input
                            v-model="question.answer"
                            type="textarea"
                            :rows="3"
                            placeholder="请输入您的回答..."
                            class="answer-input"
                        />
                    </div>
                </div>
                
                <div class="step-actions">
                    <el-button size="large" @click="currentStep = 1">上一步</el-button>
                    <el-button 
                        type="primary" 
                        size="large" 
                        @click="goToOptimization"
                        :disabled="!allQuestionsAnswered"
                        :loading="generatingOptimization"
                    >
                        下一步：需求优化
                    </el-button>
                </div>
            </div>

            <!-- Step 3: Requirement Optimization -->
            <div v-if="currentStep === 3" class="step-content optimization-content">
                <div class="editor-header">
                    <h2>优化后的需求</h2>
                    <div>
                        <el-button 
                            type="success" 
                            size="large" 
                            @click="saveUserStory" 
                            :loading="saving"
                            :disabled="!optimizationResult.optimizedRequirement"
                        >
                            保存 User Story
                        </el-button>
                    </div>
                </div>
                
                <!-- AI Provider Selector for Optimization -->
                <AIProviderSelector
                    v-if="!loadingOptimization && !optimizationResult.optimizedRequirement"
                    v-model="selectedOptimizationProvider"
                    :disabled="loadingOptimization"
                />
                
                <!-- Prompt Template Selector for Optimization -->
                <PromptTemplateSelector
                    v-if="!loadingOptimization && !optimizationResult.optimizedRequirement"
                    type="REQUIREMENT_OPTIMIZATION"
                    :project-id="userStory.projectId"
                    :user-id="userId"
                    title="需求优化提示词"
                    @template-selected="onOptimizationTemplateSelected"
                />
                
                <!-- Generate Optimization Button -->
                <div v-if="!loadingOptimization && !optimizationResult.optimizedRequirement" class="generate-button-container">
                    <el-button type="primary" @click="generateOptimization">
                        开始需求优化
                    </el-button>
                </div>
                
                <div v-if="loadingOptimization" class="loading-container">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    <p>AI 正在优化您的需求...</p>
                </div>
                
                <div v-else-if="optimizationResult.optimizedRequirement" class="optimization-result">
                    <!-- Regenerate section for edit mode -->
                    <div class="regenerate-section">
                        
                        <!-- AI Provider Selector for Regeneration -->
                        <AIProviderSelector
                            v-if="!loadingOptimization"
                            v-model="selectedOptimizationProvider"
                            :disabled="loadingOptimization"
                        />
                        
                        <!-- Prompt Template Selector for Regeneration -->
                        <PromptTemplateSelector
                            v-if="!loadingOptimization"
                            type="REQUIREMENT_OPTIMIZATION"
                            :project-id="userStory.projectId"
                            :user-id="userId"
                            title="需求优化提示词"
                            @template-selected="onOptimizationTemplateSelected"
                        />
                        
                        <div class="generate-button-container">
                            <el-button 
                                type="warning" 
                                @click="regenerateOptimization"
                                :loading="loadingOptimization"
                            >
                                重新优化需求
                            </el-button>
                        </div>
                    </div>
                    
                    <el-tabs v-model="optimizationTab" type="card">
                        <el-tab-pane label="优化后的需求" name="optimized">
                            <MilkdownProvider>
                                <MilkdownEditor
                                    ref="optimizedEditor"
                                    :content="optimizationResult.optimizedRequirement"
                                    :readonly="false"
                                    :enable-copy="true"
                                    placeholder="优化后的需求将显示在这里..."
                                    @updated-content="onOptimizedRequirementUpdate"
                                />
                            </MilkdownProvider>
                        </el-tab-pane>
                        
                        <el-tab-pane label="User Story" name="userStory" v-if="optimizationResult.userStory">
                            <MilkdownProvider>
                                <MilkdownEditor
                                    :content="optimizationResult.userStory"
                                    :readonly="false"
                                    :enable-copy="true"
                                    @updated-content="onUserStoryUpdate"
                                />
                            </MilkdownProvider>
                        </el-tab-pane>
                        
                        <el-tab-pane label="验收标准" name="acceptance" v-if="optimizationResult.acceptanceCriteria">
                            <MilkdownProvider>
                                <MilkdownEditor
                                    :content="optimizationResult.acceptanceCriteria"
                                    :readonly="false"
                                    :enable-copy="true"
                                    @updated-content="onAcceptanceCriteriaUpdate"
                                />
                            </MilkdownProvider>
                        </el-tab-pane>
                        
                        <el-tab-pane label="技术说明" name="technical" v-if="optimizationResult.technicalNotes">
                            <MilkdownProvider>
                                <MilkdownEditor
                                    :content="optimizationResult.technicalNotes"
                                    :readonly="false"
                                    :enable-copy="true"
                                    @updated-content="onTechnicalNotesUpdate"
                                />
                            </MilkdownProvider>
                        </el-tab-pane>
                    </el-tabs>
                </div>
                
                <div class="step-actions">
                    <el-button size="large" @click="currentStep = 2">上一步</el-button>
                    <el-button 
                        type="success" 
                        size="large" 
                        @click="saveUserStory" 
                        :loading="saving"
                    >
                        保存 User Story
                    </el-button>
                </div>
            </div>
        </el-card>
    </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, InfoFilled } from '@element-plus/icons-vue'
import MilkdownEditor from '@/components/MilkdownEditor.vue'
import PromptTemplateSelector from '@/components/PromptTemplateSelector.vue'
import AIProviderSelector from '@/components/AIProviderSelector.vue'
import { MilkdownProvider } from "@milkdown/vue"
import aiApi, { ClarificationQuestion, QuestionAnswer } from '@/api/backend-api'
// User ID from Pinia store
import { userStore } from '@/store/user'

const router = useRouter()
const route = useRoute()

// Check if in edit mode
const isEditMode = computed(() => !!route.params.id)
const userStoryId = ref<string>('')

// Step management
const currentStep = ref(1) // 1: Input, 2: Clarification, 3: Optimization

// Loading states
const loadingClarification = ref(false)
const loadingOptimization = ref(false)
const generatingOptimization = ref(false)
const saving = ref(false)

// Prompt template state
const clarificationTemplateId = ref<string | null>(null)
const clarificationPromptContent = ref<string>('')
const optimizationTemplateId = ref<string | null>(null)
const optimizationPromptContent = ref<string>('')
const userId = ref<string | null >(userStore().getUserId);

// AI Provider state
const selectedClarificationProvider = ref<string>('')
const selectedOptimizationProvider = ref<string>('')

console.log('User ID:', userId.value)
console.log('user store:', userStore())
// User story data
const userStory = reactive({
    projectId: '',
    title: '',
    tags: [] as string[],
    ownerId: '',
    storyId: '', // External PM system ID
    originalRequirement: '',
    description: '',
    projectContext: '', // 项目上下文
    status: 'BACKLOG',
    priority: 'MEDIUM'
})

// Clarification data
const clarificationQuestions = ref<ClarificationQuestion[]>([])

// Optimization data
const optimizationTab = ref('optimized')
const optimizationResult = reactive({
    optimizedRequirement: '',
    userStory: '',
    acceptanceCriteria: '',
    technicalNotes: '',
})

// Data loaded from backend
const projects = ref<Array<{ id: string; name: string }>>([])

const users = ref<Array<{ id: string; username: string }>>([])

// Load projects from backend
const loadProjects = async () => {
    try {
        const response = await aiApi.getProjects()
        if (response.data.success) {
            projects.value = response.data.data.map((project: any) => ({
                id: project.id,
                name: project.name
            }))
        }
    } catch (error) {
        console.error('Failed to load projects:', error)
        ElMessage.error('加载项目列表失败')
    }
}

// Load users from backend
const loadUsers = async () => {
    try {
        const response = await aiApi.getUsers()
        if (response.data.success) {
            users.value = response.data.data.map((user: any) => ({
                id: user.id,
                username: user.username
            }))
        }
    } catch (error) {
        console.error('Failed to load users:', error)
        ElMessage.error('加载用户列表失败')
    }
}

// Load existing user story (for edit mode)
const loadUserStory = async () => {
    if (!route.params.id) return
    
    const id = route.params.id as string
    userStoryId.value = id
    
    try {
        const response = await aiApi.getUserStory(id)
        if (response.data.success) {
            const story = response.data.data
            
            // Populate form with existing data
            Object.assign(userStory, {
                projectId: story.projectId || '',
                title: story.title || '',
                tags: story.tags || [],
                ownerId: story.ownerId || '',
                storyId: story.storyId || '',
                originalRequirement: story.originalRequirement || '',
                status: story.status || 'BACKLOG',
                priority: story.priority || 'MEDIUM'
            })
            
            // If already has optimized content, load it
            if (story.optimizedRequirement || story.userStory) {
                Object.assign(optimizationResult, {
                    optimizedRequirement: story.optimizedRequirement || '',
                    userStory: story.userStory || '',
                    acceptanceCriteria: story.acceptanceCriteria || '',
                    technicalNotes: story.technicalNotes || ''
                })
                
                // If has clarification QAs, load them
                if (story.clarificationQAs && story.clarificationQAs.length > 0) {
                    clarificationQuestions.value = story.clarificationQAs.map((qa: any) => ({
                        id: qa.questionId,
                        question: qa.question,
                        answer: qa.answer || '',
                        category: qa.category
                    }))
                }
            }
            
            ElMessage.success('用户故事加载成功')
        }
    } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '加载用户故事失败')
        console.error('Load user story error:', error)
        router.back()
    }
}

const availableTags = ref([
    'frontend', 'backend', 'api', 'ui/ux', 'database',
    'authentication', 'performance', 'security', 'testing'
])

// Editor references
const requirementEditor = ref()
const optimizedEditor = ref()

// Computed properties
const answeredQuestionsCount = computed(() => {
    return clarificationQuestions.value.filter(q => q.answer && q.answer.trim()).length
})

const allQuestionsAnswered = computed(() => {
    return clarificationQuestions.value.length > 0 && 
           clarificationQuestions.value.every(q => q.answer && q.answer.trim())
})

// Check if there's existing optimization (for edit mode)
const hasExistingOptimization = computed(() => {
    return isEditMode.value && (
        optimizationResult.optimizedRequirement !== '' ||
        optimizationResult.userStory !== ''
    )
})

// Methods
const onRequirementUpdate = (content: string) => {
    userStory.originalRequirement = content
}

const onOptimizedRequirementUpdate = (content: string) => {
    optimizationResult.optimizedRequirement = content
}

const onUserStoryUpdate = (content: string) => {
    optimizationResult.userStory = content
}

const onAcceptanceCriteriaUpdate = (content: string) => {
    optimizationResult.acceptanceCriteria = content
}

const onTechnicalNotesUpdate = (content: string) => {
    optimizationResult.technicalNotes = content
}

const getQuestionTagType = (category: string) => {
    const tagTypes: Record<string, any> = {
        'Users & Roles': 'success',
        'Goals & Outcomes': 'warning',
        'Business Rules': 'danger',
        'Data & Interface': 'info',
        'Non-functional Requirements': '',
        'General': 'info'
    }
    return tagTypes[category] || 'info'
}

// Prompt template handlers
const onClarificationTemplateSelected = (templateId: string | null, content: string) => {
    console.log('Selected template:', templateId, content)
    clarificationTemplateId.value = templateId
    clarificationPromptContent.value = content
}

const onOptimizationTemplateSelected = (templateId: string | null, content: string) => {
    optimizationTemplateId.value = templateId
    optimizationPromptContent.value = content
}

const goToClarification = async () => {
    if (!userStory.title.trim()) {
        ElMessage.warning('请输入标题')
        return
    }
    if (!userStory.originalRequirement.trim()) {
        ElMessage.warning('请输入需求描述')
        return
    }

    currentStep.value = 2

     // If clarification questions already exist, skip AI generation
    if (clarificationQuestions.value.length > 0) {
        ElMessage.info('已有澄清问题，继续填写')
        return
    }
}

// Generate clarification questions (called after template is selected)
const generateClarificationQuestions = async () => {
    loadingClarification.value = true
    
    try {
        // Call AI to generate clarification questions
        const response = await aiApi.clarifyRequirement({
            originalRequirement: userStory.originalRequirement,
            title: userStory.title,
            projectId: userStory.projectId || undefined,
            promptTemplateId: clarificationTemplateId.value || undefined,
            provider: selectedClarificationProvider.value || undefined,
        })

        if (response.data.success) {
            clarificationQuestions.value = response.data.questions
            ElMessage.success('澄清问题已生成')
        } else {
            ElMessage.error(response.data.message || '生成澄清问题失败')
            currentStep.value = 1
        }
    } catch (error) {
        ElMessage.error('生成澄清问题失败')
        console.error('Clarification error:', error)
        currentStep.value = 1
    } finally {
        loadingClarification.value = false
    }
}

// Regenerate clarification questions in edit mode
const regenerateClarificationQuestions = async () => {
    try {
        await ElMessageBox.confirm(
            '重新生成将覆盖现有的澄清问题及答案，确定要继续吗？',
            '确认重新生成',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        )
        
        // Call the same generation method
        await generateClarificationQuestions()
    } catch {
        // User cancelled
        return
    }
}

const viewExistingOptimization = () => {
    // Simply navigate to step 3 to view existing optimization without calling AI
    if (hasExistingOptimization.value) {
        currentStep.value = 3
        ElMessage.success('已加载现有优化需求')
    }
}

const goToOptimization = async () => {
    if (!allQuestionsAnswered.value) {
        ElMessage.warning('请回答所有问题后再继续')
        return
    }
    // Just navigate to step 3, don't auto-generate
    currentStep.value = 3
}

// Generate optimization (called after template is selected)
const generateOptimization = async () => {
    loadingOptimization.value = true
    generatingOptimization.value = true

    try {
        // Prepare Q&A data
        const clarificationAnswers: QuestionAnswer[] = clarificationQuestions.value.map(q => ({
            questionId: q.id,
            question: q.question,
            answer: q.answer || '',
            category: q.category
        }))

        // Call AI to optimize requirement
        const response = await aiApi.optimizeRequirement({
            originalRequirement: userStory.originalRequirement,
            title: userStory.title,
            clarificationAnswers,
            projectId: userStory.projectId || undefined,
            promptTemplateId: optimizationTemplateId.value || undefined,
            provider: selectedOptimizationProvider.value || undefined,
        })

        if (response.data.success) {
            optimizationResult.optimizedRequirement = response.data.optimizedRequirement
            optimizationResult.userStory = response.data.userStory
            optimizationResult.acceptanceCriteria = response.data.acceptanceCriteria
            optimizationResult.technicalNotes = response.data.technicalNotes
            
            // Update the description for saving
            userStory.description = response.data.optimizedRequirement
            
            ElMessage.success('需求优化完成')
        } else {
            ElMessage.error(response.data.message || '需求优化失败')
        }
    } catch (error) {
        ElMessage.error('需求优化失败')
        console.error('Optimization error:', error)
    } finally {
        loadingOptimization.value = false
        generatingOptimization.value = false
    }
}

// Regenerate optimization in edit mode
const regenerateOptimization = async () => {
    try {
        await ElMessageBox.confirm(
            '重新优化将覆盖现有的优化结果，确定要继续吗？',
            '确认重新优化',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        )
        
        // Call the same generation method
        await generateOptimization()
    } catch {
        // User cancelled
        return
    }
}

const saveUserStory = async () => {
    // Validation
    if (!userStory.projectId) {
        ElMessage.warning('请选择项目')
        return
    }
    if (!userStory.title.trim()) {
        ElMessage.warning('请输入标题')
        return
    }
    if (!optimizationResult.optimizedRequirement) {
        ElMessage.warning('请完成需求优化后再保存')
        return
    }

    saving.value = true
    try {
        // Build the request with all data
        const request = {
            title: userStory.title,
            projectId: userStory.projectId,
            tags: userStory.tags,
            ownerId: userStory.ownerId,
            storyId: userStory.storyId,
            originalRequirement: userStory.originalRequirement,
            projectContext: userStory.projectContext,
            clarificationQAs: clarificationQuestions.value.map(q => ({
                questionId: q.id,
                question: q.question,
                answer: q.answer || '',
                category: q.category
            })),
            optimizedRequirement: optimizationResult.optimizedRequirement,
            userStory: optimizationResult.userStory,
            acceptanceCriteria: optimizationResult.acceptanceCriteria,
            technicalNotes: optimizationResult.technicalNotes,
            status: userStory.status || 'BACKLOG',
            priority: userStory.priority
        }

        let response
        if (isEditMode.value && userStoryId.value) {
            // Update existing user story
            response = await aiApi.updateUserStory(userStoryId.value, request)
            ElMessage.success('User Story 更新成功!')
        } else {
            // Create new user story
            response = await aiApi.createUserStory(request)
            ElMessage.success('User Story 保存成功!')
        }
        
        if (response.data.success) {
            router.push('/dashboard')
        } else {
            ElMessage.error('保存失败')
        }
    } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '保存失败')
        console.error('Save error:', error)
    } finally {
        saving.value = false
    }
}

const cancel = async () => {
    const hasChanges = userStory.title || userStory.originalRequirement || userStory.tags.length > 0

    if (hasChanges) {
        try {
            await ElMessageBox.confirm(
                '您有未保存的更改，确定要离开吗？',
                '确认',
                {
                    confirmButtonText: '离开',
                    cancelButtonText: '留下',
                    type: 'warning'
                }
            )
        } catch {
            return // User cancelled
        }
    }

    router.back()
}

// Initialize data
onMounted(async () => {
    await loadProjects()
    await loadUsers()
    
    // If in edit mode, load existing user story
    if (isEditMode.value) {
        await loadUserStory()
    }
})
</script>

<style scoped>
.user-story-creation {
    padding: 20px;
    width: 100%;
    margin: 0 auto;
    max-width: 1600px;
}

.page-header {
    margin-bottom: 20px;
}

.page-header h1 {
    color: #303133;
    font-size: 24px;
    font-weight: 600;
    margin: 0;
}

.meta-section {
    margin-bottom: 20px;
}

.step-indicator {
    margin-bottom: 20px;
}

.main-section {
    margin-bottom: 20px;
    min-height: calc(100vh - 300px);
}

.step-content {
    min-height: 600px;
    display: flex;
    flex-direction: column;
}

.fullscreen-editor {
    min-height: calc(100vh - 350px);
}

.fullscreen-milkdown {
    flex: 1;
    min-height: 500px;
}

.editor-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 2px solid #ebeef5;
}

.editor-header h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #303133;
}

.step-actions {
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
    text-align: right;
}

.step-actions .el-button {
    margin-left: 10px;
}

/* Clarification styles */
.clarification-content {
    padding: 20px 0;
}

.loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 400px;
    color: #909399;
}

.loading-container .el-icon {
    font-size: 48px;
    margin-bottom: 20px;
}

.loading-container p {
    font-size: 16px;
}

.questions-container {
    max-width: 900px;
    margin: 0 auto;
}

.question-item {
    background: #f5f7fa;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 20px;
    transition: all 0.3s;
}

.question-item:hover {
    background: #ecf5ff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.question-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.question-number {
    font-size: 14px;
    color: #909399;
    font-weight: 500;
}

.question-text {
    font-size: 16px;
    color: #303133;
    font-weight: 500;
    margin-bottom: 15px;
    line-height: 1.6;
}

.answer-input {
    margin-top: 10px;
}

/* Regenerate section styles */
.regenerate-section {
    margin-bottom: 30px;
    padding: 20px;
    background: #fef0f0;
    border-radius: 8px;
    border: 1px solid #fde2e2;
}

.regenerate-section .el-alert {
    margin-bottom: 16px;
}

/* Optimization styles */
.optimization-content {
    padding: 20px 0;
}

.optimization-result {
    min-height: 500px;
}

:deep(.el-card__body) {
    padding: 20px;
}

:deep(.el-tabs__content) {
    padding: 20px 0;
    min-height: 400px;
}

:deep(.el-form-item__label) {
    font-weight: 600;
    color: #606266;
}

.field-hint {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-top: 8px;
    font-size: 12px;
    color: #909399;
    line-height: 1.5;
}

.generate-button-container {
    margin: 20px 0;
    text-align: center;
}

.field-hint .el-icon {
    font-size: 14px;
    color: #409eff;
}

:deep(.el-select .el-input__inner) {
    cursor: pointer;
}

:deep(.el-steps) {
    padding: 0 50px;
}

@media (max-width: 768px) {
    .user-story-creation {
        padding: 10px;
    }

    .editor-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
    }

    .questions-container {
        max-width: 100%;
    }

    .question-item {
        padding: 15px;
    }

    :deep(.el-steps) {
        padding: 0 10px;
    }
}
</style>