<template>
    <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div class="max-w-md w-full space-y-8">
            <div class="text-center">
                <h2 class="mt-6 text-3xl font-extrabold text-gray-900">Welcome to DevFlow</h2>
                <p class="mt-2 text-sm text-gray-600">Sign in to your account to continue</p>
            </div>

            <div class="bg-white shadow-xl rounded-lg px-8 py-8">
                <el-form
                    ref="loginForm"
                    :model="loginData"
                    :rules="rules"
                    class="space-y-6"
                    @submit.prevent="handleLogin"
                >
                    <el-form-item prop="username">
                        <label class="block text-sm font-medium text-gray-700 mb-2">Username</label>
                        <el-input
                            v-model="loginData.username"
                            placeholder="Enter your username"
                            size="large"
                            :prefix-icon="User"
                            class="w-full"
                        />
                    </el-form-item>

                    <el-form-item prop="password">
                        <label class="block text-sm font-medium text-gray-700 mb-2">Password</label>
                        <el-input
                            v-model="loginData.password"
                            type="password"
                            placeholder="Enter your password"
                            size="large"
                            :prefix-icon="Lock"
                            show-password
                            class="w-full"
                        />
                    </el-form-item>

                    <div class="flex items-center justify-between">
                        <el-checkbox v-model="loginData.rememberMe">
                            Remember me
                        </el-checkbox>
                    </div>

                    <el-form-item>
                        <el-button
                            type="primary"
                            size="large"
                            class="w-full"
                            :loading="loading"
                            @click="handleLogin"
                        >
                            Sign In
                        </el-button>
                    </el-form-item>
                </el-form>

                <div class="mt-6 text-center space-y-2">
                    <p class="text-sm text-gray-600">
                        <el-button type="text" @click="goToForgotPassword" class="font-medium text-indigo-600 hover:text-indigo-500">
                            Forgot your password?
                        </el-button>
                    </p>
                    <p class="text-sm text-gray-600">
                        Don't have an account?
                        <el-button type="text" @click="goToRegister" class="font-medium text-indigo-600 hover:text-indigo-500">
                            Register here
                        </el-button>
                    </p>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { userStore } from '@/store/user'
import userApi from '@/api/user-api'

export default defineComponent({
    name: 'LoginPage',
    components: { User, Lock },
    setup() {
        const router = useRouter()
        const store = userStore()
        const loginForm = ref()
        const loading = ref(false)

        const loginData = reactive({
            username: '',
            password: '',
            rememberMe: false
        })

        const rules = {
            username: [
                { required: true, message: 'Please enter your username', trigger: 'blur' },
                { min: 3, max: 20, message: 'Username should be 3-20 characters', trigger: 'blur' }
            ],
            password: [
                { required: true, message: 'Please enter your password', trigger: 'blur' },
                { min: 6, message: 'Password should be at least 6 characters', trigger: 'blur' }
            ]
        }

        const handleLogin = async () => {
            try {
                const valid = await loginForm.value.validate()
                if (!valid) return

                loading.value = true
                const response = await userApi.login({
                    username: loginData.username,
                    password: loginData.password,
                    rememberMe: loginData.rememberMe
                })

                if (response.data.success) {
                    const user = response.data.user;
                    store.login(user.id || '', user.username, user.email || '', user.role, user.projectIds || [])
                    ElMessage.success('Login successful!')
                    router.push('/')
                } else {
                    ElMessage.error(response.data.message || 'Login failed')
                }
            } catch (error: any) {
                ElMessage.error(error.response?.data?.message || 'Login failed. Please try again.')
            } finally {
                loading.value = false
            }
        }

        const goToRegister = () => {
            router.push('/register')
        }

        const goToForgotPassword = () => {
            router.push('/forgot-password')
        }

        return {
            loginData,
            rules,
            loading,
            loginForm,
            handleLogin,
            goToRegister,
            goToForgotPassword,
            User, Lock
        }
    }
})
</script>