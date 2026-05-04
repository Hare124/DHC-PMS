<!--RegulationAdmin-->
<template>
  <div class="regulation-management-page">
    <!-- 顶部操作区 -->
    <div class="header-actions">
      <el-button type="primary" @click="openAddDialog">新增制度</el-button>

      <!-- 查询筛选区域 -->
      <div class="search-filter-container" style="margin-left: 16px;">
        <el-form :inline="true" class="search-form">
          <el-form-item label="关键词">
            <el-input
              v-model="keyword"
              placeholder="制度名称/编号/内容"
              clearable
              style="width: 200px;"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="制度类型">
            <el-select
              v-model="regulationType"
              placeholder="请选择"
              clearable
              style="width: 150px;"
            >
              <el-option label="收费管理" value="收费管理" />
              <el-option label="消防安全" value="消防安全" />
              <el-option label="装修管理" value="装修管理" />
              <el-option label="车辆管理" value="车辆管理" />
              <el-option label="员工纪律" value="员工纪律" />
              <el-option label="岗位职责" value="岗位职责" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="status"
              placeholder="请选择"
              clearable
              style="width: 120px;"
            >
              <el-option label="草稿" value="draft" />
              <el-option label="已发布" value="published" />
              <el-option label="已下架" value="suspended" />
              <el-option label="已作废" value="invalid" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </el-form-item>
          <el-form-item>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 制度列表表格 -->
    <el-table :data="regulationList" border style="margin-top: 20px" v-loading="loading">
      <el-table-column prop="regulationNo" label="制度编号" width="100" />
      <el-table-column prop="regulationName" label="制度名称" width="210" />
      <el-table-column prop="regulationType" label="制度类型" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="版本号" width="80">
        <template #default="{ row }">
          V{{ row.version }}
        </template>
      </el-table-column>
      <el-table-column prop="effectiveDate" label="生效日期" width="100" />
      <el-table-column prop="invalidDate" label="失效日期" width="100" />
      <el-table-column prop="createByName" label="创建人" width="80" />
      <el-table-column label="操作" width="400" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openViewDialog(row)">查看</el-button>
          <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="primary" @click="handleNewVersion(row)">新版本</el-button>
          <el-button size="small" type="success" @click="handlePublish(row)" v-if="row.status === 'draft'">发布</el-button>
          <el-button size="small" type="warning" @click="handleSuspend(row)" v-if="row.status === 'published'">下架</el-button>
          <el-button size="small" type="danger" @click="handleInvalidate(row)" v-if="row.status !== 'invalid'">作废</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSearch"
      @current-change="handleSearch"
      style="margin-top: 20px; justify-content: flex-end"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑制度' : '新增制度'" width="900px">
      <el-form :model="regulationForm" :rules="rules" ref="regulationFormRef" label-width="120px">
        <el-form-item label="制度编号" prop="regulationNo">
          <el-input v-model="regulationForm.regulationNo" placeholder="请输入制度编号" />
        </el-form-item>
        <el-form-item label="制度名称" prop="regulationName">
          <el-input v-model="regulationForm.regulationName" placeholder="请输入制度名称" />
        </el-form-item>
        <el-form-item label="制度类型" prop="regulationType">
          <el-select v-model="regulationForm.regulationType" placeholder="请选择制度类型" style="width: 100%;">
            <el-option label="收费管理" value="收费管理" />
            <el-option label="消防安全" value="消防安全" />
            <el-option label="装修管理" value="装修管理" />
            <el-option label="车辆管理" value="车辆管理" />
            <el-option label="员工纪律" value="员工纪律" />
            <el-option label="岗位职责" value="岗位职责" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="制度内容" prop="content">
          <el-input
            v-model="regulationForm.content"
            type="textarea"
            :rows="10"
            placeholder="请输入制度详细内容（支持富文本）"
          />
        </el-form-item>
        <el-form-item label="附件 URL" prop="attachmentUrl">
          <el-input v-model="regulationForm.attachmentUrl" placeholder="请输入附件 URL（PDF 等文件）" />
        </el-form-item>
        <el-form-item label="附件名称" prop="attachmentName">
          <el-input v-model="regulationForm.attachmentName" placeholder="请输入附件名称" />
        </el-form-item>
        <el-form-item label="适用范围" prop="applyScope">
          <el-select v-model="regulationForm.applyScope" placeholder="请选择适用范围" style="width: 100%;">
            <el-option label="通用" value="通用" />
            <el-option label="物业人员" value="物业人员" />
            <el-option label="全体业主" value="全体业主" />
            <el-option label="业主和物业人员" value="业主和物业人员" />
          </el-select>
        </el-form-item>
        <el-form-item label="生效日期" prop="effectiveDate">
          <el-date-picker
            v-model="regulationForm.effectiveDate"
            type="date"
            placeholder="请选择生效日期"
            style="width: 100%;"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="失效日期" prop="invalidDate">
          <el-date-picker
            v-model="regulationForm.invalidDate"
            type="date"
            placeholder="请选择失效日期"
            style="width: 100%;"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-alert
          title="温馨提示"
          type="info"
          :closable="false"
          show-icon
          style="margin-top: 10px;"
        >
          <p>• 新增制度默认为草稿状态</p>
          <p>• 发布后业主和物业人员可查看</p>
          <p>• 可通过"新版本"功能进行修订</p>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="制度详情" width="900px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="制度基本信息" :span="1">
          <el-row :gutter="20">
            <el-col :span="8">
              <div style="font-weight: bold; margin-bottom: 8px;">基本信息</div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">制度 ID：</span>
                <span>{{ viewData.id }}</span>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">制度编号：</span>
                <span>{{ viewData.regulationNo }}</span>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">制度名称：</span>
                <span>{{ viewData.regulationName }}</span>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">制度类型：</span>
                <el-tag size="small">{{ viewData.regulationType }}</el-tag>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">版本号：</span>
                <span>V{{ viewData.version }}</span>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">状态：</span>
                <el-tag :type="getStatusType(viewData.status)" size="small">
                  {{ getStatusLabel(viewData.status) }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="8">
              <div style="font-weight: bold; margin-bottom: 8px;">适用范围</div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">适用范围：</span>
                <span>{{ viewData.applyScope || '未设置' }}</span>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">生效日期：</span>
                <span>{{ viewData.effectiveDate || '未设置' }}</span>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">失效日期：</span>
                <span>{{ viewData.invalidDate || '未设置' }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div style="font-weight: bold; margin-bottom: 8px;">人员信息</div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">创建人：</span>
                <span>{{ viewData.createByName || '未知' }}</span>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">创建时间：</span>
                <span>{{ formatDateTime(viewData.createTime) }}</span>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">更新人：</span>
                <span>{{ viewData.updateByName || '未知' }}</span>
              </div>
              <div style="margin-bottom: 6px;">
                <span style="color: #909399;">更新时间：</span>
                <span>{{ formatDateTime(viewData.updateTime) }}</span>
              </div>
            </el-col>
          </el-row>
        </el-descriptions-item>
        <el-descriptions-item label="附件信息" :span="1" v-if="viewData.attachmentUrl">
          <el-link :href="viewData.attachmentUrl" target="_blank" type="primary" icon="Document">
            {{ viewData.attachmentName || '下载附件' }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item label="制度内容" :span="1">
          <div style="max-height: 500px; overflow-y: auto; padding: 15px; background-color: #f5f7fa; border-radius: 4px; line-height: 1.8; white-space: pre-wrap; word-wrap: break-word; font-family: inherit;">
            {{ viewData.content }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getRegulationList,
  addRegulation,
  updateRegulation,
  deleteRegulation,
  createNewVersion,
  publishRegulation,
  suspendRegulation,
  invalidateRegulation
} from '@/api/adminApi.js'

// 分页与搜索
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const regulationType = ref('')
const status = ref('')
const regulationList = ref([])
const loading = ref(false)

// 弹窗与表单
const dialogVisible = ref(false)
const isEdit = ref(false)
const regulationFormRef = ref()
const viewDialogVisible = ref(false)

// 表单数据
const regulationForm = reactive({
  id: null,
  regulationNo: '',
  regulationName: '',
  regulationType: '',
  content: '',
  attachmentUrl: '',
  attachmentName: '',
  applyScope: '',
  effectiveDate: '',
  invalidDate: '',
  version: null,
  parentRegulationId: null,
  status: '',
  createBy: null,
  createByName: ''
})

// 查看详情数据
const viewData = reactive({
  id: null,
  regulationNo: '',
  regulationName: '',
  regulationType: '',
  content: '',
  attachmentUrl: '',
  attachmentName: '',
  applyScope: '',
  effectiveDate: '',
  invalidDate: '',
  version: null,
  status: '',
  createByName: '',
  createTime: '',
  updateByName: '',
  updateTime: ''
})

// 表单校验规则
const rules = {
  regulationNo: [{ required: true, message: '请输入制度编号', trigger: 'blur' }],
  regulationName: [{ required: true, message: '请输入制度名称', trigger: 'blur' }],
  regulationType: [{ required: true, message: '请选择制度类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入制度内容', trigger: 'blur' }],
  effectiveDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }]
}

// 获取当前登录用户信息（从 localStorage 读取）
const getCurrentUser = () => {
  try {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      return JSON.parse(userStr)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
  return { id: 1, name: '管理员' } // 默认值
}

// 获取状态标签
const getStatusLabel = (statusCode) => {
  const statusMap = {
    draft: '草稿',
    published: '已发布',
    suspended: '已下架',
    invalid: '已作废'
  }
  return statusMap[statusCode] || statusCode
}

// 获取状态类型（用于 el-tag 颜色）
const getStatusType = (statusCode) => {
  const typeMap = {
    draft: 'info',
    published: 'success',
    suspended: 'warning',
    invalid: 'danger'
  }
  return typeMap[statusCode] || 'info'
}

// 格式化时间
const formatDateTime = (datetime) => {
  if (!datetime) return '未知'
  try {
    const date = new Date(datetime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (e) {
    return datetime
  }
}

// 加载列表
const loadRegulationList = async () => {
  loading.value = true
  try {
    const res = await getRegulationList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value && keyword.value.trim() ? keyword.value.trim() : undefined,
      type: regulationType.value || undefined,
      status: status.value || undefined
    })

    console.log('完整接口响应:', res)
    console.log('后端返回的核心数据:', res.data.data)

    // 读取双层 data
    if (res.data && res.data.code === 200 && res.data.data) {
      regulationList.value = res.data.data.records || []
      total.value = res.data.data.total || 0
      ElMessage.success('制度列表加载成功')
    } else {
      regulationList.value = []
      total.value = 0
      ElMessage.warning('数据格式异常，未能加载制度列表')
    }
  } catch (error) {
    console.error('加载制度列表失败:', error)
    const errorMsg = error.response?.data?.msg || '加载制度列表失败'
    ElMessage.error(errorMsg)
    regulationList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 统一查询方法
const handleQuery = () => {
  pageNum.value = 1
  loadRegulationList()
}

// 分页变化
const handleSearch = () => {
  loadRegulationList()
}

// 重置查询
const handleReset = () => {
  keyword.value = ''
  regulationType.value = ''
  status.value = ''
  pageNum.value = 1
  loadRegulationList()
}

// 打开新增弹窗
const openAddDialog = () => {
  isEdit.value = false
  const user = getCurrentUser()
  Object.assign(regulationForm, {
    id: null,
    regulationNo: '',
    regulationName: '',
    regulationType: '',
    content: '',
    attachmentUrl: '',
    attachmentName: '',
    applyScope: '',
    effectiveDate: '',
    invalidDate: '',
    version: null,
    parentRegulationId: null,
    status: '',
    createBy: user.id,
    createByName: user.name
  })
  dialogVisible.value = true
}

// 打开查看详情弹窗
const openViewDialog = (row) => {
  Object.assign(viewData, {
    id: row.id,
    regulationNo: row.regulationNo,
    regulationName: row.regulationName,
    regulationType: row.regulationType,
    content: row.content,
    attachmentUrl: row.attachmentUrl,
    attachmentName: row.attachmentName,
    applyScope: row.applyScope,
    effectiveDate: row.effectiveDate,
    invalidDate: row.invalidDate,
    version: row.version,
    status: row.status,
    createByName: row.createByName,
    createTime: row.createTime,
    updateByName: row.updateByName,
    updateTime: row.updateTime
  })
  viewDialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = (row) => {
  isEdit.value = true
  const user = getCurrentUser()
  Object.assign(regulationForm, {
    id: row.id,
    regulationNo: row.regulationNo,
    regulationName: row.regulationName,
    regulationType: row.regulationType,
    content: row.content,
    attachmentUrl: row.attachmentUrl,
    attachmentName: row.attachmentName,
    applyScope: row.applyScope,
    effectiveDate: row.effectiveDate,
    invalidDate: row.invalidDate,
    version: row.version,
    parentRegulationId: row.parentRegulationId,
    status: row.status,
    createBy: row.createBy,
    createByName: row.createByName,
    updateBy: user.id,
    updateByName: user.name
  })
  dialogVisible.value = true
}

// 创建新版本
const handleNewVersion = (row) => {
  isEdit.value = false
  const user = getCurrentUser()
  Object.assign(regulationForm, {
    id: null,
    regulationNo: row.regulationNo + '-V' + (row.version + 1),
    regulationName: row.regulationName,
    regulationType: row.regulationType,
    content: row.content,
    attachmentUrl: row.attachmentUrl,
    attachmentName: row.attachmentName,
    applyScope: row.applyScope,
    effectiveDate: '',
    invalidDate: '',
    version: null,
    parentRegulationId: row.id,
    status: '',
    createBy: user.id,
    createByName: user.name
  })
  dialogVisible.value = true
}

// 发布制度
const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm('确认发布该制度吗？', '提示', {
      type: 'warning'
    })
    await publishRegulation(row.id)
    ElMessage.success('制度发布成功')
    loadRegulationList()
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || '发布失败，请稍后重试'
      ElMessage.error(errorMsg)
    }
  }
}

// 下架制度
const handleSuspend = async (row) => {
  try {
    await ElMessageBox.confirm('确认下架该制度吗？', '提示', {
      type: 'warning'
    })
    await suspendRegulation(row.id)
    ElMessage.success('制度下架成功')
    loadRegulationList()
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || '下架失败，请稍后重试'
      ElMessage.error(errorMsg)
    }
  }
}

// 作废制度
const handleInvalidate = async (row) => {
  try {
    await ElMessageBox.confirm('确认作废该制度吗？此操作不可恢复！', '提示', {
      type: 'warning'
    })
    await invalidateRegulation(row.id)
    ElMessage.success('制度作废成功')
    loadRegulationList()
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || '作废失败，请稍后重试'
      ElMessage.error(errorMsg)
    }
  }
}

// 提交表单
const submitForm = async () => {
  try {
    await regulationFormRef.value.validate()
    const submitData = { ...regulationForm }

    // 如果是编辑操作，确保包含更新人信息
    if (isEdit.value) {
      const user = getCurrentUser()
      submitData.updateBy = user.id
      submitData.updateByName = user.name
    } else {
      // 如果是新增操作，确保包含创建人信息
      const user = getCurrentUser()
      if (!submitData.createBy) {
        submitData.createBy = user.id
        submitData.createByName = user.name
      }
    }

    if (isEdit.value) {
      await updateRegulation(submitData.id, submitData)
      ElMessage.success('修改制度信息成功')
    } else {
      if (submitData.parentRegulationId) {
        // 创建新版本
        await createNewVersion(submitData.parentRegulationId, submitData)
        ElMessage.success('创建新版本制度成功')
      } else {
        // 新增制度
        await addRegulation(submitData)
        ElMessage.success('新增制度成功')
      }
    }
    dialogVisible.value = false
    loadRegulationList()
  } catch (error) {
    if (error.name === 'ValidationError') {
      ElMessage.error('表单校验失败，请检查输入')
    } else {
      const errorMsg = error.response?.data?.msg || '提交失败，请稍后重试'
      ElMessage.error(errorMsg)
    }
  }
}

// 删除
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该制度？此操作不可恢复！', '提示', {
      type: 'warning'
    })
    await deleteRegulation(id)
    ElMessage.success('删除制度成功')
    loadRegulationList()
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || '删除失败，请稍后重试'
      ElMessage.error(errorMsg)
    }
  }
}

onMounted(() => {
  loadRegulationList()
})
</script>

<style scoped>
.regulation-management-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.header-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.search-form {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 制度内容样式 */
.regulation-content {
  max-height: 600px;
  overflow-y: auto;
  padding: 20px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  line-height: 1.8;
  font-size: 14px;
  color: #333;
}

.regulation-content :deep(p) {
  margin-bottom: 12px;
  text-indent: 2em;
  text-align: justify;
}

.regulation-content :deep(h1),
.regulation-content :deep(h2),
.regulation-content :deep(h3),
.regulation-content :deep(h4),
.regulation-content :deep(h5),
.regulation-content :deep(h6) {
  margin-top: 20px;
  margin-bottom: 12px;
  font-weight: bold;
  color: #303133;
}

.regulation-content :deep(ul),
.regulation-content :deep(ol) {
  margin: 12px 0;
  padding-left: 2em;
}

.regulation-content :deep(li) {
  margin-bottom: 6px;
}

.regulation-content :deep(blockquote) {
  margin: 16px 0;
  padding: 12px 16px;
  background-color: #f5f7fa;
  border-left: 4px solid #409EFF;
  border-radius: 4px;
}

.regulation-content :deep(pre) {
  margin: 16px 0;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  overflow-x: auto;
}

.regulation-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}

.regulation-content :deep(th),
.regulation-content :deep(td) {
  border: 1px solid #dcdfe6;
  padding: 8px 12px;
  text-align: left;
}

.regulation-content :deep(th) {
  background-color: #f5f7fa;
  font-weight: bold;
}

.regulation-content :deep(img) {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 16px auto;
}
</style>
