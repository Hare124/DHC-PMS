<!-- src/layout/home.vue -->
<template>
  <div class="owner-home-container">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ 'sidebar-collapse': isCollapse }">
      <div class="sidebar-header">
<!--        <h3 v-show="!isCollapse">DHC-PMS</h3>-->
        <div class="mode-switch-group">
          <el-button
              v-show="!isCollapse"
              class="mode-switch-btn"
              :class="{ 'is-simple-mode': !isStandardMode }"
              @click="toggleMode"
          >
            <el-icon><Sunny v-if="isStandardMode" /><Moon v-else /></el-icon>
            <span>{{ isStandardMode ? '简洁模式' : '标准模式' }}</span>
          </el-button>
          <el-button class="collapse-btn" @click="toggleSidebar">
            <el-icon><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
          </el-button>
        </div>
      </div>
      <el-menu
          default-active="/owner"
          router
          :collapse="isCollapse"
      >
        <el-menu-item :index="isStandardMode ? '/owner/OwnerIndex02' : '/owner/OwnerIndex'">
          <el-icon><House /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        <el-menu-item index="/owner/Person">
          <el-icon><Avatar /></el-icon>
          <template #title>个人中心</template>
        </el-menu-item>
<!--        <el-sub-menu index="/fee">-->
<!--          <template #title>-->
<!--            <el-icon><Money /></el-icon>-->
<!--            <span>生活缴费</span>-->
<!--          </template>-->
          <el-menu-item index="/owner/fee/list">
            <el-icon><List /></el-icon>
            <template #title>我的账单</template>
          </el-menu-item>
          <!--          <el-menu-item index="/owner/fee/payment">-->
          <!--            <el-icon><CreditCard /></el-icon>-->
          <!--            <template #title>在线缴费</template>-->
          <!--          </el-menu-item>-->
          <!--          <el-menu-item index="/owner/fee/voucher">-->
          <!--            <el-icon><Document /></el-icon>-->
          <!--            <template #title>缴费凭证</template>-->
          <!--          </el-menu-item>-->
<!--        </el-sub-menu>-->
        <el-menu-item index="/repair/list">
          <el-icon><Tools /></el-icon>
          <template #title>报修服务</template>
        </el-menu-item>
        <el-menu-item index="/announcement/list">
          <el-icon><Bell /></el-icon>
          <template #title>公告动态</template>
        </el-menu-item>
        <el-menu-item index="/visitor/list">
          <el-icon><User /></el-icon>
          <template #title>来访申请</template>
        </el-menu-item>
        <el-menu-item index="/owner/Suggestion">
          <el-icon><ChatLineRound /></el-icon>
          <template #title>提交建议</template>
        </el-menu-item>
        <el-menu-item index="/owner/Message">
          <el-icon><ChatDotRound /></el-icon>
          <template #title>业主群</template>
        </el-menu-item>
        <el-menu-item index="/owner/DeepSeek">
          <el-icon><Service /></el-icon>
          <template #title>在线客服</template>
        </el-menu-item>
        <el-menu-item index="/owner/FloorPlan">
          <el-icon><Picture /></el-icon>
          <template #title>小区平面图</template>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 页面内容 -->
      <div class="content-wrapper">
        <router-view v-if="$route.name !== 'OwnerHome'" />
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  House,
  Money,
  List,
  Tools,
  Notification,
  User,
  Fold,
  Expand,
  Avatar,
  ChatDotRound,
  Bell,
  Service,
  Picture,
  ChatLineRound,
  Sunny,
  Moon
} from '@element-plus/icons-vue'

// 侧边栏折叠状态
const isCollapse = ref(true)

// 模式切换状态（true=标准模式，false=简洁模式）
const isStandardMode = ref(localStorage.getItem('isStandardMode') !== 'false')


// 切换侧边栏折叠
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 切换模式
const toggleMode = () => {
  isStandardMode.value = !isStandardMode.value
  // 保存模式状态到localStorage
  localStorage.setItem('isStandardMode', isStandardMode.value ? 'true' : 'false')

  if (isStandardMode.value) {
    ElMessage.success('已切换到标准模式')
  } else {
    ElMessage.success('已切换到简洁模式')
  }
  // 跳转到对应的首页
  router.push(isStandardMode.value ? '/owner/OwnerIndex02' : '/owner/OwnerIndex')
}

const router = useRouter()
// 获取本地存储的 Token
const token = ref(localStorage.getItem('token') || '')
// 记录登录时间
const loginTime = ref('')

// 初始化登录时间
onMounted(() => {
  loginTime.value = new Date().toLocaleString()
  // 验证 Token，无 Token 则跳回登录页
  if (!token.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
  }
})

// 退出登录逻辑
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
    ElMessage.success('退出登录成功')
    router.push('/login') // 跳回登录页
  }).catch(() => {
    ElMessage.info('已取消退出')
  })
}
</script>

<style scoped>
.owner-home-container {
  display: flex;
  height: 100vh;
  background-color: #f5f7fa;
}

/* 侧边栏样式 */
.sidebar {
  width: 220px;
  background-color: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(10px);
  border-right: 1px solid #e4e7ed;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
}

.sidebar-collapse {
  width: 64px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  background-color: rgba(255, 255, 255, 0.5);
  border-bottom: 1px solid #e4e7ed;
}

.mode-switch-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mode-switch-btn {
  background: transparent;
  border: 1px solid #dcdfe6;
  color: #606266;
  cursor: pointer;
  padding: 6px 12px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
  border-radius: 15px;
  transition: all 0.3s;
  white-space: nowrap;
}

.mode-switch-btn:hover {
  background-color: rgba(245, 247, 250, 0.5);
  border-color: #c0c4cc;
}

.mode-switch-btn.is-simple-mode {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: #ffffff;
}

.mode-switch-btn.is-simple-mode:hover {
  opacity: 0.9;
}

.mode-switch-btn .el-icon {
  font-size: 16px;
}

.sidebar-header h3 {
  color: #303133;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
  transition: opacity 0.3s;
}

.sidebar-collapse .sidebar-header h3 {
  opacity: 0;
  width: 0;
  overflow: hidden;
}

.collapse-btn {
  background: transparent;
  border: none;
  color: #606266;
  cursor: pointer;
  padding: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.collapse-btn:hover {
  background-color: rgba(245, 247, 250, 0.5);
}

/* 菜单样式 */
.el-menu {
  border-right: none;
  background-color: transparent;
  flex: 1;
  overflow-y: auto;
}

.el-menu-item {
  color: #606266;
}

.el-menu-item:hover {
  background-color: rgba(245, 247, 250, 0.5) !important;
  color: #409EFF !important;
}

.el-menu-item.is-active {
  background-color: rgba(236, 245, 255, 0.5) !important;
  color: #409EFF !important;
}

/* 子菜单标题 */
:deep(.el-sub-menu__title) {
  color: #606266;
}

:deep(.el-sub-menu__title:hover) {
  background-color: rgba(245, 247, 250, 0.5) !important;
  color: #409EFF !important;
}

/* 折叠时的菜单样式 */
:deep(.el-menu--collapse) {
  width: 64px;
}

:deep(.el-menu--collapse .el-submenu__title span),
:deep(.el-menu--collapse .el-menu-item span) {
  display: none;
}

/* 主内容区样式 */
.main-content {
  flex: 1;
  padding: 0;
  //overflow: auto;
}

.content-wrapper {
  height: 100%;
}
</style>
