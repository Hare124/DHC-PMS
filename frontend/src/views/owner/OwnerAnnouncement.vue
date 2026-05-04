<!-- src/views/owner/OwnerAnnouncement.vue -->
<template>
  <div class="owner-announcement">

    <!-- 公告列表 -->
    <el-card shadow="never" class="list-card">
      <div class="notice-list" v-loading="loading">
        <div
            class="notice-item"
            v-for="notice in tableData"
            :key="notice.id"
            @click="handleRowClick(notice)"
        >
          <div class="notice-title">
            <el-tag v-if="notice.isTop === 1" size="small" type="danger" style="margin-right: 8px;">置顶</el-tag>
            {{ notice.title }}
          </div>
          <div class="notice-time">{{ formatTime(notice.publishTime) }}</div>
          <div class="notice-content">{{ truncateContent(notice.content) }}</div>
        </div>
        <el-empty v-if="tableData.length === 0" description="暂无公告" />
      </div>

      <!-- 分页 -->
      <el-pagination
          v-model:current-page="searchForm.pageNum"
          v-model:page-size="searchForm.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getAnnouncementList"
          @current-change="getAnnouncementList"          style="margin-top: 20px; text-align: right"
      />
    </el-card>

    <!-- 公告详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" width="700px">
      <div class="detail-header">
        <h2>{{ detailData.title }}</h2>
        <div class="detail-meta">
          <span>发布人：{{ detailData.publisherName }}</span>
          <span>发布时间：{{ formatTime(detailData.publishTime) }}</span>
          <span>阅读次数：{{ detailData.readCount }}</span>
          <el-tag v-if="detailData.isTop === 1" type="danger">置顶</el-tag>
        </div>
      </div>
      <div class="detail-content">
        {{ detailData.content }}
      </div>
      <div class="detail-navigation">
        <div class="nav-item" @click="viewPrevious" :class="{ disabled: !hasPrevious }">
          <el-icon><ArrowLeft /></el-icon>
          <span>上一篇</span>
        </div>
        <div class="nav-item" @click="viewNext" :class="{ disabled: !hasNext }">
          <span>下一篇</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import dayjs from 'dayjs';
import {
  ownerGetAnnouncementList,
  ownerGetAnnouncementDetail
} from '@/api/announcementApi.js';
import { ArrowLeft, ArrowRight, Close } from '@element-plus/icons-vue';

// 时间格式化过滤器
const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-';
};

// 内容截断函数
const truncateContent = (content) => {
  if (!content) return '';
  if (content.length <= 50) return content;
  return content.substring(0, 50) + '...';
};

// 搜索表单
const searchForm = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  dateRange: [],
  isTop: ''
});

// 列表数据
const tableData = ref([]);
const total = ref(0);
const loading = ref(false);
const viewingDetailId = ref(null); // 防止重复点击查看
const currentIndex = ref(-1); // 当前查看的公告索引

// 是否有上一篇和下一篇
const hasPrevious = computed(() => currentIndex.value > 0);
const hasNext = computed(() => currentIndex.value < tableData.value.length - 1);

// 查看上一篇
const viewPrevious = () => {
  if (currentIndex.value > 0) {
    const previousNotice = tableData.value[currentIndex.value - 1];
    viewDetail(previousNotice.id);
  }
};

// 查看下一篇
const viewNext = () => {
  if (currentIndex.value < tableData.value.length - 1) {
    const nextNotice = tableData.value[currentIndex.value + 1];
    viewDetail(nextNotice.id);
  }
};
// 详情弹窗
const detailDialogVisible = ref(false);
const detailData = ref({});

// 重置搜索
const resetSearch = () => {
  searchForm.pageNum = 1;
  searchForm.keyword = '';
  searchForm.dateRange = [];
  searchForm.isTop = '';
  getAnnouncementList();
};

// 获取公告列表
const getAnnouncementList = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: searchForm.pageNum,
      pageSize: searchForm.pageSize,
      keyword: searchForm.keyword || '',
      startTime: searchForm.dateRange?.[0] || '',
      endTime: searchForm.dateRange?.[1] || '',
      isTop: searchForm.isTop || null
    };
    const res = await ownerGetAnnouncementList(params);
    console.log('[业主端公告列表] 响应数据:', res);
    console.log('[业主端公告列表] res.data:', res.data);
    // 修复：适配后端返回格式
    if (res.data && res.data.data && res.data.data.records) {
      tableData.value = res.data.data.records;
      total.value = res.data.data.total;
      console.log('[业主端公告列表] 数据加载成功:', tableData.value);
    } else {
      console.warn('[业主端公告列表] 数据为空');
      tableData.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('获取公告列表失败：', error);
    ElMessage.error('获取公告列表失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 查看详情
const viewDetail = async (id) => {
  // 防止重复点击
  if (viewingDetailId.value === id) {
    return;
  }

  try {
    viewingDetailId.value = id;
    const res = await ownerGetAnnouncementDetail(id);
    console.log('[公告详情] 响应数据:', res);
    console.log('[公告详情] res.data:', res.data);
    detailData.value = res.data && res.data.data ? res.data.data : res.data;
    detailDialogVisible.value = true;

    // 记录当前索引
    currentIndex.value = tableData.value.findIndex(item => item.id === id);

    getAnnouncementList();
  } catch (error) {
    console.error('查看详情失败：', error);
    ElMessage.error('查看详情失败，请稍后重试');
  } finally {
    // 重置标记
    viewingDetailId.value = null;
  }
};

// 行点击事件：查看详情
const handleRowClick = (row) => {
  viewDetail(row.id);
};

// 页面挂载时加载列表
onMounted(() => {
  getAnnouncementList();
});
</script>

<style scoped>
.owner-announcement {
  height: 100%;
}

.list-card {
  margin-bottom: 20px;
}

/* 公告列表样式 */
.notice-list {
  min-height: 400px;
}

.notice-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-item:hover {
  background-color: #f5f7fa;
  padding-left: 8px;
}

.notice-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 8px;
  line-height: 1.5;
}

.notice-time {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.notice-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 详情弹窗样式 */
.detail-header {
  margin-bottom: 20px;
}

.detail-header h2 {
  font-size: 20px;
  margin: 0 0 15px 0;
  color: #303133;
}

.detail-meta {
  display: flex;
  gap: 15px;
  font-size: 13px;
  color: #909399;
  margin-bottom: 20px;
}

.detail-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

/* 详情导航样式 */
.detail-navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #409EFF;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.nav-item:hover {
  color: #66b1ff;
}

.nav-item.disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}

.nav-item.disabled:hover {
  color: #c0c4cc;
}
</style>