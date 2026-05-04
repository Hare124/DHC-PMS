<!--CarouselAdmin-->
<template>
  <div class="carousel-management-page">
    <!-- 顶部操作区 -->
    <div class="header-actions">
      <el-button type="primary" @click="openAddDialog">新增轮播图</el-button>

      <!-- 查询筛选区域 -->
      <div class="search-filter-container" style="margin-left: 16px;">
        <el-form :inline="true" class="search-form">
          <el-form-item label="关键词">
            <el-input
              v-model="keyword"
              placeholder="轮播图标题"
              clearable
              style="width: 200px;"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="status"
              placeholder="请选择"
              clearable
              style="width: 120px;"
            >
              <el-option label="启用" value="1" />
              <el-option label="禁用" value="0" />
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

    <!-- 轮播图列表表格 -->
    <el-table :data="carouselList" border style="margin-top: 20px" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" width="200" />
      <el-table-column label="图片预览" width="150">
        <template #default="{ row }">
          <el-image
              :src="getImageUrl(row.imageUrl)"
              :preview-src-list="[getImageUrl(row.imageUrl)]"
              fit="cover"              style="width: 120px; height: 80px; border-radius: 4px;"
              @error="handleImageError"
          >
            <template #error>
              <div class="image-error">
                <el-icon><PictureFilled /></el-icon>
                <span>图片加载失败</span>
              </div>
            </template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '1' ? 'success' : 'info'">
            {{ row.status === '1' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createByName" label="创建人" width="100" />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openViewDialog(row)">查看</el-button>
          <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="success" @click="handleToggleStatus(row)" v-if="row.status === '0'">启用</el-button>
          <el-button size="small" type="warning" @click="handleToggleStatus(row)" v-if="row.status === '1'">禁用</el-button>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑轮播图' : '新增轮播图'" width="700px">
      <el-form :model="carouselForm" :rules="rules" ref="carouselFormRef" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="carouselForm.title" placeholder="请输入轮播图标题" />
        </el-form-item>
        <el-form-item label="图片 URL" prop="imageUrl" required>
          <el-input v-model="carouselForm.imageUrl" placeholder="请输入图片 URL（支持本地路径或网络图片）" />
          <el-alert
              title="温馨提示"
              type="info"
              :closable="false"
              show-icon            style="margin-top: 10px;"
          >
            <p>• 本地图片示例：/uploads/images/carousel/01.jpg</p>
            <p>• 网络图片示例：https://example.com/image.jpg</p>
            <p>• 请将图片放在 uploads/images/carousel/ 目录下</p>
          </el-alert>
        </el-form-item>
        <el-form-item label="跳转链接" prop="targetUrl">
          <el-input v-model="carouselForm.targetUrl" placeholder="请输入点击轮播图后的跳转链接（可选）" />
        </el-form-item>
        <el-form-item label="排序顺序" prop="sortOrder">
          <el-input-number v-model="carouselForm.sortOrder" :min="0" :max="999" placeholder="数字越小越靠前" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="carouselForm.status">
            <el-radio label="1">启用</el-radio>
            <el-radio label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="轮播图详情" width="700px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">{{ viewData.id }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ viewData.title }}</el-descriptions-item>
        <el-descriptions-item label="图片预览">
          <el-image
            :src="viewData.imageUrl"
            :preview-src-list="[viewData.imageUrl]"
            fit="cover"
            style="width: 300px; height: 200px; border-radius: 4px;"
          />
        </el-descriptions-item>
        <el-descriptions-item label="跳转链接">
          <el-link :href="viewData.targetUrl" target="_blank" type="primary" v-if="viewData.targetUrl">
            {{ viewData.targetUrl }}
          </el-link>
          <span v-else>无</span>
        </el-descriptions-item>
        <el-descriptions-item label="排序顺序">{{ viewData.sortOrder }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewData.status === '1' ? 'success' : 'info'">
            {{ viewData.status === '1' ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建人">{{ viewData.createByName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(viewData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新人">{{ viewData.updateByName }}</el-descriptions-item>
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
import { ElMessage, ElMessageBox, ElIcon } from 'element-plus'
import { PictureFilled } from '@element-plus/icons-vue'
import {
  getCarouselList,
  addCarousel,
  updateCarousel,
  deleteCarousel,
  updateCarouselStatus
} from '@/api/adminApi.js'

// 获取图片完整 URL
const getImageUrl = (url) => {
  if (!url) return ''
  // 如果是网络图片，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // 使用环境变量获取后端地址
  const backendUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  return url.startsWith('/') ? backendUrl + url : backendUrl + '/' + url
}

// 处理图片加载失败
const handleImageError = (e) => {
  console.error('图片加载失败:', e.target.src)
}

// 分页与搜索
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const status = ref('')
const carouselList = ref([])
const loading = ref(false)

// 弹窗与表单
const dialogVisible = ref(false)
const isEdit = ref(false)
const carouselFormRef = ref()
const viewDialogVisible = ref(false)

// 表单数据
const carouselForm = reactive({
  id: null,
  title: '',
  imageUrl: '',
  targetUrl: '',
  sortOrder: 0,
  status: '1',
  createBy: null,
  createByName: ''
})

// 查看详情数据
const viewData = reactive({
  id: null,
  title: '',
  imageUrl: '',
  targetUrl: '',
  sortOrder: 0,
  status: '',
  createByName: '',
  createTime: '',
  updateByName: '',
  updateTime: ''
})

// 表单校验规则
const rules = {
  title: [{ required: true, message: '请输入轮播图标题', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请输入图片 URL', trigger: 'blur' }]
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
const loadCarouselList = async () => {
  loading.value = true
  try {
    const res = await getCarouselList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value && keyword.value.trim() ? keyword.value.trim() : undefined,
      status: status.value || undefined
    })

    console.log('完整接口响应:', res)
    console.log('后端返回的核心数据:', res.data.data)

    // 读取双层 data
    if (res.data && res.data.code === 200 && res.data.data) {
      carouselList.value = res.data.data.records || []
      total.value = res.data.data.total || 0
      ElMessage.success('轮播图列表加载成功')
    } else {
      carouselList.value = []
      total.value = 0
      ElMessage.warning('数据格式异常，未能加载轮播图列表')
    }
  } catch (error) {
    console.error('加载轮播图列表失败:', error)
    const errorMsg = error.response?.data?.msg || '加载轮播图列表失败'
    ElMessage.error(errorMsg)
    carouselList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 统一查询方法
const handleQuery = () => {
  pageNum.value = 1
  loadCarouselList()
}

// 分页变化
const handleSearch = () => {
  loadCarouselList()
}

// 重置查询
const handleReset = () => {
  keyword.value = ''
  status.value = ''
  pageNum.value = 1
  loadCarouselList()
}

// 打开新增弹窗
const openAddDialog = () => {
  isEdit.value = false
  const user = getCurrentUser()
  Object.assign(carouselForm, {
    id: null,
    title: '',
    imageUrl: '',
    targetUrl: '',
    sortOrder: 0,
    status: '1',
    createBy: user.id,
    createByName: user.name
  })
  dialogVisible.value = true
}

// 打开查看详情弹窗
const openViewDialog = (row) => {
  Object.assign(viewData, {
    id: row.id,
    title: row.title,
    imageUrl: row.imageUrl,
    targetUrl: row.targetUrl,
    sortOrder: row.sortOrder,
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
  Object.assign(carouselForm, {
    id: row.id,
    title: row.title,
    imageUrl: row.imageUrl,
    targetUrl: row.targetUrl,
    sortOrder: row.sortOrder,
    status: row.status,
    createBy: row.createBy,
    createByName: row.createByName,
    updateBy: user.id,
    updateByName: user.name
  })
  dialogVisible.value = true
}

// 切换状态
const handleToggleStatus = async (row) => {
  try {
    const newStatus = row.status === '1' ? '0' : '1'
    const actionText = newStatus === '1' ? '启用' : '禁用'
    await ElMessageBox.confirm(`确认${actionText}该轮播图吗？`, '提示', {
      type: 'warning'
    })
    await updateCarouselStatus(row.id, newStatus)
    ElMessage.success(`${actionText}成功`)
    loadCarouselList()
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || `${actionText}失败，请稍后重试`
      ElMessage.error(errorMsg)
    }
  }
}

// 提交表单
const submitForm = async () => {
  try {
    await carouselFormRef.value.validate()
    const submitData = { ...carouselForm }

    if (isEdit.value) {
      await updateCarousel(submitData.id, submitData)
      ElMessage.success('修改轮播图信息成功')
    } else {
      await addCarousel(submitData)
      ElMessage.success('新增轮播图成功')
    }
    dialogVisible.value = false
    loadCarouselList()
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
    await ElMessageBox.confirm('确认删除该轮播图？此操作不可恢复！', '提示', {
      type: 'warning'
    })
    await deleteCarousel(id)
    ElMessage.success('删除轮播图成功')
    loadCarouselList()
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || '删除失败，请稍后重试'
      ElMessage.error(errorMsg)
    }
  }
}

onMounted(() => {
  loadCarouselList()
})
</script>

<style scoped>
.carousel-management-page {
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
.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 80px;
  color: #909399;
  font-size: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.image-error .el-icon {
  font-size: 24px;
  margin-bottom: 4px;
}
</style>
