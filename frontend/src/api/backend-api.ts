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

// User Story CRUD interfaces
export interface CreateUserStoryRequest {
    title: string;
    projectId?: string;
    originalRequirement: string;
    clarificationQAs?: Array<{
        questionId: string;
        question: string;
        answer: string;
        category: string;
    }>;
    optimizedRequirement?: string;
    userStory?: string;
    acceptanceCriteria?: string;
    technicalNotes?: string;
    status?: string;
    priority?: string;
}

export interface UserStoryResponse {
    id: string;
    title: string;
    projectId?: string;
    originalRequirement: string;
    clarificationQAs?: Array<{
        questionId: string;
        question: string;
        answer: string;
        category: string;
    }>;
    optimizedRequirement?: string;
    userStory?: string;
    acceptanceCriteria?: string;
    technicalNotes?: string;
    status: string;
    priority?: string;
    ownerId: string;
    createdBy: string;
    createdAt: string;
    updatedBy: string;
    updatedAt: string;
}

export interface UserStoryListResponse {
    success: boolean;
    data: UserStoryResponse[];
    total: number;
}

export interface UserStoryDetailResponse {
    success: boolean;
    data: UserStoryResponse;
}

// User Management interfaces
export interface CreateUserRequest {
    username: string;
    email: string;
    password: string;
    role: string; // OPERATOR, ADMIN, USER
    projectIds?: string[];
}

export interface UpdateUserRequest {
    username: string;
    email: string;
    password?: string;
    role: string;
    projectIds?: string[];
}

export interface UserManagementResponse {
    id: string;
    username: string;
    email: string;
    role: string;
    projectIds?: string[];
    createdBy: string;
    createdAt: string;
    updatedBy: string;
    updatedAt: string;
}

export interface UserListResponse {
    success: boolean;
    data: UserManagementResponse[];
    total: number;
}

export interface UserDetailResponse {
    success: boolean;
    data: UserManagementResponse;
}

// Project Management interfaces
export interface GitRepositoryDTO {
    type?: string; // GITHUB, GITLAB, BITBUCKET, AZURE_DEVOPS
    baseUrl?: string;
    repositoryIds?: string[];
    accessToken?: string;
}

export interface ProjectManagementSystemDTO {
    type?: string; // JIRA, AZURE_DEVOPS, GITHUB_ISSUES, TRELLO
    systemId?: string;
    baseUrl?: string;
    accessToken?: string;
}

export interface CreateProjectRequest {
    name: string;
    description?: string;
    adminIds?: string[];
    memberIds?: string[];
    gitRepository?: GitRepositoryDTO;
    projectManagementSystem?: ProjectManagementSystemDTO;
}

export interface UpdateProjectRequest {
    name?: string;
    description?: string;
    status?: string;
    adminIds?: string[];
    memberIds?: string[];
    gitRepository?: GitRepositoryDTO;
    projectManagementSystem?: ProjectManagementSystemDTO;
}

export interface ProjectResponse {
    id: string;
    name: string;
    description?: string;
    ownerId: string;
    status: string;
    adminIds?: string[];
    memberIds?: string[];
    gitRepository?: {
        type?: string;
        baseUrl?: string;
        repositoryIds?: string[];
        // accessToken is not included in response for security
    };
    projectManagementSystem?: {
        type?: string;
        systemId?: string;
        baseUrl?: string;
        // accessToken is not included in response for security
    };
    createdAt: string;
    updatedAt: string;
}

export interface ProjectListResponse {
    success: boolean;
    data: ProjectResponse[];
    total: number;
}

export interface ProjectDetailResponse {
    success: boolean;
    data: ProjectResponse;
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
    },
    
    // User Story CRUD API methods
    createUserStory(request: CreateUserStoryRequest): Promise<AxiosResponse<UserStoryDetailResponse>> {
        return axiosApi.post('/user-stories', request, getHeaders());
    },
    
    getUserStory(id: string): Promise<AxiosResponse<UserStoryDetailResponse>> {
        return axiosApi.get(`/user-stories/${id}`, getHeaders());
    },
    
    getUserStories(params?: { projectId?: string; ownerId?: string; status?: string }): Promise<AxiosResponse<UserStoryListResponse>> {
        return axiosApi.get('/user-stories', { ...getHeaders(), params });
    },
    
    updateUserStory(id: string, request: CreateUserStoryRequest): Promise<AxiosResponse<UserStoryDetailResponse>> {
        return axiosApi.put(`/user-stories/${id}`, request, getHeaders());
    },
    
    updateUserStoryStatus(id: string, status: string): Promise<AxiosResponse<UserStoryDetailResponse>> {
        return axiosApi.patch(`/user-stories/${id}/status`, { status }, getHeaders());
    },
    
    deleteUserStory(id: string): Promise<AxiosResponse<{ success: boolean; message: string }>> {
        return axiosApi.delete(`/user-stories/${id}`, getHeaders());
    },
    
    // User Management API methods
    createUser(request: CreateUserRequest): Promise<AxiosResponse<UserDetailResponse>> {
        return axiosApi.post('/users', request, getHeaders());
    },
    
    getUser(id: string): Promise<AxiosResponse<UserDetailResponse>> {
        return axiosApi.get(`/users/${id}`, getHeaders());
    },
    
    getUsers(params?: { role?: string; projectId?: string }): Promise<AxiosResponse<UserListResponse>> {
        return axiosApi.get('/users', { ...getHeaders(), params });
    },
    
    updateUser(id: string, request: UpdateUserRequest): Promise<AxiosResponse<UserDetailResponse>> {
        return axiosApi.put(`/users/${id}`, request, getHeaders());
    },
    
    deleteUser(id: string): Promise<AxiosResponse<{ success: boolean; message: string }>> {
        return axiosApi.delete(`/users/${id}`, getHeaders());
    },
    
    // Project Management API methods
    createProject(request: CreateProjectRequest): Promise<AxiosResponse<ProjectDetailResponse>> {
        return axiosApi.post('/projects', request, getHeaders());
    },
    
    getProject(id: string): Promise<AxiosResponse<ProjectDetailResponse>> {
        return axiosApi.get(`/projects/${id}`, getHeaders());
    },
    
    getProjects(params?: { name?: string; status?: string }): Promise<AxiosResponse<ProjectListResponse>> {
        return axiosApi.get('/projects', { ...getHeaders(), params });
    },
    
    updateProject(id: string, request: UpdateProjectRequest): Promise<AxiosResponse<ProjectDetailResponse>> {
        return axiosApi.put(`/projects/${id}`, request, getHeaders());
    },
    
    deleteProject(id: string): Promise<AxiosResponse<{ success: boolean; message: string }>> {
        return axiosApi.delete(`/projects/${id}`, getHeaders());
    }
}


