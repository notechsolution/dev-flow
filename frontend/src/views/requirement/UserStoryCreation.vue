<template>
    <div class="user-story-creation">
        <div class="page-header">
            <h1>Create User Story</h1>
        </div>

        <!-- Meta Data Section -->
        <el-card class="meta-section">

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

        <!-- Main Content Section -->
        <el-card class="main-section" shadow="hover">
            <el-tabs v-model="activeTab" type="card">
                <!-- Description Tab -->
                <el-tab-pane label="Description" name="description">
                    <div class="description-content">
                        <el-row :gutter="20">
                            <!-- Left Side - Input Description -->
                            <el-col :span="12">
                                <div class="editor-section">
                                    <div class="editor-header">
                                        <h3>Description</h3>
                                    </div>
                                    <MilkdownProvider class="min-h-[600px]">
                                        <MilkdownEditor
                                            ref="descriptionEditor"
                                            :content="userStory.description"
                                            placeholder="Please input User Story Description here..."
                                            @updated-content="onDescriptionUpdate"
                                        />
                                    </MilkdownProvider>
                                </div>
                            </el-col>

                            <!-- Right Side - AI Optimization -->
                            <el-col :span="12">
                                <div class="editor-section" >
                                    <div class="editor-header">
                                        <h3>AI Optimized Description</h3>
                                        <div>
                                            <el-button
                                                type="primary"
                                                :icon="Tools"
                                                :loading="optimizing"
                                                @click="optimizeDescription"
                                                :disabled="!userStory.description.trim()"
                                            >
                                                Optimize By AI
                                            </el-button>
                                            <el-button type="success" size="large" @click="saveUserStory" :loading="saving">
                                                Save User Story
                                            </el-button>
                                        </div>
                                    </div>
                                    <MilkdownProvider>
                                        <MilkdownEditor
                                            ref="optimizedEditor"
                                            :content="optimizedDescription"
                                            :readonly="true"
                                            :enable-copy="true"
                                            placeholder="AI optimized description will appear here..."
                                        />
                                    </MilkdownProvider>
                                </div>
                            </el-col>
                        </el-row>
                    </div>
                </el-tab-pane>

                <!-- Test Cases Tab -->
                <el-tab-pane label="Test Cases" name="testcases">
                    <div class="testcase-content">
                        <div class="testcase-header">
                            <el-button
                                type="success"
                                :icon="DataAnalysis"
                                :loading="generatingTestcases"
                                @click="generateTestcases"
                                :disabled="!userStory.description.trim()"
                            >
                                Generate by AI
                            </el-button>
                        </div>
                        <div class="testcase-editor">
                            <MilkdownProvider>
                                <MilkdownEditor
                                    ref="testcaseEditor"
                                    :content="generatedTestcases"
                                    :readonly="true"
                                    :enable-copy="true"
                                    placeholder="Generated test cases will appear here..."
                                />
                            </MilkdownProvider>
                        </div>
                    </div>
                </el-tab-pane>
            </el-tabs>
        </el-card>

        <!-- Action Buttons -->
        <div class="action-buttons">
            <el-button size="large" @click="cancel">Cancel</el-button>
            <el-button type="primary" size="large" @click="saveUserStory" :loading="saving">
                Save User Story
            </el-button>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Tools, DataAnalysis } from '@element-plus/icons-vue'
import MilkdownEditor from '@/components/MilkdownEditor.vue'
import { MilkdownProvider } from "@milkdown/vue"
import aiApi from '@/api/backend-api'

const router = useRouter()

// Reactive data
const activeTab = ref('description')
const optimizing = ref(false)
const generatingTestcases = ref(false)
const saving = ref(false)

const userStory = reactive({
    projectId: '',
    title: '',
    tags: [] as string[],
    ownerId: '',
    description: '',
})

const optimizedDescription = ref('')
const generatedTestcases = ref('')

// Mock data - replace with actual API calls
const projects = ref([
    { id: '1', name: 'Project Alpha' },
    { id: '2', name: 'Project Beta' },
    { id: '3', name: 'Project Gamma' }
])

const users = ref([
    { id: '1', username: 'john.doe' },
    { id: '2', username: 'jane.smith' },
    { id: '3', username: 'bob.wilson' }
])

const availableTags = ref([
    'frontend', 'backend', 'api', 'ui/ux', 'database',
    'authentication', 'performance', 'security', 'testing'
])

// Editor references
const descriptionEditor = ref()
const optimizedEditor = ref()
const testcaseEditor = ref()

// Methods
const onDescriptionUpdate = (content: string) => {
    userStory.description = content
}

const optimizeDescription = async () => {
    if (!userStory.description.trim()) {
        ElMessage.warning('Please enter a description first')
        return
    }
    if (!userStory.title.trim()) {
        ElMessage.warning('Please enter a title first')
            return
        }

    optimizing.value = true
    try {
        // Call the actual AI API
        const response = await aiApi.optimizeUserStory({
            description: userStory.description,
            title: userStory.title,
            projectContext: projects.value.find(p => p.id === userStory.projectId)?.name || '',
            additionalRequirements: ''
        })

        if (response.data.success) {
            // Format the optimized content            
            optimizedDescription.value = response.data.optimizedDescription
            optimizedEditor?.value.updateDefaultValue(optimizedDescription.value)
            ElMessage.success('Description optimized successfully!')
        } else {
            ElMessage.error(response.data.message || 'Failed to optimize description')
        }
    } catch (error) {
        ElMessage.error('Failed to optimize description')
        console.error('Optimization error:', error)
    } finally {
        optimizing.value = false
    }
}

const generateTestcases = async () => {
    if (!userStory.description.trim()) {
        ElMessage.warning('Please enter a description first')
        return
    }

    generatingTestcases.value = true
    try {
        // Call the actual AI API
        const response = await aiApi.generateTestCases({
            description: userStory.description,
            optimizedDescription: optimizedDescription.value,
            projectContext: projects.value.find(p => p.id === userStory.projectId)?.name || ''
        })

        if (response.data.success) {
            // Format the test cases
            let formattedTestCases = '# Test Cases\n\n'
            response.data.testCases.forEach((testCase, index) => {
                formattedTestCases += `${testCase}\n\n`
            })
            
            generatedTestcases.value = formattedTestCases
            ElMessage.success('Test cases generated successfully!')
        } else {
            ElMessage.error(response.data.message || 'Failed to generate test cases')
        }
    } catch (error) {
        ElMessage.error('Failed to generate test cases')
        console.error('Test case generation error:', error)
    } finally {
        generatingTestcases.value = false
    }
}

const saveUserStory = async () => {
    // Validation
    if (!userStory.projectId) {
        ElMessage.warning('Please select a project')
        return
    }
    if (!userStory.title.trim()) {
        ElMessage.warning('Please enter a title')
        return
    }
    if (!userStory.description.trim()) {
        ElMessage.warning('Please enter a description')
        return
    }

    saving.value = true
    try {
        // TODO: Replace with actual API call
        // const response = await userStoryApi.create({
        //   ...userStory,
        //   optimizedDescription: optimizedDescription.value,
        //   testcases: generatedTestcases.value
        // })

        await new Promise(resolve => setTimeout(resolve, 1000))
        ElMessage.success('User story saved successfully!')
        router.push('/user-stories')
    } catch (error) {
        ElMessage.error('Failed to save user story')
        console.error('Save error:', error)
    } finally {
        saving.value = false
    }
}

const cancel = async () => {
    const hasChanges = userStory.title || userStory.description || userStory.tags.length > 0

    if (hasChanges) {
        try {
            await ElMessageBox.confirm(
                'You have unsaved changes. Are you sure you want to leave?',
                'Confirm',
                {
                    confirmButtonText: 'Leave',
                    cancelButtonText: 'Stay',
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
    // TODO: Load initial data from APIs
    // await loadProjects()
    // await loadUsers()
    // await loadTags()
})
</script>

<style scoped>
.user-story-creation {
    padding: 20px;
    width: 100%;
    margin: 0 auto;
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

.section-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
}

.main-section {
    margin-bottom: 20px;
}

.description-content {
    height: 100%;
}

.editor-section {
    min-height: 500px;
    height: 100%;
    display: flex;
    flex-direction: column;
}

.editor-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    padding-bottom: 10px;
    border-bottom: 1px solid #ebeef5;
    height: 45px;
}

.editor-header h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
}

.testcase-content {
    display: flex;
    flex-direction: column;
}

.testcase-header {
    margin-bottom: 15px;
    text-align: right;
}

.testcase-editor {
    flex: 1;
}

.action-buttons {
    text-align: right;
    margin-top: 20px;
}

.action-buttons .el-button {
    margin-left: 10px;
}

:deep(.el-card__body) {
    padding: 20px;
}

:deep(.el-tabs__content) {
    padding: 0;
}

:deep(.el-form-item__label) {
    font-weight: 600;
    color: #606266;
}

:deep(.el-select .el-input__inner) {
    cursor: pointer;
}

@media (max-width: 768px) {
    .description-content .el-row {
        height: auto !important;
    }

    .description-content .el-col {
        margin-bottom: 20px;
    }

    .editor-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
    }

    .testcase-header {
        text-align: left;
    }
}
</style>