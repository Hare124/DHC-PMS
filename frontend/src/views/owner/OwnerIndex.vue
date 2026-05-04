<!--CarouselAdmin-->
<template>
  <div class="owner-index-page">
    <!-- 轮播图区域 -->
    <div class="carousel-container" v-if="carouselList.length > 0">
      <el-carousel :interval="4000" type="card" height="300px">
        <el-carousel-item v-for="item in carouselList" :key="item.id">
          <div class="carousel-item" @click="handleCarouselClick(item)">
            <el-image
                :src="item.imageUrl"
                fit="cover"              style="width: 100%; height: 100%;"
                :hide-on-click-modal="true"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                  <span>图片加载失败</span>
                </div>
              </template>
            </el-image>
            <div class="carousel-title" v-if="item.title">{{ item.title }}</div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 无轮播图提示 -->
    <el-empty v-else description="暂无轮播图" />

    <!-- 功能区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 天气和联系方式 -->
      <el-col :span="12">
        <el-card class="info-card" >
          <template #header>
            <div class="card-header">
              <el-icon><ShoppingCart /></el-icon>
              <span>快捷入口</span>
            </div>
          </template>
          <div class="service-grid">
            <div class="service-item" v-for="service in serviceList" :key="service.name" @click="handleServiceClick(service)">
              <div class="service-icon">
                <span class="emoji-icon">{{ service.icon }}</span>
              </div>
              <div class="service-name">{{ service.name }}</div>
            </div>
          </div>
        </el-card>

        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <el-icon><Sunny /></el-icon>
              <span>天气</span>
            </div>
          </template>
          <div class="weather-content" v-loading="weatherLoading">
            <div class="weather-main" v-if="weatherInfo.city">
              <div class="weather-temp">
                <el-icon class="weather-icon">
                  <Sunny v-if="weatherInfo.weather === '晴'" />
                  <Cloudy v-else-if="weatherInfo.weather === '多云'" />
                  <Umbrella v-else-if="weatherInfo.weather.includes('雨')" />
                  <Lightning v-else-if="weatherInfo.weather.includes('雷')" />
                </el-icon>
                <span class="temperature">{{ weatherInfo.temperature }}°C</span>
              </div>
              <div class="weather-detail">
                <span>{{ weatherInfo.city }} · {{ weatherInfo.weather }}</span>
                <span class="weather-desc">{{ weatherInfo.description }}</span>
              </div>
              <div class="weather-extra">
                <span>湿度：{{ weatherInfo.humidity }}%</span>
                <span>风力：{{ weatherInfo.wind }}级</span>
              </div>
            </div>
            <div class="weather-alert" v-if="weatherInfo.alert">
              <el-alert
                  :title="weatherInfo.alert"
                  type="warning"
                  :closable="false"
                  show-icon
              />
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 通知公告 -->
      <el-col :span="6">
        <el-card class="service-card">
          <template #header>
            <div class="card-header">
              <el-icon><Bell /></el-icon>
              <span>公告栏</span>
              <el-button type="primary" link @click="viewAllNotices" style="margin-left: auto;">更多</el-button>
            </div>
          </template>
          <div class="notice-list" v-loading="noticeLoading">
            <div class="notice-item" v-for="notice in noticeList" :key="notice.id" @click="viewNoticeDetail(notice)">
              <div class="notice-title">
                <el-tag v-if="notice.isTop === 1" size="small" type="danger" style="margin-right: 5px;">置顶</el-tag>
                {{ notice.title }}
              </div>
              <div class="notice-time">{{ formatTime(notice.publishTime) }}</div>
            </div>
            <el-empty v-if="noticeList.length === 0" description="暂无通知公告" />
          </div>
        </el-card>
      </el-col>

      <!-- 联系方式 -->
      <el-col :span="6">
        <el-card class="notice-card">
          <template #header>
            <div class="card-header">
              <el-icon><Phone /></el-icon>
              <span>联系方式</span>
              <el-icon style="margin-left: auto;"><Service /></el-icon>
              <el-button type="primary" link @click="goToAIAssistant" >
                在线客服
              </el-button>
            </div>
          </template>
          <div class="contact-list">
            <div class="contact-item" v-for="(contact, index) in contactList" :key="index">
              <div class="contact-icon">
                <el-icon><PhoneFilled /></el-icon>
              </div>
              <div class="contact-info">
                <div class="contact-name">{{ contact.name }}</div>
                <div class="contact-number">
                  <el-tag size="small" type="info">{{ contact.type }}</el-tag>
                  <span class="number">{{ contact.phone }}</span>
                  <el-button type="primary" size="small" circle @click="callPhone(contact.phone)">
                    <el-icon><Phone /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 公告详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="公告详情" width="700px">
      <div class="detail-header">
        <h2>{{ detailData.title }}</h2>
        <div class="detail-meta">
          <span>发布人：{{ detailData.publisherName }}</span>
          <span>发布时间：{{ formatTime(detailData.publishTime) }}</span>
          <span>阅读次数：{{ detailData.readCount }}</span>
          <el-tag v-if="detailData.isTop === 1" type="danger">置顶</el-tag>
        </div>
      </div>
      <div class="detail-content">
        {{ detailData.content }}
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ownerGetAnnouncementList, ownerGetAnnouncementDetail  } from '@/api/announcementApi.js'
import {
  Sunny,
  Cloudy,
  Umbrella,
  Lightning,
  Phone,
  PhoneFilled,
  Service,
  Bell,
  Money,
  Tools,
  User,
  ChatDotRound,
  ChatLineRound,
  Picture,
  Document,
  ShoppingCart
} from '@element-plus/icons-vue'
import { testPost, testGet, getActiveCarousels } from '@/api/adminApi.js'
import { managerGetAnnouncementList } from '@/api/announcementApi.js'
const router = useRouter()

// 轮播图数据
const carouselList = ref([])

// 天气信息
const weatherLoading = ref(false)
const weatherInfo = ref({
  city: '',
  temperature: '',
  weather: '',
  description: '',
  humidity: '',
  wind: '',
  alert: ''
})

// 联系方式
const contactList = ref([
  { name: '物业前台', type: '24 小时', phone: '0755-88888888' },
  { name: '维修服务', type: '工作日', phone: '0755-88888889' },
  { name: '安保中心', type: '24 小时', phone: '0755-88888890' },
  { name: '急救电话', type: '紧急', phone: '120' },
  { name: '火警', type: '紧急', phone: '119' },
  { name: '报警电话', type: '紧急', phone: '110' }
])

// 生活服务
const serviceList = ref([
  { icon: '📄', name: '我的账单', path: '/owner/fee/list' },
  { icon: '🔧', name: '报修服务', path: '/repair/list' },
  { icon: '👤', name: '来访申请', path: '/visitor/list' },
  { icon: '💬', name: '业主群', path: '/owner/Message' }
])

// 通知公告
const noticeLoading = ref(false)
const noticeList = ref([])

// 公告详情弹窗
const detailDialogVisible = ref(false)
const detailData = ref({})
const viewingDetailId = ref(null)

// 加载轮播图
const loadCarousels = async () => {
  try {
    const res = await getActiveCarousels()
    console.log('轮播图响应:', res)

    if (res.data && res.data.code === 200) {
      // 处理后端返回的数据，可能是数组或对象
      let carouselData = res.data.data
      if (!carouselData) {
        carouselData = []
      } else if (!Array.isArray(carouselData)) {
        // 如果不是数组，转为数组
        carouselData = [carouselData]
      }

      // 最多显示 5 张
      carouselList.value = carouselData.slice(0, 5)
      console.log('轮播图列表:', carouselList.value)

      // 检查是否有有效的图片 URL
      if (carouselList.value.length === 0) {
        ElMessage.warning('暂无轮播图')
      }
    } else {
      carouselList.value = []
      ElMessage.warning('暂无轮播图')
    }
  } catch (error) {
    console.error('加载轮播图失败:', error)
    const errorMsg = error.response?.data?.msg || '加载轮播图失败'
    ElMessage.error(errorMsg)
    carouselList.value = []
  }
}

// 加载天气信息（模拟数据，实际可接入天气 API）
const loadWeather = () => {
  weatherLoading.value = true
  // 模拟天气数据
  setTimeout(() => {
    weatherInfo.value = {
      city: '深圳',
      temperature: '28',
      weather: '晴',
      description: '晴朗，适宜外出',
      humidity: '65',
      wind: '2-3',
      alert: ''
    }
    weatherLoading.value = false
  }, 500)
}

// 加载通知公告
const loadNotices = async () => {
  noticeLoading.value = true
  try {
    const res = await ownerGetAnnouncementList({
      pageNum: 1,
      pageSize: 5,
      isTop: null, // 不过滤置顶状态
      recallStatus: 0 // 只显示未撤回的公告
    })

    if (res.data && res.data.code === 200 && res.data.data) {
      noticeList.value = res.data.data.records || []
    } else {
      noticeList.value = []
    }
  } catch (error) {
    console.error('加载通知公告失败:', error)
    noticeList.value = []
  } finally {
    noticeLoading.value = false
  }
}

// 轮播图点击事件
const handleCarouselClick = (item) => {
  if (item.targetUrl) {
    window.open(item.targetUrl, '_blank')
  } else {
    ElMessage.info('该轮播图暂无跳转链接')
  }
}

// 拨打电话
const callPhone = (phone) => {
  if (phone) {
    window.location.href = `tel:${phone}`
    ElMessage.success(`正在拨打 ${phone}`)
  }
}

// 跳转到 AI 客服
const goToAIAssistant = () => {
  router.push('/owner/DeepSeek')
}

// 生活服务点击
const handleServiceClick = (service) => {
  if (service.path) {
    router.push(service.path)
  } else {
    ElMessage.info(`即将开放${service.name}功能`)
  }
}

// 查看更多通知
const viewAllNotices = () => {
  router.push('/announcement/list')
}

// 查看公告详情
const viewNoticeDetail = async (notice) => {
  if (viewingDetailId.value === notice.id) {
    return
  }

  try {
    viewingDetailId.value = notice.id
    const res = await ownerGetAnnouncementDetail(notice.id)
    console.log('[公告详情] 响应数据:', res)
    console.log('[公告详情] res.data:', res.data)
    detailData.value = res.data && res.data.data ? res.data.data : res.data
    detailDialogVisible.value = true
  } catch (error) {
    console.error('查看公告详情失败:', error)
    ElMessage.error('查看公告详情失败，请稍后重试')
  } finally {
    viewingDetailId.value = null
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) { // 1 分钟内
    return '刚刚'
  } else if (diff < 3600000) { // 1 小时内
    return Math.floor(diff / 60000) + '分钟前'
  } else if (diff < 86400000) { // 24 小时内
    return Math.floor(diff / 3600000) + '小时前'
  } else if (diff < 604800000) { // 7 天内
    return Math.floor(diff / 86400000) + '天前'
  } else {
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  }
}

onMounted(() => {
  loadCarousels()
  loadWeather()
  loadNotices()
})
</script>

<style scoped>
.owner-index-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.carousel-container {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.carousel-item {
  position: relative;
  cursor: pointer;
  transition: transform 0.3s;
}

.carousel-item:hover {
  transform: scale(1.02);
}

.carousel-title {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  padding: 8px 15px;
  background: linear-gradient(to top, rgba(0,0,0,0.6), transparent);
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 14px;
}

.image-error .el-icon {
  font-size: 40px;
  margin-bottom: 10px;
}

.info-card,
.service-card,
.notice-card {
  border-radius: 8px;
}

.notice-card{
  height: 450px;
}

.service-card{
  height: 450px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  font-size: 16px;
}

/* 天气样式 */
.weather-content {
  min-height: 150px;
}

.weather-main {
  padding: 10px 0;
}

.weather-temp {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.weather-icon {
  font-size: 32px;
  color: #F7BA2A;
}

.temperature {
  font-size: 36px;
  font-weight: bold;
  color: #303133;
}

.weather-detail {
  display: flex;
  flex-direction: column;
  gap: 5px;
  color: #606266;
  margin-bottom: 10px;
}

.weather-desc {
  font-size: 14px;
  color: #909399;
}

.weather-extra {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #909399;
}

.weather-alert {
  margin-top: 10px;
}

/* 联系方式样式 */
.contact-list {
  max-height: 300px;
  overflow-y: auto;
}

.contact-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.contact-item:last-child {
  border-bottom: none;
}

.contact-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  color: #409EFF;
  font-size: 20px;
}

.contact-info {
  flex: 1;
}

.contact-name {
  font-size: 15px;
  color: #303133;
  margin-bottom: 5px;
  font-weight: 500;
}

.contact-number {
  display: flex;
  align-items: center;
  gap: 10px;
}

.contact-number .number {
  font-size: 14px;
  color: #606266;
  flex: 1;
}

/* 生活服务样式 */
.service-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.service-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.service-item:hover {
  background-color: #f5f7fa;
  transform: translateY(-2px);
}

.service-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
  font-size: 36px;
}

.emoji-icon {
  font-size: 36px;
  line-height: 1;
}

.service-name {
  font-size: 14px;
  color: #606266;
  text-align: center;
}

/* 通知公告样式 */
.notice-list {
  max-height: 400px;
  overflow-y: auto;
}

.notice-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-item:hover {
  background-color: #f5f7fa;
  padding-left: 8px;
}

.notice-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.notice-time {
  font-size: 12px;
  color: #909399;
}
/* 公告详情弹窗样式 */
.detail-header {
  margin-bottom: 20px;
}

.detail-header h2 {
  font-size: 20px;
  margin: 0 0 15px 0;
  color: #303133;
}

.detail-meta {
  display: flex;
  gap: 15px;
  font-size: 13px;
  color: #909399;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.detail-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  max-height: 400px;
  overflow-y: auto;
}
</style>
