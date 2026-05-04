<template>
  <div class="suggestion-page">
    <el-card shadow="always">

      <!-- 意见类型选择 -->
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="意见反馈" name="submit">
          <el-form ref="suggestionFormRef" :model="suggestionForm" :rules="suggestionRules" label-width="100px">
            <el-form-item label="意见类型" prop="type">
              <el-select v-model="suggestionForm.type" placeholder="请选择意见类型" style="width: 100%;">
                <el-option label="投诉" value="complaint" />
                <el-option label="建议" value="suggestion" />
                <el-option label="报修" value="repair" />
                <el-option label="咨询" value="consultation" />
              </el-select>
            </el-form-item>

            <el-form-item label="意见标题" prop="title">
              <el-input
                v-model="suggestionForm.title"
                placeholder="请输入简要标题（20 字以内）"
                maxlength="20"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="意见内容" prop="content">
              <el-input
                v-model="suggestionForm.content"
                type="textarea"
                :rows="5"
                placeholder="请详细描述您的意见..."
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="联系电话" prop="contactPhone">
              <el-input
                v-model="suggestionForm.contactPhone"
                placeholder="请输入联系电话（选填）"
                maxlength="11"
              />
            </el-form-item>

            <el-form-item label="上传图片" prop="images">
              <el-upload
                action="#"
                list-type="picture-card"
                :auto-upload="false"
                :on-change="handleImageChange"
                :on-remove="handleImageRemove"
                :file-list="imageList"
                :limit="5"
                :on-exceed="handleExceed"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">
                <el-icon><InfoFilled /></el-icon>
                支持 jpg/png 格式，最多上传 5 张，每张不超过 5MB
              </div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="submitSuggestion" :loading="submitting">
                <el-icon><Upload /></el-icon> 提交意见
              </el-button>
              <el-button @click="resetForm">
                <el-icon><Refresh /></el-icon> 重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="历史记录" name="history">
          <div class="history-list">
            <el-empty v-if="suggestionList.length === 0" description="暂无历史意见记录" />
            <div v-else>
              <div
                v-for="(item, index) in suggestionList"
                :key="index"
                class="suggestion-item"
                @click="viewDetail(item)"
              >
                <div class="item-header">
                  <el-tag :type="getTypeTag(item.type)" size="small">
                    {{ getTypeLabel(item.type) }}
                  </el-tag>
                  <el-tag :type="getStatusTag(item.status)" size="small">
                    {{ getStatusLabel(item.status) }}
                  </el-tag>
                </div>
                <div class="item-title">{{ item.title }}</div>
                <div class="item-content">{{ item.content }}</div>
                <div class="item-footer">
                  <span class="item-time">{{ formatDate(item.createTime) }}</span>
                  <span v-if="item.replyContent" class="has-reply">
                    <el-icon><Check /></el-icon> 已回复
                  </span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="意见详情" width="700px">
      <el-descriptions v-if="currentSuggestion" :column="1" border>
        <el-descriptions-item label="意见类型">
          <el-tag :type="getTypeTag(currentSuggestion.type)" size="small">
            {{ getTypeLabel(currentSuggestion.type) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标题">
          {{ currentSuggestion.title }}
        </el-descriptions-item>
        <el-descriptions-item label="内容">
          {{ currentSuggestion.content }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">
          {{ currentSuggestion.contactPhone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">
          {{ formatDate(currentSuggestion.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="getStatusTag(currentSuggestion.status)" size="small">
            {{ getStatusLabel(currentSuggestion.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="图片附件" v-if="currentSuggestion.images">
          <div class="detail-images">
            <el-image
              v-for="(img, idx) in currentSuggestion.images.split(',')"
              :key="idx"
              :src="img"
              :preview-src-list="currentSuggestion.images.split(',')"
              fit="cover"
              style="width: 100px; height: 100px; margin-right: 10px;"
            />
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="物业回复" v-if="currentSuggestion.replyContent">
          <div class="reply-content">
            <div class="reply-text">{{ currentSuggestion.replyContent }}</div>
            <div class="reply-time">回复时间：{{ formatDate(currentSuggestion.replyTime) }}</div>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="物业回复" v-else>
          <el-empty description="暂无回复" :image-size="80" />
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { ChatDotRound, Plus, InfoFilled, Upload, Refresh, Check } from '@element-plus/icons-vue';
import { submitSuggestion as submitSuggestionApi, getOwnerSuggestionList, getSuggestionDetail } from '@/api/suggestionApi.js';

// Tab 激活状态
const activeTab = ref('submit');

// 表单引用
const suggestionFormRef = ref(null);

// 表单数据
const suggestionForm = reactive({
  type: '',
  title: '',
  content: '',
  contactPhone: '',
  images: ''
});

// 图片列表
const imageList = ref([]);

// 提交状态
const submitting = ref(false);

// 表单验证规则
const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'));
  } else {
    callback();
  }
};

const suggestionRules = {
  type: [
    { required: true, message: '请选择意见类型', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入意见标题', trigger: 'blur' },
    { min: 2, max: 20, message: '标题长度在 2-20 个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入意见内容', trigger: 'blur' },
    { min: 10, max: 500, message: '内容长度在 10-500 个字符之间', trigger: 'blur' }
  ],
  contactPhone: [
    { validator: validatePhone, trigger: 'blur' }
  ]
};

// 历史意见列表
const suggestionList = ref([]);

// 详情对话框
const detailVisible = ref(false);
const currentSuggestion = ref(null);

// 页面加载时获取数据
onMounted(() => {
  loadSuggestionList();
});

// 加载历史意见列表
const loadSuggestionList = async () => {
  try {
    console.log('开始加载意见反馈列表...');
    const res = await getOwnerSuggestionList();
    console.log('意见反馈列表响应：', res);

    // 修复：axios 响应数据在 res.data 中
    if (res.data && res.data.code === 200) {
      suggestionList.value = res.data.data || [];
      console.log('意见反馈列表加载成功，共', suggestionList.value.length, '条记录');
      if (suggestionList.value.length === 0) {
        ElMessage.info('暂无历史意见记录');
      }
    } else {
      ElMessage.warning(res.data?.msg || '暂无历史意见记录');
      suggestionList.value = [];
    }
  } catch (error) {
    console.error('加载意见列表失败:', error);
    // 判断是否是 404 错误
    if (error.response?.status === 404) {
      ElMessage.error('接口不存在，请检查后端服务是否启动');
    } else if (error.response?.status === 401) {
      ElMessage.error('未登录或登录已过期，请重新登录');
    } else {
      ElMessage.error('加载意见列表失败：' + (error.message || '网络错误'));
    }
  }
};

// 处理图片变化
const handleImageChange = (file) => {
  // 验证图片格式
  const isImage = file.raw.type.startsWith('image/');
  if (!isImage) {
    ElMessage.error('只能上传图片文件');
    return;
  }

  // 验证图片大小（5MB）
  const isLt5M = file.raw.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB');
    return;
  }

  // 添加到列表
  imageList.value.push(file);
};

// 处理图片移除
const handleImageRemove = (file) => {
  const index = imageList.value.indexOf(file);
  if (index !== -1) {
    imageList.value.splice(index, 1);
  }
};

// 处理超出限制
const handleExceed = () => {
  ElMessage.warning('最多只能上传 5 张图片');
};

// 提交意见
const submitSuggestion = async () => {
  try {
    await suggestionFormRef.value.validate();

    // 检查是否有图片
    if (imageList.value.length > 0) {
      // TODO: 这里需要实现图片上传到服务器的逻辑
      // 目前是模拟图片路径，使用本地 images 目录
      const imagePaths = imageList.value.map(img => {
        // 使用本地 images 目录下的图片
        // 实际项目中应该调用上传接口将图片上传到服务器
        const localPath = `/src/assets/images/${img.name}`;
        console.log('本地图片路径:', localPath);
        return localPath;
      });
      suggestionForm.images = imagePaths.join(',');
    }

    submitting.value = true;

    // TODO: 实际项目中应该从 localStorage 获取用户 ID
    const userStr = localStorage.getItem('userInfo');
    let userId = 2;
    let ownerId = 2;

    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        userId = user.id || 2;
        ownerId = user.ownerId || 2;
      } catch (e) {
        console.error('解析用户信息失败:', e);
      }
    }

    const data = {
      type: suggestionForm.type,
      title: suggestionForm.title,
      content: suggestionForm.content,
      contactPhone: suggestionForm.contactPhone,
      images: suggestionForm.images,
      userId: userId,
      ownerId: ownerId
    };

    console.log('提交意见反馈数据：', data);

    const res = await submitSuggestionApi(data);
    console.log('意见反馈提交响应：', res);

    if (res.data && res.data.code === 200) {
      ElMessage.success('意见提交成功，我们会尽快处理');
      resetForm();
      loadSuggestionList();
      activeTab.value = 'history';
    } else {
      ElMessage.error(res.data?.msg || '提交失败');
    }
  } catch (error) {
    if (error.name === 'ValidationError') {
      ElMessage.error('请完善必填项');
    } else {
      console.error('提交意见失败:', error);
      ElMessage.error('提交失败，请稍后重试');
    }
  } finally {
    submitting.value = false;
  }
};

// 重置表单
const resetForm = () => {
  if (suggestionFormRef.value) {
    suggestionFormRef.value.resetFields();
  }
  imageList.value = [];
  suggestionForm.images = '';
};

// 查看详情
const viewDetail = async (item) => {
  try {
    console.log('查看意见反馈详情，id:', item.id);
    const res = await getSuggestionDetail(item.id);
    console.log('意见反馈详情响应：', res);

    // 修复：axios 响应数据在 res.data 中
    if (res.data && res.data.code === 200) {
      currentSuggestion.value = res.data.data;
      detailVisible.value = true;
    } else {
      ElMessage.error(res.data?.msg || '获取详情失败');
    }
  } catch (error) {
    console.error('获取详情失败:', error);
    ElMessage.error('获取详情失败');
  }
};

// 获取类型标签
const getTypeLabel = (type) => {
  const typeMap = {
    complaint: '投诉',
    suggestion: '建议',
    repair: '报修',
    consultation: '咨询'
  };
  return typeMap[type] || type;
};

const getTypeTag = (type) => {
  const tagMap = {
    complaint: 'danger',
    suggestion: 'success',
    repair: 'warning',
    consultation: 'info'
  };
  return tagMap[type] || 'info';
};

// 获取状态标签
const getStatusLabel = (status) => {
  const statusMap = {
    pending: '待处理',
    processing: '处理中',
    processed: '已处理',
    replied: '已回复'
  };
  return statusMap[status] || status;
};

const getStatusTag = (status) => {
  const tagMap = {
    pending: 'warning',
    processing: 'primary',
    processed: 'success',
    replied: 'success'
  };
  return tagMap[status] || 'info';
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
</script>

<style scoped>
.suggestion-page {
  //padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  font-size: 16px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.history-list {
  min-height: 400px;
}

.suggestion-item {
  padding: 15px;
  margin-bottom: 15px;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #e4e7ed;
}

.suggestion-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  gap: 10px;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.item-content {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.has-reply {
  color: #67c23a;
  display: flex;
  align-items: center;
  gap: 3px;
}

.detail-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.reply-content {
  background: #f0f9eb;
  padding: 15px;
  border-radius: 5px;
  border-left: 4px solid #67c23a;
}

.reply-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 10px;
}

.reply-time {
  font-size: 12px;
  color: #909399;
}

:deep(.el-tabs__header) {
  margin-bottom: 20px;
}

:deep(.el-upload-list__item) {
  transition: all 0.3s;
}

:deep(.el-input__inner) {
  height: 40px;
}
</style>
