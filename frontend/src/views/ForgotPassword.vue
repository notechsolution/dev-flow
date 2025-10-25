<template>
    <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div class="max-w-md w-full space-y-8">
            <div class="text-center">
                <h2 class="mt-6 text-3xl font-extrabold text-gray-900">Forgot Password</h2>
                <p class="mt-2 text-sm text-gray-600">Enter your email to receive a password reset link</p>
            </div>

            <div class="bg-white shadow-xl rounded-lg px-8 py-8">
                <el-form
                    ref="forgotPasswordForm"
                    :model="forgotPasswordData"
                    :rules="rules"
                    class="space-y-6"
                    @submit.prevent="handleForgotPassword"
                >
                    <el-form-item prop="email">
                        <label class="block text-sm font-medium text-gray-700 mb-2">Email</label>
                        <el-input
                            v-model="forgotPasswordData.email"
                            type="email"
                            placeholder="Enter your email address"
                            size="large"
                            :prefix-icon="Message"
                            class="w-full"
                        />
                    </el-form-item>

                    <el-form-item>
                        <el-button
                            type="primary"
                            size="large"
                            :loading="loading"
                            @click="handleForgotPassword"
                            class="w-full bg-indigo-600 hover:bg-indigo-700 border-indigo-600 hover:border-indigo-700"
                        >
                            Send Reset Link
                        </el-button>
                    </el-form-item>
                </el-form>

                <div class="mt-6 text-center">
                    <p class="text-sm text-gray-600">
                        Remember your password?
                        <el-button type="text" @click="goToLogin" class="font-medium text-indigo-600 hover:text-indigo-500">
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
import { Message } from '@element-plus/icons-vue'
import userApi from '@/api/user-api'

export default defineComponent({
    name: 'ForgotPassword',
    components: { Message },
    setup() {
        const router = useRouter()
        const forgotPasswordForm = ref()
        const loading = ref(false)

        const forgotPasswordData = reactive({
            email: ''
        })

        const rules = {
            email: [
                { required: true, message: 'Please enter your email', trigger: 'blur' },
                { type: 'email', message: 'Please enter a valid email address', trigger: 'blur' }
            ]
        }

        const handleForgotPassword = async () => {
            try {
                const valid = await forgotPasswordForm.value.validate()
                if (!valid) return

                loading.value = true
                const response = await userApi.forgotPassword(forgotPasswordData.email)

                if (response.data.success) {
                    ElMessage.success('Password reset email sent successfully! Please check your inbox.')
                    router.push('/login')
                } else {
                    ElMessage.error(response.data.message || 'Failed to send password reset email')
                }
            } catch (error: any) {
                ElMessage.error(error.response?.data?.message || 'Failed to send password reset email. Please try again.')
            } finally {
                loading.value = false
            }
        }

        const goToLogin = () => {
            router.push('/login')
        }

        return {
            forgotPasswordData,
            rules,
            loading,
            forgotPasswordForm,
            handleForgotPassword,
            goToLogin,
            Message
        }
    }
})
</script>