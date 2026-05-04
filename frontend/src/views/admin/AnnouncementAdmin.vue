<!-- src/views/announcement/AnnouncementAdmin.vue -->
<template>
  <div class="announcement-manager">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" class="search-form">
        <div class="search-row">
          <div class="search-item">
            <label class="search-label">关键词</label>
            <el-input v-model="searchForm.keyword" placeholder="请输入标题/内容关键词" clearable style="width: 200px;" />
          </div>
          <div class="search-item">
            <label class="search-label">置顶状态</label>
            <el-select v-model="searchForm.isTop" placeholder="全部" clearable style="width: 150px;">
              <el-option label="置顶" value="1" />
              <el-option label="未置顶" value="0" />
            </el-select>
          </div>
          <div class="search-item">
            <label class="search-label">撤回状态</label>
            <el-select v-model="searchForm.recallStatus" placeholder="全部" clearable style="width: 150px;">
              <el-option label="已撤回" value="1" />
              <el-option label="未撤回" value="0" />
            </el-select>
          </div>
        </div>

        <div class="search-row search-row-second">
          <div class="search-item">
            <label class="search-label">发布时间</label>
            <el-date-picker
                v-model="searchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
            />
          </div>
          <div class="search-item search-buttons">
            <el-button type="primary" @click="getAnnouncementList">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>

          </div>
        </div>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <div class="operate-card">
      <el-button type="primary" @click="openPublishDialog">发布公告</el-button>
      <el-button type="warning" @click="openSchedulePublishDialog">定时发布</el-button>
      <el-button type="warning" @click="openRegulationDialog">员工制度</el-button>
    </div>

    <!-- 公告列表 -->
    <el-card shadow="never" class="list-card">
      <el-table
          v-loading="loading"
          :data="tableData"
          border
          stripe
          style="width: 100%"
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="publishTime" label="发布时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="isTop" label="置顶状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isTop === 1 ? 'danger' : 'info'">
              {{ scope.row.isTop === 1 ? '置顶' : '未置顶' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="readCount" label="阅读次数" width="100" />
        <el-table-column prop="recallStatus" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row)">
              {{ getStatusText(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewDetail(scope.row)">查看</el-button>
            <el-button
                size="small"
                type="warning"
                @click="openUpdateDialog(scope.row)"
                v-if="scope.row.recallStatus === 0"
            >
              修改
            </el-button>
            <el-button
                size="small"
                type="danger"
                @click="recallAnnouncement(scope.row.id)"
                v-if="scope.row.recallStatus === 0"
            >
              撤回
            </el-button>
            <el-button
                size="small"
                type="success"
                @click="setTop(scope.row.id, scope.row.isTop === 1 ? 0 : 1)"
                v-if="scope.row.recallStatus === 0"
            >
              {{ scope.row.isTop === 1 ? '取消置顶' : '置顶' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
          v-model:current-page="searchForm.pageNum"
          v-model:page-size="searchForm.pageSize"
          :total="total"
          page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getAnnouncementList"
          @current-change="getAnnouncementList"
          style="margin-top: 20px; text-align: right"
      />
    </el-card>

    <!-- 发布公告弹窗 -->
    <el-dialog v-model="publishDialogVisible" title="发布公告" width="600px">
      <el-form :model="publishForm" :rules="publishRules" ref="publishFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="publishForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
              v-model="publishForm.content"
              type="textarea"
              :rows="6"
              placeholder="请输入公告内容"
          />
        </el-form-item>
        <el-form-item label="是否置顶">
          <el-switch v-model="publishForm.isTop" active-value="1" inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPublish">发布</el-button>
      </template>
    </el-dialog>

    <!-- 定时发布弹窗 -->
    <el-dialog v-model="scheduleDialogVisible" title="定时发布公告" width="600px">
      <el-form :model="scheduleForm" :rules="scheduleRules" ref="scheduleFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="scheduleForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
              v-model="scheduleForm.content"
              type="textarea"
              :rows="6"
              placeholder="请输入公告内容"
          />
        </el-form-item>
        <el-form-item label="是否置顶">
          <el-switch v-model="scheduleForm.isTop" active-value="1" inactive-value="0" />
        </el-form-item>
        <el-form-item label="发布时间" prop="scheduledPublishTime">
          <el-date-picker
              v-model="scheduleForm.scheduledPublishTime"
              type="datetime"
              placeholder="请选择定时发布时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSchedulePublish">确认定时发布</el-button>
      </template>
    </el-dialog>

    <!-- 修改公告弹窗 -->
    <el-dialog v-model="updateDialogVisible" title="修改公告" width="600px">
      <el-form :model="updateForm" :rules="updateRules" ref="updateFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="updateForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
              v-model="updateForm.content"
              type="textarea"
              :rows="6"
              placeholder="请输入公告内容"
          />
        </el-form-item>
        <el-form-item label="是否置顶">
          <el-switch v-model="updateForm.isTop" active-value="1" inactive-value="0" />
        </el-form-item>
        <el-form-item label="重置阅读数">
          <el-switch v-model="updateForm.resetReadCount" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="updateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUpdate">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 公告详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="公告详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="标题">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="内容">
          <div style="white-space: pre-wrap">{{ detailData.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="发布时间">
          {{ detailData.publishTime | formatTime }}
        </el-descriptions-item>
        <el-descriptions-item label="置顶状态">
          {{ detailData.isTop === 1 ? '置顶' : '未置顶' }}
        </el-descriptions-item>
        <el-descriptions-item label="阅读次数">{{ detailData.readCount }}</el-descriptions-item>
        <el-descriptions-item label="发布人">{{ detailData.publisherName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          {{ detailData.recallStatus === 1 ? '已撤回' : '正常' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 员工制度列表弹窗 -->
    <el-dialog v-model="regulationDialogVisible" title="员工制度" width="992px">
      <el-table :data="regulationList" border v-loading="regulationLoading">
        <el-table-column prop="regulationNo" label="制度编号" width="120" />
        <el-table-column prop="regulationName" label="制度名称" width="240" />
        <el-table-column prop="regulationType" label="制度类型" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getRegulationStatusType(row.status)">
              {{ getRegulationStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="版本号" width="80">
          <template #default="{ row }">
            V{{ row.version }}
          </template>
        </el-table-column>
        <el-table-column prop="effectiveDate" label="生效日期" width="100" />
        <el-table-column prop="createByName" label="创建人" width="100" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewRegulationDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="regulationDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 制度详情弹窗 -->
    <el-dialog v-model="regulationDetailDialogVisible" title="制度详情" width="900px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="制度基本信息" :span="1">
          <el-row :gutter="20">
            <el-col :span="8">
              <div style="font-weight: bold; margin-bottom: 8px;">基本信息</div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">制度 ID：</span>
                <span>{{ currentRegulation.id }}</span>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">制度编号：</span>
                <span>{{ currentRegulation.regulationNo }}</span>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">制度名称：</span>
                <span>{{ currentRegulation.regulationName }}</span>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">制度类型：</span>
                <el-tag size="small">{{ currentRegulation.regulationType }}</el-tag>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">版本号：</span>
                <span>V{{ currentRegulation.version }}</span>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">状态：</span>
                <el-tag :type="getRegulationStatusType(currentRegulation.status)" size="small">
                  {{ getRegulationStatusLabel(currentRegulation.status) }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="8">
              <div style="font-weight: bold; margin-bottom: 8px;">适用范围</div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">适用范围：</span>
                <span>{{ currentRegulation.applyScope || '未设置' }}</span>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">生效日期：</span>
                <span>{{ currentRegulation.effectiveDate || '未设置' }}</span>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">失效日期：</span>
                <span>{{ currentRegulation.invalidDate || '未设置' }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div style="font-weight: bold; margin-bottom: 8px;">人员信息</div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">创建人：</span>
                <span>{{ currentRegulation.createByName || '未知' }}</span>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">创建时间：</span>
                <span>{{ formatTime(currentRegulation.createTime) }}</span>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">更新人：</span>
                <span>{{ currentRegulation.updateByName || '未知' }}</span>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">更新时间：</span>
                <span>{{ formatTime(currentRegulation.updateTime) }}</span>
              </div>
            </el-col>
          </el-row>
        </el-descriptions-item>
        <el-descriptions-item label="附件信息" :span="1" v-if="currentRegulation.attachmentUrl">
          <el-link :href="currentRegulation.attachmentUrl" target="_blank" type="primary" icon="Document">
            {{ currentRegulation.attachmentName || '下载附件' }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item label="制度内容" :span="1">
          <div style="max-height: 500px; overflow-y: auto; padding: 15px; background-color: #f5f7fa; border-radius: 4px; line-height: 1.8; white-space: pre-wrap; word-wrap: break-word; font-family: inherit;">
            {{ currentRegulation.content }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="regulationDetailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import dayjs from 'dayjs';
import {
  managerGetAnnouncementList,
  managerPublishAnnouncement,
  managerSchedulePublish,
  managerSetTop,
  managerUpdateAnnouncement,
  managerRecallAnnouncement,
  managerGetAnnouncementDetail
}  from '@/api/announcementApi.js';
import { getRegulationList } from '@/api/adminApi.js';

// 时间格式化过滤器
const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-';
};

// 搜索表单
const searchForm = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  dateRange: [],
  isTop: '',
  recallStatus: ''
});

// 列表数据
const tableData = ref([]);
const total = ref(0);
const loading = ref(false);

// 弹窗状态
const publishDialogVisible = ref(false);
const scheduleDialogVisible = ref(false);
const updateDialogVisible = ref(false);
const detailDialogVisible = ref(false);

// 发布表单
const publishFormRef = ref(null);
const publishForm = reactive({
  title: '',
  content: '',
  isTop: '0'
});
const publishRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
};

// 定时发布表单
const scheduleFormRef = ref(null);
const scheduleForm = reactive({
  title: '',
  content: '',
  isTop: '0',
  scheduledPublishTime: ''
});
const scheduleRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }],
  scheduledPublishTime: [{ required: true, message: '请选择定时发布时间', trigger: 'change' }]
};

// 修改表单
const updateFormRef = ref(null);
const updateForm = reactive({
  id: '',
  title: '',
  content: '',
  isTop: '0',
  resetReadCount: false
});
const updateRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
};

// 详情数据
const detailData = ref({});

// 员工制度相关
const regulationDialogVisible = ref(false);
const regulationDetailDialogVisible = ref(false);
const regulationList = ref([]);
const regulationLoading = ref(false);
const currentRegulation = ref({});

// 重置搜索
const resetSearch = () => {
  searchForm.pageNum = 1;
  searchForm.keyword = '';
  searchForm.dateRange = [];
  searchForm.isTop = '';
  searchForm.recallStatus = '';
  getAnnouncementList();
};

// 打开发布公告弹窗
const openPublishDialog = () => {
  // 重置表单
  if (publishFormRef.value) {
    publishFormRef.value.resetFields();
  }
  publishForm.title = '';
  publishForm.content = '';
  publishForm.isTop = '0';
  publishDialogVisible.value = true;
};

// 打开定时发布弹窗
const openSchedulePublishDialog = () => {
  // 重置表单
  if (scheduleFormRef.value) {
    scheduleFormRef.value.resetFields();
  }
  scheduleForm.title = '';
  scheduleForm.content = '';
  scheduleForm.isTop = '0';
  scheduleForm.scheduledPublishTime = '';
  scheduleDialogVisible.value = true;
};

// 获取公告列表
const getAnnouncementList = async () => {
  loading.value = true;
  console.log('=== [开始] 获取公告列表 ===');
  console.log('[搜索表单] searchForm:', JSON.parse(JSON.stringify(searchForm)));

  try {
    // 处理时间范围 - 只传递日期部分，让后端自己处理时间
    const params = {
      ...searchForm,
      startTime: searchForm.dateRange?.[0] || '',
      endTime: searchForm.dateRange?.[1] || ''
    };

    console.log('[请求参数] params:', params);
    console.log('[请求 URL] /announcement/manager/list');

    const res = await managerGetAnnouncementList(params);

    console.log('[后端响应] 完整响应 res:', res);
    console.log('[后端响应] res.data:', res.data);
    console.log('[后端响应] res.code:', res.code);
    console.log('[后端响应] res.msg:', res.msg);

    // 修复：正确的数据路径是 res.data.data.records
    if (res.data && res.data.data && res.data.data.records) {
      console.log('[数据解析] records:', res.data.data.records);
      console.log('[数据解析] total:', res.data.data.total);

      tableData.value = res.data.data.records;
      total.value = res.data.data.total;

      console.log('[成功] 数据加载完成，表格数据:', tableData.value);
    } else {
      console.warn('[警告] res.data 或 res.data.records 为空');
      console.log('[调试] 尝试访问 res.data:', res.data);
      tableData.value = [];
      total.value = 0;
    }

  } catch (error) {
    console.error('=== [失败] 获取公告列表异常 ===');
    console.error('[错误类型]', error.constructor.name);
    console.error('[错误消息]', error.message);
    console.error('[错误详情]', error);

    if (error.response) {
      console.error('[响应状态] status:', error.response.status);
      console.error('[响应数据] data:', error.response.data);
      console.error('[响应头] headers:', error.response.headers);
    } else if (error.request) {
      console.error('[请求已发送无响应] request:', error.request);
    }

    ElMessage.error('获取公告列表失败：' + (error.message || '请稍后重试'));
  } finally {
    loading.value = false;
    console.log('=== [结束] 获取公告列表 ===\n');
  }
};

// 发布公告
const submitPublish = async () => {
  try {
    await publishFormRef.value.validate();
    const res = await managerPublishAnnouncement({
      ...publishForm,
      isTop: Number(publishForm.isTop)
    });
    // 修复：使用 res.data.msg
    ElMessage.success(res.data.msg || '发布成功');
    publishDialogVisible.value = false;
    // 重置表单
    publishFormRef.value.resetFields();
    publishForm.title = '';
    publishForm.content = '';
    publishForm.isTop = '0';
    // 刷新列表
    getAnnouncementList();
  } catch (error) {
    console.error('发布公告失败：', error);
    ElMessage.error('发布失败，请稍后重试');
  }
};

// 定时发布公告
const submitSchedulePublish = async () => {
  try {
    await scheduleFormRef.value.validate();
    // 校验时间是否晚于当前时间
    if (dayjs(scheduleForm.scheduledPublishTime).isBefore(dayjs())) {
      ElMessage.error('定时发布时间必须晚于当前时间');
      return;
    }
    const res = await managerSchedulePublish({
      ...scheduleForm,
      isTop: Number(scheduleForm.isTop),
      scheduledPublishTime: dayjs(scheduleForm.scheduledPublishTime).format('YYYY-MM-DD HH:mm:ss')
    });
    // 修复：使用 res.data.msg
    ElMessage.success(res.data.msg || '定时发布任务创建成功');
    scheduleDialogVisible.value = false;
    // 重置表单
    scheduleFormRef.value.resetFields();
    scheduleForm.title = '';
    scheduleForm.content = '';
    scheduleForm.isTop = '0';
    scheduleForm.scheduledPublishTime = '';
    // 刷新列表
    getAnnouncementList();
  } catch (error) {
    console.error('定时发布公告失败：', error);
    ElMessage.error('定时发布失败，请稍后重试');
  }
};

// 设置/取消置顶
const setTop = async (id, isTop) => {
  try {
    const res = await managerSetTop(id, isTop);
    ElMessage.success(res.data.msg || (isTop === 1 ? '置顶成功' : '取消置顶成功'));
    getAnnouncementList();
  } catch (error) {
    console.error('设置置顶失败：', error);
    ElMessage.error('操作失败，请稍后重试');
  }
};

// 打开修改弹窗
const openUpdateDialog = (row) => {
  updateForm.id = row.id;
  updateForm.title = row.title;
  updateForm.content = row.content;
  updateForm.isTop = row.isTop + '';
  updateForm.resetReadCount = false;
  updateDialogVisible.value = true;
};

// 提交修改
const submitUpdate = async () => {
  try {
    await updateFormRef.value.validate();
    const res = await managerUpdateAnnouncement(updateForm.id, {
      title: updateForm.title,
      content: updateForm.content,
      isTop: Number(updateForm.isTop),
      resetReadCount: updateForm.resetReadCount
    });
    ElMessage.success(res.data.msg || '修改成功');
    updateDialogVisible.value = false;
    // 刷新列表
    getAnnouncementList();
  } catch (error) {
    console.error('修改公告失败：', error);
    ElMessage.error('修改失败，请稍后重试');
  }
};

// 撤回公告
const recallAnnouncement = async (id) => {
  try {
    await ElMessageBox.confirm('确定要撤回该公告吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    const res = await managerRecallAnnouncement(id);
    ElMessage.success(res.data.msg || '撤回成功');
    getAnnouncementList();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤回公告失败：', error);
      ElMessage.error('撤回失败，请稍后重试');
    }
  }
};

// 查看详情
const viewDetail = async (row) => {
  try {
    // 物业端使用物业端接口
    const res = await managerGetAnnouncementDetail(row.id);
    console.log('[公告详情] 响应数据:', res);
    console.log('[公告详情] res.data:', res.data);
    // 修复：正确的数据路径是 res.data.data
    detailData.value = res.data && res.data.data ? res.data.data : res.data;
    detailDialogVisible.value = true;
  } catch (error) {
    console.error('查看详情失败：', error);
    ElMessage.error('查看详情失败，请稍后重试');
  }
};

// 打开员工制度弹窗
const openRegulationDialog = async () => {
  regulationLoading.value = true;
  try {
    const res = await getRegulationList({
      pageNum: 1,
      pageSize: 100,
      status: 'published', // 只显示已发布的制度
      type: undefined,
      keyword: undefined
    });

    if (res.data && res.data.code === 200 && res.data.data) {
      // 过滤适用范围为物业人员的制度
      const allRegulations = res.data.data.records || [];
      regulationList.value = allRegulations.filter(item => {
        return item.applyScope === '物业人员' || item.applyScope === '通用';
      });
    } else {
      regulationList.value = [];
      ElMessage.warning('未找到员工制度');
    }
    regulationDialogVisible.value = true;
  } catch (error) {
    console.error('加载员工制度失败:', error);
    const errorMsg = error.response?.data?.msg || '加载失败，请稍后重试';
    ElMessage.error(errorMsg);
  } finally {
    regulationLoading.value = false;
  }
};

// 查看制度详情
const viewRegulationDetail = (row) => {
  currentRegulation.value = { ...row };
  regulationDetailDialogVisible.value = true;
};

// 获取制度状态类型
const getRegulationStatusType = (statusCode) => {
  const statusMap = {
    draft: 'info',
    published: 'success',
    suspended: 'warning',
    invalid: 'danger'
  };
  return statusMap[statusCode] || 'info';
};

// 获取制度状态文本
const getRegulationStatusLabel = (statusCode) => {
  const statusMap = {
    draft: '草稿',
    published: '已发布',
    suspended: '已下架',
    invalid: '已作废'
  };
  return statusMap[statusCode] || statusCode;
};

// 获取状态类型
const getStatusType = (row) => {
  if (row.recallStatus === 1) {
    return 'warning'; // 已撤回 - 橙色
  }
  // 判断是否是定时发布（发布时间是未来）
  if (row.publishTime && dayjs(row.publishTime).isAfter(dayjs())) {
    return 'info'; // 定时发布 - 灰色
  }
  return 'success'; // 正常 - 绿色
};

// 获取状态文本
const getStatusText = (row) => {
  if (row.recallStatus === 1) {
    return '已撤回';
  }
  // 判断是否是定时发布（发布时间是未来）
  if (row.publishTime && dayjs(row.publishTime).isAfter(dayjs())) {
    return '等待发布';
  }
  return '正常';
};

// 批量操作（示例：批量删除/批量置顶）
const handleSelectionChange = (val) => {
  console.log('选中的行：', val);
  // 可扩展批量操作逻辑
};

// 页面挂载时加载列表
onMounted(() => {
  getAnnouncementList();
});
</script>

<style scoped>
.announcement-manager {
  padding: 20px;
}

.search-card,
.operate-card,
.list-card {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 0;
}

.search-row {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.search-row:last-child {
  margin-bottom: 0;
}

.search-row-second {
  margin-top: 8px;
}

.search-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}
</style>