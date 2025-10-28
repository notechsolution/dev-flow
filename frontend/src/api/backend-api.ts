import axios, { AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElNotification } from "element-plus";
import router from '../router/index';

export const axiosApi = axios.create({
    baseURL: `/api`,
    headers: { 'Content-Type': 'application/json' }
});

export interface User {
    username: string;
    DID: string;
    role: string;
    companyId: string;
    createdDate: string;
    updatedDate: string;
}

export interface CompanyDTO {
    companyName: string;
    companyRole: string;
    companyLegalName: string;
    companyLegalManPhoneNumber: string;
    companyLegalManEmail: string;
    isCertified: boolean;
}

export interface Company extends CompanyDTO {
    id: string;
    companyDID: string;
    issueVcType: string;
    issueVcId: string;
    issuerName: string;
    issuedVcOpTo: string;
    issuedVcOpFrom: string;
    legalName: string;
    entname: string;
    opscope: string;
    uniscid: string;
    dom: string;
    issueVcIssuanceDate: string;
    issueVcExpirationDate: string;
}

export interface LoginResultDTO {
    requestId: number;
    did: string;
    username: string;
    companyId: string;
    companyName: string;
    allowLogin: boolean;
}

export interface UserRegistrationDTO {
    username: string;
    password: string;
}

function getHeaders(): AxiosRequestConfig {
    const userDID = window.localStorage.getItem("user-did");
    return {
        headers: {
            'x-request-user-did': userDID
        }
    } as AxiosRequestConfig
}

// global exception handler
axiosApi.interceptors.response.use((response) => {
    return Promise.resolve(response);
}, error => {
    if (error.response.status === 401 || error.response.status === 403) {
        // if got 401 or 403 error in login request, notify user to login again
        if (error.response.config.url === '/auth/login') {
            ElNotification({
                title: 'Login Failed',
                message: 'Please check your username and password, and try again.',
                type: 'error',
                duration: 6000,
            })
            return Promise.reject(error);
        }
        console.log('unauthorized request found', error)
        return router.push('/landing');
    } else {
        console.log('err', error)
        ElNotification({
            title: 'Request Failed',
            message: error.response.data || error.message,
            type: 'error'
        })
        return Promise.reject(error);
    }
})

// AI API interfaces
export interface UserStoryOptimizationRequest {
    description: string;
    title: string;
    projectContext?: string;
    additionalRequirements?: string;
}

export interface UserStoryOptimizationResponse {
    optimizedDescription: string;
    acceptanceCriteria: string;
    definitionOfDone: string;
    success: boolean;
    message: string;
}

export interface TestCaseGenerationRequest {
    description: string;
    optimizedDescription?: string;
    projectContext?: string;
}

export interface TestCaseGenerationResponse {
    testCases: string[];
    success: boolean;
    message: string;
}

export interface ClarificationQuestion {
    id: string;
    question: string;
    category: string;
    answer?: string;
}

export interface RequirementClarificationRequest {
    originalRequirement: string;
    title?: string;
    projectContext?: string;
}

export interface RequirementClarificationResponse {
    questions: ClarificationQuestion[];
    success: boolean;
    message: string;
}

export interface QuestionAnswer {
    questionId: string;
    question: string;
    answer: string;
    category: string;
}

export interface RequirementOptimizationRequest {
    originalRequirement: string;
    title?: string;
    projectContext?: string;
    clarificationAnswers: QuestionAnswer[];
}

export interface RequirementOptimizationResponse {
    optimizedRequirement: string;
    userStory: string;
    acceptanceCriteria: string;
    technicalNotes: string;
    success: boolean;
    message: string;
}

export default {
    logout(): Promise<AxiosResponse<any>> {
        return axiosApi.post('/auth/logout', null, getHeaders());
    },

    // AI API methods
    optimizeUserStory(request: UserStoryOptimizationRequest): Promise<AxiosResponse<UserStoryOptimizationResponse>> {
        return axiosApi.post('/ai/optimize-user-story', request, getHeaders());
    },

    generateTestCases(request: TestCaseGenerationRequest): Promise<AxiosResponse<TestCaseGenerationResponse>> {
        return axiosApi.post('/ai/generate-test-cases', request, getHeaders());
    },
    
    clarifyRequirement(request: RequirementClarificationRequest): Promise<AxiosResponse<RequirementClarificationResponse>> {
        return axiosApi.post('/ai/clarify-requirement', request, getHeaders());
    },
    
    optimizeRequirement(request: RequirementOptimizationRequest): Promise<AxiosResponse<RequirementOptimizationResponse>> {
        return axiosApi.post('/ai/optimize-requirement', request, getHeaders());
    }
}


