<template>
    <div class="user-story-creation">
        <div class="page-header">
            <h1>Create User Story</h1>
        </div>

        <!-- Meta Data Section - Only show in first step -->
        <el-card v-if="currentStep === 1" class="meta-section">
            <el-row :gutter="20">
                <el-col :span="6">
                    <el-form-item label="Project" required>
                        <el-select
                            v-model="userStory.projectId"
                            placeholder="Select Project"
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
                    <el-form-item label="Tags">
                        <el-select
                            v-model="userStory.tags"
                            multiple
                            filterable
                            allow-create
                            default-first-option
                            placeholder="Select or create tags"
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
                    <el-form-item label="Owner">
                        <el-select
                            v-model="userStory.ownerId"
                            placeholder="Select Owner"
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
            </el-row>
            <el-row :gutter="20">
                <el-col :span="20">
                    <el-form-item label="Title" required>
                        <el-input
                            v-model="userStory.title"
                            placeholder="Enter user story title"
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
                
                <div v-if="loadingClarification" class="loading-container">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    <p>AI 正在分析您的需求并生成需要澄清的问题...</p>
                </div>
                
                <div v-else-if="clarificationQuestions.length > 0" class="questions-container">
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
                        >
                            保存 User Story
                        </el-button>
                    </div>
                </div>
                
                <div v-if="loadingOptimization" class="loading-container">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    <p>AI 正在优化您的需求...</p>
                </div>
                
                <div v-else class="optimization-result">
                    <el-tabs v-model="optimizationTab" type="card">
                        <el-tab-pane label="优化后的需求" name="optimized">
                            <MilkdownProvider>
                                <MilkdownEditor
                                    ref="optimizedEditor"
                                    :content="optimizationResult.optimizedRequirement"
                                    :readonly="true"
                                    :enable-copy="true"
                                    placeholder="优化后的需求将显示在这里..."
                                />
                            </MilkdownProvider>
                        </el-tab-pane>
                        
                        <el-tab-pane label="User Story" name="userStory" v-if="optimizationResult.userStory">
                            <MilkdownProvider>
                                <MilkdownEditor
                                    :content="optimizationResult.userStory"
                                    :readonly="true"
                                    :enable-copy="true"
                                />
                            </MilkdownProvider>
                        </el-tab-pane>
                        
                        <el-tab-pane label="验收标准" name="acceptance" v-if="optimizationResult.acceptanceCriteria">
                            <MilkdownProvider>
                                <MilkdownEditor
                                    :content="optimizationResult.acceptanceCriteria"
                                    :readonly="true"
                                    :enable-copy="true"
                                />
                            </MilkdownProvider>
                        </el-tab-pane>
                        
                        <el-tab-pane label="技术说明" name="technical" v-if="optimizationResult.technicalNotes">
                            <MilkdownProvider>
                                <MilkdownEditor
                                    :content="optimizationResult.technicalNotes"
                                    :readonly="true"
                                    :enable-copy="true"
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import MilkdownEditor from '@/components/MilkdownEditor.vue'
import { MilkdownProvider } from "@milkdown/vue"
import aiApi, { ClarificationQuestion, QuestionAnswer } from '@/api/backend-api'

const router = useRouter()

// Step management
const currentStep = ref(1) // 1: Input, 2: Clarification, 3: Optimization

// Loading states
const loadingClarification = ref(false)
const loadingOptimization = ref(false)
const generatingOptimization = ref(false)
const saving = ref(false)

// User story data
const userStory = reactive({
    projectId: '',
    title: '',
    tags: [] as string[],
    ownerId: '',
    originalRequirement: '',
    description: '',
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

// Methods
const onRequirementUpdate = (content: string) => {
    userStory.originalRequirement = content
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

    loadingClarification.value = true
    
    try {
        // Call AI to generate clarification questions
        const response = await aiApi.clarifyRequirement({
            originalRequirement: userStory.originalRequirement,
            title: userStory.title,
            projectContext: projects.value.find(p => p.id === userStory.projectId)?.name || '',
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

const goToOptimization = async () => {
    if (!allQuestionsAnswered.value) {
        ElMessage.warning('请回答所有问题后再继续')
        return
    }

    currentStep.value = 3
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
            projectContext: projects.value.find(p => p.id === userStory.projectId)?.name || '',
            clarificationAnswers
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
            currentStep.value = 2
        }
    } catch (error) {
        ElMessage.error('需求优化失败')
        console.error('Optimization error:', error)
        currentStep.value = 2
    } finally {
        loadingOptimization.value = false
        generatingOptimization.value = false
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
            originalRequirement: userStory.originalRequirement,
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

        const response = await aiApi.createUserStory(request)
        
        if (response.data.success) {
            ElMessage.success('User Story 保存成功!')
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