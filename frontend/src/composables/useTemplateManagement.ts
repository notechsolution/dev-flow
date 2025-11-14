import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { promptTemplateApi, PromptTemplateResponse, PromptTemplateRequest } from '@/api/backend-api'

export type PromptLevel = 'SYSTEM' | 'PROJECT' | 'USER'
export type PromptType = 'REQUIREMENT_CLARIFICATION' | 'REQUIREMENT_OPTIMIZATION'

export function useTemplateManagement() {
    const loading = ref(false)
    const templates = ref<PromptTemplateResponse[]>([])
    const currentTemplate = ref<PromptTemplateResponse | null>(null)

    // 加载我的提示词
    const loadMyTemplates = async (type?: PromptType) => {
        loading.value = true
        try {
            const response = await promptTemplateApi.getMyTemplates(type)
            templates.value = response.data
            return response.data
        } catch (error) {
            ElMessage.error('加载提示词失败')
            console.error('Load my templates error:', error)
            return []
        } finally {
            loading.value = false
        }
    }

    // 加载项目提示词
    const loadProjectTemplates = async (projectId: string, type?: PromptType) => {
        loading.value = true
        try {
            const response = await promptTemplateApi.getProjectTemplates(projectId, type)
            templates.value = response.data
            return response.data
        } catch (error) {
            ElMessage.error('加载项目提示词失败')
            console.error('Load project templates error:', error)
            return []
        } finally {
            loading.value = false
        }
    }

    // 加载系统提示词
    const loadSystemTemplates = async (type?: PromptType) => {
        loading.value = true
        try {
            const response = await promptTemplateApi.getSystemTemplates(type)
            templates.value = response.data
            return response.data
        } catch (error) {
            ElMessage.error('加载系统提示词失败')
            console.error('Load system templates error:', error)
            return []
        } finally {
            loading.value = false
        }
    }

    // 获取单个提示词
    const getTemplateById = async (id: string) => {
        loading.value = true
        try {
            const response = await promptTemplateApi.getTemplateById(id)
            currentTemplate.value = response.data
            return response.data
        } catch (error) {
            ElMessage.error('获取提示词详情失败')
            console.error('Get template error:', error)
            return null
        } finally {
            loading.value = false
        }
    }

    // 创建提示词
    const createTemplate = async (data: PromptTemplateRequest) => {
        loading.value = true
        try {
            const response = await promptTemplateApi.createTemplate(data)
            ElMessage.success('创建提示词成功')
            return response.data
        } catch (error: any) {
            ElMessage.error(error.response?.data?.message || '创建提示词失败')
            console.error('Create template error:', error)
            return null
        } finally {
            loading.value = false
        }
    }

    // 更新提示词
    const updateTemplate = async (id: string, data: PromptTemplateRequest) => {
        loading.value = true
        try {
            const response = await promptTemplateApi.updateTemplate(id, data)
            ElMessage.success('更新提示词成功')
            return response.data
        } catch (error: any) {
            ElMessage.error(error.response?.data?.message || '更新提示词失败')
            console.error('Update template error:', error)
            return null
        } finally {
            loading.value = false
        }
    }

    // 删除提示词
    const deleteTemplate = async (id: string) => {
        loading.value = true
        try {
            await promptTemplateApi.deleteTemplate(id)
            ElMessage.success('删除提示词成功')
            // 从列表中移除
            templates.value = templates.value.filter(t => t.id !== id)
            return true
        } catch (error: any) {
            ElMessage.error(error.response?.data?.message || '删除提示词失败')
            console.error('Delete template error:', error)
            return false
        } finally {
            loading.value = false
        }
    }

    // 获取有效提示词（按优先级）
    const getEffectiveTemplate = async (type: PromptType, projectId?: string) => {
        loading.value = true
        try {
            const response = await promptTemplateApi.getEffectiveTemplate(type, projectId)
            return response.data
        } catch (error) {
            console.error('Get effective template error:', error)
            return null
        } finally {
            loading.value = false
        }
    }

    return {
        loading,
        templates,
        currentTemplate,
        loadMyTemplates,
        loadProjectTemplates,
        loadSystemTemplates,
        getTemplateById,
        createTemplate,
        updateTemplate,
        deleteTemplate,
        getEffectiveTemplate
    }
}
