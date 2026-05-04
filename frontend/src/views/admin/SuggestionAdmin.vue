<template>
  <div class="suggestion-admin-page">
    <el-card shadow="always">

      <!-- 筛选条件 -->
      <div class="search-form">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="意见类型">
            <el-select v-model="searchForm.type" placeholder="全部类型" clearable style="width: 150px;">
              <el-option label="投诉" value="complaint" />
              <el-option label="建议" value="suggestion" />
              <el-option label="报修" value="repair" />
              <el-option label="咨询" value="consultation" />
            </el-select>
          </el-form-item>
          <el-form-item label="处理状态">
            <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 150px;">
              <el-option label="待处理" value="pending" />
              <el-option label="处理中" value="processing" />
              <el-option label="已处理" value="processed" />
              <el-option label="已回复" value="replied" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              查询
            </el-button>
            <el-button @click="handleReset">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 意见反馈列表 -->
      <div class="suggestion-table">
        <el-table
          :data="suggestionList"
          border
          stripe
          style="width: 100%;"
          v-loading="loading"
        >
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column label="意见类型" width="90">
            <template #default="scope">
              <el-tag :type="getTypeTag(scope.row.type)" size="small">
                {{ getTypeLabel(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" width="160" show-overflow-tooltip />
          <el-table-column prop="content" label="内容" width="250" show-overflow-tooltip />
          <el-table-column prop="contactPhone" label="联系电话" width="120" />
          <el-table-column label="处理状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusTag(scope.row.status)" size="small">
                {{ getStatusLabel(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="回复情况" width="90">
            <template #default="scope">
              <el-tag v-if="scope.row.replyContent" type="success" size="small">
                已回复
              </el-tag>
              <el-tag v-else type="info" size="small">
                未回复
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="提交时间" width="120">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="scope">
              <el-button
                type="primary"
                size="small"
                @click="handleViewDetail(scope.row)"
              >
                查看
              </el-button>
              <el-button
                v-if="!scope.row.replyContent"
                type="success"
                size="small"
                @click="handleReply(scope.row)"
              >
                回复
              </el-button>
              <el-dropdown @command="(command) => handleStatusChange(command, scope.row)" style="margin-left: 5px;">
                <el-button size="small">
                  更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="processing">处理中</el-dropdown-item>
                    <el-dropdown-item command="processed">已处理</el-dropdown-item>
                    <el-dropdown-item command="replied">标记为已回复</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          style="margin-top: 20px; text-align: right"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="意见反馈详情" width="800px">
      <el-descriptions v-if="currentSuggestion" :column="2" border>
        <el-descriptions-item label="ID">
          {{ currentSuggestion.id }}
        </el-descriptions-item>
        <el-descriptions-item label="意见类型">
          <el-tag :type="getTypeTag(currentSuggestion.type)" size="small">
            {{ getTypeLabel(currentSuggestion.type) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">
          {{ currentSuggestion.title }}
        </el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">
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
        <el-descriptions-item label="回复情况">
          <el-tag v-if="currentSuggestion.replyContent" type="success" size="small">
            已回复
          </el-tag>
          <el-tag v-else type="info" size="small">
            未回复
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="图片附件" :span="2" v-if="currentSuggestion.images">
          <div class="detail-images">
            <el-image
              v-for="(img, idx) in currentSuggestion.images.split(',')"
              :key="idx"
              :src="img"
              :preview-src-list="currentSuggestion.images.split(',')"
              fit="cover"
              style="width: 120px; height: 120px; margin-right: 10px; cursor: pointer;"
            />
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="回复内容" :span="2" v-if="currentSuggestion.replyContent">
          <div class="reply-content">
            <div class="reply-text">{{ currentSuggestion.replyContent }}</div>
            <div class="reply-meta">
              <span>回复人 ID: {{ currentSuggestion.replyUserId }}</span>
              <span>回复时间：{{ formatDate(currentSuggestion.replyTime) }}</span>
            </div>
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="!currentSuggestion?.replyContent"
          type="primary"
          @click="handleReply(currentSuggestion)"
        >
          回复
        </el-button>
      </template>
    </el-dialog>

    <!-- 回复对话框 -->
    <el-dialog v-model="replyVisible" title="回复意见反馈" width="600px">
      <el-form ref="replyFormRef" :model="replyForm" :rules="replyRules" label-width="100px">
        <el-alert
          title="回复提示"
          type="info"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <p>请认真回复业主的意见，您的回复将展示在业主端。</p>
        </el-alert>
        <el-form-item label="意见标题">
          <div style="color: #606266;">{{ currentSuggestion?.title }}</div>
        </el-form-item>
        <el-form-item label="意见内容">
          <div style="color: #606266; max-height: 150px; overflow-y: auto;">
            {{ currentSuggestion?.content }}
          </div>
        </el-form-item>
        <el-form-item label="回复内容" prop="replyContent">
          <el-input
            v-model="replyForm.replyContent"
            type="textarea"
            :rows="5"
            placeholder="请输入回复内容..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :loading="replying">
          提交回复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ChatDotRound, Search, Refresh, ArrowDown } from '@element-plus/icons-vue';
import {
  getSuggestionList,
  getSuggestionDetail,
  replySuggestion,
  updateSuggestionStatus
} from '@/api/suggestionApi.js';

// 搜索表单
const searchForm = reactive({
  type: '',
  status: ''
});

// 分页参数
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 意见反馈列表
const suggestionList = ref([]);
const loading = ref(false);

// 详情对话框
const detailVisible = ref(false);
const currentSuggestion = ref(null);

// 回复对话框
const replyVisible = ref(false);
const replyFormRef = ref(null);
const replyForm = reactive({
  id: null,
  replyContent: ''
});
const replying = ref(false);

// 回复表单验证规则
const replyRules = {
  replyContent: [
    { required: true, message: '请输入回复内容', trigger: 'blur' },
    { min: 10, max: 500, message: '回复内容长度在 10-500 个字符之间', trigger: 'blur' }
  ]
};

// 页面加载时获取数据
onMounted(() => {
  loadSuggestionList();
});

// 加载意见反馈列表
const loadSuggestionList = async () => {
  try {
    loading.value = true;

    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      type: searchForm.type,
      status: searchForm.status
    };

    console.log('查询意见反馈列表参数：', params);

    const res = await getSuggestionList(params);
    console.log('意见反馈列表响应：', res);
    console.log('响应 data 字段：', res.data);

    // 修复：判断 res.data.code 而不是 res.code
    if (res.data && res.data.code === 200) {
      suggestionList.value = res.data.data?.list || [];
      total.value = res.data.data?.total || 0;
      console.log('意见反馈列表加载成功，总数：', total.value);
      console.log('列表数据：', suggestionList.value);
    } else {
      ElMessage.warning(res.data?.msg || '暂无意见反馈记录');
      suggestionList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('加载意见反馈列表失败:', error);
    ElMessage.error('加载意见反馈列表失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  console.log('执行搜索，参数：', searchForm);
  pageNum.value = 1;
  loadSuggestionList();
};

// 重置
const handleReset = () => {
  searchForm.type = '';
  searchForm.status = '';
  pageNum.value = 1;
  loadSuggestionList();
};

// 查看详情
const handleViewDetail = async (row) => {
  try {
    console.log('查看意见反馈详情，id:', row.id);

    const res = await getSuggestionDetail(row.id);
    console.log('意见反馈详情响应：', res);
    console.log('响应 data 字段：', res.data);

    // 修复：判断 res.data.code 而不是 res.code
    if (res.data && res.data.code === 200) {
      currentSuggestion.value = res.data.data;
      console.log('详情数据加载成功：', currentSuggestion.value);
      detailVisible.value = true;
    } else {
      ElMessage.error(res.data?.msg || '获取详情失败');
    }
  } catch (error) {
    console.error('获取详情失败:', error);
    ElMessage.error('获取详情失败');
  }
};

// 回复意见反馈
const handleReply = (row) => {
  currentSuggestion.value = row;
  replyForm.id = row.id;
  replyForm.replyContent = '';
  replyVisible.value = true;
};

// 提交回复
const submitReply = async () => {
  try {
    await replyFormRef.value.validate();
    replying.value = true;

    // 从 localStorage 获取用户 ID
    const userStr = localStorage.getItem('user');
    let userId = 1; // 默认管理员 ID

    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        userId = user.id || 1;
        console.log('当前登录用户ID:', userId);
      } catch (e) {
        console.error('解析用户信息失败:', e);
      }
    }

    const data = {
      id: replyForm.id,
      replyContent: replyForm.replyContent,
      replyUserId: userId
    };

    console.log('提交回复数据：', data);

    const res = await replySuggestion(data);
    console.log('回复响应完整对象：', res);
    console.log('res.data:', res.data);
    console.log('res.data.code:', res.data?.code);
    console.log('res.data.msg:', res.data?.msg);

    // 正确判断响应数据
    if (res.data && res.data.code === 200) {
      ElMessage.success('回复成功');
      replyVisible.value = false;
      detailVisible.value = false;
      loadSuggestionList();
    } else {
      ElMessage.error(res.data?.msg || '回复失败');
    }
  } catch (error) {
    console.error('回复意见反馈失败，错误详情：', error);
    console.error('错误响应：', error.response);
    console.error('错误响应数据：', error.response?.data);

    if (error.name === 'ValidationError') {
      ElMessage.error('请完善必填项');
    } else {
      const errorMsg = error.response?.data?.msg || error.msg || error.message || '回复失败，请稍后重试';
      ElMessage.error(errorMsg);
    }
  } finally {
    replying.value = false;
  }
};

// 更新状态
const handleStatusChange = async (command, row) => {
  try {
    console.log('更新意见反馈状态，id:', row.id, 'status:', command);

    const data = {
      id: row.id,
      status: command
    };

    const res = await updateSuggestionStatus(data);
    console.log('状态更新响应：', res);
    console.log('响应 data 字段：', res.data);

    if (res.data && res.data.code === 200) {
      ElMessage.success('状态更新成功');
      loadSuggestionList();
    } else {
      ElMessage.error(res.data?.msg || '状态更新失败');
    }
  } catch (error) {
    console.error('更新状态失败:', error);
    ElMessage.error('状态更新失败');
  }
};

// 分页大小改变
const handleSizeChange = (size) => {
  pageSize.value = size;
  pageNum.value = 1;
  loadSuggestionList();
};

// 页码改变
const handleCurrentChange = (page) => {
  pageNum.value = page;
  loadSuggestionList();
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
.suggestion-admin-page {
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

.search-form {
}

.suggestion-table {
}

.detail-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.reply-content {
  background: #f0f9eb;
  border-radius: 5px;
  border-left: 4px solid #67c23a;
}

.reply-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 10px;
}

.reply-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  justify-content: space-between;
  gap: 20px;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-dialog__header) {
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-dialog__footer) {
  padding: 10px 20px;
  border-top: 1px solid #ebeef5;
}
</style>
