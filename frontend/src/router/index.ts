import {createRouter, createWebHistory} from 'vue-router'
import { userStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const routes = [
    {
        path: '/login',
        component: () => import('@/views/Login.vue')
    },
    {
        path: '/register',
        component: () => import('@/views/Register.vue')
    },
    {
        path: '/forgot-password',
        component: () => import('@/views/ForgotPassword.vue')
    },
    {
        path: '/reset-password',
        component: () => import('@/views/ResetPassword.vue')
    },
    {
        path: '/',
        component: () => import('@/views/Home.vue'),
        redirect: '/dashboard',
        children: [
            {
                path: '/requirement/UserStoryCreation',
                component: () => import('@/views/requirement/UserStoryCreation.vue')
            },
            {
                path: '/requirement/UserStoryDetail/:id',
                component: () => import('@/views/requirement/UserStoryDetail.vue')
            },
            {
                path: '/requirement/UserStoryEdit/:id',
                component: () => import('@/views/requirement/UserStoryCreation.vue')
            },
            {
                path: "/dashboard",
                component: () => import('@/views/Dashboard.vue')
            },
            {
                path: "/users",
                component: () => import('@/views/user/UserManagement.vue')
            },
            {
                path: "/profile",
                component: () => import('@/views/user/UserProfile.vue')
            },
            {
                path: "/change-password",
                component: () => import('@/views/user/ChangePassword.vue')
            },
            {
                path: "/projects",
                component: () => import('@/views/ProjectManagement.vue')
            },
            {
                path: "/projects/new",
                component: () => import('@/views/ProjectForm.vue')
            },
            {
                path: "/projects/edit/:id",
                component: () => import('@/views/ProjectForm.vue')
            },
            {
                path: "/prompt-templates",
                component: () => import('@/views/prompt-templates/PromptTemplateManagement.vue')
            },
            {
                path: "/user/prompt-settings",
                component: () => import('@/views/user/UserPromptSettings.vue')
            },
            {
                path: "/admin/ai-providers",
                component: () => import('@/views/admin/AIProviderManagement.vue'),
                meta: { requiresOperator: true }
            }
        ]
    },
    // otherwise redirect to home
    {path: '/:pathMatch(.*)*', redirect: '/'}
]

const router = createRouter({
    history: createWebHistory(), // uris without hashes #, see https://router.vuejs.org/guide/essentials/history-mode.html#html5-history-mode
    routes
});

// Navigation guard for role-based access control
router.beforeEach((to, from, next) => {
    const user = userStore();
    
    // Check if route requires OPERATOR role
    if (to.meta.requiresOperator) {
        if (user.role !== 'OPERATOR') {
            ElMessage.error('此页面仅限运维人员访问');
            next(from.path || '/dashboard');
            return;
        }
    }
    
    next();
});

export default router;
