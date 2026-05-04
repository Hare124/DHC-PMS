<template>
<div class="owner-visitor-container">
    <el-page-header content="我的访客邀请"></el-page-header>

    <!-- 创建邀请表单 -->
    <el-card shadow="hover" class="create-invite-card">
      <template #header>
        <div class="card-header">
          <span>创建访客邀请</span>
        </div>
      </template>
      <el-form
          ref="inviteFormRef"
          :model="inviteForm"
          :rules="inviteRules"
          label-width="100px"
          inline
      >
        <el-form-item label="访客姓名" prop="visitorName">
          <el-input
              v-model="inviteForm.visitorName"
              placeholder="请输入访客姓名"
              maxlength="50"
          ></el-input>
        </el-form-item>
        <el-form-item label="访客手机号" prop="visitorPhone">
          <el-input
              v-model="inviteForm.visitorPhone"
              placeholder="请输入访客手机号"
              maxlength="20"
          ></el-input>
        </el-form-item>
        <el-form-item label="预约时间" prop="visitTime">
          <el-date-picker
              v-model="inviteForm.visitTime"
              type="datetime"
              placeholder="选择预约时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="有效期">
          <el-input-number
              v-model="inviteForm.validHours"
              :min="1"
              :max="24"
              placeholder="小时"
              style="width: 100px"
          ></el-input-number>
          <span class="unit">小时（默认2小时）</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleCreateInvite">生成邀请码</el-button>
          <el-button @click="resetInviteForm">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 二维码展示 -->
      <div v-if="qrCodeBase64" class="qrcode-container">
        <el-divider content-position="left">邀请二维码</el-divider>
        <img :src="`data:image/png;base64,${qrCodeBase64}`" alt="邀请二维码" class="qrcode-img" />
        <p class="expire-tip">
          二维码有效期至：{{ expireTime }}（{{ qrCodeValid ? '有效' : '已过期' }}）
        </p>
      </div>
    </el-card>

    <!-- 邀请记录列表 -->
    <el-card shadow="hover" class="record-list-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>我的邀请记录</span>
          <el-button type="primary" size="small" @click="getInviteRecords">刷新</el-button>
        </div>
      </template>
      <el-table :data="inviteRecords" border stripe style="width: 100%">
        <el-table-column prop="id" label="邀请ID" width="80"></el-table-column>
        <el-table-column prop="visitorName" label="访客姓名" width="120"></el-table-column>
        <el-table-column prop="visitorPhone" label="访客手机号" width="150"></el-table-column>
        <el-table-column prop="visitTime" label="预约时间" width="200">
          <template #default="scope">
            {{ scope.row.visitTime?.replace('T', ' ') }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="200">
          <template #default="scope">
            {{ scope.row.createTime?.replace('T', ' ') }}
          </template>
        </el-table-column>
        <el-table-column prop="expireTime" label="过期时间" width="200">
          <template #default="scope">
            {{ scope.row.expireTime?.replace('T', ' ') }}
          </template>
        </el-table-column>
        <el-table-column prop="statusDesc" label="状态" width="100">
          <template #default="scope">
            <el-tag
                :type="scope.row.statusDesc === '待核销' ? 'warning' : scope.row.statusDesc === '已核销' ? 'success' : 'danger'"
            >
              {{ scope.row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
                size="small"
                type="danger"
                icon="el-icon-delete"
                @click="handleCancelInvite(scope.row.id)"
                :disabled="scope.row.statusDesc !== '待核销'"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 测试按钮（保留） -->
<!--    <div class="test-btn-group" style="margin-top: 20px">-->
<!--      <el-button @click="handleTestPost">测试POST</el-button>-->
<!--      <el-button @click="handleTestGet">测试GET</el-button>-->
<!--    </div>-->
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  testPost,
  testGet,
  ownerCreateInvite,
  ownerGetInviteRecords,
  ownerCancelInvite
} from '@/api/visitorApi.js';

// 表单相关
const inviteFormRef = ref(null);
const inviteForm = reactive({
  visitorName: '',
  visitorPhone: '',
  visitTime: '',
  validHours: 2
});
const inviteRules = reactive({
  visitorName: [{ required: true, message: '请输入访客姓名', trigger: 'blur' }],
  visitorPhone: [{ required: true, message: '请输入访客手机号', trigger: 'blur' }],
  visitTime: [{ required: true, message: '请选择预约时间', trigger: 'change' }]
});

// 二维码相关
const qrCodeBase64 = ref('');
const expireTime = ref('');
const qrCodeValid = ref(true);

// 邀请记录
const inviteRecords = ref([]);

// 页面加载时获取记录
onMounted(() => {
  // 校验 userId 是否存在（适配 request 拦截器的提示）
  const userId = localStorage.getItem('userId')?.trim();
  if (!userId) {
    ElMessage.warning('未检测到用户ID，请重新登录');
    setTimeout(() => {
      window.location.href = '/login';
    }, 1500);
    return;
  }
  getInviteRecords();
});

// 创建邀请
const handleCreateInvite = async () => {
  try {
    await inviteFormRef.value.validate();
    const res = await ownerCreateInvite(inviteForm);
    // 直接取响应数据（request 拦截器已返回原始 response）
    const result = res.data;
    if (result.code === 200) {
      ElMessage.success(result.msg);
      // 展示二维码
      qrCodeBase64.value = result.data.qrCodeBase64;
      expireTime.value = result.data.expireTime;
      qrCodeValid.value = result.data.inviteInfo.qrCodeValid;
      // 刷新记录
      getInviteRecords();
      // 重置表单
      resetInviteForm();
    } else {
      ElMessage.error(result.msg || '创建邀请失败');
    }
  } catch (error) {
    console.error('创建邀请失败：', error);
    // 兼容 401 错误（request 拦截器已提示，这里仅补充）
    if (error.response?.status === 401) {
      ElMessage.error('登录状态失效，请重新登录');
    } else {
      ElMessage.error('创建邀请失败，请重试');
    }
  }
};

// 重置表单
const resetInviteForm = () => {
  inviteFormRef.value.resetFields();
  inviteForm.validHours = 2;
  qrCodeBase64.value = '';
  expireTime.value = '';
  qrCodeValid.value = true;
};

// 获取邀请记录
const getInviteRecords = async () => {
  try {
    const userId = localStorage.getItem('userId')?.trim();
    console.log('当前 userId:', userId); // 调试信息

    if (!userId) {
      ElMessage.error('未找到用户 ID，请重新登录');
      setTimeout(() => {
        window.location.href = '/login';
      }, 1500);
      return;
    }

    const res = await ownerGetInviteRecords();
    const result = res.data;
    console.log('邀请记录响应:', result); // 调试信息

    if (result.code === 200) {
      inviteRecords.value = result.data;
      console.log('邀请记录数量:', result.data.length); // 调试信息
    } else {
      ElMessage.error(result.msg || '获取记录失败');
    }
  } catch (error) {
    console.error('获取邀请记录失败：', error);
    console.error('错误详情:', error.response?.data); // 调试信息
    if (error.response?.status === 401) {
      ElMessage.error('登录状态失效，请重新登录');
    } else {
      ElMessage.error(error.response?.data?.msg || '获取邀请记录失败，请重试');
    }
  }
};

// 取消邀请
const handleCancelInvite = async (inviteId) => {
  try {
    await ElMessageBox.confirm('确定要取消该邀请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    const res = await ownerCancelInvite(inviteId);
    const result = res.data;
    if (result.code === 200) {
      ElMessage.success(result.msg);
      getInviteRecords();
    } else {
      ElMessage.error(result.msg || '取消邀请失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消邀请失败：', error);
      if (error.response?.status === 401) {
        ElMessage.error('登录状态失效，请重新登录');
      } else {
        ElMessage.error('取消邀请失败，请重试');
      }
    }
  }
};

// 测试 POST
const handleTestPost = async () => {
  try {
    const res = await testPost({
      name: '张三',
      age: 18
    });
    ElMessage.success('POST 请求成功');
    console.log('POST 请求成功：', res);
  } catch (err) {
    ElMessage.error('POST 请求失败');
    console.error('POST 请求失败：', err);
  }
};

// 测试 GET
const handleTestGet = async () => {
  try {
    const res = await testGet();
    ElMessage.success('GET 请求成功');
    console.log('GET 请求成功：', res);
  } catch (err) {
    ElMessage.error('GET 请求失败');
    console.error('GET 请求失败：', err);
  }
};
</script>

<style scoped>
.owner-visitor-container {
  padding: 20px;
}

.create-invite-card,
.record-list-card {
  background: #fff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.qrcode-container {
  margin-top: 20px;
  text-align: center;
}

.qrcode-img {
  width: 200px;
  height: 200px;
}

.expire-tip {
  margin-top: 10px;
  color: #666;
}

.unit {
  margin-left: 10px;
  color: #999;
}

.test-btn-group {
  display: flex;
  gap: 10px;
}
</style>