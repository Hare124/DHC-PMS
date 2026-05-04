<!-- src/views/admin/OwnerAdmin.vue -->
<template>
  <div class="owner-management-page">
    <!-- 顶部操作区 -->
    <div class="header-actions">
      <el-button type="primary" @click="openAddDialog">新增业主</el-button>

      <!-- 查询筛选区域 -->
      <div class="search-filter-container" style="margin-left: 16px;">
        <el-form :inline="true" class="search-form">
          <el-form-item label="快速搜索">
            <el-input
                v-model="fuzzyKeyword"
                placeholder="楼房号/姓名/电话"
                clearable                style="width: 250px;"
                @keyup.enter="handleQuery"
            >
            </el-input>
          </el-form-item>
          <!-- 精确查询类型 -->
          <el-form-item label="条件筛选">
            <el-select
                v-model="searchType"
                placeholder="请选择"
                clearable                style="width: 150px;"
            >
              <el-option label="楼号" value="buildingNo" />
              <el-option label="房号" value="roomNo" />
              <el-option label="姓名" value="userName" />
              <el-option label="联系电话" value="phone" />
              <el-option label="身份证号" value="idCard" />
              <el-divider />
              <el-option label="房屋建筑面积" value="houseArea" />
              <el-option label="套内建筑面积" value="internalArea" />
              <el-option label="房屋户型" value="houseLayout" />
              <el-option label="房屋用途" value="houseUsage" />
              <el-option label="房屋结构" value="houseStructure" />
              <el-option label="共有情况" value="ownershipType" />
              <el-option label="共有人姓名" value="coOwnerName" />
              <el-divider />
              <el-option label="不动产权证书号" value="propertyCertNo" />
              <el-option label="不动产单元号" value="propertyUnitNo" />
              <el-option label="登记日期" value="registrationDate" />
              <el-option label="发证机关" value="registrationAuthority" />
              <el-option label="土地使用年限" value="landUseYears" />
              <el-option label="土地性质" value="landNature" />
              <el-divider />
              <el-option label="是否抵押" value="isMortgaged" />
              <el-option label="抵押金额" value="mortgageAmount" />
              <el-option label="抵押权人姓名" value="mortgageeName" />
              <el-option label="是否查封" value="isSeized" />
              <el-option label="是否设立居住权" value="hasResidenceRight" />
              <el-divider />
              <el-option label="房屋交付日期" value="deliveryDate" />
              <el-option label="产权登记用途" value="registeredUsage" />
              <el-option label="是否配套车位" value="hasParkingSpace" />
              <el-option label="是否配套储藏室" value="hasStorageRoom" />
              <el-option label="车位产权类型" value="parkingSpaceType" />
              <el-option label="车位编号" value="parkingSpaceNo" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-input
                v-model="searchKeyword"
                placeholder="输入关键词"
                clearable                style="width: 150px;"
                @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </el-form-item>
          <el-form-item>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

<!--      <el-button type="text" @click="handleLogout" class="logout-btn" style="margin-left: auto;">退出登录</el-button>-->
    </div>

    <!-- 业主列表表格（增加loading状态，提升体验） -->
    <el-table :data="sortedOwnerList" border style="margin-top: 20px" v-loading="loading">
      <el-table-column prop="roomNo" label="楼房号" width="150" sortable />
      <el-table-column prop="ownerName" label="产权人姓名" width="120" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="idCard" label="身份证号" width="180" />

      <!-- 动态字段列 1：默认显示登记日期 -->
      <el-table-column :label="column1Label" width="200">
        <template #header>
          <el-select v-model="field1" size="small" style="width: 100%;" @change="handleField1Change">
            <el-option label="登记日期" value="registrationDate" />
            <el-divider />
            <el-option label="注册时间" value="registerTime" />
            <el-option label="房屋建筑面积" value="houseArea" />
            <el-option label="套内建筑面积" value="internalArea" />
            <el-option label="房屋户型" value="houseLayout" />
            <el-option label="房屋用途" value="houseUsage" />
            <el-option label="房屋结构" value="houseStructure" />
            <el-divider />
            <el-option label="共有情况" value="ownershipType" />
            <el-option label="共有人姓名" value="coOwnerName" />
            <el-option label="共有人身份证号" value="coOwnerIdCard" />
            <el-divider />
            <el-option label="不动产权证书号" value="propertyCertNo" />
            <el-option label="不动产单元号" value="propertyUnitNo" />
            <el-option label="发证机关" value="registrationAuthority" />
            <el-option label="土地使用年限" value="landUseYears" />
            <el-option label="土地性质" value="landNature" />
            <el-divider />
            <el-option label="是否抵押" value="isMortgaged" />
            <el-option label="抵押金额" value="mortgageAmount" />
            <el-option label="抵押权人姓名" value="mortgageeName" />
            <el-option label="是否查封" value="isSeized" />
            <el-option label="是否设立居住权" value="hasResidenceRight" />
            <el-divider />
            <el-option label="房屋交付日期" value="deliveryDate" />
            <el-option label="产权登记用途" value="registeredUsage" />
            <el-divider />
            <el-option label="是否配套车位" value="hasParkingSpace" />
            <el-option label="车位产权类型" value="parkingSpaceType" />
            <el-option label="车位编号" value="parkingSpaceNo" />
            <el-option label="是否配套储藏室" value="hasStorageRoom" />
          </el-select>
        </template>
        <template #default="{ row }">
          {{ getFieldValue(row, field1) }}
        </template>
      </el-table-column>

      <!-- 动态字段列 2：默认显示注册时间 -->
      <el-table-column :label="column2Label" width="200">
        <template #header>
          <el-select v-model="field2" size="small" style="width: 100%;" @change="handleField2Change">
            <el-option label="注册时间" value="registerTime" />
            <el-divider />
            <el-option label="登记日期" value="registrationDate" />
            <el-option label="房屋建筑面积" value="houseArea" />
            <el-option label="套内建筑面积" value="internalArea" />
            <el-option label="房屋户型" value="houseLayout" />
            <el-option label="房屋用途" value="houseUsage" />
            <el-option label="房屋结构" value="houseStructure" />
            <el-divider />
            <el-option label="共有情况" value="ownershipType" />
            <el-option label="共有人姓名" value="coOwnerName" />
            <el-option label="共有人身份证号" value="coOwnerIdCard" />
            <el-divider />
            <el-option label="不动产权证书号" value="propertyCertNo" />
            <el-option label="不动产单元号" value="propertyUnitNo" />
            <el-option label="发证机关" value="registrationAuthority" />
            <el-option label="土地使用年限" value="landUseYears" />
            <el-option label="土地性质" value="landNature" />
            <el-divider />
            <el-option label="是否抵押" value="isMortgaged" />
            <el-option label="抵押金额" value="mortgageAmount" />
            <el-option label="抵押权人姓名" value="mortgageeName" />
            <el-option label="是否查封" value="isSeized" />
            <el-option label="是否设立居住权" value="hasResidenceRight" />
            <el-divider />
            <el-option label="房屋交付日期" value="deliveryDate" />
            <el-option label="产权登记用途" value="registeredUsage" />
            <el-divider />
            <el-option label="是否配套车位" value="hasParkingSpace" />
            <el-option label="车位产权类型" value="parkingSpaceType" />
            <el-option label="车位编号" value="parkingSpaceNo" />
            <el-option label="是否配套储藏室" value="hasStorageRoom" />
          </el-select>
        </template>
        <template #default="{ row }">
          {{ getFieldValue(row, field2) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="250">
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑业主' : '新增业主'" width="900px">
      <el-form :model="ownerForm" :rules="rules" ref="ownerFormRef" label-width="120px">
        <el-divider content-position="left">用户信息</el-divider>

        <!-- User 表字段 -->
        <el-form-item label="用户名" prop="username">
          <el-input v-model="ownerForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="ownerForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="ownerForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="ownerForm.idCard" placeholder="请输入 18 位身份证号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="ownerForm.role" placeholder="请选择角色" style="width: 100%;">
            <el-option label="业主" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="ownerForm.status" placeholder="请选择状态" style="width: 100%;">
            <el-option label="禁用" :value="0" />
            <el-option label="正常" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="ownerForm.password" type="password" placeholder="请输入密码（新增时必填）" show-password />
        </el-form-item>

        <el-divider content-position="left">房屋基本信息</el-divider>

        <!-- Owner 表字段 -->
        <el-form-item label="楼栋号" prop="buildingNo">
          <el-input v-model="ownerForm.buildingNo" placeholder="请输入楼栋号" />
        </el-form-item>
        <el-form-item label="房号" prop="roomNo">
          <el-input v-model="ownerForm.roomNo" placeholder="请输入房号" />
        </el-form-item>

        <el-divider content-position="left">房屋面积信息</el-divider>

        <el-form-item label="房屋建筑面积" prop="houseArea">
          <el-input-number v-model="ownerForm.houseArea" :min="0" :precision="2" style="width: 100%;" placeholder="请输入建筑面积（㎡）" />
        </el-form-item>
        <el-form-item label="套内建筑面积" prop="internalArea">
          <el-input-number v-model="ownerForm.internalArea" :min="0" :precision="2" style="width: 100%;" placeholder="请输入套内面积（㎡）" />
        </el-form-item>

        <el-divider content-position="left">房屋详细信息</el-divider>

        <el-form-item label="房屋户型" prop="houseLayout">
          <el-input v-model="ownerForm.houseLayout" placeholder="如：三室两厅一卫" />
        </el-form-item>
        <el-form-item label="房屋用途" prop="houseUsage">
          <el-select v-model="ownerForm.houseUsage" placeholder="请选择房屋用途" style="width: 100%;">
            <el-option label="住宅" value="住宅" />
            <el-option label="商业" value="商业" />
            <el-option label="办公" value="办公" />
          </el-select>
        </el-form-item>
        <el-form-item label="房屋结构" prop="houseStructure">
          <el-select v-model="ownerForm.houseStructure" placeholder="请选择房屋结构" style="width: 100%;">
            <el-option label="砖混" value="砖混" />
            <el-option label="框架" value="框架" />
            <el-option label="钢结构" value="钢结构" />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">共有情况</el-divider>

        <el-form-item label="共有情况" prop="ownershipType">
          <el-select v-model="ownerForm.ownershipType" placeholder="请选择共有情况" style="width: 100%;">
            <el-option label="单独所有" value="单独所有" />
            <el-option label="共同共有" value="共同共有" />
            <el-option label="按份共有" value="按份共有" />
          </el-select>
        </el-form-item>
        <el-form-item label="共有人姓名" prop="coOwnerName">
          <el-input v-model="ownerForm.coOwnerName" placeholder="请输入共有人姓名" />
        </el-form-item>
        <el-form-item label="共有人身份证号" prop="coOwnerIdCard">
          <el-input v-model="ownerForm.coOwnerIdCard" placeholder="请输入共有人身份证号" />
        </el-form-item>

        <el-divider content-position="left">产权证书信息</el-divider>

        <el-form-item label="不动产权证书号" prop="propertyCertNo">
          <el-input v-model="ownerForm.propertyCertNo" placeholder="请输入房产证号" />
        </el-form-item>
        <el-form-item label="不动产单元号" prop="propertyUnitNo">
          <el-input v-model="ownerForm.propertyUnitNo" placeholder="请输入不动产单元号" />
        </el-form-item>
        <el-form-item label="登记日期" prop="registrationDate">
          <el-date-picker v-model="ownerForm.registrationDate" type="date" placeholder="请选择登记日期" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="发证机关" prop="registrationAuthority">
          <el-input v-model="ownerForm.registrationAuthority" placeholder="请输入发证机关" />
        </el-form-item>
        <el-form-item label="土地使用年限" prop="landUseYears">
          <el-input-number v-model="ownerForm.landUseYears" :min="0" style="width: 100%;" placeholder="请输入土地使用年限（年）" />
        </el-form-item>
        <el-form-item label="土地性质" prop="landNature">
          <el-select v-model="ownerForm.landNature" placeholder="请选择土地性质" style="width: 100%;">
            <el-option label="出让" value="出让" />
            <el-option label="划拨" value="划拨" />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">抵押查封情况</el-divider>

        <el-form-item label="是否抵押" prop="isMortgaged">
          <el-switch v-model="ownerForm.isMortgaged" active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item label="抵押金额" prop="mortgageAmount" v-if="ownerForm.isMortgaged">
          <el-input-number v-model="ownerForm.mortgageAmount" :min="0" :precision="2" style="width: 100%;" placeholder="请输入抵押金额" />
        </el-form-item>
        <el-form-item label="抵押权人姓名" prop="mortgageeName" v-if="ownerForm.isMortgaged">
          <el-input v-model="ownerForm.mortgageeName" placeholder="请输入抵押权人姓名" />
        </el-form-item>
        <el-form-item label="是否查封" prop="isSeized">
          <el-switch v-model="ownerForm.isSeized" active-text="是" inactive-text="否" />
        </el-form-item>

        <el-divider content-position="left">居住权情况</el-divider>

        <el-form-item label="是否设立居住权" prop="hasResidenceRight">
          <el-switch v-model="ownerForm.hasResidenceRight" active-text="是" inactive-text="否" />
        </el-form-item>

        <el-divider content-position="left">房屋交付信息</el-divider>

        <el-form-item label="房屋交付日期" prop="deliveryDate">
          <el-date-picker v-model="ownerForm.deliveryDate" type="date" placeholder="请选择交付日期" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="产权登记用途" prop="registeredUsage">
          <el-select v-model="ownerForm.registeredUsage" placeholder="请选择产权登记用途" style="width: 100%;">
            <el-option label="住宅" value="住宅" />
            <el-option label="商业" value="商业" />
            <el-option label="办公" value="办公" />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">配套设施信息</el-divider>

        <el-form-item label="是否配套车位" prop="hasParkingSpace">
          <el-switch v-model="ownerForm.hasParkingSpace" active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item label="车位产权类型" prop="parkingSpaceType" v-if="ownerForm.hasParkingSpace">
          <el-select v-model="ownerForm.parkingSpaceType" placeholder="请选择车位产权类型" style="width: 100%;">
            <el-option label="产权" value="产权" />
            <el-option label="租赁" value="租赁" />
          </el-select>
        </el-form-item>
        <el-form-item label="车位编号" prop="parkingSpaceNo" v-if="ownerForm.hasParkingSpace">
          <el-input v-model="ownerForm.parkingSpaceNo" placeholder="请输入车位编号" />
        </el-form-item>
        <el-form-item label="是否配套储藏室" prop="hasStorageRoom">
          <el-switch v-model="ownerForm.hasStorageRoom" active-text="是" inactive-text="否" />
        </el-form-item>

        <el-form-item label="变更原因" prop="changeReason" v-if="isEdit">
          <el-input v-model="ownerForm.changeReason" type="textarea" :rows="3" placeholder="请填写变更原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="业主详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="业主 ID">{{ viewData.id }}</el-descriptions-item>
        <el-descriptions-item label="用户 ID">{{ viewData.userId }}</el-descriptions-item>
        <el-descriptions-item label="楼房号">{{ viewData.roomNo }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ viewData.idCard }}</el-descriptions-item>
        <el-descriptions-item label="产权人姓名">{{ viewData.ownerName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ viewData.phone }}</el-descriptions-item>
        <el-descriptions-item label="房屋建筑面积">{{ viewData.houseArea }} ㎡</el-descriptions-item>
        <el-descriptions-item label="套内建筑面积">{{ viewData.internalArea }} ㎡</el-descriptions-item>
        <el-descriptions-item label="房屋户型">{{ viewData.houseLayout || '-' }}</el-descriptions-item>
        <el-descriptions-item label="房屋用途">{{ viewData.houseUsage || '-' }}</el-descriptions-item>
        <el-descriptions-item label="房屋结构">{{ viewData.houseStructure || '-' }}</el-descriptions-item>
        <el-descriptions-item label="共有情况">{{ viewData.ownershipType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="共有人姓名">{{ viewData.coOwnerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="共有人身份证号">{{ viewData.coOwnerIdCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="不动产权证书号">{{ viewData.propertyCertNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="不动产单元号">{{ viewData.propertyUnitNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="登记日期">{{ viewData.registrationDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发证机关">{{ viewData.registrationAuthority || '-' }}</el-descriptions-item>
        <el-descriptions-item label="土地使用年限">{{ viewData.landUseYears }} 年</el-descriptions-item>
        <el-descriptions-item label="土地性质">{{ viewData.landNature || '-' }}</el-descriptions-item>
        <el-descriptions-item label="是否抵押">{{ viewData.isMortgaged ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="抵押金额">{{ viewData.mortgageAmount || '-' }}</el-descriptions-item>
        <el-descriptions-item label="抵押权人姓名">{{ viewData.mortgageeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="是否查封">{{ viewData.isSeized ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="是否设立居住权">{{ viewData.hasResidenceRight ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="房屋交付日期">{{ viewData.deliveryDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="产权登记用途">{{ viewData.registeredUsage || '-' }}</el-descriptions-item>
        <el-descriptions-item label="是否配套车位">{{ viewData.hasParkingSpace ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="是否配套储藏室">{{ viewData.hasStorageRoom ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="车位产权类型">{{ viewData.parkingSpaceType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="车位编号">{{ viewData.parkingSpaceNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">
          {{ viewData.registerTime ? new Date(viewData.registerTime).toLocaleString() : '-' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router' // 补充：原代码遗漏的路由引入
import {
  getOwnerList,
  addOwner,
  updateOwner,
  deleteOwner
} from '@/api/ownerApi.js'

const router = useRouter()

// 分页与搜索
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const fuzzyKeyword = ref('')
const searchType = ref('')
const searchKeyword = ref('')
const ownerList = ref([])
const loading = ref(false)

// 动态字段配置
const field1 = ref('registrationDate') // 列 1 默认显示登记日期
const field2 = ref('registerTime')     // 列 2 默认显示注册时间
const column1Label = ref('登记日期')
const column2Label = ref('注册时间')

const sortedOwnerList = ownerList

// 搜索表单
const searchForm = reactive({
  fuzzyKeyword: '',
  searchType: '',
  keyword: ''
})

// 弹窗与表单
const dialogVisible = ref(false)
const isEdit = ref(false)
const ownerFormRef = ref()
const viewDialogVisible = ref(false)

// 字段映射（用于显示中文标签）
const fieldLabels = {
  registerTime: '注册时间',
  registrationDate: '登记日期',
  houseArea: '房屋建筑面积',
  internalArea: '套内建筑面积',
  houseLayout: '房屋户型',
  houseUsage: '房屋用途',
  houseStructure: '房屋结构',
  ownershipType: '共有情况',
  coOwnerName: '共有人姓名',
  coOwnerIdCard: '共有人身份证号',
  propertyCertNo: '不动产权证书号',
  propertyUnitNo: '不动产单元号',
  registrationAuthority: '发证机关',
  landUseYears: '土地使用年限',
  landNature: '土地性质',
  isMortgaged: '是否抵押',
  mortgageAmount: '抵押金额',
  mortgageeName: '抵押权人姓名',
  isSeized: '是否查封',
  hasResidenceRight: '是否设立居住权',
  deliveryDate: '房屋交付日期',
  registeredUsage: '产权登记用途',
  hasParkingSpace: '是否配套车位',
  parkingSpaceType: '车位产权类型',
  parkingSpaceNo: '车位编号',
  hasStorageRoom: '是否配套储藏室'
}


// 获取字段值
const getFieldValue = (row, field) => {
  const value = row[field]

  // 特殊处理布尔值
  if (field === 'isMortgaged' || field === 'isSeized' ||
      field === 'hasResidenceRight' || field === 'hasParkingSpace' ||
      field === 'hasStorageRoom') {
    return value ? '是' : '否'
  }

  // 处理日期时间
  if (field === 'registerTime' || field === 'registrationDate' || field === 'deliveryDate') {
    return value ? new Date(value).toLocaleString() : '-'
  }

  // 处理数值型字段，添加单位
  if (field === 'houseArea' || field === 'internalArea') {
    return value ? value + ' ㎡' : '-'
  }

  if (field === 'landUseYears') {
    return value ? value + ' 年' : '-'
  }

  if (field === 'mortgageAmount') {
    return value ? '¥' + value.toLocaleString() : '-'
  }

  // 其他字段直接返回或返回'-'
  return value || '-'
}

// 字段 1 改变时的处理
const handleField1Change = (value) => {
  column1Label.value = fieldLabels[value] || '未知字段'
}

// 字段 2 改变时的处理
const handleField2Change = (value) => {
  column2Label.value = fieldLabels[value] || '未知字段'
}

// 扩展表单数据，包含 User 和 Owner 的所有字段
const ownerForm = reactive({
  id: null,
  userId: null,
  // User 表字段
  username: '',
  name: '',
  phone: '',
  password: '',
  role: 2,  // 默认为业主
  status: 1,  // 默认为正常
  // Owner 表字段
  buildingNo: '',
  roomNo: '',
  idCard: '',
  houseArea: null,
  internalArea: null,
  houseLayout: '',
  houseUsage: '',
  houseStructure: '',
  ownershipType: '',
  coOwnerName: '',
  coOwnerIdCard: '',
  propertyCertNo: '',
  propertyUnitNo: '',
  registrationDate: null,
  registrationAuthority: '',
  landUseYears: null,
  landNature: '',
  isMortgaged: false,
  mortgageAmount: null,
  mortgageeName: '',
  isSeized: false,
  hasResidenceRight: false,
  deliveryDate: null,
  registeredUsage: '',
  hasParkingSpace: false,
  parkingSpaceType: '',
  parkingSpaceNo: '',
  hasStorageRoom: false,
  // 编辑时使用
  changeReason: ''
})

// 查看详情数据
const viewData = reactive({
  id: null,
  userId: null,
  roomNo: '',
  idCard: '',
  ownerName: '',
  phone: '',
  houseArea: null,
  internalArea: null,
  houseLayout: '',
  houseUsage: '',
  houseStructure: '',
  ownershipType: '',
  coOwnerName: '',
  coOwnerIdCard: '',
  propertyCertNo: '',
  propertyUnitNo: '',
  registrationDate: '',
  registrationAuthority: '',
  landUseYears: null,
  landNature: '',
  isMortgaged: false,
  mortgageAmount: null,
  mortgageeName: '',
  isSeized: false,
  hasResidenceRight: false,
  deliveryDate: '',
  registeredUsage: '',
  hasParkingSpace: false,
  hasStorageRoom: false,
  parkingSpaceType: '',
  parkingSpaceNo: '',
  registerTime: null
})

// 表单校验规则
const rules = {
  // User 表字段校验
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/, message: '请输入正确的 18 位身份证号', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  // Owner 表字段校验
  buildingNo: [{ required: true, message: '请输入楼栋号', trigger: 'blur' }],
  roomNo: [{ required: true, message: '请输入房号', trigger: 'blur' }],
  changeReason: [{ required: true, message: '请填写变更原因', trigger: 'blur' }]
}

// 加载列表
const loadOwnerList = async () => {
  loading.value = true
  try {
    // 判断使用哪个查询参数
    let finalSearchType = searchType.value
    let finalKeyword = searchKeyword.value

    // 如果模糊查询有值，优先使用模糊查询
    if (fuzzyKeyword.value && fuzzyKeyword.value.trim()) {
      finalSearchType = 'fuzzy'
      finalKeyword = fuzzyKeyword.value.trim()
    }

    const res = await getOwnerList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      searchType: finalSearchType,
      keyword: finalKeyword
    })

    console.log('完整接口响应:', res);
    console.log('后端返回的核心数据:', res.data.data);

    // 读取双层 data
    if (res.data && res.data.code === 200 && res.data.data) {
      ownerList.value = res.data.data.records || [];
      total.value = res.data.data.total || 0;
      ElMessage.success('业主列表加载成功');
    } else {
      ownerList.value = [];
      total.value = 0;
      ElMessage.warning('数据格式异常，未能加载业主列表');
    }
  } catch (error) {
    console.error('加载业主列表失败:', error);
    const errorMsg = error.response?.data?.msg || '加载业主列表失败';
    ElMessage.error(errorMsg);
    ownerList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

// 统一查询方法
const handleQuery = () => {
  if (fuzzyKeyword.value && fuzzyKeyword.value.trim()) {
    searchType.value = ''
    searchKeyword.value = ''
  }
  else if (searchType.value && searchKeyword.value && searchKeyword.value.trim()) {
    fuzzyKeyword.value = ''
  }

  loadOwnerList()
}

// 精确查询
const handleSearch = () => {
  fuzzyKeyword.value = ''
  loadOwnerList()
}

// 重置查询
const handleReset = () => {
  fuzzyKeyword.value = ''
  searchType.value = ''
  searchKeyword.value = ''
  pageNum.value = 1
  loadOwnerList()
}

// 打开新增弹窗
const openAddDialog = () => {
  isEdit.value = false
  // 重置表单为默认值
  Object.assign(ownerForm, {
    id: null,
    userId: null,
    username: '',
    name: '',
    phone: '',
    password: '',
    role: 2,
    status: 1,
    buildingNo: '',
    roomNo: '',
    idCard: '',
    houseArea: null,
    internalArea: null,
    houseLayout: '',
    houseUsage: '',
    houseStructure: '',
    ownershipType: '',
    coOwnerName: '',
    coOwnerIdCard: '',
    propertyCertNo: '',
    propertyUnitNo: '',
    registrationDate: null,
    registrationAuthority: '',
    landUseYears: null,
    landNature: '',
    isMortgaged: false,
    mortgageAmount: null,
    mortgageeName: '',
    isSeized: false,
    hasResidenceRight: false,
    deliveryDate: null,
    registeredUsage: '',
    hasParkingSpace: false,
    parkingSpaceType: '',
    parkingSpaceNo: '',
    hasStorageRoom: false,
    changeReason: ''
  })
  dialogVisible.value = true
}

// 打开查看详情弹窗
const openViewDialog = async (row) => {
  // 填充基本信息
  viewData.id = row.id
  viewData.userId = row.userId
  viewData.roomNo = row.roomNo
  viewData.idCard = row.idCard
  viewData.ownerName = row.ownerName
  viewData.phone = row.phone
  viewData.registerTime = row.registerTime

  // 填充详细信息（如果后端返回）
  viewData.houseArea = row.houseArea
  viewData.internalArea = row.internalArea
  viewData.houseLayout = row.houseLayout
  viewData.houseUsage = row.houseUsage
  viewData.houseStructure = row.houseStructure
  viewData.ownershipType = row.ownershipType
  viewData.coOwnerName = row.coOwnerName
  viewData.coOwnerIdCard = row.coOwnerIdCard
  viewData.propertyCertNo = row.propertyCertNo
  viewData.propertyUnitNo = row.propertyUnitNo
  viewData.registrationDate = row.registrationDate
  viewData.registrationAuthority = row.registrationAuthority
  viewData.landUseYears = row.landUseYears
  viewData.landNature = row.landNature
  viewData.isMortgaged = row.isMortgaged
  viewData.mortgageAmount = row.mortgageAmount
  viewData.mortgageeName = row.mortgageeName
  viewData.isSeized = row.isSeized
  viewData.hasResidenceRight = row.hasResidenceRight
  viewData.deliveryDate = row.deliveryDate
  viewData.registeredUsage = row.registeredUsage
  viewData.hasParkingSpace = row.hasParkingSpace
  viewData.hasStorageRoom = row.hasStorageRoom
  viewData.parkingSpaceType = row.parkingSpaceType
  viewData.parkingSpaceNo = row.parkingSpaceNo

  viewDialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = async (row) => {
  isEdit.value = true
  // 拆分房号为楼栋号和房号
  const roomParts = row.roomNo ? row.roomNo.split('-') : ['', '']

  // 填充表单数据（包含 User 和 Owner 的所有字段）
  Object.assign(ownerForm, {
    id: row.id,
    userId: row.userId,
    // User 表字段
    username: row.username || '',
    name: row.ownerName || '',
    phone: row.phone || '',
    password: '',  // 编辑时不显示密码
    role: row.role || 2,
    status: row.status || 1,
    // Owner 表字段
    buildingNo: roomParts[0] || '',
    roomNo: roomParts[1] || '',
    idCard: row.idCard || '',
    houseArea: row.houseArea || null,
    internalArea: row.internalArea || null,
    houseLayout: row.houseLayout || '',
    houseUsage: row.houseUsage || '',
    houseStructure: row.houseStructure || '',
    ownershipType: row.ownershipType || '',
    coOwnerName: row.coOwnerName || '',
    coOwnerIdCard: row.coOwnerIdCard || '',
    propertyCertNo: row.propertyCertNo || '',
    propertyUnitNo: row.propertyUnitNo || '',
    registrationDate: row.registrationDate || null,
    registrationAuthority: row.registrationAuthority || '',
    landUseYears: row.landUseYears || null,
    landNature: row.landNature || '',
    isMortgaged: row.isMortgaged || false,
    mortgageAmount: row.mortgageAmount || null,
    mortgageeName: row.mortgageeName || '',
    isSeized: row.isSeized || false,
    hasResidenceRight: row.hasResidenceRight || false,
    deliveryDate: row.deliveryDate || null,
    registeredUsage: row.registeredUsage || '',
    hasParkingSpace: row.hasParkingSpace || false,
    parkingSpaceType: row.parkingSpaceType || '',
    parkingSpaceNo: row.parkingSpaceNo || '',
    hasStorageRoom: row.hasStorageRoom || false,
    changeReason: ''
  })

  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  try {
    await ownerFormRef.value.validate()
    const submitData = { ...ownerForm }

    // 处理日期格式
    if (submitData.registrationDate) {
      submitData.registrationDate = new Date(submitData.registrationDate).toISOString().split('T')[0]
    }
    if (submitData.deliveryDate) {
      submitData.deliveryDate = new Date(submitData.deliveryDate).toISOString().split('T')[0]
    }

    if (isEdit.value) {
      // 编辑模式：调用更新接口
      await updateOwner(submitData)
      ElMessage.success('修改业主信息成功')
    } else {
      // 新增模式：调用新增接口
      await addOwner(submitData)
      ElMessage.success('新增业主成功')
    }
    dialogVisible.value = false
    loadOwnerList()
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
    await ElMessageBox.confirm('确认删除该业主？', '提示', {
      type: 'warning'
    })
    await deleteOwner(id)
    ElMessage.success('删除业主成功')
    loadOwnerList()
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || '删除失败，请稍后重试'
      ElMessage.error(errorMsg)
    }
  }
}
// 退出登录功能
const handleLogout = () => {
  ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(() => {
    localStorage.removeItem('token') // 清除 Token
    localStorage.removeItem('userRole') // 清除用户角色
    ElMessage.success('退出登录成功')
    router.push('/login') // 跳回登录页
  }).catch(() => {
    ElMessage.info('已取消退出')
  })
}

onMounted(() => {
  loadOwnerList()
})
</script>

<style scoped>
.owner-management-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}
.header-actions {
  display: flex;
  align-items: center;
}
/* 关键修改：添加退出按钮的样式，实现右对齐 */
.logout-btn {
  margin-left: auto !important; /* auto 会自动填充剩余空间，实现右对齐 */
  margin-right: 0 !important;
}
</style>