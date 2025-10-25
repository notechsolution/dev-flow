<template>
    <div class="min-h-screen bg-gradient-to-br from-purple-50 to-pink-100 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div class="max-w-md w-full space-y-8">
            <div class="text-center">
                <h2 class="mt-6 text-3xl font-extrabold text-gray-900">Reset Password</h2>
                <p class="mt-2 text-sm text-gray-600">Enter your new password</p>
            </div>

            <div class="bg-white shadow-xl rounded-lg px-8 py-8">
                <el-form
                    ref="resetPasswordForm"
                    :model="resetPasswordData"
                    :rules="rules"
                    class="space-y-6"
                    @submit.prevent="handleResetPassword"
                >
                    <el-form-item prop="newPassword">
                        <label class="block text-sm font-medium text-gray-700 mb-2">New Password</label>
                        <el-input
                            v-model="resetPasswordData.newPassword"
                            type="password"
                            placeholder="Enter your new password"
                            size="large"
                            :prefix-icon="Lock"
                            show-password
                            class="w-full"
                        />
                    </el-form-item>

                    <el-form-item prop="confirmPassword">
                        <label class="block text-sm font-medium text-gray-700 mb-2">Confirm New Password</label>
                        <el-input
                            v-model="resetPasswordData.confirmPassword"
                            type="password"
                            placeholder="Confirm your new password"
                            size="large"
                            :prefix-icon="Lock"
                            show-password
                            class="w-full"
                        />
                    </el-form-item>

                    <el-form-item>
                        <el-button
                            type="primary"
                            size="large"
                            :loading="loading"
                            @click="handleResetPassword"
                            class="w-full bg-purple-600 hover:bg-purple-700 border-purple-600 hover:border-purple-700"
                        >
                            Reset Password
                        </el-button>
                    </el-form-item>
                </el-form>

                <div class="mt-6 text-center">
                    <p class="text-sm text-gray-600">
                        <el-button type="text" @click="goToLogin" class="font-medium text-purple-600 hover:text-purple-500">
                            Back to Sign In
                        </el-button>
                    </p>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import userApi from '@/api/user-api'

export default defineComponent({
    name: 'ResetPassword',
    components: { Lock },
    setup() {
        const router = useRouter()
        const route = useRoute()
        const resetPasswordForm = ref()
        const loading = ref(false)
        const token = ref('')

        const resetPasswordData = reactive({
            newPassword: '',
            confirmPassword: ''
        })

        const validateConfirmPassword = (rule: any, value: string, callback: any) => {
            if (value !== resetPasswordData.newPassword) {
                callback(new Error('Passwords do not match'))
            } else {
                callback()
            }
        }

        const rules = {
            newPassword: [
                { required: true, message: 'Please enter your new password', trigger: 'blur' },
                { min: 6, message: 'Password should be at least 6 characters', trigger: 'blur' }
            ],
            confirmPassword: [
                { required: true, message: 'Please confirm your password', trigger: 'blur' },
                { validator: validateConfirmPassword, trigger: 'blur' }
            ]
        }

        const handleResetPassword = async () => {
            try {
                const valid = await resetPasswordForm.value.validate()
                if (!valid) return

                if (!token.value) {
                    ElMessage.error('Invalid reset token')
                    return
                }

                loading.value = true
                const response = await userApi.resetPassword(token.value, resetPasswordData.newPassword)

                if (response.data.success) {
                    ElMessage.success('Password reset successfully! Please login with your new password.')
                    router.push('/login')
                } else {
                    ElMessage.error(response.data.message || 'Failed to reset password')
                }
            } catch (error: any) {
                ElMessage.error(error.response?.data?.message || 'Failed to reset password. Please try again.')
            } finally {
                loading.value = false
            }
        }

        const goToLogin = () => {
            router.push('/login')
        }

        onMounted(() => {
            // Get token from URL query parameters
            token.value = route.query.token as string || ''
            if (!token.value) {
                ElMessage.error('Invalid or missing reset token')
                router.push('/login')
            }
        })

        return {
            resetPasswordData,
            rules,
            loading,
            resetPasswordForm,
            handleResetPassword,
            goToLogin,
            Lock
        }
    }
})
</script>