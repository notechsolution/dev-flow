<template>
    <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑提示词' : '创建提示词'"
        width="800px"
        :close-on-click-modal="false"
        @close="handleClose"
    >
        <el-form
            ref="formRef"
            :model="formData"
            :rules="rules"
            label-width="100px"
        >
            <el-form-item label="名称" prop="name">
                <el-input
                    v-model="formData.name"
                    placeholder="请输入提示词名称"
                    maxlength="50"
                    show-word-limit
                />
            </el-form-item>

            <el-form-item label="类型" prop="type">
                <el-select v-model="formData.type" placeholder="请选择类型" style="width: 100%">
                    <el-option label="需求澄清" value="REQUIREMENT_CLARIFICATION" />
                    <el-option label="需求优化" value="REQUIREMENT_OPTIMIZATION" />
                </el-select>
            </el-form-item>

            <el-form-item label="级别" prop="level">
                <el-select v-model="formData.level" placeholder="请选择级别" style="width: 100%">
                    <el-option label="用户级" value="USER" />
                    <el-option label="项目级" value="PROJECT" v-if="showProjectLevel" />
                    <el-option label="系统级" value="SYSTEM" v-if="isAdmin" />
                </el-select>
                <div class="level-hint">
                    <el-icon><InfoFilled /></el-icon>
                    <span>{{ getLevelHint(formData.level) }}</span>
                </div>
            </el-form-item>

            <el-form-item label="所属项目" prop="projectId" v-if="formData.level === 'PROJECT'">
                <el-select 
                    v-model="formData.projectId" 
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

            <el-form-item label="描述">
                <el-input
                    v-model="formData.description"
                    type="textarea"
                    :rows="2"
                    placeholder="请输入提示词描述（可选）"
                    maxlength="200"
                    show-word-limit
                />
            </el-form-item>

            <el-form-item label="提示词内容" prop="content">
                <el-input
                    v-model="formData.content"
                    type="textarea"
                    :rows="12"
                    placeholder="请输入提示词模板内容..."
                    show-word-limit
                />
                <div class="content-hint">
                    <el-icon><InfoFilled /></el-icon>
                    <span>支持 StringTemplate 语法，可使用变量如 $title$, $originalRequirement$ 等</span>
                </div>
            </el-form-item>

            <el-form-item label="状态">
                <el-switch
                    v-model="formData.enabled"
                    active-text="启用"
                    inactive-text="禁用"
                />
            </el-form-item>
        </el-form>

        <template #footer>
            <el-button @click="handleClose">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">
                {{ isEdit ? '保存' : '创建' }}
            </el-button>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { PromptTemplateRequest, PromptTemplateResponse } from '@/api/backend-api'
import { userStore } from '@/store/user'
import { useTemplateManagement } from '@/composables/useTemplateManagement'
import aiApi from '@/api/backend-api'

// Props
const props = defineProps<{
    modelValue: boolean
    template?: PromptTemplateResponse | null
    projectId?: string
}>()

// Emits
const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
}>()

// Use composable
const { createTemplate, updateTemplate } = useTemplateManagement()

// Data
const dialogVisible = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
})

const formRef = ref<FormInstance>()
const submitting = ref(false)
const projects = ref<Array<{ id: string; name: string }>>([])

const isEdit = computed(() => !!props.template)
const store = userStore()
const isAdmin = computed(() => store.role === 'ADMIN') // 需要根据实际角色判断
const showProjectLevel = computed(() => true) // 可以根据权限控制

const formData = reactive<PromptTemplateRequest>({
    name: '',
    type: 'REQUIREMENT_CLARIFICATION',
    level: 'USER',
    content: '',
    description: '',
    projectId: props.projectId,
    enabled: true
})

// Validation rules
const rules: FormRules = {
    name: [
        { required: true, message: '请输入提示词名称', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
    ],
    type: [
        { required: true, message: '请选择类型', trigger: 'change' }
    ],
    level: [
        { required: true, message: '请选择级别', trigger: 'change' }
    ],
    content: [
        { required: true, message: '请输入提示词内容', trigger: 'blur' },
        { min: 10, message: '内容至少 10 个字符', trigger: 'blur' }
    ],
    projectId: [
        { 
            required: true, 
            message: '请选择项目', 
            trigger: 'change',
            validator: (rule, value, callback) => {
                if (formData.level === 'PROJECT' && !value) {
                    callback(new Error('项目级提示词必须选择项目'))
                } else {
                    callback()
                }
            }
        }
    ]
}

// Methods
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
    }
}

const getLevelHint = (level: string) => {
    const hints: Record<string, string> = {
        'USER': '仅您自己可见和使用',
        'PROJECT': '项目成员可见和使用',
        'SYSTEM': '所有用户可见和使用（仅管理员可创建）'
    }
    return hints[level] || ''
}

const handleSubmit = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid) => {
        if (!valid) return

        submitting.value = true
        try {
            if (isEdit.value && props.template) {
                // Update existing template
                await updateTemplate(props.template.id, formData)
            } else {
                // Create new template
                await createTemplate(formData)
            }
            
            emit('success')
            handleClose()
        } catch (error) {
            console.error('Submit error:', error)
        } finally {
            submitting.value = false
        }
    })
}

const handleClose = () => {
    formRef.value?.resetFields()
    emit('update:modelValue', false)
}

// Watch template changes
watch(() => props.template, (newVal) => {
    if (newVal) {
        Object.assign(formData, {
            name: newVal.name,
            type: newVal.type,
            level: newVal.level,
            content: newVal.content,
            description: newVal.description || '',
            projectId: newVal.projectId,
            enabled: newVal.enabled
        })
    } else {
        // Reset form when creating new
        Object.assign(formData, {
            name: '',
            type: 'REQUIREMENT_CLARIFICATION',
            level: 'USER',
            content: '',
            description: '',
            projectId: props.projectId,
            enabled: true
        })
    }
}, { immediate: true })

// Lifecycle
onMounted(() => {
    loadProjects()
})

// Expose formData for parent component
defineExpose({
    formData,
    formRef
})
</script>

<style scoped lang="scss">
.level-hint,
.content-hint {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-top: 8px;
    font-size: 12px;
    color: #909399;

    .el-icon {
        font-size: 14px;
    }
}

:deep(.el-textarea__inner) {
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}
</style>
