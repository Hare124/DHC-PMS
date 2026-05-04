<!-- src/views/owner/OwnerRepair02.vue -->
<template>
  <div class="owner-repair">
    <!-- 权限校验 -->
    <div v-if="!hasPermission" class="no-permission">
      <el-alert
          title="无访问权限"
          type="error"
          description="您当前的角色无权限访问该页面，请联系管理员"
          show-icon
      />
      <el-button type="primary" @click="goBack" style="margin-top: 20px;">返回上一页</el-button>
    </div>

    <div v-else>
      <!-- 提交报修按钮 -->
      <div class="repair-header">
        <el-button type="primary" @click="openSubmitDialog" >
          <el-icon><Plus /></el-icon>
          提交新报修
        </el-button>
      </div>

      <!-- 报修列表 -->
      <el-card shadow="never" class="list-card">
        <div class="repair-list" v-loading="loading">
          <div
              class="repair-item"
              v-for="repair in repairList"
              :key="repair.id"
              @click="handleViewDetail(repair.id)"
          >
            <div class="repair-header-content">
              <div class="repair-title">
                <el-tag
                    :type="getStatusType(repair.status)"
                    size="small"
                    style="margin-right: 8px;"
                >
                  {{ getStatusDesc(repair.status) }}
                </el-tag>
                <span class="repair-title-text">{{ repair.title }}</span>
              </div>
            </div>

            <div class="repair-content">
              <div class="repair-info">
                <span><el-icon><Ticket /></el-icon> 单号：{{ repair.repairOrderNo }}</span>
                <span><el-icon><Document /></el-icon> 类型：{{ repair.repairTypeName }}</span>
                <span><el-icon><Clock /></el-icon> 创建时间：{{ formatTime(repair.createTime) }}</span>
                <span v-if="repair.handlerName"><el-icon><User /></el-icon> 维修人员：{{ repair.handlerName }}</span>
                <span v-if="repair.completeTime"><el-icon><Check /></el-icon> 完成时间：{{ formatTime(repair.completeTime) }}</span>
              </div>

              <div class="repair-actions">
                <el-button
                    type="primary"
                    size="small"
                    @click.stop="handleViewDetail(repair.id)"
                >
                  详情
                </el-button>
                <el-button
                    v-if="repair.status === 0"
                    type="danger"
                    size="small"
                    @click.stop="handleCancelApply(repair)"
                >
                  取消申请
                </el-button>
                <el-button
                    v-if="repair.status === 4"
                    type="warning"
                    size="small"
                    @click.stop="openResubmitDialog(repair)"
                >
                  重新提交
                </el-button>
                <el-button
                    v-if="repair.status === 5"
                    type="success"
                    size="small"
                    @click.stop="openAcceptDialog(repair)"
                >
                  验收
                </el-button>
              </div>

              <div class="repair-desc">
                {{ truncateContent(repair.content) }}
              </div>
            </div>
          </div>
          <el-empty v-if="repairList.length === 0" description="暂无报修记录" />
        </div>
      </el-card>

      <!-- 提交报修弹窗 -->
      <el-dialog
        v-model="submitDialogVisible"
        title="提交报修申请"
        width="500px"
        @close="resetSubmitForm"
      >
        <el-form :model="submitForm" label-width="100px" :rules="submitRules" ref="submitFormRef">
          <el-form-item label="报修类型" prop="repairTypeId">
            <el-select v-model="submitForm.repairTypeId" placeholder="请选择报修类型" style="width: 100%;">
              <el-option
                  v-for="item in repairTypeList"
                  :key="item.id"
                  :label="item.typeName"
                  :value="item.id.toString()"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="报修标题" prop="title">
            <el-input v-model="submitForm.title" placeholder="请输入报修标题" />
          </el-form-item>
          <el-form-item label="报修内容" prop="content">
            <el-input
              v-model="submitForm.content"
              type="textarea"
              :rows="4"
              placeholder="请详细描述报修问题"
            />
          </el-form-item>
          <el-form-item label="预约时间">
            <el-date-picker
                v-model="submitForm.expectedTime"
                type="datetime"
                placeholder="请选择预计上门时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm"
                :disabled-date="disabledDate"
                :disabled-time="disabledTime"
                style="width: 100%;"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="submitDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">提交申请</el-button>
        </template>
      </el-dialog>

      <!-- 验收弹窗 -->
      <el-dialog
        v-model="acceptDialogVisible"
        title="验收报修单"
        width="450px"
        @close="resetAcceptForm"
      >
        <el-form :model="acceptForm" label-width="100px">
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
            <el-rate v-model="acceptForm.score" :max="5" :disabled="false" allow-half show-text />
          </el-form-item>
          <el-form-item
              v-if="acceptForm.acceptResult"
              label="评价"
              :rules="[{ required: true, message: '请输入评价内容' }]"
          >
            <el-input
              v-model="acceptForm.comment"
              type="textarea"
              :rows="3"
              placeholder="请输入对维修服务的评价"
            />
          </el-form-item>
          <el-form-item
              v-else
              label="原因"
              :rules="[{ required: true, message: '请输入未通过原因' }]"
          >
            <el-input
              v-model="acceptForm.comment"
              type="textarea"
              :rows="3"
              placeholder="请输入验收未通过的原因"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="acceptDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAccept">确认验收</el-button>
        </template>
      </el-dialog>

      <!-- 重新提交弹窗 -->
      <el-dialog
        v-model="resubmitDialogVisible"
        title="重新提交报修"
        width="500px"
        @close="resetResubmitForm"
      >
        <el-form :model="resubmitForm" label-width="100px" :rules="resubmitRules" ref="resubmitFormRef">
          <el-form-item label="报修标题" prop="title">
            <el-input v-model="resubmitForm.title" placeholder="请输入报修标题" />
          </el-form-item>
          <el-form-item label="报修内容" prop="content">
            <el-input
              v-model="resubmitForm.content"
              type="textarea"
              :rows="4"
              placeholder="请修改报修内容后重新提交"
            />
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
          width="750px"
          :close-on-click-modal="false"
      >
        <div v-if="currentDetail">
          <!-- 基本信息 -->
          <el-descriptions :column="2" border>
            <el-descriptions-item label="报修单号">{{ currentDetail.repairOrderNo }}</el-descriptions-item>
            <el-descriptions-item label="报修类型">{{ currentDetail.repairTypeName || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="报修标题" :span="2">{{ currentDetail.title || '无标题' }}</el-descriptions-item>
            <el-descriptions-item label="状态" :span="2">
              <el-tag :type="getStatusType(currentDetail.status)" size="small">
                {{ getStatusDesc(currentDetail.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="报修内容" :span="2">
              <div style="white-space: pre-wrap; word-break: break-all;">{{ currentDetail.content || '无' }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="报修地址">{{ currentDetail.address || '无' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatTime(currentDetail.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="维修人员">{{ currentDetail.handlerName || '待派单' }}</el-descriptions-item>
            <el-descriptions-item label="完成时间">{{ formatTime(currentDetail.completeTime) || '未完成' }}</el-descriptions-item>

            <!-- 评价信息 -->
            <template v-if="currentDetail.status === 3 || currentDetail.status === 6">
              <el-descriptions-item label="评分" :span="2">
                <el-rate v-model="currentDetail.score" disabled show-score text-color="#67C23A" />
              </el-descriptions-item>
              <el-descriptions-item label="评价内容" :span="2">{{ currentDetail.comment || '无' }}</el-descriptions-item>
              <el-descriptions-item label="评价时间" :span="2">{{ formatTime(currentDetail.evaluateTime) }}</el-descriptions-item>
            </template>
          </el-descriptions>

          <!-- 进度预测模块 -->
          <div class="progress-section" v-if="showProgress && progressData">
            <el-divider content-position="left">
              <el-icon><TrendCharts /></el-icon>
              维修进度预测
            </el-divider>

            <!-- 进度条 -->
            <div class="progress-bar-wrapper">
              <el-progress
                  :percentage="progressData.progressPercent"
                  :status="getProgressStatus()"
                  :stroke-width="18"
              >
                <template #default="{ percentage }">
                  <span class="progress-text">{{ percentage }}%</span>
                </template>
              </el-progress>
            </div>

            <!-- 进度信息卡片 -->
            <el-row :gutter="15" class="progress-info">
              <el-col :span="8">
                <el-card shadow="hover" class="info-card">
                  <div class="info-item">
                    <div class="info-label">当前阶段</div>
                    <div class="info-value">{{ progressData.currentStage }}</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card shadow="hover" class="info-card">
                  <div class="info-item">
                    <div class="info-label">下一步</div>
                    <div class="info-value">{{ progressData.nextStep }}</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card shadow="hover" class="info-card">
                  <div class="info-item">
                    <div class="info-label">预计完成</div>
                    <div class="info-value">
                      {{ formatTime(progressData.estimatedCompleteTime) }}
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <!-- 时间轴 -->
            <el-timeline class="time-line">
              <el-timeline-item
                  timestamp="已耗时"
                  placement="top"
                  :type="progressData.isOverdueRisk ? 'danger' : 'primary'"
              >
                <el-card>
                  <h4>{{ Math.floor(progressData.elapsedMinutes / 60) }}小时{{ progressData.elapsedMinutes % 60 }}分钟</h4>
                </el-card>
              </el-timeline-item>
              <el-timeline-item
                  timestamp="预计剩余"
                  placement="top"
                  :type="progressData.isOverdueRisk ? 'warning' : 'success'"
              >
                <el-card>
                  <h4>{{ Math.floor(progressData.remainingMinutes / 60) }}小时{{ progressData.remainingMinutes % 60 }}分钟</h4>
                </el-card>
              </el-timeline-item>
              <el-timeline-item
                  timestamp="总预计时长"
                  placement="top"
                  type="info"
              >
                <el-card>
                  <h4>{{ Math.floor(progressData.estimatedTotalMinutes / 60) }}小时{{ progressData.estimatedTotalMinutes % 60 }}分钟</h4>
                </el-card>
              </el-timeline-item>
            </el-timeline>

            <!-- 风险预警 -->
            <el-alert
                v-if="progressData.isOverdueRisk"
                :title="progressData.suggestion"
                type="warning"
                show-icon
                :closable="false"
                class="risk-alert"
            >
              <template #default>
                <p>风险等级: <strong>{{ progressData.riskLevel }}</strong></p>
                <p>{{ progressData.suggestion }}</p>
              </template>
            </el-alert>

            <el-alert
                v-else
                title="维修正常进行中"
                type="success"
                show-icon
                :closable="false"
                class="normal-alert"
            >
              <template #default>
                <p>风险等级: <strong>{{ progressData.riskLevel }}</strong></p>
                <p>{{ progressData.suggestion }}</p>
              </template>
            </el-alert>
          </div>
        </div>

        <template #footer>
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button
              type="primary"
              @click="refreshProgress"
              v-if="showProgress"
          >
            刷新进度
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Plus,
  Document,
  Clock,
  User,
  Check, Ticket, TrendCharts
} from '@element-plus/icons-vue';
import dayjs from 'dayjs';
import {
  testPost,
  testGet,
  submitRepairApply,
  getOwnerRepairList,
  getRepairDetail,
  acceptRepair,
  resubmitRepairApply,
  getRepairTypeList,
  cancelRepairApply,
  getRepairProgress
} from '@/api/repairApi.js';

// ========== 1. 权限校验 ==========
const userRoleNum = localStorage.getItem('role');
const hasPermission = computed(() => {
  return userRoleNum === '2';
});

const goBack = () => {
  window.history.go(-1);
};

// ========== 2. 基础状态 ==========
const repairList = ref([]);
const loading = ref(false);

// ========== 3. 时间格式化 ==========
const formatTime = (time) => {
  if (!time) return '-';
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss');
};

// ========== 4. 内容截断 ==========
const truncateContent = (content) => {
  if (!content) return '';
  if (content.length <= 80) return content;
  return content.substring(0, 80) + '...';
};

// ========== 5. 状态映射 ==========
const getStatusDesc = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已派单',
    2: '维修中',
    3: '已完成',
    4: '已取消',
    5: '待验收',
    6: '已验收'
  };
  return statusMap[status] || '未知';
};

const getStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'primary',
    2: 'warning',
    3: 'success',
    4: 'info',
    5: 'danger',
    6: 'success'
  };
  return typeMap[status] || 'info';
};

// ========== 6. 报修类型 ==========
const repairTypeList = ref([]);

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

// ========== 7. 报修列表 ==========
const loadRepairList = () => {
  loading.value = true;
  getOwnerRepairList()
    .then(res => {
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

// ========== 8. 提交报修 ==========
const submitDialogVisible = ref(false);
const submitFormRef = ref(null);
const submitForm = reactive({
  repairTypeId: '',
  title: '',
  content: '',
  expectedTime: ''
});

const submitRules = ref({
  repairTypeId: [{ required: true, message: '请选择报修类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入报修标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入报修内容', trigger: 'blur' }]
});

const openSubmitDialog = () => {
  submitForm.repairTypeId = '';
  submitForm.title = '';
  submitForm.content = '';
  submitForm.expectedTime = '';
  submitDialogVisible.value = true;
};

const resetSubmitForm = () => {
  if (submitFormRef.value) {
    submitFormRef.value.resetFields();
  }
};

const handleSubmit = () => {
  if (!submitFormRef.value) return;

  submitFormRef.value.validate((valid) => {
    if (!valid) return;

    loading.value = true;
    console.log('📤 提交报修数据:', submitForm);

    submitRepairApply(submitForm)
      .then(res => {
        const response = res.data;
        console.log('✅ 后端响应:', response);

        if (response?.code === 200) {
          const repairOrderNo = response.data?.repairOrderNo || '未知';
          ElMessage.success(`${response.msg}（报修单号：${repairOrderNo}）`);
          submitDialogVisible.value = false;
          loadRepairList();
        } else {
          ElMessage.warning(response?.msg || '提交失败');
        }
      })
      .catch(err => {
        console.error('❌ 提交失败:', err);
        ElMessage.error(err?.msg || err?.response?.data?.msg || '提交失败');
      })
      .finally(() => {
        loading.value = false;
      });
  });
};

// ========== 9. 验收报修 ==========
const acceptDialogVisible = ref(false);
const acceptForm = reactive({
  repairOrderId: '',
  acceptResult: true,
  score: 5,
  comment: ''
});

const openAcceptDialog = (row) => {
  acceptForm.repairOrderId = row.id;
  acceptForm.acceptResult = true;
  acceptForm.score = 5;
  acceptForm.comment = '';
  acceptDialogVisible.value = true;
};

const resetAcceptForm = () => {
  acceptForm.acceptResult = true;
  acceptForm.score = 5;
  acceptForm.comment = '';
};

const handleAccept = () => {
  loading.value = true;
  acceptRepair(acceptForm)
    .then(res => {
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

// ========== 10. 重新提交 ==========
const resubmitDialogVisible = ref(false);
const resubmitFormRef = ref(null);
const resubmitForm = reactive({
  repairOrderId: '',
  title: '',
  content: ''
});

const resubmitRules = ref({
  title: [{ required: true, message: '请输入报修标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入报修内容', trigger: 'blur' }]
});

const openResubmitDialog = (row) => {
  resubmitForm.repairOrderId = row.id;
  resubmitForm.title = row.title;
  resubmitForm.content = row.content;
  resubmitDialogVisible.value = true;
};

const resetResubmitForm = () => {
  if (resubmitFormRef.value) {
    resubmitFormRef.value.resetFields();
  }
};

const handleResubmit = () => {
  if (!resubmitFormRef.value) return;

  resubmitFormRef.value.validate((valid) => {
    if (!valid) return;

    loading.value = true;
    resubmitRepairApply(resubmitForm)
      .then(res => {
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
  });
};

// ========== 11. 查看详情 ==========
const detailDialogVisible = ref(false);
const currentDetail = ref(null);
const showProgress = ref(false);
const progressData = ref(null);
const progressTimer = ref(null);

const handleViewDetail = (repairOrderId) => {
  loading.value = true;
  getRepairDetail(repairOrderId)
      .then(res => {
        const response = res.data;
        console.log('✅ 报修详情响应:', response);

        if (response?.code === 200 && response.data) {
          currentDetail.value = response.data;
          detailDialogVisible.value = true;

          // 如果状态不是已完成或已取消,启动进度预测
          if (![3, 4, 6].includes(currentDetail.value.status)) {
            showProgress.value = true;
            loadProgress(repairOrderId);
            // 每30秒刷新一次进度
            if (progressTimer.value) {
              clearInterval(progressTimer.value);
            }
            progressTimer.value = setInterval(() => {
              loadProgress(repairOrderId);
            }, 30000);
          } else {
            showProgress.value = false;
          }
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

// 加载进度预测
const loadProgress = (repairOrderId) => {
  getRepairProgress(repairOrderId)
      .then(res => {
        const response = res.data;
        if (response?.code === 200) {
          progressData.value = response.data;
          console.log('✅ 进度预测加载成功:', progressData.value);
        }
      })
      .catch(err => {
        console.error('❌ 加载进度预测失败:', err);
      });
};

// 刷新进度
const refreshProgress = () => {
  if (currentDetail.value) {
    loadProgress(currentDetail.value.id);
    ElMessage.success('进度已刷新');
  }
};

// 获取进度条状态
const getProgressStatus = () => {
  if (!progressData.value) return '';
  if (progressData.value.isOverdueRisk) return 'exception';
  if (progressData.value.progressPercent === 100) return 'success';
  return '';
};

// 清理定时器
const clearProgressTimer = () => {
  if (progressTimer.value) {
    clearInterval(progressTimer.value);
    progressTimer.value = null;
  }
};

// ========== 12. 取消申请 ==========
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
  }).catch(() => {});
};

// ========== 13. 页面加载 ==========
onMounted(() => {
  if (hasPermission.value) {
    loadRepairTypeList();
    loadRepairList();
  }
});

// 组件卸载时清理定时器
onUnmounted(() => {
  clearProgressTimer();
});
</script>

<style scoped>
.owner-repair {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.repair-header {
  margin-bottom: 20px;
  text-align: left;
}

.list-card {
  margin-bottom: 20px;
}

.repair-list {
  .repair-item {
    padding: 20px;
    border-bottom: 1px solid #ebeef5;
    cursor: pointer;
    transition: all 0.3s;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background-color: #f5f7fa;
      transform: translateX(5px);
    }

    .repair-header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;

      .repair-title {
        display: flex;
        align-items: center;
        font-size: 16px;
        font-weight: bold;

        .repair-title-text {
          color: #303133;
        }
      }

      .repair-order-no {
        font-size: 14px;
        color: #909399;
      }
    }

    .repair-content {
      margin-bottom: 15px;

      .repair-info {
        display: flex;
        flex-wrap: wrap;
        gap: 15px;
        font-size: 14px;
        color: #606266;
        margin-bottom: 10px;

        span {
          display: flex;
          align-items: center;
          gap: 5px;

          .el-icon {
            color: #409EFF;
          }
        }
      }

      .repair-actions {
        display: flex;
        gap: 10px;
        justify-content: flex-start;
        margin-bottom: 10px;
        padding-top: 10px;
        border-top: 1px solid #f0f0f0;
      }

      .repair-desc {
        font-size: 14px;
        color: #606266;
        line-height: 1.6;
      }
    }

    .no-permission {
      padding: 40px;
      text-align: center;
      max-width: 600px;
      margin: 0 auto;
    }
  }
}

/* 进度预测样式 */
.progress-section {
  margin-top: 25px;
}

.progress-bar-wrapper {
  margin: 20px 0;
}

.progress-text {
  font-size: 14px;
  font-weight: bold;
}

.progress-info {
  margin: 20px 0;
}

.info-card {
  text-align: center;
  min-height: 80px;
}

.info-item {
  padding: 8px 0;
}

.info-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.info-value {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  word-break: break-word;
}

.time-line {
  margin: 25px 0;
}

.risk-alert,
.normal-alert {
  margin-top: 15px;
}

.risk-alert p,
.normal-alert p {
  margin: 5px 0;
  font-size: 14px;
}
</style>
