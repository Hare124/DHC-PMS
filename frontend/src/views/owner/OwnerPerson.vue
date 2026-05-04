<template>
  <div class="owner-person-page">
    <el-card shadow="always">
      <template #header>
        <div class="card-header">
          <span><el-icon><UserFilled /></el-icon> 个人中心</span>
        </div>
      </template>

      <!-- 用户基本信息展示 -->
      <div class="user-info-section">
        <div class="avatar-area">
          <el-avatar :size="100" :src="defaultAvatar">
            <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" alt="默认头像" />
          </el-avatar>
        </div>
        <div class="info-area">
          <h2 class="user-name">{{ userInfo.name || '-' }}</h2>
          <p class="user-role">
            <el-tag type="warning" size="small">业主</el-tag>
          </p>
          <p class="room-number">
            <el-icon><House /></el-icon>
            {{ formatRoomInfo(ownerInfo) }}
          </p>
        </div>
      </div>

      <el-divider />

      <!-- 个人信息详情 -->
      <div class="info-tabs">
        <el-tabs v-model="activeTab" type="border-card">
          <!-- 基本信息 Tab -->
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="用户名">
                <el-tag size="small">{{ userInfo.username || '-' }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="姓名">
                {{ userInfo.name || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="手机号码">
                {{ userInfo.phone || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="账号状态">
                <el-tag :type="userInfo.status === 1 ? 'success' : 'danger'" size="small">
                  {{ userInfo.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="身份证号">
                {{ formatIdCard(ownerInfo.idCard) }}
              </el-descriptions-item>
              <el-descriptions-item label="注册时间">
                {{ formatDate(ownerInfo.registerTime) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <!-- 房屋信息 Tab -->
          <el-tab-pane label="房屋信息" name="house">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="楼栋号">
                {{ ownerInfo.buildingNo || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="房号">
                {{ ownerInfo.roomNo || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="房屋户型">
                {{ ownerInfo.houseLayout || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="房屋用途">
                {{ ownerInfo.houseUsage || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="建筑面积">
                {{ ownerInfo.houseArea || '0' }}㎡
              </el-descriptions-item>
              <el-descriptions-item label="套内面积">
                {{ ownerInfo.internalArea || '0' }}㎡
              </el-descriptions-item>
              <el-descriptions-item label="房屋结构">
                {{ ownerInfo.houseStructure || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="共有情况">
                {{ ownerInfo.ownershipType || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="是否配套车位">
                {{ ownerInfo.hasParkingSpace ? '是' : '否' }}
              </el-descriptions-item>
              <el-descriptions-item label="是否配套储藏室">
                {{ ownerInfo.hasStorageRoom ? '是' : '否' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 操作按钮区域 -->
      <div class="action-buttons">
        <el-button type="primary" @click="handleEditPhone">
          修改手机号
        </el-button>
        <el-button type="warning" @click="handleChangePassword">
          修改密码
        </el-button>
        <el-button type="danger" @click="handleLogout">
          退出登录
        </el-button>
      </div>
    </el-card>

    <!-- 修改手机号对话框 -->
    <el-dialog v-model="phoneDialogVisible" title="修改手机号" width="450px">
      <el-form ref="phoneFormRef" :model="phoneForm" :rules="phoneRules" label-width="100px">
        <el-alert
          title="温馨提示"
          type="info"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <p>修改手机号后，您需要使用新手机号进行登录。</p>
        </el-alert>
        <el-form-item label="当前手机号" prop="currentPhone">
          <el-input
            v-model="phoneForm.currentPhone"
            disabled
            :placeholder="userInfo.phone || '未设置'"
          />
        </el-form-item>
        <el-form-item label="新手机号" prop="newPhone">
          <el-input
            v-model="phoneForm.newPhone"
            placeholder="请输入新的手机号"
            maxlength="11"
          />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-input">
            <el-input
              v-model="phoneForm.code"
              placeholder="请输入短信验证码"
              maxlength="6"
              style="width: 200px;"
            />
            <el-button
              type="primary"
              :disabled="countdown > 0"
              @click="handleSendCode"
              style="margin-left: 10px;"
            >
              {{ countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="phoneDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPhoneUpdate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="450px">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-alert
          title="密码安全提示"
          type="warning"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <p>密码长度至少 8 位，建议包含大小写字母、数字和特殊字符的组合。</p>
        </el-alert>
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPasswordUpdate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { UserFilled, Edit, Lock, House } from '@element-plus/icons-vue';
import { getOwnerDetailByUserId, updatePhone, updatePassword, sendSmsCode } from '@/api/commonApi.js';

const router = useRouter();

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// Tab 激活状态
const activeTab = ref('basic');

// 用户信息
const userInfo = reactive({
  id: null,
  username: '',
  name: '',
  phone: '',
  role: 2,
  status: 1
});

// 业主详细信息
const ownerInfo = reactive({
  id: null,
  userId: null,
  idCard: '',
  buildingNo: '',
  roomNo: '',
  houseArea: null,
  internalArea: null,
  houseLayout: '',
  houseUsage: '',
  houseStructure: '',
  ownershipType: '',
  hasParkingSpace: false,
  hasStorageRoom: false,
  registerTime: null
});

// 修改手机号相关
const phoneDialogVisible = ref(false);
const phoneFormRef = ref(null);
const phoneForm = reactive({
  currentPhone: '',
  newPhone: '',
  code: ''
});
const countdown = ref(0);
let timer = null;

// 手机号验证规则
const phoneRules = {
  currentPhone: [
    { required: true, message: '当前手机号不能为空', trigger: 'blur' }
  ],
  newPhone: [
    { required: true, message: '请输入新手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为 6 位数字', trigger: 'blur' }
  ]
};

// 修改密码相关
const passwordDialogVisible = ref(false);
const passwordFormRef = ref(null);
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 密码验证规则
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
};

// 页面加载时获取数据
onMounted(() => {
  loadUserInfo();
  loadOwnerInfo();
});

// 加载用户信息
const loadUserInfo = () => {
  try {
    // 修复：读取 localStorage 时使用正确的 key（'userInfo'）
    const userStr = localStorage.getItem('userInfo');
    if (userStr) {
      const user = JSON.parse(userStr);
      userInfo.id = user.id;
      userInfo.username = user.username;
      userInfo.name = user.name;
      userInfo.phone = user.phone;
      userInfo.role = user.role;
      userInfo.status = user.status;

      // 填充到手机号表单
      phoneForm.currentPhone = user.phone || '';
    } else {
      ElMessage.warning('未检测到登录信息，请重新登录');
      router.push('/login');
    }
  } catch (error) {
    console.error('加载用户信息失败:', error);
    ElMessage.error('加载用户信息失败');
  }
};

// 加载业主详细信息
const loadOwnerInfo = async () => {
  try {
    // 先检查 userId 是否有效
    if (!userInfo.id) {
      console.error('用户 ID 为空，无法加载业主信息');
      ElMessage.warning('用户信息不完整，请重新登录');
      return;
    }

    // 调用后端接口获取业主详细信息
    const res = await getOwnerDetailByUserId(userInfo.id);
    console.log('获取业主详细信息响应数据：', res);

    if (res.code === 200 && res.data) {
      // 修复：使用逐个字段赋值，确保响应式更新
      const data = res.data;
      ownerInfo.id = data.id || null;
      ownerInfo.userId = data.userId || null;
      ownerInfo.idCard = data.idCard || '';
      ownerInfo.buildingNo = data.buildingNo || '';
      ownerInfo.roomNo = data.roomNo || '';
      ownerInfo.houseArea = data.houseArea || null;
      ownerInfo.internalArea = data.internalArea || null;
      ownerInfo.houseLayout = data.houseLayout || '';
      ownerInfo.houseUsage = data.houseUsage || '';
      ownerInfo.houseStructure = data.houseStructure || '';
      ownerInfo.ownershipType = data.ownershipType || '';
      ownerInfo.hasParkingSpace = data.hasParkingSpace || false;
      ownerInfo.hasStorageRoom = data.hasStorageRoom || false;
      ownerInfo.registerTime = data.registerTime || null;

      console.log('业主信息加载成功：', ownerInfo);
    } else {
      ElMessage.warning('未查询到业主详细信息');
    }
  } catch (error) {
    console.error('加载业主信息失败:', error);
    ElMessage.error('加载业主信息失败');
  }
};

// 格式化房间信息
const formatRoomInfo = (info) => {
  if (!info.buildingNo && !info.roomNo) {
    return '暂无房屋信息';
  }
  return `${info.buildingNo || '-'}${info.roomNo || '-'}号`;
};

// 格式化身份证号（脱敏显示）
const formatIdCard = (idCard) => {
  if (!idCard) return '-';
  return idCard.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2');
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  try {
    const date = new Date(dateStr);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
  } catch (e) {
    return dateStr;
  }
};

// 修改手机号
const handleEditPhone = () => {
  phoneDialogVisible.value = true;
};

// 发送验证码
const handleSendCode = async () => {
  if (!phoneForm.newPhone || !/^1[3-9]\d{9}$/.test(phoneForm.newPhone)) {
    ElMessage.warning('请先输入正确的手机号');
    return;
  }

  try {
    // 调用发送验证码接口
    const result = await sendSmsCode(phoneForm.newPhone);
    if (result.code === 200 || result.msg === '验证码发送成功') {
      ElMessage.success('验证码已发送');
      countdown.value = 60;

      timer = setInterval(() => {
        countdown.value--;
        if (countdown.value <= 0) {
          clearInterval(timer);
        }
      }, 1000);
    } else {
      ElMessage.error(result.msg || '验证码发送失败');
    }
  } catch (error) {
    console.error('发送验证码失败:', error);
    ElMessage.error('验证码发送失败，请稍后重试');
  }
};

// 提交手机号修改
const submitPhoneUpdate = async () => {
  try {
    await phoneFormRef.value.validate();

    // 调用修改手机号接口
    const data = {
      userId: userInfo.id,
      newPhone: phoneForm.newPhone,
      code: phoneForm.code
    };

    const result = await updatePhone(data);

    if (result.code === 200 || result.msg === '手机号修改成功') {
      ElMessage.success('手机号修改成功');
      userInfo.phone = phoneForm.newPhone;
      phoneDialogVisible.value = false;

      // 修复：更新 localStorage 中的用户信息（使用正确的 key：'userInfo'）
      const userStr = localStorage.getItem('userInfo');
      if (userStr) {
        const user = JSON.parse(userStr);
        user.phone = phoneForm.newPhone;
        localStorage.setItem('userInfo', JSON.stringify(user));
      }
    } else {
      ElMessage.error(result.msg || '手机号修改失败');
    }
  } catch (error) {
    if (error.name === 'ValidationError') {
      ElMessage.error('请完善必填项');
    } else {
      console.error('修改手机号失败:', error);
      ElMessage.error('修改失败，请稍后重试');
    }
  }
};

// 修改密码
const handleChangePassword = () => {
  passwordDialogVisible.value = true;
};

// 提交密码修改
const submitPasswordUpdate = async () => {
  try {
    await passwordFormRef.value.validate();

    // 调用修改密码接口
    const data = {
      userId: userInfo.id,
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    };

    const result = await updatePassword(data);

    if (result.code === 200 || result.msg === '密码修改成功') {
      ElMessage.success('密码修改成功，请重新登录');
      passwordDialogVisible.value = false;

      // 清除登录信息并跳转到登录页
      localStorage.removeItem('user');
      setTimeout(() => {
        router.push('/login');
      }, 1000);
    } else {
      ElMessage.error(result.msg || '密码修改失败');
    }
  } catch (error) {
    if (error.name === 'ValidationError') {
      ElMessage.error('请完善必填项');
    } else {
      console.error('修改密码失败:', error);
      ElMessage.error('修改失败，请稍后重试');
    }
  }
};

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
        '确定要退出登录吗？',
        '退出确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    );

    // 清除本地存储
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    localStorage.removeItem('userId');
    localStorage.removeItem('userRole');

    ElMessage.success('已退出登录');

    // 跳转到登录页
    setTimeout(() => {
      router.push('/login');
    }, 500);
  } catch (err) {
    // 用户取消退出
    if (err !== 'cancel') {
      console.error('退出登录异常:', err);
    }
  }
};
</script>

<style scoped>
.owner-person-page {
  //padding: 20px;
  //background-color: #fafafa;
  //min-height: calc(100vh - 120px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  font-size: 16px;
}

/* 用户信息区域 */
.user-info-section {
  display: flex;
  align-items: center;
  gap: 30px;
  padding: 20px 0;
}

.avatar-area {
  flex-shrink: 0;
}

.info-area .user-name {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #303133;
  font-weight: 600;
}

.info-area .user-role {
  margin: 0 0 10px 0;
}

.info-area .room-number {
  margin: 0;
  font-size: 14px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 5px;
}

/* 标签页样式 */
.info-tabs {
  margin: 20px 0;
}

:deep(.el-tabs__header) {
  margin-bottom: 20px;
}

/* 操作按钮区域 */
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 20px 0;
  border-top: 1px solid #ebeef5;
  margin-top: 20px;
}

.action-buttons .el-button {
  min-width: 150px;
  height: 45px;
  font-size: 15px;
}

/* 验证码输入框 */
.code-input {
  display: flex;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-info-section {
    flex-direction: column;
    text-align: center;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }
}
</style>
