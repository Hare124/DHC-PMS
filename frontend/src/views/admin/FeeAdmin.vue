<template>
  <div class="admin-fee-container">
    <!-- 顶部操作区：新增退出登录按钮 -->
<!--    <div class="header-actions" style="margin-bottom: 20px">-->
<!--      <el-button type="text" @click="handleLogout" class="logout-btn">退出登录</el-button>-->
<!--    </div>-->
    <!-- 功能标签页 -->
    <el-tabs v-model="activeTab" type="card" @tab-change="handleTabChange">
      <!-- 费用查询标签页 -->
      <el-tab-pane label="费用记录管理" v-if="userRole === '1'" name="feeList">
        <!-- 高级筛选 -->
        <div class="search-form">
          <el-form :inline="true" :model="searchForm" class="demo-form-inline">
            <el-form-item label="楼房号/业主姓名">
              <el-input v-model="searchForm.keyword" placeholder="模糊搜索房号/业主姓名"></el-input>
            </el-form-item>
            <el-form-item label="费用类型" style="width: 210px;">
              <el-select
                v-model="searchForm.typeName"
                placeholder="请选择费用类型"
                clearable
                filterable
                :model-value="searchForm.typeName"
                @update:model-value="handleFeeTypeModelUpdate"
                @visible-change="handleSelectVisibleChange"
              >
                <el-option
                  v-for="type in feeTypeList"
                  :key="type.id"
                  :label="type.typeName || '未知类型'"
                  :value="type.typeName || ''"
                  :disabled="!type.typeName"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="缴费状态" style="width: 210px;">
              <el-select
                v-model="searchForm.status"
                placeholder="请选择"
                filterable
                style="width: 160px;"
              >
                <el-option label="未缴费" value="0"></el-option>
                <el-option label="已缴费" value="1"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="截止日期" style="width: 400px;">
              <el-date-picker
                  v-model="dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
              ></el-date-picker>
            </el-form-item>
            <el-form-item label="金额区间">
              <el-input
                  v-model="searchForm.minAmount"
                  placeholder="最小金额"                  style="width: 100px;"
                  type="number"
              ></el-input>
              <span style="margin: 0 10px;">至</span>
              <el-input
                  v-model="searchForm.maxAmount"
                  placeholder="最大金额"                  style="width: 100px;"
                  type="number"
              ></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="fetchAdminFeeList">查询</el-button>
              <el-button @click="resetSearchForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 批量操作按钮 -->
        <div class="batch-operation">
          <el-button
              type="success"
              @click="handleBatchWriteOff"
              :disabled="selectedFeeIds.length === 0"
          >
            批量核销
          </el-button>
          <el-button
              type="warning"
              @click="handleBatchReminder"
              :disabled="selectedFeeIds.length === 0"
          >
            批量催缴
          </el-button>
          <el-button
              type="primary"
              @click="handleExportExcel"
          >
            导出报表
          </el-button>
        </div>

        <!-- 费用列表 -->
        <div class="fee-table">
          <el-table
              :data="feeList"
              border
              stripe
              style="width: 100%"
              @selection-change="handleSelectionChange"
              v-loading="loading"
          >
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column prop="roomNo" label="楼房号" width="120"></el-table-column>
            <el-table-column prop="ownerName" label="姓名" width="120"></el-table-column>
            <el-table-column prop="feeTypeName" label="费用类型" width="120"></el-table-column>
            <el-table-column prop="amount" label="金额（元）" width="100"></el-table-column>
            <el-table-column prop="dueDate" label="截止日期" width="120"></el-table-column>
            <el-table-column prop="statusDesc" label="状态" width="150">
              <template #default="scope">
                <el-tag v-if="scope.row.statusDesc === '已缴费'" type="success">
                  {{ scope.row.statusDesc }}
                </el-tag>
                <el-tag v-else-if="scope.row.statusDesc === '待缴费'" type="warning">
                  {{ scope.row.statusDesc }}
                </el-tag>
                <el-tag v-else type="danger">
                  {{ scope.row.statusDesc }}（{{ scope.row.overdueDays }}天）
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="催缴信息" width="100">
              <template #default="scope">
                {{ scope.row.latestReminder && scope.row.latestReminder.includes('已发送') ? '已发送' : '无' }}
              </template>
            </el-table-column>
            <el-table-column prop="operatorName" label="核销人员" width="100"></el-table-column>
            <el-table-column label="操作" width="280">
              <template #default="scope">
                <el-button
                    type="info"
                    size="small"
                    @click="handleViewDetail(scope.row)"
                >
                  查看
                </el-button>
                <el-button
                    v-if="scope.row.statusDesc !== '已缴费'"
                    type="success"
                    size="small"
                    @click="handleSingleWriteOff(scope.row.id)"
                >
                  核销
                </el-button>
                <el-button
                    v-if="scope.row.statusDesc !== '已缴费'"
                    type="warning"
                    size="small"
                    @click="handleSingleReminder(scope.row.id)"
                >
                  催缴
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <el-pagination
              v-model:current-page="searchForm.pageNum"
              v-model:page-size="searchForm.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchAdminFeeList"
              @current-change="fetchAdminFeeList"
              style="margin-top: 20px; justify-content: flex-end"
          ></el-pagination>
        </div>
      </el-tab-pane>

      <!-- 费用类型管理标签页 -->
      <el-tab-pane v-if="userRole === '3'" label="费用类型管理" name="feeType">
        <!-- 新增费用类型按钮 -->
        <div class="fee-type-form">
          <el-button type="primary" @click="showAddTypeDialog = true">新增费用类型</el-button>
        </div>

        <!-- 费用类型列表 -->
        <div class="fee-type-table">
          <el-table
              :data="feeTypeList"
              border
              stripe              style="width: 100%"
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
                    @click="handleViewFeeType(scope.row)"                    style="margin-right: 5px;"
                >
                  查看
                </el-button>
                <el-button
                    type="warning"
                    size="small"
                    @click="handleEditFeeType(scope.row)"                    style="margin-right: 5px;"
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
              @current-change="handleFeeTypeCurrentChange"              style="margin-top: 20px; justify-content: flex-end;"
          />
        </div>
      </el-tab-pane>

      <!-- 批量生成账单标签页 -->
      <el-tab-pane label="批量生成账单" v-if="userRole === '1'" name="batchGenerate">
        <div class="batch-generate-form">
          <el-form :model="generateForm" label-width="120px" style="max-width: 700px;">
            <el-form-item label="楼栋号" required>
              <el-input
                  v-model="generateForm.buildingNo"
                  placeholder="请输入楼栋号（如：1 栋）"
                  clearable
                  @change="handleBuildingNoChange"
                  @input="handleBuildingNoChange"
              >
                <template #append>
                  <el-button icon="el-icon-refresh" @click="fetchBuildingNos"></el-button>
                </template>
              </el-input>
              <div style="margin-top: 8px;">
                <el-tag
                    v-for="building in buildingNoList"
                    :key="building"
                    size="small"                    style="margin-right: 8px; margin-bottom: 8px; cursor: pointer;"
                    :type="generateForm.buildingNo === building ? 'primary' : ''"
                    @click="handleBuildingTagClick(building)"
                >
                  {{ building }}
                </el-tag>
                <el-tag
                    v-if="buildingNoList.length === 0"
                    size="small"
                    type="info"
                >
                  暂无楼栋数据
                </el-tag>
              </div>
            </el-form-item>
            <el-form-item label="房间号">
              <el-input
                  v-model="generateForm.roomNos"
                  placeholder="请输入房间号，多个房间用逗号分隔（如：1001,1002）"
                  clearable
                  :rows="3"
                  type="textarea"
              >
                <template #append>
                  <el-button
                      icon="el-icon-refresh"
                      @click="fetchRoomNos"
                      :disabled="!generateForm.buildingNo"
                  ></el-button>
                </template>
              </el-input>
              <div style="margin-top: 8px; margin-bottom: 8px; display: block; clear: both;">
                <el-tag
                    v-for="room in roomNoList"
                    :key="room"
                    size="small"                    style="margin-right: 8px; margin-bottom: 8px; cursor: pointer; display: inline-block;"
                    type="success"
                    @click="addRoomNo(room)"
                >
                  {{ room }}
                </el-tag>
                <el-tag
                    v-if="roomNoList.length === 0 && generateForm.buildingNo"
                    size="small"
                    type="info"
                >
                  该楼栋暂无房间数据
                </el-tag>
                <el-tag
                    v-if="!generateForm.buildingNo"
                    size="small"
                    type="info"
                >
                  请先选择楼栋号
                </el-tag>
              </div>
              <div style="width: 100%; font-size: 12px; color: #909399; line-height: 1.5; margin-top: 8px; padding-top: 5px; border-top: 1px solid #e4e7ed;">
                提示：为空则生成该楼栋所有房间，多个房间号用英文逗号分隔，点击房间号快速添加
              </div>
            </el-form-item>
            <el-form-item label="收费项目" required>
              <el-select
                  v-model="generateForm.feeTypeId"
                  placeholder="请选择收费项目"                  style="width: 100%;"
                  @change="handleFeeTypeChange"
                  :key="feeTypeListKey"
              >
                <el-option
                    v-for="type in feeTypeList"
                    :key="type.id"
                    :label="type.typeName"
                    :value="type.id"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="计费类型">
              <el-input
                  v-model="generateForm.displayCalcType"
                  disabled
                  placeholder="-"                  style="width: 100%;"
              ></el-input>
            </el-form-item>
            <el-form-item label="关联字段" v-if="generateForm.calcType === '1'">
              <div style="display: flex; flex-wrap: wrap; gap: 15px;">
                <el-tag
                    v-for="field in generateForm.displayRelatedFields"
                    :key="field"
                    size="normal"
                >
                  {{ getFieldLabel(field) }}
                </el-tag>
              </div>
            </el-form-item>
            <el-form-item label="单价（元/单位）" v-if="generateForm.calcType === '1'">
              <el-input
                  v-model="generateForm.unitPrice"
                  type="number"
                  placeholder="-"
                  :disabled="true"                  style="width: 100%;"
              >
                <template #append>
                  <div style="padding: 0 10px; color: #909399;">
                    {{ selectedFeeTypeName }}
                  </div>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="金额（元）" v-if="generateForm.calcType === '2'">
              <el-input
                  v-model="generateForm.displayAmount"
                  disabled
                  placeholder="-"                  style="width: 100%;"
              >
                <template #append>
                  <div style="padding: 0 10px; color: #909399;">
                    {{ selectedFeeTypeName }}
                  </div>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="截止日期" required>
              <el-date-picker
                  v-model="generateForm.dueDate"
                  type="date"
                  placeholder="请选择截止日期"
                  value-format="YYYY-MM-DD"                  style="width: 100%;"
              ></el-date-picker>
            </el-form-item>
            <el-form-item label="备注">
              <el-input
                  v-model="generateForm.remark"
                  type="textarea"
                  placeholder="选填，补充说明"
                  :rows="3"
              ></el-input>
            </el-form-item>

            <!-- 预览区域 -->
            <el-form-item label="预览">
              <div class="preview-box" v-if="previewData.length > 0">
                <el-table :data="previewData" border stripe size="small" max-height="300">
                  <el-table-column prop="roomNo" label="房号" width="120"></el-table-column>
                  <el-table-column prop="ownerName" label="业主姓名" width="100"></el-table-column>
                  <el-table-column prop="area" label="面积（㎡）" width="100"></el-table-column>
                  <el-table-column prop="calculatedAmount" label="计算金额（元）" width="120"></el-table-column>
                  <el-table-column prop="remark" label="备注"></el-table-column>
                </el-table>
                <div class="preview-summary">
                  <span>预计生成 <strong>{{ previewData.length }}</strong> 条账单</span>
                  <span style="margin-left: 20px;">
                    总金额：<strong style="color: #67c23a;">¥{{ totalAmount }}</strong>
                  </span>
                </div>
              </div>
              <div v-else class="preview-empty">
                <el-empty description="请先选择收费项目和计费类型，点击预览查看结果"></el-empty>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handlePreview" :loading="previewLoading">
                预览
              </el-button>
              <el-button type="success" @click="handleGenerateFee" :loading="generating" :disabled="previewData.length === 0">
                确认生成
              </el-button>
              <el-button @click="resetGenerateForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>

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
                  size="small"
                  style="width: 150px;"
              ></el-input>
            </div>
          </div>
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
        <el-button type="primary" @click="handleAddFeeType">确认新增</el-button>
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
                  size="small"
                  style="width: 150px;"
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

    <!-- 催缴操作弹窗 -->
    <el-dialog
        title="发送催缴通知"
        v-model="showReminderDialog"
        width="500px"
        @close="resetReminderForm"
    >
      <div v-if="currentFeeRecord" class="reminder-info">
        <!-- 费用信息卡片 -->
        <el-card shadow="never" style="margin-bottom: 16px; background-color: #f5f7fa;">
          <template #header>
            <div style="font-weight: 600; color: #303133;">
              <el-icon><Warning /></el-icon>
              费用信息
            </div>
          </template>
          <el-descriptions :column="2" size="small">
            <el-descriptions-item label="房号">{{ currentFeeRecord.roomNo }}</el-descriptions-item>
            <el-descriptions-item label="业主">{{ currentFeeRecord.ownerName }}</el-descriptions-item>
            <el-descriptions-item label="收费项目">{{ currentFeeRecord.feeTypeName }}</el-descriptions-item>
            <el-descriptions-item label="应收金额">
              <span style="color: #f56c6c; font-weight: 600;">¥{{ currentFeeRecord.amount }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="截止日期">{{ currentFeeRecord.dueDate }}</el-descriptions-item>
            <el-descriptions-item label="逾期天数" v-if="currentFeeRecord.overdueDays">
              <el-tag type="danger" size="small">{{ currentFeeRecord.overdueDays }}天</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 催缴设置 -->
        <el-form :model="reminderForm" label-width="100px">
          <el-form-item label="催缴方式" required>
            <el-radio-group v-model="reminderForm.reminderType" @change="handleReminderTypeChange">
              <el-radio label="2">
                <el-icon><ChatDotRound /></el-icon>
                短信通知
              </el-radio>
              <el-radio label="1">
                <el-icon><Message /></el-icon>
                模板消息
              </el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="催缴备注">
            <el-input
                v-model="reminderForm.remark"
                type="textarea"
                :rows="3"
                placeholder="选填，可添加个性化催缴说明（如：请尽快缴纳物业费）"
                maxlength="200"
                show-word-limit
            ></el-input>
          </el-form-item>

          <!-- 提示信息 -->
          <el-alert
              title="温馨提示"
              type="info"
              :closable="false"
              show-icon
          >
            <template #default>
              <div style="font-size: 13px; line-height: 1.6;">
                • 系统将自动检查是否已存在催缴记录<br/>
                • 首次催缴将创建新记录并立即发送<br/>
                • 已有记录将直接重新发送通知
              </div>
            </template>
          </el-alert>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="showReminderDialog = false">取消</el-button>
        <el-button
            type="primary"
            @click="handleCreateReminder"
            :loading="sendingReminder"
        >
          <el-icon><Promotion /></el-icon>
          确认发送
        </el-button>
      </template>
    </el-dialog>

    <!-- 费用详情弹窗 -->
    <el-dialog
        title="费用记录详情"
        v-model="showDetailDialog"
        width="900px"
    >
      <div v-if="detailData.id">
        <el-descriptions :column="2" border>
          <!-- 1. 账单基础信息 -->
          <el-descriptions-item label="账单 ID" >{{ detailData.id }}</el-descriptions-item>
          <el-descriptions-item label="楼房号">{{ detailData.roomNo || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="业主姓名">{{ detailData.ownerName || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detailData.phone || '未知' }}</el-descriptions-item>

          <!-- 2. 费用周期信息 -->
          <el-descriptions-item label="收费项目">{{ detailData.feeTypeName || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="计费周期">{{ detailData.chargePeriod || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="房屋面积">{{ detailData.houseArea ? detailData.houseArea + '㎡' : '未知' }}</el-descriptions-item>
          <el-descriptions-item label="套内面积">{{ detailData.internalArea ? detailData.internalArea + '㎡' : '未知' }}</el-descriptions-item>
          <el-descriptions-item label="单价">{{ detailData.unitPrice ? '¥' + detailData.unitPrice.toFixed(2) + '/㎡' : '未知' }}</el-descriptions-item>
          <el-descriptions-item label="截止日期">{{ detailData.dueDate || '未知' }}</el-descriptions-item>

          <!-- 3. 费用金额信息 -->
          <el-descriptions-item label="应收金额" :span="2">¥{{ detailData.amount ? detailData.amount.toFixed(2) : '0.00' }}</el-descriptions-item>
          <el-descriptions-item label="减免金额" :span="2">¥{{ detailData.discountAmount ? detailData.discountAmount.toFixed(2) : '0.00' }}</el-descriptions-item>
          <el-descriptions-item label="滞纳金" :span="2">¥{{ detailData.lateFee ? detailData.lateFee.toFixed(2) : '0.00' }}</el-descriptions-item>
          <el-descriptions-item label="实收金额" :span="2" style="font-weight: bold; color: #67c23a;">
            ¥{{ detailData.actualAmount ? detailData.actualAmount.toFixed(2) : '0.00' }}
          </el-descriptions-item>
          <el-descriptions-item label="欠费金额" :span="2" style="font-weight: bold; color: #f56c6c;">
            ¥{{ detailData.arrearsAmount ? detailData.arrearsAmount.toFixed(2) : '0.00' }}
          </el-descriptions-item>

          <!-- 4. 缴费状态信息 -->
          <el-descriptions-item label="缴费状态">
            <el-tag v-if="detailData.statusDesc === '已缴费'" type="success">
              {{ detailData.statusDesc }}
            </el-tag>
            <el-tag v-else-if="detailData.statusDesc === '待缴费'" type="warning">
              {{ detailData.statusDesc }}
            </el-tag>
            <el-tag v-else-if="detailData.statusDesc === '已撤销'" type="info">
              {{ detailData.statusDesc }}
            </el-tag>
            <el-tag v-else type="danger">
              {{ detailData.statusDesc }}{{ detailData.overdueDays ? '（' + detailData.overdueDays + '天）' : '' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="缴费方式">{{ detailData.payType || '未缴费' }}</el-descriptions-item>
          <el-descriptions-item label="缴费时间" v-if="detailData.payTime">
            {{ detailData.payTime.replace('T', ' ') }}
          </el-descriptions-item>
          <el-descriptions-item label="缴费流水号" v-if="detailData.transactionNo">
            {{ detailData.transactionNo }}
          </el-descriptions-item>
          <el-descriptions-item label="核销人员" v-if="detailData.operatorName">
            {{ detailData.operatorName }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2" v-if="detailData.remark">
            {{ detailData.remark }}
          </el-descriptions-item>

          <!-- 5. 系统管理信息 -->
          <el-descriptions-item label="创建时间" :span="2" v-if="detailData.createTime">
            {{ detailData.createTime.replace('T', ' ') }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2" v-if="detailData.updateTime">
            {{ detailData.updateTime.replace('T', ' ') }}
          </el-descriptions-item>
          <el-descriptions-item label="最新催缴" :span="2">
            {{ detailData.latestReminder || '无' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 欠费风险预测模块 -->
        <div class="risk-section" v-if="showRiskAssessment && riskData">
          <el-divider content-position="left">
            <el-icon><Warning /></el-icon>
            欠费风险预测
          </el-divider>

          <!-- 风险等级卡片 -->
          <el-card shadow="hover" class="risk-level-card">
            <div class="risk-header">
              <div class="risk-badge" :class="'risk-' + riskData.riskLevel.toLowerCase()">
                {{ getRiskLevelText(riskData.riskLevel) }}
              </div>
              <div class="risk-score">
                风险评分：<strong>{{ riskData.riskScore }}</strong> / 100
              </div>
            </div>
            <el-alert
                :title="riskData.suggestion"
                :type="getRiskAlertType(riskData.riskLevel)"
                show-icon
                :closable="false"
                class="risk-suggestion"
            />
          </el-card>

          <!-- 详细指标 -->
          <el-row :gutter="15" class="risk-indicators">
            <el-col :span="6">
              <el-card shadow="hover" class="indicator-card">
                <div class="indicator-label">缴费及时性</div>
                <div class="indicator-value" :style="{ color: getRiskColor(riskData.indicators.paymentTimeliness) }">
                  {{ riskData.indicators.paymentTimeliness }}
                </div>
                <el-progress
                    :percentage="riskData.indicators.paymentTimeliness"
                    :color="getRiskProgressColor(riskData.indicators.paymentTimeliness)"
                    :stroke-width="8"
                    :show-text="false"
                />
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="indicator-card">
                <div class="indicator-label">欠费频率</div>
                <div class="indicator-value" :style="{ color: getRiskColor(riskData.indicators.arrearsFrequency) }">
                  {{ riskData.indicators.arrearsFrequency }}
                </div>
                <el-progress
                    :percentage="riskData.indicators.arrearsFrequency"
                    :color="getRiskProgressColor(riskData.indicators.arrearsFrequency)"
                    :stroke-width="8"
                    :show-text="false"
                />
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="indicator-card">
                <div class="indicator-label">逾期天数</div>
                <div class="indicator-value" :style="{ color: getRiskColor(riskData.indicators.overdueDays) }">
                  {{ riskData.indicators.overdueDays }}
                </div>
                <el-progress
                    :percentage="riskData.indicators.overdueDays"
                    :color="getRiskProgressColor(riskData.indicators.overdueDays)"
                    :stroke-width="8"
                    :show-text="false"
                />
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="indicator-card">
                <div class="indicator-label">金额波动</div>
                <div class="indicator-value" :style="{ color: getRiskColor(riskData.indicators.amountTrend) }">
                  {{ riskData.indicators.amountTrend }}
                </div>
                <el-progress
                    :percentage="riskData.indicators.amountTrend"
                    :color="getRiskProgressColor(riskData.indicators.amountTrend)"
                    :stroke-width="8"
                    :show-text="false"
                />
              </el-card>
            </el-col>
          </el-row>

          <!-- 当前欠费统计 -->
          <el-descriptions :column="3" border class="current-arrears">
            <el-descriptions-item label="当前欠费笔数">
              <strong style="color: #f56c6c;">{{ riskData.currentUnpaidCount }}</strong> 笔
            </el-descriptions-item>
            <el-descriptions-item label="当前欠费金额">
              <strong style="color: #f56c6c;">¥{{ riskData.currentUnpaidAmount ? riskData.currentUnpaidAmount.toFixed(2) : '0.00' }}</strong>
            </el-descriptions-item>
            <el-descriptions-item label="历史记录总数">
              {{ riskData.totalRecords }} 笔
            </el-descriptions-item>
          </el-descriptions>

          <!-- 历史缴费统计 -->
          <el-descriptions :column="2" border class="history-stats">
            <el-descriptions-item label="已缴费次数">
              <span style="color: #67c23a;">{{ riskData.paidCount }}</span> 次
            </el-descriptions-item>
            <el-descriptions-item label="未缴费次数">
              <span style="color: #e6a23c;">{{ riskData.unpaidCount }}</span> 次
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <template #footer>
        <el-button
            v-if="detailData.statusDesc !== '已缴费' && detailData.statusDesc !== '已撤销'"
            type="success"
            @click="handleWriteOff(detailData)"
        >
          核销账单
        </el-button>
        <el-button
            v-if="detailData.statusDesc !== '已缴费' && detailData.statusDesc !== '已撤销'"
            type="warning"
            @click="handleSendReminder(detailData)"
        >
          发送催缴通知
        </el-button>
        <el-button
            type="primary"
            @click="loadRiskAssessment"
            v-if="!showRiskAssessment"
        >
          查看风险评估
        </el-button>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning, Message, ChatDotRound, Promotion } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

// 导入物业端接口
import {
  queryAdminFee,
  addFeeType,
  batchGenerateFee,
  writeOffFee,
  batchWriteOffFee,
  createReminder,
  sendReminder,
  getFeeTypes,
  deleteFeeType,
  updateFeeType,
  exportFeeRecords,
  getOwnerArrearsRisk
} from '@/api/feeApi.js'

// 路由实例
const router = useRouter()

const feeTypeListKey = ref(1);

// 解析管理员ID
let adminId = 0
const userInfo = localStorage.getItem('user')
if (userInfo) {
  try {
    const parsedUser = JSON.parse(userInfo)
    adminId = Number(parsedUser.id) || 0
    console.log('解析到的管理员ID：', adminId)
  } catch (e) {
    console.error('解析用户信息失败:', e)
    adminId = 1
  }
} else {
  adminId = 1
}

// 获取用户角色
const userRole = localStorage.getItem('userRole') || ''

// 标签页激活状态
const activeTab = ref('feeList')

// 批量生成账单相关
const generating = ref(false)
const previewLoading = ref(false)
const previewData = ref([])
const totalAmount = ref('0.00')
const selectedFeeTypeName = ref('')

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

// 楼栋号列表
const buildingNoList = ref([])

// 房间号列表
const roomNoList = ref([])

// 添加房间号到输入框
const addRoomNo = (room) => {
  if (!generateForm.roomNos) {
    generateForm.roomNos = room
  } else {
    // 检查是否已存在
    const currentRooms = generateForm.roomNos.split(',').map(r => r.trim()).filter(r => r)
    if (!currentRooms.includes(room)) {
      generateForm.roomNos += ',' + room
    } else {
      ElMessage.info('房间号已添加')
    }
  }
}

// 处理楼栋号变化
const handleBuildingNoChange = () => {
  // 清空房间号
  generateForm.roomNos = ''
  // 清空房间号列表
  roomNoList.value = []
  // 获取新楼栋的房间号
  fetchRoomNos()
}

// 处理楼栋号标签点击
const handleBuildingTagClick = (building) => {
  // 如果点击的是当前已选中的楼栋，不做任何操作
  if (generateForm.buildingNo === building) {
    return
  }
  // 清空房间号
  generateForm.roomNos = ''
  // 设置新楼栋号
  generateForm.buildingNo = building
  // 清空房间号列表
  roomNoList.value = []
  // 获取新楼栋的房间号
  fetchRoomNos()
}

// 获取所有楼栋号
const fetchBuildingNos = async () => {
  try {
    const res = await queryAdminFee({
      adminId: adminId,
      pageNum: 1,
      pageSize: 1000,
      keyword: ''
    })

    if (res.data && res.data.code === 200 && res.data.data) {
      const allRecords = res.data.data.records || []
      // 提取所有楼栋号并去重
      const buildingSet = new Set()
      allRecords.forEach(record => {
        if (record.buildingNo) {
          buildingSet.add(record.buildingNo)
        }
      })
      buildingNoList.value = Array.from(buildingSet).sort()
    }
  } catch (error) {
    console.error('获取楼栋号失败:', error)
  }
}

// 根据楼栋号获取房间号
const fetchRoomNos = async () => {
  if (!generateForm.buildingNo || !generateForm.buildingNo.trim()) {
    roomNoList.value = []
    return
  }

  try {
    const res = await queryAdminFee({
      adminId: adminId,
      pageNum: 1,
      pageSize: 1000,
      keyword: generateForm.buildingNo
    })

    if (res.data && res.data.code === 200 && res.data.data) {
      const allRecords = res.data.data.records || []
      // 提取该楼栋的所有房间号并去重
      const roomSet = new Set()
      allRecords.forEach(record => {
        if (record.buildingNo === generateForm.buildingNo && record.roomNo) {
          // 提取房间号部分（去掉楼栋号前缀）
          let room = record.roomNo
          if (room.startsWith(generateForm.buildingNo + '-')) {
            room = room.substring(generateForm.buildingNo.length + 1)
          }
          roomSet.add(room)
        }
      })
      roomNoList.value = Array.from(roomSet).sort()
    }
  } catch (error) {
    console.error('获取房间号失败:', error)
    roomNoList.value = []
  }
}

// 费用查询相关
const loading = ref(false)
const searchForm = reactive({
  adminId: adminId,
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  typeName: '',
  status: '',
  startDueDate: '',
  endDueDate: '',
  overdueStatus: '',
  minAmount: '',
  maxAmount: ''
})

const dateRange = ref([])
const feeList = ref([])
const total = ref(0)
const selectedFeeIds = ref([])

// 费用类型相关
const feeTypeList = ref([])
const feeTypePagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

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

// 新增：处理下拉框可见性变化
const handleSelectVisibleChange = (visible) => {
  if (!visible) {
    // 下拉框关闭时强制刷新显示
    nextTick(() => {
      const currentVal = searchForm.typeName;
      // 临时清空再设置，强制组件重新渲染
      searchForm.typeName = '';
      nextTick(() => {
        searchForm.typeName = currentVal;
      });
    });
  }
};

// 保持现有的 handleFeeTypeModelUpdate 方法
const handleFeeTypeModelUpdate = (value) => {
  console.log('模型更新 - 选择的费用类型:', value);
  searchForm.typeName = value === null || value === undefined ? '' : String(value).trim();
};



// 弹窗控制
const showAddTypeDialog = ref(false)
const showEditDialog = ref(false)
const showViewDialog = ref(false)

// 查看数据
const viewData = ref(null)

// 编辑表单
const editForm = reactive({
  id: null,
  typeName: '',
  remark: '',
  unitPrice: '',
  calcType: '1',
  relatedFields: ['HOUSE_AREA'],
  conditionType: 'NONE',
  conditionField: '',
  conditionValue: ''
})

// 生成账单表单
const generateForm = reactive({
  buildingNo: '',
  roomNos: '',
  roomNo: '',
  feeTypeId: '',
  calcType: '',
  relatedFields: [],
  displayCalcType: '',
  displayRelatedFields: [],
  unitPrice: '',
  displayAmount: '',
  amount: '',
  remark: '',
  dueDate: ''
})

// 控制是否显示金额输入框
const showAmountInput = ref(true)

// 催缴相关
const showReminderDialog = ref(false)
const sendingReminder = ref(false) // 发送中状态
const currentFeeRecord = ref(null) // 当前催缴的费用记录
const reminderForm = reactive({
  reminderType: '2',
  remark: '',
  targetFeeId: ''
})
// 详情弹窗控制
const showDetailDialog = ref(false)
const detailData = reactive({})
const showRiskAssessment = ref(false)
const riskData = ref(null)

// 获取物业端费用列表
const fetchAdminFeeList = async () => {
  loading.value = true
  try {
    // 处理日期范围
    if (dateRange.value.length) {
      searchForm.startDueDate = dateRange.value[0]
      searchForm.endDueDate = dateRange.value[1]
    } else {
      searchForm.startDueDate = ''
      searchForm.endDueDate = ''
    }

    console.log('请求参数（含adminId）:', searchForm)
    const res = await queryAdminFee(searchForm)

    console.log('完整接口响应:', res)
    console.log('后端返回的核心数据:', res.data?.data)

    // 清空旧数据
    feeList.value = []
    total.value = 0

    if (res.data && res.data.code === 200 && res.data.data) {
      feeList.value = res.data.data.records || []
      total.value = res.data.data.total || 0
      // 处理催缴字段映射和空值
      feeList.value.forEach(item => {
        if (!item.latestReminder && item.reminderInfo) {
          item.latestReminder = item.reminderInfo
        }
        item.latestReminder = item.latestReminder || '无'
      })
    } else {
      ElMessage.warning('数据格式异常，未能加载费用列表')
    }
  } catch (error) {
    console.error('加载费用列表失败:', error)
    const errorMsg = error.response?.data?.msg || '加载费用列表失败'
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}

// 重置搜索表单
const resetSearchForm = () => {
  searchForm.keyword = ''
  searchForm.typeName = ''
  searchForm.status = ''
  searchForm.overdueStatus = ''
  searchForm.minAmount = ''
  searchForm.maxAmount = ''
  dateRange.value = []
  searchForm.pageNum = 1
  searchForm.adminId = adminId
  fetchAdminFeeList()
}

// 表格选中事件
const handleSelectionChange = (val) => {
  selectedFeeIds.value = val.map(item => item.id)
}

// 标签页切换
const handleTabChange = (tab) => {
  if (tab === 'feeType') {
    // 可补充加载费用类型列表接口
  }
}

// 处理费用类型选择变化
const handleFeeTypeChange = (feeTypeId) => {
  const selectedType = feeTypeList.value.find(type => type.id === feeTypeId)
  if (selectedType) {
    selectedFeeTypeName.value = selectedType.typeName
    // 自动填充单价和计费类型
    if (selectedType.unitPrice) {
      generateForm.unitPrice = selectedType.unitPrice.toString()
      generateForm.displayAmount = selectedType.unitPrice.toString()
    }
    if (selectedType.calcType) {
      generateForm.calcType = selectedType.calcType.toString()
      // 设置显示的计费类型
      generateForm.displayCalcType = selectedType.calcType === '1' ? '关联计算' : '固定金额'
    }
    // 解析关联字段
    if (selectedType.relatedField) {
      if (selectedType.relatedField.includes(',')) {
        generateForm.relatedFields = selectedType.relatedField.split(',')
        generateForm.displayRelatedFields = selectedType.relatedField.split(',')
      } else {
        generateForm.relatedFields = [selectedType.relatedField]
        generateForm.displayRelatedFields = [selectedType.relatedField]
      }
    } else {
      generateForm.relatedFields = []
      generateForm.displayRelatedFields = []
    }
    handleCalcTypeChange()
  } else {
    selectedFeeTypeName.value = ''
    generateForm.unitPrice = ''
    generateForm.displayAmount = ''
    generateForm.displayCalcType = ''
    generateForm.displayRelatedFields = []
  }
}

// 处理计费类型选择变化
const handleCalcTypeChange = () => {
  // 此函数现在不需要了，因为计费类型和金额都是自动读取的
  console.log('计费类型:', generateForm.calcType)
}

// 预览数据
const handlePreview = async () => {
  if (!generateForm.feeTypeId) {
    ElMessage.warning('请选择收费项目')
    return
  }
  if (!generateForm.calcType) {
    ElMessage.warning('计费类型无效')
    return
  }
  if (!generateForm.dueDate) {
    ElMessage.warning('请选择截止日期')
    return
  }
  if (generateForm.calcType === '2' && (!generateForm.displayAmount || Number(generateForm.displayAmount) <= 0)) {
    ElMessage.warning('费用类型配置的金额无效')
    return
  }

  previewLoading.value = true
  try {
    // 批量生成账单的预览：查询所有相关费用记录
    const searchParams = {
      adminId: adminId,
      pageNum: 1,
      pageSize: 1000,
      keyword: ''  // 不使用 keyword，直接查询所有
    }

    // 楼栋号校验
    if (!generateForm.buildingNo || !generateForm.buildingNo.trim()) {
      ElMessage.warning('请输入楼栋号')
      previewLoading.value = false
      return
    }

    // 解析多个房间号
    let targetRooms = []
    if (generateForm.roomNos && generateForm.roomNos.trim()) {
      // 支持英文逗号和中文逗号分隔
      targetRooms = generateForm.roomNos.split(/[,,]/)
          .map(room => room.trim())
          .filter(room => room.length > 0)
    }

    const res = await queryAdminFee(searchParams)

    if (res.data && res.data.code === 200 && res.data.data) {
      let allRecords = res.data.data.records || []

      // 前端过滤：根据楼栋号和房间号
      allRecords = allRecords.filter(record => {
        // 先匹配楼栋号
        const matchBuilding = record.roomNo && record.roomNo.startsWith(generateForm.buildingNo)
        if (!matchBuilding) return false

        // 如果没有指定房间号，返回该楼栋所有房间
        if (targetRooms.length === 0) {
          return true
        }

        // 匹配指定的房间号（支持完整房号匹配）
        const matchRoom = targetRooms.some(room => {
          return record.roomNo === room ||
              record.roomNo === generateForm.buildingNo + '-' + room ||
              record.roomNo.endsWith('-' + room)
        })

        return matchRoom
      })

      // 获取选中的费用类型
      const selectedType = feeTypeList.value.find(type => type.id === generateForm.feeTypeId)

      // 生成预览数据
      previewData.value = []
      let total = 0

      // 去重：按房号分组
      const roomRecordMap = {}
      allRecords.forEach(record => {
        const roomKey = record.roomNo
        if (!roomRecordMap[roomKey]) {
          roomRecordMap[roomKey] = record
        }
      })

      Object.values(roomRecordMap).forEach(record => {
        // 计算金额
        let amount = 0
        let factor = 0  // 计费因子（面积、数量等）
        let factorStr = ''

        if (generateForm.calcType === '1' && selectedType && selectedType.unitPrice) {
          // 先判断是否满足计费条件
          let shouldCharge = true
          let conditionDesc = ''

          if (selectedType.conditionType && selectedType.conditionType !== 'NONE' &&
              selectedType.conditionField && selectedType.conditionValue) {
            const fieldValue = record[selectedType.conditionField.toLowerCase()]
            const conditionValue = selectedType.conditionValue

            if (selectedType.conditionType === 'EQUAL') {
              shouldCharge = (fieldValue === conditionValue)
              conditionDesc = `${selectedType.conditionField}=${conditionValue}`
            } else if (selectedType.conditionType === 'NOT_EQUAL') {
              shouldCharge = (fieldValue !== conditionValue)
              conditionDesc = `${selectedType.conditionField}≠${conditionValue}`
            }
          }

          if (shouldCharge) {
            // 关联计算：支持多个字段组合
            const relatedFields = generateForm.relatedFields || []
            const factors = []

            relatedFields.forEach(field => {
              switch(field) {
                case 'HOUSE_AREA':
                  if (record.houseArea) {
                    factors.push(parseFloat(record.houseArea))
                    factors.push(`建面:${record.houseArea}㎡`)
                  }
                  break
                case 'INTERNAL_AREA':
                  if (record.internalArea) {
                    factors.push(parseFloat(record.internalArea))
                    factors.push(`套内:${record.internalArea}㎡`)
                  }
                  break
                case 'HOUSE_LAYOUT':
                  if (record.houseLayout) {
                    factors.push(1)  // 户型作为系数
                    factors.push(`户型:${record.houseLayout}`)
                  }
                  break
                case 'HOUSE_USAGE':
                  if (record.houseUsage) {
                    factors.push(1)  // 用途作为系数
                    factors.push(`用途:${record.houseUsage}`)
                  }
                  break
                case 'HOUSE_STRUCTURE':
                  if (record.houseStructure) {
                    factors.push(1)  // 结构作为系数
                    factors.push(`结构:${record.houseStructure}`)
                  }
                  break
                case 'HAS_PARKING_SPACE':
                  if (record.hasParkingSpace) {
                    factors.push(1)  // 车位作为系数
                    factors.push(`车位:有`)
                  }
                  break
                case 'PARKING_SPACE_TYPE':
                  if (record.parkingSpaceType) {
                    factors.push(1)  // 车位类型作为系数
                    const typeText = record.parkingSpaceType === '1' ? '产权' : record.parkingSpaceType === '2' ? '租赁' : record.parkingSpaceType
                    factors.push(`车位类型:${typeText}`)
                  }
                  break
                case 'HAS_STORAGE_ROOM':
                  if (record.hasStorageRoom) {
                    factors.push(1)  // 储藏室作为系数
                    factors.push(`储藏室:有`)
                  }
                  break
              }
            })

            // 计算因子：如果有面积字段，取面积之和；否则取系数乘积
            const areaFactors = factors.filter((_, index) => index % 2 === 0)
            factor = areaFactors.length > 0 ? areaFactors.reduce((sum, val) => sum + val, 0) : factors.reduce((prod, val) => prod * val, 1)

            factorStr = factors.filter((_, index) => index % 2 === 1).join(', ') || '未知'
            if (conditionDesc) {
              factorStr = `${factorStr} [${conditionDesc}]`
            }
            amount = factor * parseFloat(selectedType.unitPrice)
          } else {
            // 不满足条件，不收费
            amount = 0
            factorStr = `不满足计费条件${conditionDesc ? '：' + conditionDesc : ''}`
          }
        } else if (generateForm.calcType === '2' && selectedType && selectedType.unitPrice) {
          // 固定金额：直接使用费用类型的单价
          amount = parseFloat(selectedType.unitPrice)
          factorStr = `固定金额：¥${selectedType.unitPrice}`
        } else {
          amount = 0
          factorStr = '-'
        }

        amount = parseFloat(amount.toFixed(2))
        total += amount

        previewData.value.push({
          roomNo: record.roomNo,
          ownerName: record.ownerName || '未知',
          area: factorStr,
          calculatedAmount: '¥' + amount.toFixed(2),
          remark: generateForm.remark || '-'
        })
      })

      totalAmount.value = total.toFixed(2)

      if (previewData.value.length === 0) {
        ElMessage.warning('未找到符合条件的数据')
      }
    } else {
      previewData.value = []
      totalAmount.value = '0.00'
      ElMessage.warning('未找到符合条件的数据')
    }
  } catch (error) {
    console.error('预览失败:', error)
    ElMessage.error('预览失败，请稍后重试')
    previewData.value = []
    totalAmount.value = '0.00'
  } finally {
    previewLoading.value = false
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

// 重置生成账单表单
const resetGenerateForm = () => {
  generateForm.buildingNo = ''
  generateForm.roomNos = ''
  generateForm.roomNo = ''
  generateForm.feeTypeId = ''
  generateForm.calcType = ''
  generateForm.relatedFields = []
  generateForm.displayCalcType = ''
  generateForm.displayRelatedFields = []
  generateForm.unitPrice = ''
  generateForm.displayAmount = ''
  generateForm.amount = ''
  generateForm.remark = ''
  generateForm.dueDate = ''
  previewData.value = []
  totalAmount.value = '0.00'
  selectedFeeTypeName.value = ''
}

// 批量生成账单
// 批量生成账单
const handleGenerateFee = async () => {
  if (!generateForm.feeTypeId) {
    ElMessage.warning('请选择收费项目')
    return
  }
  if (!generateForm.calcType) {
    ElMessage.warning('请选择计费类型')
    return
  }
  if (!generateForm.dueDate) {
    ElMessage.warning('请选择截止日期')
    return
  }
  if (previewData.value.length === 0) {
    ElMessage.warning('请先预览数据')
    return
  }

  try {
    generating.value = true
    // 构造请求数据
    const requestData = {
      adminId: adminId,
      buildingNo: generateForm.buildingNo,
      roomNos: generateForm.roomNos || undefined,  // 改为传递 roomNos
      feeTypeId: Number(generateForm.feeTypeId),
      calcType: generateForm.calcType,
      relatedField: generateForm.relatedFields && generateForm.relatedFields.length > 0
          ? generateForm.relatedFields.join(',')
          : null,
      unitPrice: generateForm.unitPrice ? Number(generateForm.unitPrice) : null,
      amount: generateForm.displayAmount ? Number(generateForm.displayAmount) : null,
      conditionType: generateForm.conditionType || 'NONE',
      conditionField: generateForm.conditionField || null,
      conditionValue: generateForm.conditionValue || null,
      remark: generateForm.remark,
      dueDate: generateForm.dueDate
    }
    console.log('发送批量生成费用请求:', requestData)
    const res = await batchGenerateFee(requestData)
    console.log('批量生成费用响应:', res)
    if (res.data.code === 200) {
      ElMessage.success(res.data.msg || '账单添加成功')
      resetGenerateForm()
      // 切换到费用列表标签页并刷新
      activeTab.value = 'feeList'
      fetchAdminFeeList()
    } else {
      ElMessage.error(res.data?.msg || '操作失败')
    }
  } catch (err) {
    console.error('批量生成费用失败:', err)
    const errorMsg = err.response?.data?.msg || '生成账单失败'
    ElMessage.error(errorMsg)
  } finally {
    generating.value = false
  }
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
    console.log('addFeeType 响应:', res);
    console.log('res.code:', res.code);
    console.log('res.msg:', res.msg);
    console.log('res.data:', res.data);
    if (res.data && res.data.code === 200) {
      ElMessage.success(res.data.msg || '新增费用类型成功')
      // 强制重新渲染组件
      feeTypeListKey.value += 1;

      // 同时重新加载数据
      fetchFeeTypes();

      // 关闭弹窗并重置表单
      showAddTypeDialog.value = false
      resetAddTypeForm()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (err) {
    console.error('新增费用类型失败:', err)
    ElMessage.error('新增失败')
  }
}

// 新增获取费用类型列表的方法
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
      console.log('DEBUG - searchForm.typeName:', searchForm.typeName);
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

// 单个核销
const handleSingleWriteOff = async (feeRecordId) => {
  try {
    await ElMessageBox.confirm('确定核销该费用记录吗？', '提示', {
      type: 'warning'
    })
    const res = await writeOffFee({
      adminId: adminId,
      feeRecordId: feeRecordId
    })

    console.log('核销接口返回:', res)
    console.log('res.data:', res.data)
    console.log('res.data.code:', res.data?.code)

    if (res.data && res.data.code === 200) {
      ElMessage.success('核销成功')
      fetchAdminFeeList()
    } else {
      ElMessage.error(res.data?.msg || '核销失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('核销失败详情:', error)
      const errorMsg = error.response?.data?.msg || error.msg || '核销失败，请稍后重试'
      ElMessage.error(errorMsg)
    }
  }
}

// 批量核销
const handleBatchWriteOff = async () => {
  try {
    await ElMessageBox.confirm(
        `确认核销选中的${selectedFeeIds.value.length}条费用记录吗？`,
        '提示',
        { type: 'warning' }
    )
    const res = await batchWriteOffFee({
      adminId: adminId,
      feeRecordIds: selectedFeeIds.value
    })
    if (res.data.code === 200) {
      ElMessage.success(res.data.msg)
      fetchAdminFeeList()
    } else {
      ElMessage.error(res.data.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || '批量核销失败，请稍后重试'
      ElMessage.error(errorMsg)
    }
  }
}

// 重置催缴表单
const resetReminderForm = () => {
  reminderForm.reminderType = '2'
  reminderForm.remark = ''
  reminderForm.targetFeeId = ''
  currentFeeRecord.value = null
}

// 处理催缴方式变化
const handleReminderTypeChange = (value) => {
  if (value === '1' && !reminderForm.remark) {
    // 选择模板消息且备注为空时，自动填充默认提示
    reminderForm.remark = `尊敬的业主，您的${currentFeeRecord.value?.feeTypeName || '费用'}账单已到期，请尽快缴纳。如有疑问请联系物业服务中心。`
  } else if (value === '2' && reminderForm.remark.includes('模板消息')) {
    // 切换到短信时，清空模板消息的默认文本
    reminderForm.remark = ''
  }
}

// 单个催缴弹窗
const handleSingleReminder = (feeRecordId) => {
  // 从列表中查找对应的费用记录
  const record = feeList.value.find(item => item.id === feeRecordId)
  if (record) {
    currentFeeRecord.value = record
    reminderForm.targetFeeId = feeRecordId
    // 重置备注，让handleReminderTypeChange自动填充
    reminderForm.remark = ''
    showReminderDialog.value = true
  } else {
    ElMessage.error('未找到费用记录')
  }
}

// 批量催缴
const handleBatchReminder = async () => {
  try {
    await ElMessageBox.confirm(
        `确定为选中的${selectedFeeIds.value.length}条记录创建催缴吗？`,
        '提示',
        { type: 'warning' }
    )
    const promiseList = selectedFeeIds.value.map(id =>
        createReminder({
          adminId: adminId,
          feeRecordId: id,
          reminderType: 1
        })
    )
    const results = await Promise.all(promiseList)
    const successCount = results.filter(res => res.data.code === 200).length

    ElMessage.success(`成功创建${successCount}条催缴记录`)
    setTimeout(() => {
      fetchAdminFeeList()
    }, 500)
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || '批量催缴失败，请稍后重试'
      ElMessage.error(errorMsg)
    }
  }
}

// 创建催缴记录并发送（核心：自动刷新）
const handleCreateReminder = async () => {
  try {
    sendingReminder.value = true

    const res = await createReminder({
      adminId: adminId,
      feeRecordId: reminderForm.targetFeeId,
      reminderType: Number(reminderForm.reminderType),
      remark: reminderForm.remark
    })

    if (res.data.code === 200) {
      ElMessage.success({
        message: '催缴通知发送成功',
        duration: 2000
      })
      showReminderDialog.value = false
      resetReminderForm()

      // 延迟刷新确保数据落地
      setTimeout(() => {
        fetchAdminFeeList()
      }, 500)
    } else {
      ElMessage.error(res.data.msg || '操作失败')
    }
  } catch (err) {
    console.error('创建催缴失败:', err)
    ElMessage.error(err.response?.data?.msg || '发送催缴通知失败')
  } finally {
    sendingReminder.value = false
  }
}

// 发送催缴通知
const handleSendReminder = async (feeData) => {
  // 设置当前费用记录
  currentFeeRecord.value = feeData
  reminderForm.targetFeeId = feeData.id
  // 重置备注
  reminderForm.remark = ''
  // 打开催缴弹窗
  showReminderDialog.value = true
}

// 查看详情
const handleViewDetail = async (row) => {
  Object.assign(detailData, row)
  showDetailDialog.value = true
  showRiskAssessment.value = false
  riskData.value = null
}

// 加载风险评估
const loadRiskAssessment = async () => {
  if (!detailData.ownerId) {
    ElMessage.warning('无法获取业主信息')
    return
  }

  try {
    const res = await getOwnerArrearsRisk(adminId, detailData.ownerId)
    if (res.data && res.data.code === 200) {
      riskData.value = res.data.data
      showRiskAssessment.value = true
      ElMessage.success('风险评估加载成功')
    } else {
      ElMessage.error(res.data?.msg || '风险评估失败')
    }
  } catch (error) {
    console.error('加载风险评估失败:', error)
    ElMessage.error(error.response?.data?.msg || '加载风险评估失败')
  }
}

// 获取风险等级文本
const getRiskLevelText = (level) => {
  const levelMap = {
    'HIGH': '高风险',
    'MEDIUM': '中等风险',
    'LOW': '低风险',
    'VERY_LOW': '优质业主'
  }
  return levelMap[level] || '未知'
}

// 获取风险警告类型
const getRiskAlertType = (level) => {
  const typeMap = {
    'HIGH': 'error',
    'MEDIUM': 'warning',
    'LOW': 'success',
    'VERY_LOW': 'success'
  }
  return typeMap[level] || 'info'
}

// 获取指标颜色
const getRiskColor = (riskScore) => {
  if (riskScore >= 80) return '#f56c6c' // 高风险-红色
  if (riskScore >= 60) return '#e6a23c' // 中风险-橙色
  if (riskScore >= 40) return '#67c23a' // 低风险-绿色
  return '#409eff' // 优质-蓝色
}

// 获取进度条颜色
const getRiskProgressColor = (riskScore) => {
  if (riskScore >= 80) return '#f56c6c'
  if (riskScore >= 60) return '#e6a23c'
  if (riskScore >= 40) return '#67c23a'
  return '#409eff'
}

// 核销账单
const handleWriteOff = async (feeData) => {
  try {
    await ElMessageBox.confirm('确定核销该账单吗？', '提示', {
      type: 'warning'
    })
    const res = await writeOffFee({
      adminId: adminId,
      feeRecordId: feeData.id
    })
    if (res.code === 200) {
      ElMessage.success('核销成功')
      showDetailDialog.value = false
      fetchAdminFeeList()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || '核销失败，请稍后重试'
      ElMessage.error(errorMsg)
    }
  }
}

// 导出 Excel
const handleExportExcel = async () => {
  try {
    // 检查是否有选中的记录
    if (selectedFeeIds.value.length === 0) {
      ElMessage.warning('请先选择要导出的费用记录')
      return
    }

    ElMessage.info(`正在导出 ${selectedFeeIds.value.length} 条记录...`)

    // 选中的记录 ID 列表
    const feeRecordIds = selectedFeeIds.value

    console.log('选中的费用记录 ID:', feeRecordIds)

    // 调用导出接口（传递 ID 列表）
    const exportParams = {
      adminId: adminId,
      feeRecordIds: feeRecordIds.join(',') // 转为逗号分隔的字符串
    }

    console.log('导出参数:', exportParams)

    // 调用导出接口
    const response = await exportFeeRecords(exportParams)

    // 创建下载链接
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url

    // 生成文件名：费用记录_年月日时分秒.xlsx
    const timestamp = new Date().getTime()
    link.download = `费用记录_${timestamp}.xlsx`

    // 触发下载
    link.click()

    // 释放 URL 对象
    window.URL.revokeObjectURL(url)

    ElMessage.success(`成功导出 ${selectedFeeIds.value.length} 条记录`)
  } catch (error) {
    console.error('导出 Excel 失败:', error)
    ElMessage.error('导出失败，请稍后重试')
  }
}

// 退出登录
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
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    ElMessage.success('退出登录成功')
    router.push('/login')
  }).catch(() => {
    ElMessage.info('已取消退出')
  })
}



// 页面初始化
onMounted(() => {
  if (adminId <= 0) {
    ElMessage.warning('未获取到管理员信息，请重新登录')
    return
  }
  fetchFeeTypes()
  fetchBuildingNos()
  fetchAdminFeeList()
})
</script>

<style scoped>
.admin-fee-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

/* 退出按钮样式 */
.header-actions {
  display: flex;
  align-items: center;
}
.logout-btn {
  margin-left: auto !important;
  margin-right: 0 !important;
}

.search-form {
  margin-bottom: 20px;
  padding: 10px;
  background: #fff;
  border-radius: 4px;
}

.batch-operation {
  margin-bottom: 20px;
}

.fee-table,
.fee-type-table {
  margin-top: 20px;
}

.fee-type-form {
  margin-bottom: 20px;
  padding: 10px;
  background: #fff;
  border-radius: 4px;
}

.batch-generate-form {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  max-width: 800px;
}

.preview-box {
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  background: #f5f7fa;
}

.preview-summary {
  margin-top: 10px;
  padding: 10px;
  background: #fff;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
}

.preview-empty {
  width: 100%;
  padding: 40px 0;
  background: #f5f7fa;
  border-radius: 4px;
}

.el-dialog__body {
  max-height: 400px;
  overflow-y: auto;
}

/* 欠费风险预测样式 */
.risk-section {
  margin-top: 25px;
}

.risk-level-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
}

.risk-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.risk-badge {
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 16px;
  font-weight: bold;
  color: white;
}

.risk-high {
  background: linear-gradient(135deg, #f56c6c 0%, #ff8787 100%);
}

.risk-medium {
  background: linear-gradient(135deg, #e6a23c 0%, #ffc069 100%);
}

.risk-low {
  background: linear-gradient(135deg, #67c23a 0%, #95de64 100%);
}

.risk-very_low {
  background: linear-gradient(135deg, #409eff 0%, #69c0ff 100%);
}

.risk-score {
  font-size: 18px;
  color: #303133;
}

.risk-score strong {
  font-size: 24px;
  color: #409eff;
}

.risk-suggestion {
  margin-top: 10px;
}

.risk-indicators {
  margin: 20px 0;
}

.indicator-card {
  text-align: center;
  min-height: 120px;
  transition: all 0.3s;
}

.indicator-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.indicator-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.indicator-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 10px;
}

.current-arrears,
.history-stats {
  margin-top: 15px;
}
/* 催缴弹窗样式优化 */
.reminder-info {
  padding: 0 10px;
}

.reminder-info :deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid #e4e7ed;
}

.reminder-info :deep(.el-descriptions__label) {
  font-weight: 500;
  color: #606266;
}

.reminder-info :deep(.el-radio) {
  margin-right: 20px;
  height: auto;
}

.reminder-info :deep(.el-radio__label) {
  display: flex;
  align-items: center;
  gap: 4px;
}

.reminder-info :deep(.el-alert) {
  margin-top: 16px;
}

</style>