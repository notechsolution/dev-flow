<template>
  <div class="change-password-page">
    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <el-icon><Lock /></el-icon>
          <h2>修改密码</h2>
        </div>
      </template>

      <el-form 
        ref="passwordFormRef" 
        :model="passwordForm" 
        :rules="passwordRules" 
        label-width="120px"
        label-position="left"
      >
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input 
            v-model="passwordForm.currentPassword" 
            type="password" 
            show-password
            placeholder="请输入当前密码"
            clearable
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            show-password
            placeholder="请输入新密码（至少6个字符）"
            clearable
          />
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            show-password
            placeholder="请再次输入新密码"
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button @click="goBack" size="large">取消</el-button>
          <el-button 
            type="primary" 
            @click="submitPasswordChange" 
            :loading="submitting"
            size="large"
          >
            确认修改
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, FormInstance } from 'element-plus';
import { Lock } from '@element-plus/icons-vue';
import api from '../../api/backend-api';
import { userStore as useUserStore } from '../../store/user';

const router = useRouter();
const userStore = useUserStore();

// State
const submitting = ref(false);
const passwordFormRef = ref<FormInstance>();

// Password form
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// Validation rules
const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'));
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
};

// Methods
const submitPasswordChange = async () => {
  if (!passwordFormRef.value) return;
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    submitting.value = true;
    try {
      // Note: This assumes the backend has an endpoint to change password
      // You may need to adjust this based on your actual API
      await api.updateUser(userStore.id!, {
        username: userStore.username!,
        email: userStore.email!,
        password: passwordForm.newPassword,
        role: userStore.role!,
        projectIds: userStore.projectIds
      });
      
      ElMessage.success('密码修改成功，请重新登录');
      
      // Logout and redirect to login page
      await userStore.logout();
      router.push('/login');
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '修改密码失败');
    } finally {
      submitting.value = false;
    }
  });
};

const goBack = () => {
  router.push('/profile');
};
</script>

<style scoped>
.change-password-page {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.password-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
}

:deep(.el-form-item) {
  margin-bottom: 28px;
}

:deep(.el-form-item:last-child) {
  margin-top: 40px;
  margin-bottom: 0;
}
</style>
