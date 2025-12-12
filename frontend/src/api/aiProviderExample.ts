// 示例：在需求澄清流程中使用AI提供商选择器

import axios from 'axios';

// 类型定义
interface ClarificationRequest {
  originalRequirement: string;
  title: string;
  projectContext: string;
  projectId: string;
  provider?: string;  // 可选的AI提供商
}

interface OptimizationRequest {
  originalRequirement: string;
  title: string;
  projectContext: string;
  projectId: string;
  provider?: string;
  clarificationAnswers: Array<{
    questionId: string;
    question: string;
    answer: string;
    category: string;
  }>;
}

interface UserStoryOptimizationRequest {
  description: string;
  title: string;
  projectContext: string;
  projectId: string;
  provider?: string;
}

interface TestCaseGenerationRequest {
  description: string;
  optimizedDescription?: string;
  projectContext: string;
  provider?: string;
}

// 使用示例1：需求澄清流程
export async function clarifyRequirement(
  requirement: ClarificationRequest
) {
  try {
    const response = await axios.post('/api/ai/clarify-requirement', requirement);
    return response.data;
  } catch (error) {
    console.error('Error clarifying requirement:', error);
    throw error;
  }
}

// 使用示例2：优化需求
export async function optimizeRequirement(
  request: OptimizationRequest
) {
  try {
    const response = await axios.post('/api/ai/optimize-requirement', request);
    return response.data;
  } catch (error) {
    console.error('Error optimizing requirement:', error);
    throw error;
  }
}

// 使用示例3：优化User Story
export async function optimizeUserStory(
  request: UserStoryOptimizationRequest
) {
  try {
    const response = await axios.post('/api/ai/optimize-user-story', request);
    return response.data;
  } catch (error) {
    console.error('Error optimizing user story:', error);
    throw error;
  }
}

// 使用示例4：生成测试用例
export async function generateTestCases(
  request: TestCaseGenerationRequest
) {
  try {
    const response = await axios.post('/api/ai/generate-test-cases', request);
    return response.data;
  } catch (error) {
    console.error('Error generating test cases:', error);
    throw error;
  }
}

// 完整的需求优化流程示例
export async function completeRequirementOptimizationFlow(
  originalRequirement: string,
  title: string,
  projectContext: string,
  projectId: string,
  selectedProvider: string = ''  // 用户选择的提供商
) {
  try {
    // 步骤1：生成澄清问题
    console.log(`使用 ${selectedProvider || '默认'} 提供商生成澄清问题...`);
    const clarificationResponse = await clarifyRequirement({
      originalRequirement,
      title,
      projectContext,
      projectId,
      provider: selectedProvider
    });

    if (!clarificationResponse.success) {
      throw new Error('Failed to generate clarification questions');
    }

    // 步骤2：收集用户答案（这里模拟自动回答）
    const clarificationAnswers = clarificationResponse.questions.map((q: any) => ({
      questionId: q.id,
      question: q.question,
      answer: '示例答案',  // 实际应用中由用户填写
      category: q.category
    }));

    // 步骤3：基于答案优化需求
    console.log(`使用 ${selectedProvider || '默认'} 提供商优化需求...`);
    const optimizationResponse = await optimizeRequirement({
      originalRequirement,
      title,
      projectContext,
      projectId,
      provider: selectedProvider,  // 使用相同的提供商保持一致性
      clarificationAnswers
    });

    if (!optimizationResponse.success) {
      throw new Error('Failed to optimize requirement');
    }

    // 步骤4：生成测试用例
    console.log(`使用 ${selectedProvider || '默认'} 提供商生成测试用例...`);
    const testCasesResponse = await generateTestCases({
      description: originalRequirement,
      optimizedDescription: optimizationResponse.optimizedRequirement,
      projectContext,
      provider: selectedProvider
    });

    return {
      clarificationQuestions: clarificationResponse.questions,
      optimizedRequirement: optimizationResponse.optimizedRequirement,
      userStory: optimizationResponse.userStory,
      acceptanceCriteria: optimizationResponse.acceptanceCriteria,
      technicalNotes: optimizationResponse.technicalNotes,
      testCases: testCasesResponse.testCases
    };

  } catch (error) {
    console.error('Error in requirement optimization flow:', error);
    throw error;
  }
}

// Vue组件使用示例
export const useAIProviderInComponent = () => {
  return {
    // 在组件中的使用示例
    async handleClarifyRequirement(
      requirement: string,
      title: string,
      selectedProvider: string
    ) {
      const projectContext = 'Your project context';
      const projectId = 'your-project-id';

      const result = await clarifyRequirement({
        originalRequirement: requirement,
        title,
        projectContext,
        projectId,
        provider: selectedProvider  // 传递用户选择的提供商
      });

      return result;
    }
  };
};
