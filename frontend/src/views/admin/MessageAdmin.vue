<template>
  <div class="message-admin-page">
    <el-card shadow="always">
      <!-- 筛选条件 -->
      <div class="search-form">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="群聊名称">
            <el-select v-model="searchForm.chatRoomName" placeholder="全部群聊" clearable style="width: 200px;">
              <el-option label="鼎湖社区业主群" value="鼎湖社区业主群" />
              <el-option label="1 号楼业主群" value="1 号楼业主群" />
              <el-option label="2 号楼业主群" value="2 号楼业主群" />
              <el-option label="3 号楼业主群" value="3 号楼业主群" />
            </el-select>
          </el-form-item>
          <el-form-item label="消息类型">
            <el-select v-model="searchForm.messageType" placeholder="全部类型" clearable style="width: 120px;">
              <el-option label="文本" value="text" />
              <el-option label="图片" value="image" />
              <el-option label="文件" value="file" />
            </el-select>
          </el-form-item>
          <el-form-item label="发送人">
            <el-input
              v-model="searchForm.senderName"
              placeholder="发送人姓名"
              clearable
              style="width: 150px;"
            />
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

      <!-- 消息列表 -->
      <div class="message-table">
        <el-table
          :data="messageList"
          border
          stripe
          style="width: 100%"
          v-loading="loading"
        >
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="chatRoomName" label="群聊名称" width="130" />
          <el-table-column prop="senderName" label="发送人" width="120" />
          <el-table-column label="消息类型" width="100">
            <template #default="scope">
              <el-tag :type="getTypeTag(scope.row.messageType)" size="small">
                {{ getTypeLabel(scope.row.messageType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="messageContent" label="消息内容" min-width="250" show-overflow-tooltip />
          <el-table-column label="附件" width="80">
            <template #default="scope">
              <el-tag v-if="scope.row.fileUrl" type="info" size="small">
                有附件
              </el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="scope">
              <el-tag v-if="scope.row.isWithdrawn" type="info" size="small">
                已撤回
              </el-tag>
              <el-tag v-else type="success" size="small">
                正常
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sendTime" label="发送时间" width="140">
            <template #default="scope">
              {{ formatDate(scope.row.sendTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button
                type="primary"
                size="small"
                @click="handleViewDetail(scope.row)"
              >
                查看
              </el-button>
              <el-button
                v-if="!scope.row.isWithdrawn"
                type="danger"
                size="small"
                @click="handleWithdraw(scope.row)"
              >
                撤回
              </el-button>
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
    <el-dialog v-model="detailVisible" title="消息详情" width="800px">
      <el-descriptions v-if="currentMessage" :column="2" border>
        <el-descriptions-item label="ID">
          {{ currentMessage.id }}
        </el-descriptions-item>
        <el-descriptions-item label="群聊名称">
          {{ currentMessage.chatRoomName }}
        </el-descriptions-item>
        <el-descriptions-item label="发送人 ID">
          {{ currentMessage.senderId }}
        </el-descriptions-item>
        <el-descriptions-item label="发送人">
          {{ currentMessage.senderName }}
        </el-descriptions-item>
        <el-descriptions-item label="消息类型">
          <el-tag :type="getTypeTag(currentMessage.messageType)" size="small">
            {{ getTypeLabel(currentMessage.messageType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发送时间">
          {{ formatDate(currentMessage.sendTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentMessage.isWithdrawn" type="info" size="small">
            已撤回
          </el-tag>
          <el-tag v-else type="success" size="small">
            正常
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="撤回时间" v-if="currentMessage.isWithdrawn">
          {{ formatDate(currentMessage.withdrawTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="消息内容" :span="2">
          <div class="message-content-detail">
            {{ currentMessage.messageContent }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="附件" :span="2" v-if="currentMessage.fileUrl">
          <div v-if="currentMessage.messageType === 'image'" class="detail-image">
            <el-image
              :src="currentMessage.fileUrl"
              fit="cover"
              style="width: 300px; height: 300px; cursor: pointer;"
              :preview-src-list="[currentMessage.fileUrl]"
            />
            <div class="file-name">{{ currentMessage.fileName }}</div>
          </div>
          <div v-else-if="currentMessage.messageType === 'file'" class="detail-file">
            <el-button type="primary" link @click="downloadFile(currentMessage)">
              <el-icon><Document /></el-icon>
              {{ currentMessage.fileName || '下载文件' }}
            </el-button>
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="!currentMessage?.isWithdrawn"
          type="danger"
          @click="handleWithdraw(currentMessage)"
        >
          撤回消息
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ChatDotRound, Search, Refresh, Document } from '@element-plus/icons-vue';
import {
  getMessageList,
  withdrawMessage
} from '@/api/messageApi.js';

// 搜索表单
const searchForm = reactive({
  chatRoomName: '',
  messageType: '',
  senderName: ''
});

// 分页参数
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 消息列表
const messageList = ref([]);
const loading = ref(false);

// 详情对话框
const detailVisible = ref(false);
const currentMessage = ref(null);

// 页面加载时获取数据
onMounted(() => {
  loadMessageList();
});

// 加载消息列表
const loadMessageList = async () => {
  try {
    loading.value = true;

    const params = {
      chatRoomName: searchForm.chatRoomName || '鼎湖社区业主群', // 默认群聊
      messageType: searchForm.messageType || undefined, // 可选
      senderName: searchForm.senderName || undefined, // 可选
      pageNum: pageNum.value,
      pageSize: pageSize.value
    };

    console.log('查询消息列表参数：', params);

    const res = await getMessageList(params);
    console.log('消息列表响应：', res);

    if (res.data && res.data.code === 200) {
      messageList.value = res.data.data?.list || [];
      total.value = res.data.data?.total || 0;
      console.log('消息列表加载成功，总数：', total.value);
    } else {
      ElMessage.warning(res.data?.msg || '暂无消息记录');
      messageList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('加载消息列表失败:', error);
    ElMessage.error('加载消息列表失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  console.log('执行搜索，参数：', searchForm);
  pageNum.value = 1;
  loadMessageList();
};

// 重置
const handleReset = () => {
  searchForm.chatRoomName = '';
  searchForm.messageType = '';
  searchForm.senderName = '';
  pageNum.value = 1;
  loadMessageList();
};

// 查看详情
const handleViewDetail = (row) => {
  currentMessage.value = row;
  detailVisible.value = true;
};

// 撤回消息
const handleWithdraw = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要撤回这条消息吗？撤回后将无法恢复。`,
      '撤回确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    console.log('撤回消息，id:', row.id);

    const res = await withdrawMessage(row.id);
    console.log('撤回响应：', res);

    if (res.data && res.data.code === 200) {
      ElMessage.success('撤回成功');
      loadMessageList();
      if (detailVisible.value) {
        detailVisible.value = false;
      }
    } else {
      ElMessage.error(res.data?.msg || '撤回失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤回消息失败:', error);
      ElMessage.error('撤回失败，请稍后重试');
    }
  }
};

// 分页大小改变
const handleSizeChange = (size) => {
  pageSize.value = size;
  pageNum.value = 1;
  loadMessageList();
};

// 页码改变
const handleCurrentChange = (page) => {
  pageNum.value = page;
  loadMessageList();
};

// 获取类型标签
const getTypeLabel = (type) => {
  const typeMap = {
    text: '文本',
    image: '图片',
    file: '文件'
  };
  return typeMap[type] || '未知';
};

const getTypeTag = (type) => {
  const tagMap = {
    text: 'info',      // 文本消息 - 灰色
    image: 'success',  // 图片消息 - 绿色
    file: 'warning'    // 文件消息 - 橙色
  };
  return tagMap[type] || 'info';
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

// 下载文件
const downloadFile = (msg) => {
  if (msg.fileUrl) {
    window.open(msg.fileUrl, '_blank');
  }
};
</script>

<style scoped>
.message-admin-page {
  padding: 20px;
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
  margin-bottom: 20px;
}

.message-table {
  margin-top: 20px;
}

.message-content-detail {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 5px;
  border-left: 4px solid #409eff;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.detail-image {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.file-name {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
}

.detail-file {
  padding: 10px 0;
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
