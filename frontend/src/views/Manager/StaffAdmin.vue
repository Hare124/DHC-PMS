<!--StaffAdmin-->
<template>
  <div class="staff-management-page">
    <!-- 顶部操作区 -->
    <div class="header-actions">
      <el-button type="primary" @click="openAddDialog">新增物业人员</el-button>

      <!-- 查询筛选区域 -->
      <div class="search-filter-container" style="margin-left: 16px;">
        <el-form :inline="true" class="search-form">
          <el-form-item label="关键词搜索">
            <el-input
              v-model="fuzzyKeyword"
              placeholder="姓名/电话/用户名"
              clearable
              style="width: 250px;"
              @keyup.enter="handleQuery"
            >
            </el-input>
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

    <!-- 物业人员列表表格 -->
    <el-table :data="staffList" border style="margin-top: 20px" v-loading="loading">
      <el-table-column prop="username" label="用户名" width="150" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="staffNo" label="工号" width="120" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag type="warning">物业人员</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'danger'">
            {{ row.status === 1 ? '在职' : row.status === 2 ? '试用期' : '离职' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openViewDialog(row)">查看</el-button>
          <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑物业人员' : '新增物业人员'" width="600px">
      <el-form :model="staffForm" :rules="rules" ref="staffFormRef" label-width="100px">
        <!-- User 表字段 -->
        <el-form-item label="用户名" prop="username">
          <el-input v-model="staffForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="staffForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="staffForm.phone" placeholder="请输入联系电话" />
        </el-form-item>

        <!-- Staff 表字段 -->
        <el-divider content-position="left">员工信息</el-divider>
        <el-form-item label="工号" prop="staffNo">
          <el-input v-model="staffForm.staffNo" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="staffForm.idCard" placeholder="请输入身份证号" maxlength="18" />
        </el-form-item>
        <el-form-item label="员工状态" prop="staffStatus">
          <el-select v-model="staffForm.staffStatus" placeholder="请选择员工状态" style="width: 100%;">
            <el-option label="在职" :value="1" />
            <el-option label="试用期" :value="2" />
            <el-option label="离职" :value="0" />
          </el-select>
        </el-form-item>

        <!-- User 表状态 -->
        <el-divider content-position="left">账号信息</el-divider>
        <el-form-item label="账号状态" prop="status">
          <el-select v-model="staffForm.status" placeholder="请选择账号状态" style="width: 100%;">
            <el-option label="禁用" :value="0" />
            <el-option label="正常" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
              v-model="staffForm.password"
              type="password"
              :placeholder="isEdit ? '不修改请留空' : '请输入密码（新增时必填）'"
              show-password
          />
        </el-form-item>
        <el-alert
            title="温馨提示"
            type="info"
            :closable="false"
            show-icon
            style="margin-top: 10px;"
        >
          <p>• 角色固定为物业人员，不可修改</p>
          <p v-if="!isEdit">• 默认密码：12345678（可不填，不填则使用默认密码）</p>
          <p v-else>• 如需修改密码，请填写新密码；不填写则保持原密码不变</p>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="物业人员详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户 ID">{{ viewData.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ viewData.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ viewData.name }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ viewData.phone }}</el-descriptions-item>
        <el-descriptions-item label="工号">{{ viewData.staffNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ viewData.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="员工状态">
          <el-tag :type="viewData.staffStatus === 1 ? 'success' : viewData.staffStatus === 2 ? 'warning' : 'danger'">
            {{ viewData.staffStatus === 1 ? '在职' : viewData.staffStatus === 2 ? '试用期' : '离职' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag type="warning">物业人员</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="账号状态">
          <el-tag :type="viewData.status === 1 ? 'success' : 'danger'">
            {{ viewData.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(viewData.createTime) }}
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
  getStaffList,
  addStaff,
  updateStaff,
  deleteStaff
} from '@/api/adminApi.js'

// 分页与搜索
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const fuzzyKeyword = ref('')
const staffList = ref([])
const loading = ref(false)

// 弹窗与表单
const dialogVisible = ref(false)
const isEdit = ref(false)
const staffFormRef = ref()
const viewDialogVisible = ref(false)

// 表单数据
const staffForm = reactive({
  id: null,
  username: '',
  name: '',
  phone: '',
  password: '',
  status: 1,
  role: 1,
  // Staff 表字段
  staffNo: '',
  idCard: '',
  staffStatus: 1
})

// 查看详情数据
const viewData = reactive({
  id: null,
  username: '',
  name: '',
  phone: '',
  status: 1,
  staffNo: '',
  idCard: '',
  staffStatus: 1,
  createTime: ''
})

// 表单校验规则
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  staffNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  status: [{ required: true, message: '请选择账号状态', trigger: 'change' }],
  staffStatus: [{ required: true, message: '请选择员工状态', trigger: 'change' }],
  password: [
    {
      validator: (rule, value, callback) => {
        if (!isEdit.value && !value) {
          callback(new Error('请输入密码'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
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
const loadStaffList = async () => {
  loading.value = true
  try {
    const res = await getStaffList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: fuzzyKeyword.value && fuzzyKeyword.value.trim() ? fuzzyKeyword.value.trim() : undefined
    })

    console.log('完整接口响应:', res)
    console.log('后端返回的核心数据:', res.data.data)

    if (res.data && res.data.code === 200 && res.data.data) {
      staffList.value = res.data.data.records || []
      total.value = res.data.data.total || 0
      ElMessage.success('物业人员列表加载成功')
    } else {
      staffList.value = []
      total.value = 0
      ElMessage.warning('数据格式异常，未能加载物业人员列表')
    }
  } catch (error) {
    console.error('加载物业人员列表失败:', error)
    const errorMsg = error.response?.data?.msg || '加载物业人员列表失败'
    ElMessage.error(errorMsg)
    staffList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 统一查询方法
const handleQuery = () => {
  pageNum.value = 1
  loadStaffList()
}

// 分页变化
const handleSearch = () => {
  loadStaffList()
}

// 重置查询
const handleReset = () => {
  fuzzyKeyword.value = ''
  pageNum.value = 1
  loadStaffList()
}

// 打开新增弹窗
const openAddDialog = () => {
  isEdit.value = false
  Object.assign(staffForm, {
    id: null,
    username: '',
    name: '',
    phone: '',
    password: '',
    status: 1,
    role: 1,
    staffNo: '',
    idCard: '',
    staffStatus: 1
  })
  dialogVisible.value = true
}

// 打开查看详情弹窗
const openViewDialog = (row) => {
  Object.assign(viewData, {
    id: row.id,
    username: row.username,
    name: row.name,
    phone: row.phone,
    status: row.status,
    staffNo: row.staffNo,
    idCard: row.idCard,
    staffStatus: row.staffStatus,
    createTime: row.createTime
  })
  viewDialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = (row) => {
  isEdit.value = true
  Object.assign(staffForm, {
    id: row.id,
    username: row.username,
    name: row.name,
    phone: row.phone,
    password: '',
    status: row.status,
    role: 1,
    staffNo: row.staffNo || '',
    idCard: row.idCard || '',
    staffStatus: row.staffStatus || 1
  })
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  try {
    await staffFormRef.value.validate()
    const submitData = { ...staffForm }

    if (isEdit.value && !submitData.password) {
      delete submitData.password
    }

    if (isEdit.value) {
      await updateStaff(submitData)
      ElMessage.success('修改物业人员信息成功')
    } else {
      await addStaff(submitData)
      ElMessage.success('新增物业人员成功')
    }
    dialogVisible.value = false
    loadStaffList()
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
    await ElMessageBox.confirm('确认删除该物业人员？此操作不可恢复！', '提示', {
      type: 'warning'
    })
    await deleteStaff(id)
    ElMessage.success('删除物业人员成功')
    loadStaffList()
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || '删除失败，请稍后重试'
      ElMessage.error(errorMsg)
    }
  }
}

onMounted(() => {
  loadStaffList()
})
</script>

<style scoped>
.staff-management-page {
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
</style>
