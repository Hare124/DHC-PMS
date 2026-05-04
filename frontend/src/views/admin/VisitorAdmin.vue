<template>
  <div class="visitor-admin-page">
    <el-tabs v-model="activeTab" type="card" class="visitor-tabs">
      <!-- 邀请记录查询 Tab -->
      <el-tab-pane name="invite" label="邀请记录">
          <el-card shadow="hover" class="invite-list-card">
            <el-form :model="queryForm" label-width="65px" inline style="margin-bottom: 10px;">
              <el-form-item label="关键词">
                <el-input
                    v-model="queryForm.keyword"
                    placeholder="姓名/电话/房号"                  style="width: 200px"
                ></el-input>
              </el-form-item>
              <el-form-item label="状态" >
                <el-select v-model="queryForm.status" placeholder="选择状态" style="width: 120px">
                  <el-option label="待核销" value="0"></el-option>
                  <el-option label="已核销" value="1"></el-option>
                  <el-option label="已过期" value="2"></el-option>
                </el-select>
              </el-form-item>
            </el-form>

            <!-- 第二行：预约时间查询 -->
            <el-form :model="queryForm" label-width="80px" inline style="margin-bottom: 10px; margin-top: 10px;">
              <el-form-item label="预约时间">
                <el-date-picker
                    v-model="queryForm.dateRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    value-format="YYYY-MM-DD"                  style="width: 240px"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="queryInvites">查询</el-button>
                <el-button @click="resetQueryForm">重置</el-button>
                <el-button
                    type="warning"
                    @click="handleAutoExpireInvites"
                >
                  自动失效过期邀请
                </el-button>
              </el-form-item>
            </el-form>
          <el-table :data="inviteList" border stripe style="width: 100%">
            <el-table-column prop="visitorName" label="访客姓名" width="100"></el-table-column>
            <el-table-column prop="visitorPhone" label="访客电话" width="120"></el-table-column>
            <el-table-column prop="ownerName" label="邀请人姓名" width="100"></el-table-column>
            <el-table-column prop="ownerRoomNo" label="房号" width="100"></el-table-column>
            <el-table-column prop="visitTime" label="预约时间" width="180">
              <template #default="scope">
                {{ scope.row.visitTime?.replace('T', ' ') }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="scope">
                {{ scope.row.createTime?.replace('T', ' ') }}
              </template>
            </el-table-column>
            <el-table-column prop="expireTime" label="过期时间" width="180">
              <template #default="scope">
                {{ scope.row.expireTime?.replace('T', ' ') }}
              </template>
            </el-table-column>

            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag
                    :type="scope.row.status === 0 ? 'warning' : scope.row.status === 1 ? 'success' : 'danger'"
                >
                  {{ scope.row.status === 0 ? '待核销' : scope.row.status === 1 ? '已核销' : '已过期' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页组件 -->
          <el-pagination
              v-model:current-page="pagination.currentPage"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"              style="margin-top: 20px; justify-content: flex-end;"
          />
        </el-card>
      </el-tab-pane>

      <!-- 访客通行记录 Tab -->
      <el-tab-pane name="record" label="通行记录">
        <el-card shadow="hover" class="record-list-card">
          <el-form :inline="true" :model="recordQueryForm" class="record-query-form">
            <el-form-item label="关键字">
              <el-input
                  v-model="recordQueryForm.keyword"
                  placeholder="访客姓名/手机号"                  style="width: 200px"
              ></el-input>
            </el-form-item>
            <el-form-item label="入园时间">
              <el-date-picker
                  v-model="recordQueryForm.date"
                  type="date"
                  placeholder="选择日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"                  style="width: 200px"
              ></el-date-picker>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="getRecordList">查询</el-button>
              <el-button @click="resetRecordQueryForm">重置</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="recordList" border stripe style="width: 100%">
            <el-table-column prop="visitorName" label="访客姓名" width="100"></el-table-column>
            <el-table-column prop="visitorPhone" label="访客电话" width="120"></el-table-column>
            <el-table-column prop="visitTime" label="预约时间" width="180">
              <template #default="scope">
                {{ scope.row.visitTime?.replace('T', ' ') || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="checkinTime" label="入园时间" width="180">
              <template #default="scope">
                {{ scope.row.checkinTime?.replace('T', ' ') }}
              </template>
            </el-table-column>
            <el-table-column prop="checkoutTime" label="离园时间" width="180">
              <template #default="scope">
                {{ scope.row.checkoutTime?.replace('T', ' ') }}
              </template>
            </el-table-column>
            <el-table-column prop="checkerName" label="核销人" width="120"></el-table-column>
            <el-table-column prop="passStatus" label="通行状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.passStatus === '已入园' ? 'warning' : 'success'">
                  {{ scope.row.passStatus }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页组件 -->
          <el-pagination
              v-model:current-page="recordPagination.currentPage"
              v-model:page-size="recordPagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="recordPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleRecordSizeChange"
              @current-change="handleRecordCurrentChange"              style="margin-top: 20px; justify-content: flex-end;"
          />
        </el-card>
      </el-tab-pane>

      <!-- 邀请核销 Tab -->
      <el-tab-pane name="verify" label="邀请核销">
        <el-card shadow="hover" class="verify-card">
          <el-tabs v-model="verifySubTab" type="border-card">

            <!-- 手动核销 -->
            <el-tab-pane label="手动核销" name="manual">
              <el-form
                  ref="manualFormRef"
                  :model="manualForm"
                  :rules="manualRules"
                  label-width="100px"
              >
                <el-form-item label="访客手机号" prop="visitorPhone">
                  <el-input
                      v-model="manualForm.visitorPhone"
                      placeholder="请输入访客手机号"
                  ></el-input>
                </el-form-item>
                <el-form-item label="核销人 ID" prop="checkerId">
                  <el-input
                      v-model="manualForm.checkerId"
                      placeholder="请输入核销人 ID"
                      type="number"
                  ></el-input>
                </el-form-item>
                <el-form-item label="核销类型" prop="checkType">
                  <el-select v-model="manualForm.checkType" placeholder="选择核销类型">
                    <el-option label="手动" value="手动"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="备注">
                  <el-input
                      v-model="manualForm.remark"
                      placeholder="选填"
                      type="textarea"
                      rows="2"
                  ></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleVerifyInvite('manual')">确认核销</el-button>
                  <el-button @click="resetManualForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            <!-- 扫码核销 -->
            <el-tab-pane label="扫码核销" name="scan">
              <el-form
                  ref="scanFormRef"
                  :model="scanForm"
                  :rules="scanRules"
                  label-width="100px"
              >
                <el-form-item label="加密邀请 ID" prop="encryptInviteId">
                  <el-input
                      v-model="scanForm.encryptInviteId"
                      placeholder="请输入扫码获取的加密 ID"
                  ></el-input>
                </el-form-item>
                <el-form-item label="核销人 ID" prop="checkerId">
                  <el-input
                      v-model="scanForm.checkerId"
                      placeholder="请输入核销人 ID"
                      type="number"
                  ></el-input>
                </el-form-item>
                <el-form-item label="核销类型" prop="checkType">
                  <el-select v-model="scanForm.checkType" placeholder="选择核销类型">
                    <el-option label="自动" value="自动"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="备注">
                  <el-input
                      v-model="scanForm.remark"
                      placeholder="选填"
                      type="textarea"
                      rows="2"
                  ></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleVerifyInvite('scan')">确认核销</el-button>
                  <el-button @click="resetScanForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-tab-pane>

      <!-- 访客离园登记 Tab -->
      <el-tab-pane name="checkout" label="访客离园登记">
        <el-card shadow="hover" class="checkout-card">
          <el-form
              ref="checkoutFormRef"
              :model="checkoutForm"
              :rules="checkoutRules"
              label-width="100px"
          >
            <el-form-item label="邀请 ID/手机号" prop="key">
              <el-input
                  v-model="checkoutForm.key"
                  placeholder="输入邀请 ID 或访客手机号"
              ></el-input>
            </el-form-item>
            <el-form-item label="操作人 ID" prop="checkerId">
              <el-input
                  v-model="checkoutForm.checkerId"
                  placeholder="请输入操作人 ID"
                  type="number"
              ></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleCheckout">确认登记</el-button>
              <el-button @click="resetCheckoutForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import {
  testPost,
  testGet,
  adminQueryInvites,
  adminVerifyInvite,
  adminCheckout,
  adminGetRecordList,
  adminAutoExpireInvites,
  chatWithDeepSeek
} from '@/api/visitorApi.js';

// 主 Tab 状态
const activeTab = ref('verify'); // 默认显示邀请核销

// 核销子 Tab 状态
const verifySubTab = ref('scan'); // 默认显示扫码核销

// 扫码核销表单
const scanFormRef = ref(null);
const scanForm = reactive({
  encryptInviteId: '',
  checkerId: '',
  checkType: '自动',
  remark: ''
});
const scanRules = reactive({
  encryptInviteId: [{ required: true, message: '请输入加密邀请ID', trigger: 'blur' }],
  checkerId: [{ required: true, message: '请输入核销人ID', trigger: 'blur' }],
  checkType: [{ required: true, message: '请选择核销类型', trigger: 'change' }]
});

// 手动核销表单
const manualFormRef = ref(null);
const manualForm = reactive({
  visitorPhone: '',
  checkerId: '',
  checkType: '手动',
  remark: ''
});
const manualRules = reactive({
  visitorPhone: [{ required: true, message: '请输入访客手机号', trigger: 'blur' }],
  checkerId: [{ required: true, message: '请输入核销人ID', trigger: 'blur' }],
  checkType: [{ required: true, message: '请选择核销类型', trigger: 'change' }]
});

// 离园登记表单
const checkoutFormRef = ref(null);
const checkoutForm = reactive({
  key: '', // 邀请ID或手机号
  checkerId: ''
});
const checkoutRules = reactive({
  key: [{ required: true, message: '请输入邀请ID或访客手机号', trigger: 'blur' }],
  checkerId: [{ required: true, message: '请输入操作人ID', trigger: 'blur' }]
});

// 邀请记录查询表单
const queryForm = reactive({
  status: '',
  keyword: '',
  dateRange: [] // [startDate, endDate]
});
const inviteList = ref([]);

// 分页配置
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 通行记录查询
const recordDate = ref('');
const recordList = ref([]);

// 通行记录查询表单
const recordQueryForm = reactive({
  date: '',
  keyword: ''
});

// 通行记录分页配置
const recordPagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// DeepSeek 调试相关
const deepSeekDialogVisible = ref(false);
const deepSeekInput = ref('');
const deepSeekResponse = ref('');
const deepSeekLoading = ref(false);
const activeNames = ref(['request']);
const requestInfo = ref('');
const responseInfo = ref('');
const errorInfo = ref('');

// 页面加载时初始化
onMounted(() => {
  // 校验 userId 是否存在
  const userId = localStorage.getItem('userId')?.trim();
  if (!userId) {
    ElMessage.warning('未检测到用户ID，请重新登录');
    setTimeout(() => {
      window.location.href = '/login';
    }, 1500);
    return;
  }
  queryInvites();
  getRecordList();
});

// 核销邀请
const handleVerifyInvite = async (type) => {
  try {
    let formRef, formData;
    if (type === 'scan') {
      formRef = scanFormRef;
      formData = scanForm;
    } else {
      formRef = manualFormRef;
      formData = manualForm;
    }

    await formRef.value.validate();
    const res = await adminVerifyInvite(formData);
    const result = res.data;
    if (result.code === 200) {
      ElMessage.success(result.msg);
      // 重置表单 + 刷新列表
      type === 'scan' ? resetScanForm() : resetManualForm();
      queryInvites();
      getRecordList();
    } else {
      ElMessage.error(result.msg || '核销失败');
    }
  } catch (error) {
    console.error('核销邀请失败：', error);
    if (error.response?.status === 401) {
      ElMessage.error('登录状态失效，请重新登录');
    } else {
      ElMessage.error('核销邀请失败，请重试');
    }
  }
};

// 离园登记
const handleCheckout = async () => {
  try {
    await checkoutFormRef.value.validate();
    // 构建参数（区分邀请 ID/手机号）
    const data = {
      checkerId: checkoutForm.checkerId
    };

    // 添加调试信息
    console.log('离园登记输入:', checkoutForm.key);

    if (/^\d+$/.test(checkoutForm.key)) {
      // 数字，需要判断是邀请 ID 还是手机号
      if (checkoutForm.key.length <= 11) {
        // 11 位或更短，判定为手机号
        data.visitorPhone = checkoutForm.key;
        console.log('判定为手机号:', data.visitorPhone);
      } else {
        // 超过 11 位，判定为邀请 ID
        data.inviteId = Number(checkoutForm.key);
        console.log('判定为邀请 ID:', data.inviteId);
      }
    } else {
      // 非数字，可能是带空格的手机号或其他格式
      data.visitorPhone = checkoutForm.key.trim();
      console.log('判定为手机号 (非纯数字):', data.visitorPhone);
    }

    const res = await adminCheckout(data);
    const result = res.data;
    console.log('离园登记响应:', result); // 调试信息
    if (result.code === 200) {
      ElMessage.success(result.msg);
      resetCheckoutForm();
      getRecordList();
    } else {
      ElMessage.error(result.msg || '离园登记失败');
    }
  } catch (error) {
    console.error('离园登记失败：', error);
    console.error('错误详情:', error.response?.data); // 调试信息
    if (error.response?.status === 401) {
      ElMessage.error('登录状态失效，请重新登录');
    } else {
      ElMessage.error('离园登记失败，请重试');
    }
  }
};

// 查询邀请记录
const queryInvites = async () => {
  try {
    // 处理参数（当 status 为空时，不传 status 参数，查询全部状态）
    const params = {
      keyword: queryForm.keyword,
      page: pagination.value.currentPage,
      size: pagination.value.pageSize
    };

    // 仅当用户选择了状态时才传递 status
    if (queryForm.status !== '' && queryForm.status !== null && queryForm.status !== undefined) {
      params.status = queryForm.status;
    }

    // 处理时间范围
    if (queryForm.dateRange && queryForm.dateRange.length === 2) {
      params.startTime = queryForm.dateRange[0] + ' 00:00:00';
      params.endTime = queryForm.dateRange[1] + ' 23:59:59';
    }

    const res = await adminQueryInvites(params);
    const result = res.data;
    console.log('查询邀请记录响应:', result); // 调试信息
    if (result.code === 200) {
      // 后端返回分页数据
      const pageData = result.data;
      inviteList.value = Array.isArray(pageData.records) ? pageData.records : [];
      pagination.value.total = pageData.total || 0;
      console.log('邀请记录数量:', inviteList.value.length); // 调试信息
    } else {
      ElMessage.error(result.msg || '查询记录失败');
      inviteList.value = [];
      pagination.value.total = 0;
    }
  } catch (error) {
    console.error('查询邀请记录失败：', error);
    console.error('错误详情:', error.response?.data); // 调试信息
    if (error.response?.status === 401) {
      ElMessage.error('登录状态失效，请重新登录');
    } else {
      ElMessage.error('查询邀请记录失败，请重试');
    }
    inviteList.value = [];
    pagination.value.total = 0;
  }
};

// 处理每页显示数量变化
const handleSizeChange = (val) => {
  pagination.value.pageSize = val;
  pagination.value.currentPage = 1; // 重置到第一页
  queryInvites();
};

// 处理页码变化
const handleCurrentChange = (val) => {
  pagination.value.currentPage = val;
  queryInvites();
};

// 查询通行记录
const getRecordList = async () => {
  try {
    console.log('查询日期:', recordQueryForm.date); // 调试信息
    const res = await adminGetRecordList(recordQueryForm.date);
    const result = res.data;
    console.log('通行记录响应:', result); // 调试信息
    if (result.code === 200) {
      // 后端返回数组，进行前端分页和关键字过滤
      let allRecords = result.data || [];

      // 前端关键字过滤：支持访客姓名、访客手机号
      if (recordQueryForm.keyword && recordQueryForm.keyword.trim()) {
        const keyword = recordQueryForm.keyword.trim().toLowerCase();
        allRecords = allRecords.filter(record => {
          const nameMatch = record.visitorName && record.visitorName.toLowerCase().includes(keyword);
          const phoneMatch = record.visitorPhone && record.visitorPhone.includes(keyword);
          return nameMatch || phoneMatch;
        });
      }

      recordPagination.value.total = allRecords.length;

      // 计算当前页的数据
      const startIndex = (recordPagination.value.currentPage - 1) * recordPagination.value.pageSize;
      const endIndex = startIndex + recordPagination.value.pageSize;
      recordList.value = allRecords.slice(startIndex, endIndex);

      console.log('通行记录数量:', allRecords.length); // 调试信息
    } else {
      ElMessage.error(result.msg || '查询通行记录失败');
      recordList.value = [];
      recordPagination.value.total = 0;
    }
  } catch (error) {
    console.error('查询通行记录失败：', error);
    console.error('错误详情:', error.response?.data); // 调试信息
    if (error.response?.status === 401) {
      ElMessage.error('登录状态失效，请重新登录');
    } else {
      ElMessage.error('查询通行记录失败，请重试');
    }
    recordList.value = [];
    recordPagination.value.total = 0;
  }
};

// 重置通行记录查询表单
const resetRecordQueryForm = () => {
  recordQueryForm.date = '';
  recordQueryForm.keyword = '';
  recordPagination.value.currentPage = 1;
  getRecordList();
};

// 处理通行记录每页显示数量变化
const handleRecordSizeChange = (val) => {
  recordPagination.value.pageSize = val;
  recordPagination.value.currentPage = 1; // 重置到第一页
  getRecordList();
};

// 处理通行记录页码变化
const handleRecordCurrentChange = (val) => {
  recordPagination.value.currentPage = val;
  getRecordList();
};

// 自动失效过期邀请
const handleAutoExpireInvites = async () => {
  try {
    const res = await adminAutoExpireInvites();
    const result = res.data;
    if (result.code === 200) {
      ElMessage.success(`自动失效完成，共处理 ${result.data} 条记录`);
      queryInvites();
    } else {
      ElMessage.error(result.msg || '自动失效失败');
    }
  } catch (error) {
    console.error('自动失效失败：', error);
    if (error.response?.status === 401) {
      ElMessage.error('登录状态失效，请重新登录');
    } else {
      ElMessage.error('自动失效失败，请重试');
    }
  }
};

// 重置表单
const resetScanForm = () => scanFormRef.value.resetFields();
const resetManualForm = () => manualFormRef.value.resetFields();
const resetCheckoutForm = () => checkoutFormRef.value.resetFields();
// 重置查询表单
const resetQueryForm = () => {
  queryForm.status = '';
  queryForm.keyword = '';
  queryForm.dateRange = [];
  pagination.value.currentPage = 1;
  queryInvites();
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

// 打开 DeepSeek 调试对话框
const handleTestDeepSeek = () => {
  deepSeekDialogVisible.value = true;
  deepSeekInput.value = '';
  deepSeekResponse.value = '';
  requestInfo.value = '';
  responseInfo.value = '';
  errorInfo.value = '';
  activeNames.value = ['request'];
};

// 发送 DeepSeek 请求
const sendDeepSeekRequest = async () => {
  if (!deepSeekInput.value.trim()) {
    ElMessage.warning('请输入要询问的消息');
    return;
  }

  deepSeekLoading.value = true;
  deepSeekResponse.value = '正在思考中...';
  requestInfo.value = '';
  responseInfo.value = '';
  errorInfo.value = '';

  const startTime = Date.now();
  const requestData = {
    message: deepSeekInput.value
  };

  // 记录请求信息
  requestInfo.value = JSON.stringify({
    '请求 URL': '/api/chat',
    '请求方法': 'POST',
    '请求头': {
      'Content-Type': 'application/json',
      'Authorization': localStorage.getItem('token') ? 'Bearer ***' : '未携带 Token',
      'user_id': localStorage.getItem('userId') || '未设置'
    },
    '请求参数': requestData,
    '超时时间': '30000ms'
  }, null, 2);

  try {
    console.log('\n========== DeepSeek 请求开始 ==========');
    console.log('📤 请求时间:', new Date().toLocaleString());
    console.log('📤 请求 URL:', '/api/chat');
    console.log('📤 请求方法:', 'POST');
    console.log('📤 请求数据:', requestData);
    console.log('📤 使用的库:', 'axios (request 封装)');
    console.log('========================================\n');

    // 使用封装好的 visitorApi 调用 DeepSeek 接口
    const response = await chatWithDeepSeek(requestData);

    const endTime = Date.now();
    const duration = endTime - startTime;

    console.log('\n========== DeepSeek 响应开始 ==========');
    console.log('📥 响应时间:', new Date().toLocaleString());
    console.log('⏱️  请求耗时:', duration + 'ms');
    console.log('📥 响应状态:', response.status);
    console.log('📥 响应数据:', response.data);
    console.log('========================================\n');

    // 显示响应结果
    const responseData = response.data;
    deepSeekResponse.value = typeof responseData === 'string'
        ? responseData
        : JSON.stringify(responseData, null, 2);

    // 记录响应信息
    responseInfo.value = JSON.stringify({
      '响应状态码': response.status || 200,
      '请求耗时': duration + 'ms',
      '响应时间': new Date().toLocaleString(),
      '数据长度': typeof responseData === 'string' ? responseData.length + ' 字符' : '对象',
      '响应内容预览': typeof responseData === 'string'
          ? responseData.substring(0, 200) + (responseData.length > 200 ? '...' : '')
          : JSON.stringify(responseData).substring(0, 200) + '...'
    }, null, 2);

    ElMessage.success(`✅ AI 响应成功！耗时：${duration}ms`);

    // 自动展开响应详情
    if (!activeNames.value.includes('response')) {
      activeNames.value.push('response');
    }

  } catch (error) {
    const endTime = Date.now();
    const duration = endTime - startTime;

    console.error('\n========== DeepSeek 错误开始 ==========');
    console.error('❌ 错误时间:', new Date().toLocaleString());
    console.error('⏱️  请求耗时:', duration + 'ms');
    console.error('❌ 错误类型:', error.constructor.name);

    if (error.response) {
      console.error('❌ 错误状态码:', error.response.status);
      console.error('❌ 错误响应:', error.response.data);
      console.error('❌ 错误头信息:', error.response.headers);
    } else if (error.request) {
      console.error('❌ 请求已发送但未收到响应');
      console.error('❌ 请求对象:', error.request);
    } else {
      console.error('❌ 错误消息:', error.message);
    }
    console.error('======================================\n');

    // 构建错误信息
    let errorMessage = `请求失败！\n\n`;
    errorMessage += `❌ 错误类型：${error.constructor.name}\n`;
    errorMessage += `⏱️  请求耗时：${duration}ms\n\n`;

    if (error.response) {
      errorMessage += `📛 状态码：${error.response.status}\n`;
      errorMessage += `📛 响应数据：${JSON.stringify(error.response.data, null, 2)}\n`;
    } else if (error.request) {
      errorMessage += `📛 未收到响应，可能原因：\n`;
      errorMessage += `   - 后端服务未启动\n`;
      errorMessage += `   - 网络连接问题\n`;
      errorMessage += `   - 跨域配置问题\n`;
      errorMessage += `   - 请求超时（>30 秒）\n`;
    } else {
      errorMessage += `📛 错误消息：${error.message}\n`;
    }

    deepSeekResponse.value = errorMessage;

    // 记录详细错误信息
    errorInfo.value = JSON.stringify({
      '错误类型': error.constructor.name,
      '错误时间': new Date().toLocaleString(),
      '请求耗时': duration + 'ms',
      '状态码': error.response?.status || '无响应',
      '错误消息': error.message || '未知错误',
      '响应数据': error.response?.data ? JSON.stringify(error.response.data) : '无',
      '排查建议': !error.response
          ? '1. 检查后端服务是否启动\n2. 检查网络是否正常\n3. 检查跨域配置\n4. 查看后端控制台日志'
          : '查看后端应用日志，确认 DeepSeek API 调用是否成功'
    }, null, 2);

    // 自动展开错误日志
    if (!activeNames.value.includes('error')) {
      activeNames.value.push('error');
    }

    ElMessage.error(`❌ AI 请求失败：${error.message}`);
  } finally {
    deepSeekLoading.value = false;
  }
};
</script>

<style scoped>
.visitor-admin-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.visitor-tabs {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
}

.verify-card,
.checkout-card,
.invite-list-card,
.record-list-card {
  border: none;
  box-shadow: none;
}

.verify-card :deep(.el-tabs__header),
.checkout-card :deep(.el-tabs__header),
.invite-list-card :deep(.el-tabs__header),
.record-list-card :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}
.record-query-form {
  margin-bottom: 10px;
  padding: 10px 0;
  border-radius: 4px;
}
</style>