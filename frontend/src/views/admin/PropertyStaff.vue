<template>
  <div class="property-staff-page">
    <!-- 顶部操作区 -->
    <div class="header-actions">
      <el-button type="primary" @click="openAddDialog">新增物业人员</el-button>
      <el-input
          v-model="searchKeyword"
          placeholder="按姓名/手机号搜索"
          style="width: 250px; margin-left: 16px"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <!-- 人员列表表格 -->
    <el-table :data="staffList" border style="margin-top: 20px">
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="phone" label="手机号" width="150" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        @size-change="getStaffList"
        @current-change="getStaffList"
        style="margin-top: 20px; justify-content: flex-end"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="物业人员信息" width="500px">
      <el-form :model="staffForm" :rules="rules" ref="staffFormRef" label-width="80px">
        <!-- 新增用户名输入项 -->
        <el-form-item label="用户名" prop="username">
          <el-input v-model="staffForm.username" placeholder="请输入登录用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="staffForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="staffForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <!-- 密码项：新增时显示，编辑时隐藏 -->
        <el-form-item label="密码" prop="password" v-if="!staffForm.id">
          <el-input v-model="staffForm.password" placeholder="不填默认123456" type="password" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="staffForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
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
const searchKeyword = ref('')
const staffList = ref([])

// 弹窗与表单
const dialogVisible = ref(false)
const staffFormRef = ref()
const staffForm = reactive({
  id: null,
  username: '', // 新增：用户名（登录用）
  name: '',     // 姓名
  password: '', // 密码（新增时可选，默认123456）
  phone: '',    // 手机号
  role: 1,      // 固定为物业人员，前端隐藏该字段
  status: 1     // 状态：0-禁用，1-正常
})

// 表单校验规则（新增用户名校验）
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

// 加载列表
const loadStaffList = async () => { // 避免函数名重复（原getStaffList和导入的接口重名）
  const res = await getStaffList({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    keyword: searchKeyword.value
  })
  staffList.value = res.data.records
  total.value = res.data.total
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  loadStaffList()
}

// 打开新增弹窗
const openAddDialog = () => {
  staffForm.id = null
  staffForm.username = ''
  staffForm.name = ''
  staffForm.password = ''
  staffForm.phone = ''
  staffForm.role = 1
  staffForm.status = 1
  dialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = (row) => {
  staffForm.id = row.id
  staffForm.username = row.username // 回显用户名
  staffForm.name = row.name
  staffForm.phone = row.phone
  // 编辑时不回显密码，也不允许修改
  staffForm.password = ''
  staffForm.role = 1 // 固定为物业人员
  staffForm.status = row.status
  dialogVisible.value = true
}

// 提交表单（新增/编辑）
const submitForm = async () => {
  try {
    await staffFormRef.value.validate()
    if (staffForm.id) {
      // 编辑时：删除password字段，避免覆盖原有密码
      const { password, ...updateData } = staffForm
      await updateStaff(updateData)
      ElMessage.success('修改成功')
    } else {
      // 新增时：传递完整表单数据（包含password）
      await addStaff(staffForm)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadStaffList()
  } catch (error) {
    ElMessage.error('表单校验失败，请检查输入')
    console.error(error)
  }
}

// 删除
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该物业人员？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteStaff(id)
    ElMessage.success('删除成功')
    loadStaffList()
  } catch (error) {
    ElMessage.info('已取消删除')
  }
}

onMounted(() => {
  loadStaffList()
})
</script>

<style scoped>
.property-staff-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}
.header-actions {
  display: flex;
  align-items: center;
}
</style>