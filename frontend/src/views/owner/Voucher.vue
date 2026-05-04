<template>
  <div class="voucher-container">
    <el-card class="voucher-card">
      <pre>{{ voucherInfo }}</pre>
      <div class="button-group" style="margin-top: 20px">
        <el-button type="info" @click="handleBack">返回</el-button>
        <el-button type="primary" @click="handlePrint">打印凭证</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const voucherInfo = ref('');

onMounted(() => {
  // 获取路由参数中的凭证信息
  const info = route.query.voucherInfo;
  if (info) {
    voucherInfo.value = decodeURIComponent(info);
  } else {
    ElMessage.error('凭证信息不存在');
  }
});

// 打印凭证
const handlePrint = () => {
  window.print();
};

// 返回
const handleBack = () => {
  router.back();
};
</script>

<style>/* 全局打印样式 - 隐藏侧边栏和布局组件 */
@media print {
  /* 隐藏所有元素 */
  body * {
    visibility: hidden !important;
  }

  /* 只显示凭证容器及其内容 */
  .voucher-container,
  .voucher-container *,
  .voucher-card,
  .voucher-card * {
    visibility: visible !important;
  }

  /* 隐藏常见的布局组件类名（Element Plus 和自定义） */
  .el-aside,
  .el-header,
  .el-menu,
  .sidebar,
  .layout-container,
  .app-container,
  [class*="sidebar"],
  [class*="menu"],
  [class*="header"],
  [class*="aside"],
  [class*="layout"] {
    display: none !important;
  }

  /* 重置凭证容器位置 */
  .voucher-container {
    position: absolute !important;
    left: 0 !important;
    top: 0 !important;
    width: 100% !important;
    padding: 0 !important;
    margin: 0 !important;
    background: white !important;
  }

  .voucher-card {
    width: 190mm !important;
    max-width: none !important;
    padding: 20px !important;
    margin: 0 !important;
    box-shadow: none !important;
    border: 2px solid #000 !important;
  }

  pre {
    font-size: 14px !important;
    line-height: 1.6 !important;
  }

  /* 隐藏所有按钮和交互元素 */
  button,
  .el-button,
  .el-menu,
  .el-header {
    display: none !important;
  }
}
</style>

<style scoped>
.voucher-container {
  padding: 20px;
  text-align: center;
}

.voucher-card {
  display: inline-block;
  padding: 30px;
  text-align: left;
  font-family: "SimSun", "宋体", serif;
  background: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  margin: 0 auto;
}

pre {
  font-size: 16px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: "SimSun", "宋体", serif;
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 15px;
}
</style>