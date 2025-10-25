<template>
    <div class="min-h-screen bg-gradient-to-br from-green-50 to-emerald-100 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div class="max-w-md w-full space-y-8">
            <div class="text-center">
                <h2 class="mt-6 text-3xl font-extrabold text-gray-900">Create Your Account</h2>
                <p class="mt-2 text-sm text-gray-600">Join Development Flow and start managing your projects</p>
            </div>

            <div class="bg-white shadow-xl rounded-lg px-8 py-8">
                <el-form
                    ref="registerForm"
                    :model="registerData"
                    :rules="rules"
                    class="space-y-6"
                    @submit.prevent="handleRegister"
                >
                    <el-form-item prop="username">
                        <label class="block text-sm font-medium text-gray-700 mb-2">Username</label>
                        <el-input
                            v-model="registerData.username"
                            placeholder="Enter your username"
                            size="large"
                            :prefix-icon="User"
                            class="w-full"
                        />
                    </el-form-item>

                    <el-form-item prop="email">
                        <label class="block text-sm font-medium text-gray-700 mb-2">Email</label>
                        <el-input
                            v-model="registerData.email"
                            type="email"
                            placeholder="Enter your email"
                            size="large"
                            :prefix-icon="Message"
                            class="w-full"
                        />
                    </el-form-item>

                    <el-form-item prop="password">
                        <label class="block text-sm font-medium text-gray-700 mb-2">Password</label>
                        <el-input
                            v-model="registerData.password"
                            type="password"
                            placeholder="Enter your password"
                            size="large"
                            :prefix-icon="Lock"
                            show-password
                            class="w-full"
                        />
                    </el-form-item>

                    <el-form-item prop="confirmPassword">
                        <label class="block text-sm font-medium text-gray-700 mb-2">Confirm Password</label>
                        <el-input
                            v-model="registerData.confirmPassword"
                            type="password"
                            placeholder="Confirm your password"
                            size="large"
                            :prefix-icon="Lock"
                            show-password
                            class="w-full"
                        />
                    </el-form-item>

                    <el-form-item>
                        <el-button
                            type="success"
                            size="large"
                            class="w-full"
                            :loading="loading"
                            @click="handleRegister"
                        >
                            Create Account
                        </el-button>
                    </el-form-item>
                </el-form>

                <div class="mt-6 text-center">
                    <p class="text-sm text-gray-600">
                        Already have an account?
                        <el-button type="text" @click="goToLogin" class="font-medium text-emerald-600 hover:text-emerald-500">
                            Sign in here
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
import { User, Lock, Message } from '@element-plus/icons-vue'
import userApi from '@/api/user-api'

export default defineComponent({
    name: 'RegisterPage',
    components: { User, Lock, Message },
    setup() {
        const router = useRouter()
        const registerForm = ref()
        const loading = ref(false)

        const registerData = reactive({
            username: '',
            email: '',
            password: '',
            confirmPassword: ''
        })

        const validateConfirmPassword = (rule: any, value: string, callback: any) => {
            if (value !== registerData.password) {
                callback(new Error('Passwords do not match'))
            } else {
                callback()
            }
        }

        const rules = {
            username: [
                { required: true, message: 'Please enter your username', trigger: 'blur' },
                { min: 3, max: 20, message: 'Username should be 3-20 characters', trigger: 'blur' },
                { pattern: /^[a-zA-Z0-9_]+$/, message: 'Username can only contain letters, numbers, and underscores', trigger: 'blur' }
            ],
            email: [
                { required: true, message: 'Please enter your email', trigger: 'blur' },
                { type: 'email', message: 'Please enter a valid email address', trigger: 'blur' }
            ],
            password: [
                { required: true, message: 'Please enter your password', trigger: 'blur' },
                { min: 8, message: 'Password should be at least 8 characters', trigger: 'blur' }
            ],
            confirmPassword: [
                { required: true, message: 'Please confirm your password', trigger: 'blur' },
                { validator: validateConfirmPassword, trigger: 'blur' }
            ]
        }

        const handleRegister = async () => {
            try {
                const valid = await registerForm.value.validate()
                if (!valid) return

                loading.value = true
                const response = await userApi.register({
                    username: registerData.username,
                    email: registerData.email,
                    password: registerData.password
                })

                if (response.data.success) {
                    ElMessage.success('Registration successful! Please login.')
                    router.push('/login')
                } else {
                    ElMessage.error(response.data.message || 'Registration failed')
                }
            } catch (error: any) {
                ElMessage.error(error.response?.data?.message || 'Registration failed. Please try again.')
            } finally {
                loading.value = false
            }
        }

        const goToLogin = () => {
            router.push('/login')
        }

        return {
            registerData,
            rules,
            loading,
            registerForm,
            handleRegister,
            goToLogin,
            User, Lock, Message
        }
    }
})
</script>