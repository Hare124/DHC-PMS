<!--EquipmentAdmin-->
<template>
  <div class="equipment-management-page">
    <!-- 顶部操作区 -->
    <div class="header-actions">
      <el-button type="primary" @click="openAddDialog">新增设备</el-button>

      <!-- 查询筛选区域 -->
      <div class="search-filter-container" style="margin-left: 16px;">
        <el-form :inline="true" class="search-form">
          <el-form-item label="关键词">
            <el-input
              v-model="keyword"
              placeholder="设备名称/编号/位置"
              clearable
              style="width: 200px;"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="设备类型">
            <el-select
              v-model="equipmentType"
              placeholder="请选择"
              clearable
              style="width: 150px;"
            >
              <el-option label="电梯" value="电梯" />
              <el-option label="消防" value="消防" />
              <el-option label="供水" value="供水" />
              <el-option label="供电" value="供电" />
              <el-option label="照明" value="照明" />
              <el-option label="安防" value="安防" />
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
              <el-option label="正常" value="normal" />
              <el-option label="故障" value="fault" />
              <el-option label="维修中" value="maintaining" />
              <el-option label="停用" value="disabled" />
              <el-option label="报废" value="scrapped" />
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

    <!-- 设备列表表格 -->
    <el-table :data="equipmentList" border style="margin-top: 20px" v-loading="loading">
      <el-table-column prop="equipmentNo" label="设备编号" width="120" />
      <el-table-column prop="equipmentName" label="设备名称" width="150" />
      <el-table-column prop="equipmentType" label="设备类型" width="100" />
      <el-table-column prop="brand" label="品牌" width="120" />
      <el-table-column prop="model" label="型号" width="120" />
      <el-table-column prop="installLocation" label="安装位置" width="200" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑设备' : '新增设备'" width="700px">
      <el-form :model="equipmentForm" :rules="rules" ref="equipmentFormRef" label-width="120px">
        <el-form-item label="设备编号" prop="equipmentNo">
          <el-input v-model="equipmentForm.equipmentNo" placeholder="请输入设备编号" />
        </el-form-item>
        <el-form-item label="设备名称" prop="equipmentName">
          <el-input v-model="equipmentForm.equipmentName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="equipmentType">
          <el-select v-model="equipmentForm.equipmentType" placeholder="请选择设备类型" style="width: 100%;">
            <el-option label="电梯" value="电梯" />
            <el-option label="消防" value="消防" />
            <el-option label="供水" value="供水" />
            <el-option label="供电" value="供电" />
            <el-option label="照明" value="照明" />
            <el-option label="安防" value="安防" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌" prop="brand">
          <el-input v-model="equipmentForm.brand" placeholder="请输入品牌" />
        </el-form-item>
        <el-form-item label="型号" prop="model">
          <el-input v-model="equipmentForm.model" placeholder="请输入型号" />
        </el-form-item>
        <el-form-item label="安装位置" prop="installLocation">
          <el-input v-model="equipmentForm.installLocation" placeholder="请输入安装位置" />
        </el-form-item>
        <el-form-item label="所属楼栋" prop="buildingNo">
          <el-input v-model="equipmentForm.buildingNo" placeholder="请输入所属楼栋号" />
        </el-form-item>
        <el-form-item label="所属区域" prop="area">
          <el-input v-model="equipmentForm.area" placeholder="请输入所属区域" />
        </el-form-item>
        <el-form-item label="采购日期" prop="purchaseDate">
          <el-date-picker
            v-model="equipmentForm.purchaseDate"
            type="date"
            placeholder="请选择采购日期"
            style="width: 100%;"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="质保期（月）" prop="warrantyPeriod">
          <el-input-number v-model="equipmentForm.warrantyPeriod" :min="0" style="width: 100%;" placeholder="请输入质保期（月）" />
        </el-form-item>
        <el-form-item label="使用年限（年）" prop="serviceLife">
          <el-input-number v-model="equipmentForm.serviceLife" :min="1" style="width: 100%;" placeholder="请输入使用年限" />
        </el-form-item>
        <el-form-item label="设备状态" prop="status">
          <el-select v-model="equipmentForm.status" placeholder="请选择设备状态" style="width: 100%;">
            <el-option label="正常" value="normal" />
            <el-option label="故障" value="fault" />
            <el-option label="维修中" value="maintaining" />
            <el-option label="停用" value="disabled" />
            <el-option label="报废" value="scrapped" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="设备详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="设备 ID">{{ viewData.id }}</el-descriptions-item>
        <el-descriptions-item label="设备编号">{{ viewData.equipmentNo }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ viewData.equipmentName }}</el-descriptions-item>
        <el-descriptions-item label="设备类型">{{ viewData.equipmentType }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ viewData.brand }}</el-descriptions-item>
        <el-descriptions-item label="型号">{{ viewData.model }}</el-descriptions-item>
        <el-descriptions-item label="安装位置">{{ viewData.installLocation }}</el-descriptions-item>
        <el-descriptions-item label="所属楼栋">{{ viewData.buildingNo }}</el-descriptions-item>
        <el-descriptions-item label="所属区域">{{ viewData.area }}</el-descriptions-item>
        <el-descriptions-item label="采购日期">{{ viewData.purchaseDate }}</el-descriptions-item>
        <el-descriptions-item label="质保期">{{ viewData.warrantyPeriod }} 个月</el-descriptions-item>
        <el-descriptions-item label="使用年限">{{ viewData.serviceLife }} 年</el-descriptions-item>
        <el-descriptions-item label="设备状态">
          <el-tag :type="getStatusType(viewData.status)">
            {{ getStatusLabel(viewData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(viewData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ formatDateTime(viewData.updateTime) }}
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
  getEquipmentList,
  addEquipment,
  updateEquipment,
  deleteEquipment
} from '@/api/adminApi.js'

// 分页与搜索
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const equipmentType = ref('')
const status = ref('')
const equipmentList = ref([])
const loading = ref(false)

// 弹窗与表单
const dialogVisible = ref(false)
const isEdit = ref(false)
const equipmentFormRef = ref()
const viewDialogVisible = ref(false)

// 表单数据
const equipmentForm = reactive({
  id: null,
  equipmentNo: '',
  equipmentName: '',
  equipmentType: '',
  brand: '',
  model: '',
  installLocation: '',
  buildingNo: '',
  area: '',
  purchaseDate: '',
  warrantyPeriod: null,
  serviceLife: null,
  status: 'normal'
})

// 查看详情数据
const viewData = reactive({
  id: null,
  equipmentNo: '',
  equipmentName: '',
  equipmentType: '',
  brand: '',
  model: '',
  installLocation: '',
  buildingNo: '',
  area: '',
  purchaseDate: '',
  warrantyPeriod: null,
  serviceLife: null,
  status: '',
  createTime: '',
  updateTime: ''
})

// 表单校验规则
const rules = {
  equipmentNo: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  equipmentName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  equipmentType: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
  installLocation: [{ required: true, message: '请输入安装位置', trigger: 'blur' }],
  status: [{ required: true, message: '请选择设备状态', trigger: 'change' }]
}

// 获取状态标签
const getStatusLabel = (statusCode) => {
  const statusMap = {
    normal: '正常',
    fault: '故障',
    maintaining: '维修中',
    disabled: '停用',
    scrapped: '报废'
  }
  return statusMap[statusCode] || statusCode
}

// 获取状态类型（用于 el-tag 颜色）
const getStatusType = (statusCode) => {
  const typeMap = {
    normal: 'success',
    fault: 'danger',
    maintaining: 'warning',
    disabled: 'info',
    scrapped: 'info'
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
const loadEquipmentList = async () => {
  loading.value = true
  try {
    const res = await getEquipmentList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value && keyword.value.trim() ? keyword.value.trim() : undefined,
      type: equipmentType.value || undefined,
      status: status.value || undefined
    })

    console.log('完整接口响应:', res)
    console.log('后端返回的核心数据:', res.data.data)

    // 读取双层 data
    if (res.data && res.data.code === 200 && res.data.data) {
      equipmentList.value = res.data.data.records || []
      total.value = res.data.data.total || 0
      ElMessage.success('设备列表加载成功')
    } else {
      equipmentList.value = []
      total.value = 0
      ElMessage.warning('数据格式异常，未能加载设备列表')
    }
  } catch (error) {
    console.error('加载设备列表失败:', error)
    const errorMsg = error.response?.data?.msg || '加载设备列表失败'
    ElMessage.error(errorMsg)
    equipmentList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 统一查询方法
const handleQuery = () => {
  pageNum.value = 1
  loadEquipmentList()
}

// 分页变化
const handleSearch = () => {
  loadEquipmentList()
}

// 重置查询
const handleReset = () => {
  keyword.value = ''
  equipmentType.value = ''
  status.value = ''
  pageNum.value = 1
  loadEquipmentList()
}

// 打开新增弹窗
const openAddDialog = () => {
  isEdit.value = false
  Object.assign(equipmentForm, {
    id: null,
    equipmentNo: '',
    equipmentName: '',
    equipmentType: '',
    brand: '',
    model: '',
    installLocation: '',
    buildingNo: '',
    area: '',
    purchaseDate: '',
    warrantyPeriod: null,
    serviceLife: null,
    status: 'normal'
  })
  dialogVisible.value = true
}

// 打开查看详情弹窗
const openViewDialog = (row) => {
  Object.assign(viewData, {
    id: row.id,
    equipmentNo: row.equipmentNo,
    equipmentName: row.equipmentName,
    equipmentType: row.equipmentType,
    brand: row.brand,
    model: row.model,
    installLocation: row.installLocation,
    buildingNo: row.buildingNo,
    area: row.area,
    purchaseDate: row.purchaseDate,
    warrantyPeriod: row.warrantyPeriod,
    serviceLife: row.serviceLife,
    status: row.status,
    createTime: row.createTime,
    updateTime: row.updateTime
  })
  viewDialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = (row) => {
  isEdit.value = true
  Object.assign(equipmentForm, {
    id: row.id,
    equipmentNo: row.equipmentNo,
    equipmentName: row.equipmentName,
    equipmentType: row.equipmentType,
    brand: row.brand,
    model: row.model,
    installLocation: row.installLocation,
    buildingNo: row.buildingNo,
    area: row.area,
    purchaseDate: row.purchaseDate,
    warrantyPeriod: row.warrantyPeriod,
    serviceLife: row.serviceLife,
    status: row.status
  })
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  try {
    await equipmentFormRef.value.validate()
    const submitData = { ...equipmentForm }

    if (isEdit.value) {
      await updateEquipment(submitData.id, submitData)
      ElMessage.success('修改设备信息成功')
    } else {
      await addEquipment(submitData)
      ElMessage.success('新增设备成功')
    }
    dialogVisible.value = false
    loadEquipmentList()
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
    await ElMessageBox.confirm('确认删除该设备？此操作不可恢复！', '提示', {
      type: 'warning'
    })
    await deleteEquipment(id)
    ElMessage.success('删除设备成功')
    loadEquipmentList()
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || '删除失败，请稍后重试'
      ElMessage.error(errorMsg)
    }
  }
}

onMounted(() => {
  loadEquipmentList()
})
</script>

<style scoped>
.equipment-management-page {
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
