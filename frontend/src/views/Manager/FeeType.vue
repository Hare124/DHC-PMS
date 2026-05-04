<template>
  <div class="fee-type-page">
    <!-- 新增费用类型按钮 -->
    <div class="fee-type-form">
      <el-button type="primary" @click="showAddTypeDialog = true">新增费用类型</el-button>
    </div>

    <!-- 费用类型列表 -->
    <div class="fee-type-table">
      <el-table
          :data="feeTypeList"
          border
          stripe          style="width: 100%"
          v-loading="loading"
          :key="feeTypeListKey"
      >
        <el-table-column prop="typeName" label="收费项目" width="140"></el-table-column>
        <el-table-column label="计费类型" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.calcType === '1'" type="success">关联计算</el-tag>
            <el-tag v-else-if="scope.row.calcType === '2'" type="warning">固定金额</el-tag>
            <el-tag v-else>{{ scope.row.calcType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="关联字段" width="120">
          <template #default="scope">
            <div v-if="scope.row.relatedField">
                <span v-for="(field, index) in scope.row.relatedField.split(',')" :key="index">
                  <el-tag size="small" style="margin-right: 5px; margin-bottom: 5px;">
                    {{ getFieldLabel(field) }}
                  </el-tag>
                </span>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="计费条件" width="120">
          <template #default="scope">
            <div v-if="scope.row.conditionType && scope.row.conditionType !== 'NONE'">
              <span>{{ getConditionTypeLabel(scope.row.conditionType) }}</span>
              <span>{{ scope.row.conditionValue }}</span>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="80">
          <template #default="scope">
            <span v-if="scope.row.unitPrice">¥{{ scope.row.unitPrice }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" width="350" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="scope">
            <el-button
                type="primary"
                size="small"
                @click="handleViewFeeType(scope.row)"                style="margin-right: 5px;"
            >
              查看
            </el-button>
            <el-button
                type="warning"
                size="small"
                @click="handleEditFeeType(scope.row)"                style="margin-right: 5px;"
            >
              编辑
            </el-button>
            <el-button
                type="danger"
                size="small"
                @click="handleDeleteFeeType(scope.row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
          v-model:current-page="feeTypePagination.currentPage"
          v-model:page-size="feeTypePagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="feeTypePagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleFeeTypeSizeChange"
          @current-change="handleFeeTypeCurrentChange"          style="margin-top: 20px; justify-content: flex-end;"
      />
    </div>

    <!-- 新增费用类型弹窗 -->
    <el-dialog
        title="新增费用类型"
        v-model="showAddTypeDialog"
        width="600px"
        @close="resetAddTypeForm"
    >
      <el-form :model="feeTypeForm" label-width="120px">
        <el-form-item label="收费项目" required>
          <el-input
              v-model="feeTypeForm.typeName"
              placeholder="请输入收费项目名称（如物业费/水电费）"
          ></el-input>
        </el-form-item>
        <el-form-item label="计费类型" required>
          <el-select v-model="feeTypeForm.calcType" placeholder="请选择计费类型" style="width: 100%;">
            <el-option label="关联计算" value="1"></el-option>
            <el-option label="固定金额" value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="单价（元）" v-if="feeTypeForm.calcType === '2'" required>
          <el-input
              v-model="feeTypeForm.unitPrice"
              type="number"
              placeholder="请输入固定金额（如：300）"
              step="0.01"
              min="0"
          >
            <template #append>
              <span style="padding: 0 10px;">元</span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="关联字段" v-if="feeTypeForm.calcType === '1'" required>
          <el-checkbox-group v-model="feeTypeForm.relatedFields">
            <div style="display: flex; flex-wrap: wrap; gap: 15px;">
              <el-checkbox label="HOUSE_AREA">房屋建筑面积</el-checkbox>
              <el-checkbox label="INTERNAL_AREA">套内建筑面积</el-checkbox>
              <el-checkbox label="HOUSE_LAYOUT">房屋户型</el-checkbox>
              <el-checkbox label="HOUSE_USAGE">房屋用途</el-checkbox>
              <el-checkbox label="HOUSE_STRUCTURE">房屋结构</el-checkbox>
              <el-checkbox label="HAS_PARKING_SPACE">是否配套车位</el-checkbox>
              <el-checkbox label="PARKING_SPACE_TYPE">车位产权类型</el-checkbox>
              <el-checkbox label="HAS_STORAGE_ROOM">是否配套储藏室</el-checkbox>
            </div>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="单价（元/㎡）" v-if="feeTypeForm.calcType === '1'" required>
          <el-input
              v-model="feeTypeForm.unitPrice"
              type="number"
              placeholder="请输入单价（如：1.35）"
              step="0.01"
              min="0"
          >
            <template #append>
              <span style="padding: 0 10px;">元/单位</span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="计费条件" v-if="feeTypeForm.calcType === '1'">
          <div style="border: 1px solid #dcdfe6; border-radius: 4px; padding: 10px; background: #f5f7fa;">
            <el-radio-group v-model="feeTypeForm.conditionType" size="small">
              <el-radio-button label="NONE">不设置条件</el-radio-button>
              <el-radio-button label="EQUAL">等于</el-radio-button>
              <el-radio-button label="NOT_EQUAL">不等于</el-radio-button>
            </el-radio-group>
            <div style="margin-top: 10px; display: flex; gap: 10px; align-items: center;">
              <span style="font-size: 12px; color: #606266;">关联字段：</span>
              <el-select v-model="feeTypeForm.conditionField" placeholder="选择字段" size="small" style="width: 180px;">
                <el-option label="车位产权类型" value="PARKING_SPACE_TYPE"></el-option>
                <el-option label="房屋用途" value="HOUSE_USAGE"></el-option>
                <el-option label="房屋结构" value="HOUSE_STRUCTURE"></el-option>
              </el-select>
              <span style="font-size: 12px; color: #606266;">条件值：</span>
              <el-input
                  v-model="feeTypeForm.conditionValue"
                  placeholder="如：产权"
                  size="small"                  style="width: 150px;"
              ></el-input>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
              v-model="feeTypeForm.remark"
              type="textarea"
              placeholder="选填，补充说明"
              :rows="3"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddTypeDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddFeeType">确定</el-button>
      </template>
    </el-dialog>


    <!-- 编辑费用类型弹窗 -->
    <el-dialog
        title="编辑费用类型"
        v-model="showEditDialog"
        width="600px"
        @close="resetEditForm"
    >
      <el-form :model="editForm" label-width="120px">
        <el-form-item label="收费项目" required>
          <el-input
              v-model="editForm.typeName"
              placeholder="请输入收费项目名称（如物业费/水电费）"
          ></el-input>
        </el-form-item>
        <el-form-item label="计费类型" required>
          <el-select v-model="editForm.calcType" placeholder="请选择计费类型" style="width: 100%;">
            <el-option label="关联计算" value="1"></el-option>
            <el-option label="固定金额" value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="单价（元）" v-if="editForm.calcType === '2'" required>
          <el-input
              v-model="editForm.unitPrice"
              type="number"
              placeholder="请输入固定金额（如：300）"
              step="0.01"
              min="0"
          >
            <template #append>
              <span style="padding: 0 10px;">元</span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="关联字段" v-if="editForm.calcType === '1'" required>
          <el-checkbox-group v-model="editForm.relatedFields">
            <div style="display: flex; flex-wrap: wrap; gap: 15px;">
              <el-checkbox label="HOUSE_AREA">房屋建筑面积</el-checkbox>
              <el-checkbox label="INTERNAL_AREA">套内建筑面积</el-checkbox>
              <el-checkbox label="HOUSE_LAYOUT">房屋户型</el-checkbox>
              <el-checkbox label="HOUSE_USAGE">房屋用途</el-checkbox>
              <el-checkbox label="HOUSE_STRUCTURE">房屋结构</el-checkbox>
              <el-checkbox label="HAS_PARKING_SPACE">是否配套车位</el-checkbox>
              <el-checkbox label="PARKING_SPACE_TYPE">车位产权类型</el-checkbox>
              <el-checkbox label="HAS_STORAGE_ROOM">是否配套储藏室</el-checkbox>
            </div>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="单价（元/㎡）" v-if="editForm.calcType === '1'" required>
          <el-input
              v-model="editForm.unitPrice"
              type="number"
              placeholder="请输入单价（如：1.35）"
              step="0.01"
              min="0"
          >
            <template #append>
              <span style="padding: 0 10px;">元/单位</span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="计费条件" v-if="editForm.calcType === '1'">
          <div style="border: 1px solid #dcdfe6; border-radius: 4px; padding: 10px; background: #f5f7fa;">
            <el-radio-group v-model="editForm.conditionType" size="small">
              <el-radio-button label="NONE">不设置条件</el-radio-button>
              <el-radio-button label="EQUAL">等于</el-radio-button>
              <el-radio-button label="NOT_EQUAL">不等于</el-radio-button>
            </el-radio-group>
            <div style="margin-top: 10px; display: flex; gap: 10px; align-items: center;">
              <span style="font-size: 12px; color: #606266;">关联字段：</span>
              <el-select v-model="editForm.conditionField" placeholder="选择字段" size="small" style="width: 180px;">
                <el-option label="车位产权类型" value="PARKING_SPACE_TYPE"></el-option>
                <el-option label="房屋用途" value="HOUSE_USAGE"></el-option>
                <el-option label="房屋结构" value="HOUSE_STRUCTURE"></el-option>
              </el-select>
              <span style="font-size: 12px; color: #606266;">条件值：</span>
              <el-input
                  v-model="editForm.conditionValue"
                  placeholder="如：产权"
                  size="small"                  style="width: 150px;"
              ></el-input>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
              v-model="editForm.remark"
              type="textarea"
              placeholder="选填，补充说明"
              :rows="3"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">保存修改</el-button>
      </template>
    </el-dialog>

    <!-- 查看费用类型详情弹窗 -->
    <el-dialog
        title="费用类型详情"
        v-model="showViewDialog"
        width="700px"
    >
      <el-descriptions :column="1" border v-if="viewData">
        <el-descriptions-item label="收费项目">{{ viewData.typeName }}</el-descriptions-item>
        <el-descriptions-item label="计费类型">
          <el-tag v-if="viewData.calcType === '1'" type="success">关联计算</el-tag>
          <el-tag v-else-if="viewData.calcType === '2'" type="warning">固定金额</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="关联字段">
          <div v-if="viewData.relatedField">
            <el-tag
                v-for="(field, index) in viewData.relatedField.split(',')"
                :key="index"
                size="small"                style="margin-right: 5px; margin-bottom: 5px;"
            >
              {{ getFieldLabel(field) }}
            </el-tag>
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="计费条件">
          <div v-if="viewData.conditionType && viewData.conditionType !== 'NONE'">
            <el-tag size="small">{{ getConditionTypeLabel(viewData.conditionType) }}</el-tag>
            <span style="margin-left: 5px;">{{ getFieldLabel(viewData.conditionField) || viewData.conditionField }}</span>
            <span style="margin-left: 5px;">=</span>
            <span style="margin-left: 5px;">{{ viewData.conditionValue }}</span>
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="单价">
          <span v-if="viewData.unitPrice" style="color: #f56c6c; font-weight: bold;">¥{{ viewData.unitPrice }}</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注">
          <span v-if="viewData.remark">{{ viewData.remark }}</span>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="showViewDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  getFeeTypes,
  addFeeType,
  updateFeeType,
  deleteFeeType
} from '@/api/feeApi.js';

// 获取管理员 ID
let adminId = 1
const userInfo = localStorage.getItem('userInfo')
if (userInfo) {
  try {
    const parsedUser = JSON.parse(userInfo)
    adminId = Number(parsedUser.id) || 1
  } catch (e) {
    console.error('解析用户信息失败:', e)
  }
}

// 加载状态
const loading = ref(false)

// 列表渲染 key
const feeTypeListKey = ref(0)

// 费用类型列表
const feeTypeList = ref([])

// 分页配置
const feeTypePagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 弹窗控制
const showAddTypeDialog = ref(false)
const showEditDialog = ref(false)
const showViewDialog = ref(false)

// 新增费用类型表单
const feeTypeForm = reactive({
  typeName: '',
  remark: '',
  unitPrice: '',
  calcType: '1',
  relatedFields: ['HOUSE_AREA'],  // 改为数组，支持多个关联字段
  conditionType: 'NONE',  // 条件类型：NONE-不设置、EQUAL-等于、NOT_EQUAL-不等于
  conditionField: '',     // 条件字段
  conditionValue: ''      // 条件值
})

// 编辑表单
const editForm = reactive({
  id: null,
  typeName: '',
  remark: '',
  unitPrice: '',
  calcType: '1',
  relatedFields: [],
  conditionType: 'NONE',
  conditionField: '',
  conditionValue: ''
})

// 查看数据
const viewData = ref(null)

// 获取字段标签
const getFieldLabel = (field) => {
  const fieldMap = {
    'HOUSE_AREA': '房屋建筑面积',
    'INTERNAL_AREA': '套内建筑面积',
    'HOUSE_LAYOUT': '房屋户型',
    'HOUSE_USAGE': '房屋用途',
    'HOUSE_STRUCTURE': '房屋结构',
    'HAS_PARKING_SPACE': '是否配套车位',
    'PARKING_SPACE_TYPE': '车位产权类型',
    'HAS_STORAGE_ROOM': '是否配套储藏室'
  }
  return fieldMap[field] || field
}

// 获取条件类型标签
const getConditionTypeLabel = (type) => {
  const typeMap = {
    'NONE': '不设置',
    'EQUAL': '等于',
    'NOT_EQUAL': '不等于'
  }
  return typeMap[type] || type
}

// 获取费用类型列表
const fetchFeeTypes = async () => {
  try {
    const res = await getFeeTypes()
    console.log('费用类型列表响应:', res)

    if (res.data && res.data.code === 200 && res.data.data) {
      const allTypes = res.data.data || []
      feeTypePagination.value.total = allTypes.length

      // 前端分页
      const startIndex = (feeTypePagination.value.currentPage - 1) * feeTypePagination.value.pageSize
      const endIndex = startIndex + feeTypePagination.value.pageSize
      feeTypeList.value = allTypes.slice(startIndex, endIndex)

      console.log('DEBUG - feeTypeList:', JSON.stringify(feeTypeList.value));
    } else {
      // 如果接口返回异常，使用默认数据作为兜底
      feeTypeList.value = [
        { id: 1, typeName: '物业费', remark: '月度物业费' },
        { id: 2, typeName: '水电费', remark: '月度水电公摊' },
        { id: 3, typeName: '停车费', remark: '月度停车费' }
      ]
      feeTypePagination.value.total = feeTypeList.value.length
      ElMessage.warning('费用类型列表加载异常，使用默认数据')
    }
  } catch (error) {
    console.error('获取费用类型列表失败:', error)
    // 错误情况下也使用默认数据
    feeTypeList.value = [
      { id: 1, typeName: '物业费', remark: '月度物业费' },
      { id: 2, typeName: '水电费', remark: '月度水电公摊' },
      { id: 3, typeName: '停车费', remark: '月度停车费' }
    ]
    feeTypePagination.value.total = feeTypeList.value.length
    ElMessage.error('获取费用类型列表失败，使用默认数据')
  }
}

// 处理费用类型分页大小变化
const handleFeeTypeSizeChange = (val) => {
  feeTypePagination.value.pageSize = val
  feeTypePagination.value.currentPage = 1 // 重置到第一页
  fetchFeeTypes()
}

// 处理费用类型页码变化
const handleFeeTypeCurrentChange = (val) => {
  feeTypePagination.value.currentPage = val
  fetchFeeTypes()
}

// 新增费用类型
const handleAddFeeType = async () => {
  if (!feeTypeForm.typeName) {
    ElMessage.warning('请输入收费项目名称')
    return
  }
  if (!feeTypeForm.calcType) {
    ElMessage.warning('请选择计费类型')
    return
  }
  if (feeTypeForm.calcType === '1' && !feeTypeForm.unitPrice) {
    ElMessage.warning('请输入单价')
    return
  }

  // 调试：检查 adminId
  console.log('准备添加费用类型，adminId:', adminId);
  if (adminId <= 0) {
    ElMessage.error('管理员 ID 无效，请重新登录');
    return;
  }
  try {
    // 构造请求数据
    const requestData = {
      adminId: adminId,
      typeName: feeTypeForm.typeName,
      remark: feeTypeForm.remark,
      unitPrice: feeTypeForm.unitPrice ? Number(feeTypeForm.unitPrice) : null,
      calcType: feeTypeForm.calcType,
      relatedField: feeTypeForm.relatedFields && feeTypeForm.relatedFields.length > 0
          ? feeTypeForm.relatedFields.join(',')
          : null,
      conditionType: feeTypeForm.conditionType || 'NONE',
      conditionField: feeTypeForm.conditionField || null,
      conditionValue: feeTypeForm.conditionValue || null
    }

    console.log('发送新增费用类型请求:', requestData)
    const res = await addFeeType(requestData)
    console.log('新增费用类型响应:', res)

    if (res.data && res.data.code === 200) {
      ElMessage.success('新增费用类型成功')
      showAddTypeDialog.value = false
      // 强制重新渲染组件
      feeTypeListKey.value += 1
      // 重新加载数据
      fetchFeeTypes()
      resetAddTypeForm()
    } else {
      ElMessage.error(res.data?.msg || '新增失败')
    }
  } catch (err) {
    console.error('新增费用类型失败:', err)
    ElMessage.error('新增失败')
  }
}

// 查看费用类型详情
const handleViewFeeType = (row) => {
  viewData.value = row
  showViewDialog.value = true
}

// 编辑费用类型
const handleEditFeeType = (row) => {
  editForm.id = row.id
  editForm.typeName = row.typeName
  editForm.remark = row.remark || ''
  editForm.unitPrice = row.unitPrice ? row.unitPrice.toString() : ''
  editForm.calcType = row.calcType || '1'

  // 解析关联字段
  if (row.relatedField) {
    if (row.relatedField.includes(',')) {
      editForm.relatedFields = row.relatedField.split(',')
    } else {
      editForm.relatedFields = [row.relatedField]
    }
  } else {
    editForm.relatedFields = []
  }

  // 如果是固定金额，清空关联字段
  if (editForm.calcType === '2') {
    editForm.relatedFields = []
  }

  // 设置计费条件
  editForm.conditionType = row.conditionType || 'NONE'
  editForm.conditionField = row.conditionField || ''
  editForm.conditionValue = row.conditionValue || ''

  showEditDialog.value = true
}

// 保存编辑
const handleSaveEdit = async () => {
  if (!editForm.typeName) {
    ElMessage.warning('请输入收费项目名称')
    return
  }
  if (!editForm.calcType) {
    ElMessage.warning('请选择计费类型')
    return
  }
  if (editForm.calcType === '1' && !editForm.unitPrice) {
    ElMessage.warning('请输入单价')
    return
  }

  try {
    const res = await updateFeeType({
      id: editForm.id,
      typeName: editForm.typeName,
      remark: editForm.remark,
      unitPrice: editForm.unitPrice ? Number(editForm.unitPrice) : null,
      calcType: editForm.calcType,
      relatedField: editForm.relatedFields && editForm.relatedFields.length > 0
          ? editForm.relatedFields.join(',')
          : null,
      conditionType: editForm.conditionType || 'NONE',
      conditionField: editForm.conditionField || null,
      conditionValue: editForm.conditionValue || null
    })

    if (res.data && res.data.code === 200) {
      ElMessage.success('修改成功')
      showEditDialog.value = false
      // 强制重新渲染组件
      feeTypeListKey.value += 1
      // 重新加载数据
      fetchFeeTypes()
      resetEditForm()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (err) {
    console.error('修改失败:', err)
    ElMessage.error('修改失败')
  }
}

// 重置新增费用类型表单
const resetAddTypeForm = () => {
  feeTypeForm.typeName = ''
  feeTypeForm.remark = ''
  feeTypeForm.unitPrice = ''
  feeTypeForm.calcType = '1'
  feeTypeForm.relatedFields = ['HOUSE_AREA']
  feeTypeForm.conditionType = 'NONE'
  feeTypeForm.conditionField = ''
  feeTypeForm.conditionValue = ''
}

// 重置编辑表单
const resetEditForm = () => {
  editForm.id = null
  editForm.typeName = ''
  editForm.remark = ''
  editForm.unitPrice = ''
  editForm.calcType = '1'
  editForm.relatedFields = []
  editForm.conditionType = 'NONE'
  editForm.conditionField = ''
  editForm.conditionValue = ''
}

// 删除费用类型
const handleDeleteFeeType = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该费用类型吗？', '提示', {
      type: 'warning'
    })

    // 调用后端删除接口
    const res = await deleteFeeType({ id })

    // 检查后端响应
    if (res.data && res.data.code === 200) {
      // 前端更新列表
      feeTypeList.value = feeTypeList.value.filter(item => item.id !== id)
      ElMessage.success('删除费用类型成功')
      // 重新加载数据
      fetchFeeTypes()
    } else {
      ElMessage.error(res.data?.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || '删除失败，请稍后重试'
      ElMessage.error(errorMsg)
    }
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchFeeTypes()
})
</script>

<style scoped>
.fee-type-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.fee-type-form {
  margin-bottom: 20px;
}

.fee-type-table {
  margin-top: 20px;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-dialog__header) {
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-dialog__footer) {
  padding: 10px 20px;
  border-top: 1px solid #ebeef5;
}
</style>
