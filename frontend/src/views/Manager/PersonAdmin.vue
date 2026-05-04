<template>
  <div class="person-admin-container">
    <!-- 页面标题 -->
    <el-card class="header-card">
      <div class="page-header">
        <h2>👤 物业经理个人中心</h2>
        <p class="description">管理您的个人信息和账号设置</p>
      </div>
    </el-card>

    <!-- 个人信息卡片 -->
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>📋 基本信息</span>
          <el-button type="primary" size="small" @click="handleEdit">
            修改信息
          </el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">
          <el-tag type="info">{{ userInfo.username }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="姓名">
          {{ userInfo.name || '未设置' }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号">
          {{ userInfo.phone || '未绑定' }}
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag type="warning">物业经理</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="账号状态">
          <el-tag :type="userInfo.status === 1 ? 'success' : 'danger'">
            {{ userInfo.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(userInfo.createTime) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 修改信息对话框 -->
    <el-dialog
        v-model="editDialogVisible"
        title="修改个人信息"
        width="500px"
        :close-on-click-modal="false"
    >
      <el-form
          ref="editFormRef"
          :model="editForm"
          :rules="editRules"
          label-width="80px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入真实姓名"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" maxlength="11"></el-input>
        </el-form-item>
        <el-alert
            title="温馨提示"
            type="info"
            :closable="false"
            show-icon
            style="margin-top: 10px;"
        >
          <p>• 用户名不可修改</p>
          <p>• 手机号修改后需重新登录</p>
          <p>• 角色信息不可修改</p>
        </el-alert>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitEdit" :loading="isSubmitting">
          保存修改
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog
        v-model="passwordDialogVisible"
        title="修改密码"
        width="500px"
        :close-on-click-modal="false"
    >
      <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="100px"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入当前密码"
              show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
          ></el-input>
        </el-form-item>
        <el-alert
            title="密码要求"
            type="warning"
            :closable="false"
            show-icon
            style="margin-top: 10px;"
        >
          <p>• 密码长度：6-20 位</p>
          <p>• 建议包含字母和数字的组合</p>
          <p>• 修改成功后需重新登录</p>
        </el-alert>
      </el-form>

      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitPassword" :loading="isSubmitting">
          确认修改
        </el-button>
      </template>
    </el-dialog>

    <!-- 操作按钮区域 -->
    <el-card class="action-card">
      <template #header>
        <div class="card-header">
          <span>⚙️ 账号设置</span>
        </div>
      </template>

      <div class="action-buttons">
        <el-button type="primary" @click="handleEditPassword">
          修改密码
        </el-button>
        <el-button type="danger" @click="handleLogout">
          退出登录
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, ElForm } from 'element-plus';
import { Edit, Lock, SwitchButton } from '@element-plus/icons-vue';
import { updateUserProfile, changePassword } from '@/api/adminApi.js';

const router = useRouter();

// 当前登录用户信息
const userInfo = ref({
  id: null,
  username: '',
  name: '',
  phone: '',
  role: 1,
  status: 1,
  createTime: ''
});

// 修改信息对话框
const editDialogVisible = ref(false);
const editFormRef = ref(null);
const editForm = reactive({
  id: null,
  name: '',
  phone: ''
});

// 修改密码对话框
const passwordDialogVisible = ref(false);
const passwordFormRef = ref(null);
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 加载状态
const isSubmitting = ref(false);

// 修改信息验证规则
const editRules = reactive({
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
});

// 修改密码验证规则
const passwordRules = reactive({
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
    {
      pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,20}$/,
      message: '密码需包含字母和数字',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
});

// 页面加载时获取用户信息
onMounted(() => {
  loadUserInfo();
});

/**
 * 加载用户信息
 */
const loadUserInfo = () => {
  try {
    console.log('\n===== 开始加载用户信息 =====');

    // 从 localStorage 获取用户信息
    const userStr = localStorage.getItem('user');
    console.log('localStorage 中的 user 字符串:', userStr);

    if (!userStr) {
      console.error('localStorage 中 user 为空');
      ElMessage.warning('未获取到用户信息，请重新登录');

      // 延迟 2 秒后跳转到登录页
      setTimeout(() => {
        router.push('/login');
      }, 2000);
      return;
    }

    const user = JSON.parse(userStr);
    console.log('解析后的用户对象:', user);
    console.log('用户 ID:', user.id);
    console.log('用户名:', user.username);
    console.log('用户角色:', user.role);

    // 校验必要字段
    if (!user.id) {
      console.error('用户 ID 为空');
      ElMessage.error('用户信息不完整，请重新登录');
      setTimeout(() => {
        router.push('/login');
      }, 2000);
      return;
    }

    // 填充用户信息
    userInfo.value = {
      id: user.id,
      username: user.username || '',
      name: user.name || '',
      phone: user.phone || '',
      role: user.role || 1, // 默认物业人员
      status: user.status !== undefined ? user.status : 1,
      createTime: user.createTime || ''
    };

    // 填充编辑表单
    editForm.id = user.id;
    editForm.name = user.name || '';
    editForm.phone = user.phone || '';

    console.log('用户信息加载成功:', userInfo.value);
    console.log('编辑表单已填充:', editForm);
    console.log('===== 用户信息加载完成 =====\n');
  } catch (err) {
    console.error('加载用户信息异常:', err);
    console.error('错误堆栈:', err.stack);
    ElMessage.error('加载用户信息失败：' + err.message);

    // 异常情况下也跳转到登录页
    setTimeout(() => {
      router.push('/login');
    }, 2000);
  }
};

/**
 * 修改信息按钮点击
 */
const handleEdit = () => {
  editDialogVisible.value = true;
};

/**
 * 提交修改信息
 */
const handleSubmitEdit = async () => {
  if (!editFormRef.value) return;

  await editFormRef.value.validate(async (valid) => {
    if (!valid) return;

    try {
      isSubmitting.value = true;

      // 调用修改接口
      const res = await updateUserProfile(editForm);
      const businessData = res.data;

      if (businessData && businessData.code === 200) {
        ElMessage.success('修改成功');
        editDialogVisible.value = false;

        // 更新本地用户信息
        const updatedUser = {
          ...userInfo.value,
          name: editForm.name,
          phone: editForm.phone
        };
        localStorage.setItem('user', JSON.stringify(updatedUser));
        userInfo.value = updatedUser;

        // 如果手机号修改了，提示重新登录
        if (editForm.phone !== userInfo.value.phone) {
          ElMessage.warning('手机号已修改，请重新登录');
          setTimeout(() => {
            handleLogout();
          }, 1500);
        }
      } else {
        ElMessage.error(businessData?.msg || '修改失败');
      }
    } catch (err) {
      console.error('修改信息失败:', err);
      ElMessage.error('修改失败，请稍后重试');
    } finally {
      isSubmitting.value = false;
    }
  });
};

/**
 * 修改密码按钮点击
 */
const handleEditPassword = () => {
  passwordDialogVisible.value = true;
  // 重置表单
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
};

/**
 * 提交修改密码
 */
const handleSubmitPassword = async () => {
  if (!passwordFormRef.value) return;

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return;

    try {
      isSubmitting.value = true;

      // 调用修改密码接口
      const res = await changePassword({
        userId: userInfo.value.id,
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      });
      const businessData = res.data;

      if (businessData && businessData.code === 200) {
        ElMessage.success('密码修改成功，请重新登录');
        passwordDialogVisible.value = false;

        // 清除登录信息
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        localStorage.removeItem('userId');

        // 延迟跳转到登录页
        setTimeout(() => {
          router.push('/login');
        }, 1500);
      } else {
        ElMessage.error(businessData?.msg || '修改密码失败');
      }
    } catch (err) {
      console.error('修改密码失败:', err);
      ElMessage.error('修改密码失败，请稍后重试');
    } finally {
      isSubmitting.value = false;
    }
  });
};

/**
 * 退出登录
 */
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 清除所有登录信息
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('userId');

    ElMessage.success('已退出登录');
    router.push('/login');
  }).catch(() => {
    // 取消操作
  });
};

/**
 * 格式化时间
 */
const formatDateTime = (datetime) => {
  if (!datetime) return '未知';
  try {
    const date = new Date(datetime);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (e) {
    return datetime;
  }
};
</script>

<style scoped>
.person-admin-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.page-header {
  text-align: center;
}

.page-header h2 {
  font-size: 28px;
  margin: 0 0 10px 0;
}

.page-header .description {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.profile-card,
.building-card,
.password-card {
  margin-bottom: 20px;
}

.building-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.building-item {
  width: 100%;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 10px;
}

.building-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.building-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.building-stats {
  display: flex;
  gap: 15px;
  font-size: 13px;
  color: #666;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.room-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 8px;
  margin-top: 10px;
}

.room-tag {
  background: #ecf5ff;
  color: #409eff;
  padding: 5px 10px;
  border-radius: 4px;
  text-align: center;
  font-size: 13px;
}

.empty-text {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

/* 密码修改表单 */
.password-form {
  max-width: 500px;
  margin: 0 auto;
}

/* 编辑资料对话框 */
.edit-dialog {
  max-width: 600px;
}

/* 成功提示 */
.success-content {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  font-size: 60px;
  color: #67c23a;
  margin-bottom: 15px;
}

.success-text {
  font-size: 18px;
  color: #333;
  margin-bottom: 10px;
}

.hint-text {
  font-size: 14px;
  color: #909399;
}

/* 按钮组 */
.button-group {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .person-admin-container {
    padding: 10px;
  }

  .page-header h2 {
    font-size: 22px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .building-stats {
    flex-direction: column;
    gap: 5px;
  }

  .room-list {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  }
}
</style>
