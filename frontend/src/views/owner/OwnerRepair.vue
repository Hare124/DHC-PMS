<!-- src/views/owner/OwnerRepair.vue -->
<template>
  <div class="owner-repair-container">
    <!-- 权限校验：页面内自行判断角色，无权限则提示 -->
    <div v-if="!hasPermission" class="no-permission">
      <el-alert
          title="无访问权限"
          type="error"
          description="您当前的角色无权限访问该页面，请联系管理员"
          show-icon
      />
      <el-button type="primary" @click="goBack">返回上一页</el-button>
    </div>

    <div v-else>

      <!-- 提交报修按钮 -->
      <el-button type="primary" @click="openSubmitDialog" style="margin: 10px 0;">提交新报修</el-button>

      <!-- 报修列表 -->
      <el-table
          :data="Array.isArray(repairList) ? repairList : []"
          border
          stripe
          style="width: 100%;"
          v-loading="loading"
      >
        <el-table-column prop="repairOrderNo" label="报修单号" width="120" />
        <el-table-column prop="repairTypeName" label="报修类型" width="120" />
        <el-table-column prop="title" label="报修标题" width="200" />
        <el-table-column prop="statusDesc" label="状态" width="120" />
        <el-table-column label="创建时间" width="200">
          <template #default="scope">
            {{ scope.row.createTime ? scope.row.createTime.replace('T', ' ') : '无' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button
                size="small"
                @click="handleViewDetail(scope.row.id)"
            >
              详情
            </el-button>
            <el-button
                v-if="scope.row.status === 0"
                type="danger"
                size="small"
                @click="handleCancelApply(scope.row)"
            >
              取消申请
            </el-button>

            <el-button
                v-if="scope.row.status === 4"
                type="warning"
                size="small"
                @click="openResubmitDialog(scope.row)"
            >
              重新提交
            </el-button>

            <el-button
                v-if="scope.row.status === 5"
                type="success"
                size="small"
                @click="openAcceptDialog(scope.row)"
            >
              验收
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 提交报修弹窗 -->
      <el-dialog v-model="submitDialogVisible" title="提交报修申请" width="500px">
        <el-form :model="submitForm" label-width="80px" :rules="submitRules">
          <el-form-item label="报修类型" prop="repairTypeId">
            <el-select v-model="submitForm.repairTypeId" placeholder="请选择报修类型">
              <el-option
                  v-for="item in repairTypeList"
                  :key="item.id"
                  :label="item.typeName"
                  :value="item.id.toString()"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="报修标题" prop="title">
            <el-input v-model="submitForm.title" />
          </el-form-item>
          <el-form-item label="报修内容" prop="content">
            <el-input v-model="submitForm.content" type="textarea" rows="4" />
          </el-form-item>
          <el-form-item label="预约时间">
            <el-date-picker
                v-model="submitForm.expectedTime"
                type="datetime"
                placeholder="请选择预计上门时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm"
                :disabled-date="disabledDate"
                :disabled-time="disabledTime"            style="width: 100%;"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="submitDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">提交申请</el-button>
        </template>
      </el-dialog>

      <!-- 验收弹窗 -->
      <el-dialog v-model="acceptDialogVisible" title="验收报修单" width="400px">
        <el-form :model="acceptForm" label-width="80px">
          <el-form-item label="验收结果">
            <el-radio-group v-model="acceptForm.acceptResult">
              <el-radio :label="true">通过</el-radio>
              <el-radio :label="false">不通过</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
              label="评分"
              v-if="acceptForm.acceptResult"
              :rules="[{ required: true, message: '请输入评分（1-5）', type: 'number', min: 1, max: 5 }]"
          >
            <el-rate v-model="acceptForm.score" :max="5" :disabled="false" allow-half />
          </el-form-item>
          <el-form-item
              v-if="acceptForm.acceptResult"
              label="评价"
              :rules="[{ required: true, message: '请输入评价内容' }]"
          >
            <el-input v-model="acceptForm.comment" type="textarea" rows="3" placeholder="请输入对维修服务的评价" />
          </el-form-item>
          <el-form-item
              v-else
              label="原因"
              :rules="[{ required: true, message: '请输入未通过原因' }]"
          >
            <el-input v-model="acceptForm.comment" type="textarea" rows="3" placeholder="请输入验收未通过的原因" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="acceptDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAccept">确认验收</el-button>
        </template>
      </el-dialog>

      <!-- 重新提交弹窗 -->
      <el-dialog v-model="resubmitDialogVisible" title="重新提交报修" width="500px">
        <el-form :model="resubmitForm" label-width="80px" :rules="resubmitRules">
          <el-form-item label="报修标题" prop="title">
            <el-input v-model="resubmitForm.title" />
          </el-form-item>
          <el-form-item label="报修内容" prop="content">
            <el-input v-model="resubmitForm.content" type="textarea" rows="4" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="resubmitDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleResubmit">重新提交</el-button>
        </template>
      </el-dialog>

      <!-- 查看详情弹窗 -->
      <el-dialog
          v-model="detailDialogVisible"
          title="报修详情"
          width="600px"
          :close-on-click-modal="false"
      >
        <el-descriptions :column="1" border v-if="currentDetail">
          <el-descriptions-item label="报修单号">{{ currentDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="报修标题">{{ currentDetail.title || '无标题' }}</el-descriptions-item>
          <el-descriptions-item label="报修类型">{{ currentDetail.repairTypeName || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentDetail.status)" size="small">
              {{ getStatusDesc(currentDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="报修内容" :span="1">
            <div style="white-space: pre-wrap; word-break: break-all; max-width: 400px;">{{ currentDetail.content || '无' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="报修地址">{{ currentDetail.address || '无' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentDetail.createTime ? currentDetail.createTime.replace('T', ' ') : '无' }}</el-descriptions-item>
          <el-descriptions-item label="维修人员">{{ currentDetail.handlerName || '待派单' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ currentDetail.completeTime ? currentDetail.completeTime.replace('T', ' ') : '未完成' }}</el-descriptions-item>

          <!-- 评价信息（仅已完成或已验收状态显示） -->
          <template v-if="currentDetail.status === 3 || currentDetail.status === 6">
            <el-descriptions-item label="评分">
              <el-rate v-model="currentDetail.score" disabled show-score text-color="#67C23A" />
            </el-descriptions-item>
            <el-descriptions-item label="评价内容">{{ currentDetail.comment || '无' }}</el-descriptions-item>
            <el-descriptions-item label="评价时间">{{ currentDetail.evaluateTime ? currentDetail.evaluateTime.replace('T', ' ') : '无' }}</el-descriptions-item>
          </template>
        </el-descriptions>

        <template #footer>
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  testPost, testGet, submitRepairApply, getOwnerRepairList,
  getRepairDetail, acceptRepair, resubmitRepairApply, getRepairTypeList,
  cancelRepairApply
} from '@/api/repairApi.js';

// ========== 1. 页面内处理角色匹配 ==========
const userRoleNum = localStorage.getItem('role'); // 数字：2=业主，3=管理员
const hasPermission = computed(() => {
  // 页面要求角色是owner，对应数字2
  return userRoleNum === '2';
});

// 返回上一页
const goBack = () => {
  window.history.go(-1);
};

// ========== 2. 基础状态管理 ==========
// 报修列表
const repairList = ref([]);
// 加载状态
const loading = ref(false);

// ========== 3. 测试按钮事件 ==========
const handleTestPost = () => {
  loading.value = true;
  testPost({
    name: '张三',
    age: 18
  }).then(res => {
    ElMessage.success('POST 请求成功');
    console.log('POST 请求成功：', res);
  }).catch(err => {
    // 统一错误提示，兼容401/500等状态
    ElMessage.error(err?.msg || err?.response?.data?.msg || 'POST 请求失败');
    console.error('POST 请求失败：', err);
  }).finally(() => {
    loading.value = false;
  });
};

const handleTestGet = () => {
  loading.value = true;
  testGet().then(res => {
    ElMessage.success('GET 请求成功');
    console.log('GET 请求成功：', res);
  }).catch(err => {
    ElMessage.error(err?.msg || err?.response?.data?.msg || 'GET 请求失败');
    console.error('GET 请求失败：', err);
  }).finally(() => {
    loading.value = false;
  });
};

// ========== 4. 加载报修列表 ==========
onMounted(() => {
  if (hasPermission.value) {
    loadRepairList();
  }
});


const loadRepairList = () => {
  loading.value = true;
  getOwnerRepairList()
      .then(res => {
        // 后端返回的数据在 res.data 中
        const response = res.data;
        if (response?.code === 200) {
          repairList.value = Array.isArray(response.data) ? response.data : [];
          console.log('✅ 报修列表加载成功:', repairList.value.length, '条记录');
        } else {
          ElMessage.warning(response?.msg || '获取报修列表为空');
          repairList.value = [];
        }
      })
      .catch(err => {
        ElMessage.error(err?.msg || err?.response?.data?.msg || '获取报修列表失败');
        repairList.value = [];
      })
      .finally(() => {
        loading.value = false;
      });
};

// ========== 5. 提交报修申请 ==========
const submitDialogVisible = ref(false);
const submitForm = ref({
  repairTypeId: '',
  title: '',
  content: '',
  expectedTime: ''
});
const submitRules = ref({
  repairTypeId: [{ required: true, message: '请选择报修类型' }],
  title: [{ required: true, message: '请输入报修标题' }],
  content: [{ required: true, message: '请输入报修内容' }]
});

const openSubmitDialog = () => {
  submitForm.value = {
    repairTypeId: '',
    title: '',
    content: '',
    expectedTime: ''
  };
  submitDialogVisible.value = true;
};

const handleSubmit = () => {
  loading.value = true;

  // 打印提交的完整数据
  console.log('📤 提交报修数据:', submitForm.value);

  submitRepairApply(submitForm.value)
      .then(res => {
        // 后端返回的数据在 res.data 中
        const response = res.data;
        console.log('✅ 后端响应 - response:', response);

        if (response?.code === 200) {
          // 显示报修单号
          const repairOrderNo = response.data?.repairOrderNo || '未知';
          ElMessage.success(`${response.msg}（报修单号：${repairOrderNo}）`);
          submitDialogVisible.value = false;
          loadRepairList(); // 刷新列表
        } else {
          console.error('❌ 提交失败 - 后端返回错误:', response?.msg);
          ElMessage.warning(response?.msg || '提交失败');
        }
      })
      .catch(err => {
        console.error('❌ 提交失败 - 请求异常:', err);
        console.error('完整错误信息:', err.response);
        ElMessage.error(err?.msg || err?.response?.data?.msg || '提交失败');
      })
      .finally(() => {
        loading.value = false;
      });
};

// ========== 6. 验收报修单 ==========
const acceptDialogVisible = ref(false);
const acceptForm = ref({
  repairOrderId: '',
  acceptResult: true,
  score: 5,
  comment: ''
});

const openAcceptDialog = (row) => {
  acceptForm.value = {
    repairOrderId: row.id,
    acceptResult: true,
    score: 5,
    comment: ''
  };
  acceptDialogVisible.value = true;
};

const handleAccept = () => {
  loading.value = true;
  acceptRepair(acceptForm.value)
      .then(res => {
        // 后端返回的数据在 res.data 中
        const response = res.data;
        console.log('✅ 验收响应:', response);

        if (response?.code === 200) {
          ElMessage.success(response.msg || '验收成功');
          acceptDialogVisible.value = false;
          loadRepairList();
        } else {
          ElMessage.warning(response?.msg || '验收失败');
        }
      })
      .catch(err => {
        ElMessage.error(err?.msg || err?.response?.data?.msg || '验收失败');
      })
      .finally(() => {
        loading.value = false;
      });
};

// ========== 7. 重新提交报修 ==========
const resubmitDialogVisible = ref(false);
const resubmitForm = ref({
  repairOrderId: '',
  title: '',
  content: ''
});
const resubmitRules = ref({
  title: [{ required: true, message: '请输入报修标题' }],
  content: [{ required: true, message: '请输入报修内容' }]
});

const openResubmitDialog = (row) => {
  resubmitForm.value = {
    repairOrderId: row.id,
    title: row.title,
    content: row.content
  };
  resubmitDialogVisible.value = true;
};

const handleResubmit = () => {
  loading.value = true;
  resubmitRepairApply(resubmitForm.value)
      .then(res => {
        // 后端返回的数据在 res.data 中
        const response = res.data;
        console.log('✅ 重新提交响应:', response);

        if (response?.code === 200) {
          ElMessage.success(response.msg || '重新提交成功');
          resubmitDialogVisible.value = false;
          loadRepairList();
        } else {
          ElMessage.warning(response?.msg || '重新提交失败');
        }
      })
      .catch(err => {
        ElMessage.error(err?.msg || err?.response?.data?.msg || '重新提交失败');
      })
      .finally(() => {
        loading.value = false;
      });
};

// ========== 8. 查看报修详情 ==========
const detailDialogVisible = ref(false);
const currentDetail = ref(null);

const handleViewDetail = (repairOrderId) => {
  loading.value = true;
  getRepairDetail(repairOrderId)
      .then(res => {
        // 后端返回的数据在 res.data 中
        const response = res.data;
        console.log('✅ 报修详情响应:', response);

        if (response?.code === 200 && response.data) {
          currentDetail.value = response.data;
          detailDialogVisible.value = true;
        } else {
          ElMessage.warning(response?.msg || '暂无报修详情');
        }
      })
      .catch(err => {
        ElMessage.error(err?.msg || err?.response?.data?.msg || '获取详情失败');
      })
      .finally(() => {
        loading.value = false;
      });
};

// 获取状态描述
const getStatusDesc = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已派单',
    2: '维修中',
    3: '已完成',
    4: '已取消（驳回）',
    5: '待验收',
    6: '已验收'
  };
  return statusMap[status] || '未知';
};

// 获取状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',      // 待审核 - 灰色
    1: 'primary',   // 已派单 - 蓝色
    2: 'warning',   // 维修中 - 橙色
    3: 'success',   // 已完成 - 绿色
    4: 'info',      // 已取消 - 灰色
    5: 'danger',    // 待验收 - 红色
    6: 'success'    // 已验收 - 绿色
  };
  return typeMap[status] || 'info';
};


// ==========  加载报修类型列表 ==========
const repairTypeList = ref([]);

// 页面加载时获取报修类型列表
onMounted(() => {
  loadRepairTypeList();
  if (hasPermission.value) {
    loadRepairList();
  }
});

const loadRepairTypeList = () => {
  getRepairTypeList()
      .then(res => {
        const response = res.data;
        if (response?.code === 200) {
          repairTypeList.value = response.data || [];
          console.log('✅ 报修类型加载成功:', repairTypeList.value.length, '个类型');
        } else {
          ElMessage.warning(response?.msg || '加载报修类型失败');
        }
      })
      .catch(err => {
        ElMessage.error(err?.msg || err?.response?.data?.msg || '加载报修类型失败');
      });
};

// ========== 9. 取消报修申请 ==========
const handleCancelApply = (row) => {
  ElMessageBox.prompt(
      `请输入取消报修申请 "${row.title}" 的原因：`,
      '提示',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '再想想',
        inputPattern: /.+/,
        inputErrorMessage: '请输入取消原因',
        type: 'warning'
      }
  ).then(({ value }) => {
    loading.value = true;

    cancelRepairApply({
      repairOrderId: row.id,
      cancelReason: value
    })
        .then(res => {
          const response = res.data;
          if (response?.code === 200) {
            ElMessage.success(response.msg || '取消申请成功');
            loadRepairList();
          } else {
            ElMessage.warning(response?.msg || '取消申请失败');
          }
        })
        .catch(err => {
          ElMessage.error(err?.msg || err?.response?.data?.msg || '取消申请失败');
        })
        .finally(() => {
          loading.value = false;
        });
  }).catch(() => {
    // 用户取消操作
  });
};
</script>

<style scoped>
.owner-repair-container {
  padding: 20px;
}

.test-btn-group {
  margin-bottom: 20px;
}

.test-btn-group button {
  margin-right: 10px;
  padding: 8px 16px;
  cursor: pointer;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
}

.test-btn-group button:hover {
  background: #f5f5f5;
}

.no-permission {
  padding: 40px;
  text-align: center;
}

.no-permission .el-alert {
  max-width: 600px;
  margin: 0 auto 20px;
}
</style>