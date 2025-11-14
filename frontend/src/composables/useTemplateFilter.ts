import { ref, computed, Ref } from 'vue'
import { PromptTemplateResponse } from '@/api/backend-api'

export type PromptType = 'REQUIREMENT_CLARIFICATION' | 'REQUIREMENT_OPTIMIZATION' | 'ALL'
export type PromptLevel = 'SYSTEM' | 'PROJECT' | 'USER' | 'ALL'

export interface FilterOptions {
    type: PromptType
    level: PromptLevel
    keyword: string
    enabled: boolean | null
    projectId?: string
}

export function useTemplateFilter(templates: Ref<PromptTemplateResponse[]> | PromptTemplateResponse[]) {
    const filterOptions = ref<FilterOptions>({
        type: 'ALL',
        level: 'ALL',
        keyword: '',
        enabled: null
    })

    // 过滤后的模板列表
    const filteredTemplates = computed(() => {
        const templateList = Array.isArray(templates) ? templates : templates.value
        let result = [...templateList]

        // 按类型过滤
        if (filterOptions.value.type !== 'ALL') {
            result = result.filter(t => t.type === filterOptions.value.type)
        }

        // 按级别过滤
        if (filterOptions.value.level !== 'ALL') {
            result = result.filter(t => t.level === filterOptions.value.level)
        }

        // 按关键词过滤（名称、描述、内容）
        if (filterOptions.value.keyword) {
            const keyword = filterOptions.value.keyword.toLowerCase()
            result = result.filter(t => 
                t.name.toLowerCase().includes(keyword) ||
                t.description?.toLowerCase().includes(keyword) ||
                t.content.toLowerCase().includes(keyword)
            )
        }

        // 按启用状态过滤
        if (filterOptions.value.enabled !== null) {
            result = result.filter(t => t.enabled === filterOptions.value.enabled)
        }

        // 按项目过滤
        if (filterOptions.value.projectId) {
            result = result.filter(t => t.projectId === filterOptions.value.projectId)
        }

        return result
    })

    // 重置过滤条件
    const resetFilters = () => {
        filterOptions.value = {
            type: 'ALL',
            level: 'ALL',
            keyword: '',
            enabled: null
        }
    }

    // 按类型分组
    const groupedByType = computed(() => {
        const groups: Record<string, PromptTemplateResponse[]> = {
            REQUIREMENT_CLARIFICATION: [],
            REQUIREMENT_OPTIMIZATION: []
        }

        filteredTemplates.value.forEach(template => {
            if (groups[template.type]) {
                groups[template.type].push(template)
            }
        })

        return groups
    })

    // 按级别分组
    const groupedByLevel = computed(() => {
        const groups: Record<string, PromptTemplateResponse[]> = {
            SYSTEM: [],
            PROJECT: [],
            USER: []
        }

        filteredTemplates.value.forEach(template => {
            if (groups[template.level]) {
                groups[template.level].push(template)
            }
        })

        return groups
    })

    // 统计信息
    const statistics = computed(() => {
        const templateList = Array.isArray(templates) ? templates : templates.value
        return {
            total: templateList.length,
            filtered: filteredTemplates.value.length,
            byType: {
                clarification: templateList.filter((t: PromptTemplateResponse) => t.type === 'REQUIREMENT_CLARIFICATION').length,
                optimization: templateList.filter((t: PromptTemplateResponse) => t.type === 'REQUIREMENT_OPTIMIZATION').length
            },
            byLevel: {
                system: templateList.filter((t: PromptTemplateResponse) => t.level === 'SYSTEM').length,
                project: templateList.filter((t: PromptTemplateResponse) => t.level === 'PROJECT').length,
                user: templateList.filter((t: PromptTemplateResponse) => t.level === 'USER').length
            },
            enabled: templateList.filter((t: PromptTemplateResponse) => t.enabled).length,
            disabled: templateList.filter((t: PromptTemplateResponse) => !t.enabled).length
        }
    })

    return {
        filterOptions,
        filteredTemplates,
        groupedByType,
        groupedByLevel,
        statistics,
        resetFilters
    }
}
