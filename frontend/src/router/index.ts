import {createRouter, createWebHistory} from 'vue-router'

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

export default router;
