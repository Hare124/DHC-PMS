<template>
  <div class="floor-plan-container">
    <!-- 页面标题 -->
<!--    <el-card class="header-card">-->
<!--      <div class="page-header">-->
<!--        <h2>🏘️ 小区平面图</h2>-->
<!--        <p class="description">查看小区整体布局、楼栋分布及配套设施</p>-->
<!--      </div>-->
<!--    </el-card>-->

    <!-- 平面图展示区域 -->
    <el-card class="plan-card">
      <template #header>
        <div class="card-header">
          <span>📍 楼栋分布图</span>
          <el-tag type="info" size="small">点击图片可放大查看</el-tag>
        </div>
      </template>

      <div class="image-container">
        <el-image
            :src="floorPlanImage"
            alt="小区平面图"
            fit="contain"
            class="floor-plan-image"
            :preview-src-list="[floorPlanImage]"
        >
          <template #placeholder>
            <div class="image-loading">
              <el-icon><Loading /></el-icon>
              <span>正在加载图片...</span>
            </div>
          </template>

          <template #error>
            <div class="image-error">
              <el-icon><Warning /></el-icon>
              <span>图片加载失败，请稍后重试</span>
            </div>
          </template>
        </el-image>
      </div>

<!--      <div class="image-tips">-->
<!--        <el-alert-->
<!--            title="温馨提示"-->
<!--            type="info"-->
<!--            :closable="false"-->
<!--            show-icon-->
<!--        >-->
<!--          <p>📌 图中标注了各楼栋位置及编号，以及小区内的公共设施分布</p>-->
<!--          <p>🔍 点击图片可以放大查看详细标注</p>-->
<!--          <p>📱 支持鼠标滚轮缩放和拖拽查看</p>-->
<!--        </el-alert>-->
<!--      </div>-->
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { Loading, Warning } from '@element-plus/icons-vue';

// 导入平面图图片
import floorPlanImage from '@/assets/Plan.png';
</script>

<style scoped>
.floor-plan-container {
  //padding: 20px;
  max-width: 1600px;
  margin: 0 auto;
  height: 100%;
}

.header-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.page-header {
  text-align: center;
}

.page-header h2 {
  font-size: 28px;
  margin: 0 0 10px 0;
}

.description {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.plan-card {
  min-height: calc(100vh - 200px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.image-container {
  width: 100%;
  //height: calc(100vh - 300px);
  //min-height: 600px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
}

.floor-plan-image {
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.image-loading,
.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #909399;
  font-size: 14px;
}

.image-error {
  color: #f56c6c;
}

.image-tips {
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .floor-plan-container {
    padding: 10px;
  }

  .page-header h2 {
    font-size: 22px;
  }

  .image-container {
    height: calc(100vh - 250px);
    min-height: 400px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
