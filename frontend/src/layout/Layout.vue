<!-- src/layout/Layout.vue -->
<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <el-menu default-active="/admin/OwnerList" router>
        <el-menu-item v-if="userRole === 1"  index="/admin/IndexList">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        <el-menu-item v-if="userRole === 3"  index="/admin/IndexList2">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        <el-menu-item v-if="userRole === 1"  index="/admin/PersonList">
          <el-icon><User /></el-icon>
          <template #title>个人中心</template>
        </el-menu-item>
        <el-menu-item v-if="userRole === 3"  index="/admin/PersonList2">
          <el-icon><User /></el-icon>
          <template #title>个人中心</template>
        </el-menu-item>
        <el-menu-item v-if="userRole === 3"   index="/admin/OwnerList">
          <el-icon><Avatar /></el-icon>
          <template #title>业主管理</template>
        </el-menu-item>
        <el-menu-item v-if="userRole === 3" index="/admin/StaffAdmin">
          <el-icon><UserFilled /></el-icon>
          <template #title>物业人员管理</template>
        </el-menu-item>
        <el-menu-item v-if="userRole === 1"   index="/admin/OwnerList">
          <el-icon><Avatar /></el-icon>
          <template #title>业主信息管理</template>
        </el-menu-item>
        <el-menu-item v-if="userRole === 3"  index="/admin/RegulationAdmin">
          <el-icon><Document /></el-icon>
          <template #title>规章制度管理</template>
        </el-menu-item>
        <el-menu-item v-if="userRole === 3"  index="/admin/EquipmentAdmin">
          <el-icon><Setting /></el-icon>
          <template #title>设备台账管理</template>
        </el-menu-item>
        <el-menu-item v-if="userRole === 1"  index="/admin/FeeAdmin">
          <el-icon><Money /></el-icon>
          <template #title>费用缴纳管理</template>
        </el-menu-item>
        <el-menu-item v-if="userRole === 3"  index="/admin/FeeTypeAdmin">
          <el-icon><Money /></el-icon>
          <template #title>统一定价管理</template>
        </el-menu-item>
        <el-menu-item v-if="userRole === 1" index="/admin/RepairAdmin">
          <el-icon><Tools /></el-icon>
          <template #title>报修流程管理</template>
        </el-menu-item>
        <el-menu-item v-if="userRole === 1"  index="/admin/SuggestionList">
          <el-icon><ChatDotRound /></el-icon>
          <template #title>投诉建议管理</template>
        </el-menu-item>
        <el-menu-item v-if="userRole === 1"  index="/admin/AnnouncementAdmin">
          <el-icon><Bell /></el-icon>
          <template #title>公告通知管理</template>
        </el-menu-item>
        <el-menu-item v-if="userRole === 1"  index="/admin/CarouselList">
          <el-icon><Picture /></el-icon>
          <template #title>轮播图片管理</template>
        </el-menu-item>
        <el-menu-item v-if="userRole === 1"  index="/admin/VisitorAdmin">
          <el-icon><User /></el-icon>
          <template #title>访客登记管理</template>
        </el-menu-item>
        <el-menu-item v-if="userRole === 1"  index="/admin/MessageList">
          <el-icon><ChatLineRound /></el-icon>
          <template #title>业主群聊管理</template>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  HomeFilled,
  User,
  UserFilled,
  Avatar,
  Document,
  Setting,
  Money,
  Tools,
  ChatDotRound,
  Bell,
  Picture,
  ChatLineRound
} from '@element-plus/icons-vue'

// 当前用户角色
const userRole = ref(null)

// 页面加载时获取用户角色
onMounted(() => {
  try {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      const user = JSON.parse(userStr)
      userRole.value = user.role
    }
  } catch (error) {
    console.error('获取用户角色失败:', error)
  }
})
</script>

<style scoped>
.layout-container {
  display: flex;
  //height: 100vh;
}
.sidebar {
  width: 200px;
  background-color: #ffffff; /* 核心修改：侧边栏背景改为白色 */
  border-right: 1px solid #e5e7eb; /* 可选：加右侧边框，区分主内容区 */
}
.main-content {
  flex: 1;
  padding: 0;
  overflow: auto;
}
/* 核心修改：调整菜单样式，适配白色背景 */
.el-menu {
  height: 100%;
  background-color: #ffffff !important; /* 菜单背景改为白色 */
  color: #333333; /* 菜单文字默认颜色 */
}
/* 菜单激活项样式（可选，提升视觉效果） */
.el-menu-item.is-active {
  background-color: #f0f9ff !important; /* 激活项浅蓝背景 */
  color: #1677ff !important; /* 激活项文字蓝色 */
}
/* 菜单 hover 效果（可选） */
.el-menu-item:hover {
  background-color: #f9fafb !important;
}
/* 图标颜色适配 */
:deep(.el-icon) {
  color: #666666;
}
.el-menu-item.is-active :deep(.el-icon) {
  color: #1677ff !important; /* 激活项图标蓝色 */
}
</style>