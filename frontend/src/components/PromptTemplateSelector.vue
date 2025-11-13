<template>
    <div class="prompt-template-selector">
        <el-card class="prompt-card">
            <template #header>
                <div class="card-header">
                    <span>{{ title }}</span>
                    <el-tooltip content="提示词用于指导AI生成内容，您可以选择或自定义提示词" placement="top">
                        <el-icon><QuestionFilled /></el-icon>
                    </el-tooltip>
                </div>
            </template>

            <!-- 提示词选择 -->
            <el-form-item label="选择提示词">
                <el-select
                    v-model="selectedTemplateId"
                    placeholder="请选择提示词模板"
                    style="width: 100%"
                    @change="onTemplateChange"
                >
                    <el-option label="系统默认" value="SYSTEM" />
                    <el-option
                        v-if="projectTemplate"
                        :label="`项目预设: ${projectTemplate.name}`"
                        :value="projectTemplate.id"
                    />
                    <el-option-group label="我的提示词" v-if="userTemplates.length > 0">
                        <el-option
                            v-for="template in userTemplates"
                            :key="template.id"
                            :label="template.name"
                            :value="template.id"
                        />
                    </el-option-group>
                </el-select>
            </el-form-item>

            <!-- 提示词内容预览/编辑 -->
            <el-form-item label="提示词内容">
                <el-input
                    v-model="promptContent"
                    type="textarea"
                    :rows="10"
                    placeholder="提示词内容..."
                    :readonly="!allowEdit"
                />
                <div class="template-info" v-if="currentTemplate">
                    <el-tag :type="getLevelType(currentTemplate.level)" size="small">
                        {{ getLevelLabel(currentTemplate.level) }}
                    </el-tag>
                    <span class="template-name">{{ currentTemplate.name }}</span>
                    <span class="template-desc" v-if="currentTemplate.description">
                        - {{ currentTemplate.description }}
                    </span>
                </div>
            </el-form-item>

            <!-- 操作按钮 -->
            <div class="action-buttons">
                <el-checkbox v-model="allowEdit" @change="onEditModeChange">
                    允许编辑提示词
                </el-checkbox>
                <div class="button-group">
                    <el-button
                        v-if="allowEdit && contentModified"
                        @click="saveAsMyTemplate"
                        type="primary"
                        :icon="Document"
                    >
                        保存为我的提示词
                    </el-button>
                    <el-button @click="resetToOriginal" v-if="contentModified">
                        还原
                    </el-button>
                </div>
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { QuestionFilled, Document } from '@element-plus/icons-vue'
import { promptTemplateApi, PromptTemplateResponse } from '@/api/backend-api'

// Props
const props = defineProps<{
    type: 'REQUIREMENT_CLARIFICATION' | 'REQUIREMENT_OPTIMIZATION'
    projectId?: string
    userId?: string | null
    title?: string
}>()

// Emits
const emit = defineEmits<{
    (e: 'template-selected', templateId: string | null, content: string): void
}>()

// State
const selectedTemplateId = ref<string | null>("SYSTEM")
const promptContent = ref('')
const originalContent = ref('')
const allowEdit = ref(false)
const currentTemplate = ref<PromptTemplateResponse | null>(null)
const systemTemplate = ref<PromptTemplateResponse | null>(null)
const projectTemplate = ref<PromptTemplateResponse | null>(null)
const userTemplates = ref<PromptTemplateResponse[]>([])

// Computed
const contentModified = computed(() => {
    console.log('userId :', props.userId)
    return promptContent.value !== originalContent.value
})

// Methods
const loadTemplates = async () => {
    try {
        // Load system default
        const systemResp = await promptTemplateApi.getSystemDefaultTemplate(props.type)
        systemTemplate.value = systemResp.data
        
        // Load project templates if projectId provided
        if (props.projectId) {
            const projectResp = await promptTemplateApi.getProjectTemplates(props.projectId, props.type)
            projectTemplate.value = projectResp.data.length > 0 ? projectResp.data[0] : null
        }
        
        // Load user templates
        const userResp = await promptTemplateApi.getMyTemplates(props.type)
        userTemplates.value = userResp.data
        
        // Load effective template by default
        if (props.projectId) {
            const effectiveResp = await promptTemplateApi.getEffectiveTemplate(props.type, props.projectId)
            if (effectiveResp.data) {
                currentTemplate.value = effectiveResp.data
                promptContent.value = effectiveResp.data.content
                originalContent.value = effectiveResp.data.content
                
                // Set selected template ID
                if (effectiveResp.data.level === 'USER') {
                    selectedTemplateId.value = effectiveResp.data.id
                } else if (effectiveResp.data.level === 'PROJECT') {
                    selectedTemplateId.value = projectTemplate.value?.id || null
                }
            }
        } else {
            // Use system default
            currentTemplate.value = systemTemplate.value
            promptContent.value = systemTemplate.value?.content || ''
            originalContent.value = systemTemplate.value?.content || ''
        }
    } catch (error) {
        console.error('Failed to load prompt templates:', error)
        ElMessage.error('加载提示词模板失败')
    }
}

const onTemplateChange = async (templateId: string | null) => {
    if (templateId === "SYSTEM") {
        // System default
        currentTemplate.value = systemTemplate.value
        promptContent.value = systemTemplate.value?.content || ''
    } else if (templateId === projectTemplate.value?.id) {
        // Project template
        currentTemplate.value = projectTemplate.value
        promptContent.value = projectTemplate.value?.content || ''
    } else {
        // User template
        const userTemplate = userTemplates.value.find(t => t.id === templateId)
        if (userTemplate) {
            currentTemplate.value = userTemplate
            promptContent.value = userTemplate.content
        }
    }
    originalContent.value = promptContent.value
    emit('template-selected', templateId, promptContent.value)
}

const onEditModeChange = (value: boolean) => {
    if (!value && contentModified.value) {
        ElMessageBox.confirm(
            '您对提示词的修改尚未保存，关闭编辑模式将丢失修改。确定要继续吗？',
            '确认',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        ).then(() => {
            resetToOriginal()
        }).catch(() => {
            allowEdit.value = true
        })
    }
}

const saveAsMyTemplate = async () => {
    try {
        const { value: name } = await ElMessageBox.prompt('请输入提示词模板名称', '保存为我的提示词', {
            confirmButtonText: '保存',
            cancelButtonText: '取消',
            inputPattern: /^.{1,50}$/,
            inputErrorMessage: '名称长度必须在1-50个字符之间'
        })
        
        if (!name) return
        
        await promptTemplateApi.createTemplate({
            name: name,
            type: props.type,
            userId: props.userId || '',
            level: 'USER',
            content: promptContent.value,
            description: `基于${currentTemplate.value?.name || '系统默认'}修改`,
            enabled: true
        })
        
        ElMessage.success('保存成功')
        
        // Reload templates
        await loadTemplates()
        
    } catch (error: any) {
        if (error !== 'cancel') {
            console.error('Failed to save template:', error)
            ElMessage.error('保存失败')
        }
    }
}

const resetToOriginal = () => {
    promptContent.value = originalContent.value
}

const getLevelType = (level: string) => {
    switch (level) {
        case 'SYSTEM': return 'info'
        case 'PROJECT': return 'warning'
        case 'USER': return 'success'
        default: return ''
    }
}

const getLevelLabel = (level: string) => {
    switch (level) {
        case 'SYSTEM': return '系统'
        case 'PROJECT': return '项目'
        case 'USER': return '用户'
        default: return ''
    }
}

// Watch for content changes
watch(promptContent, (newVal) => {
    emit('template-selected', selectedTemplateId.value, newVal)
})

// Lifecycle
onMounted(() => {
    loadTemplates()
})
</script>

<style scoped lang="scss">
.prompt-template-selector {
    margin: 20px 0;

    .prompt-card {
        .card-header {
            display: flex;
            align-items: center;
            gap: 8px;
            font-weight: 600;

            .el-icon {
                color: #909399;
                cursor: help;
            }
        }
    }

    .template-info {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-top: 8px;
        font-size: 13px;
        color: #606266;

        .template-name {
            font-weight: 500;
        }

        .template-desc {
            color: #909399;
        }
    }

    .action-buttons {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 16px;
        padding-top: 16px;
        border-top: 1px solid #EBEEF5;

        .button-group {
            display: flex;
            gap: 8px;
        }
    }
}
</style>
