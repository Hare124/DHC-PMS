<template>
  <div class="admin-home-container">
    <!-- 页面标题 -->
    <el-card class="header-card">
      <div class="page-header">
        <h2>🏢 物业人员首页</h2>
        <p class="description">欢迎使用物业管理系统，快速处理日常业务</p>
      </div>
    </el-card>

    <!-- 数据概览卡片 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-item">
            <div class="overview-icon building-icon">🏘️</div>
            <div class="overview-content">
              <div class="overview-value">{{ overview.buildingCount }}</div>
              <div class="overview-label">小区楼栋数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-item">
            <div class="overview-icon household-icon">👨‍👩‍👧‍👦</div>
            <div class="overview-content">
              <div class="overview-value">{{ overview.totalHouseholds }}</div>
              <div class="overview-label">总户数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-item">
            <div class="overview-icon occupied-icon">🏠</div>
            <div class="overview-content">
              <div class="overview-value">{{ overview.occupiedHouseholds }}</div>
              <div class="overview-label">在住户数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-item">
            <div class="overview-icon rate-icon">💰</div>
            <div class="overview-content">
              <div class="overview-value">{{ overview.paymentRate }}%</div>
              <div class="overview-label">本月收缴率</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-item">
            <div class="overview-icon arrears-icon">⚠️</div>
            <div class="overview-content">
              <div class="overview-value">{{ overview.arrearsHouseholds }}</div>
              <div class="overview-label">欠费户数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-item">
            <div class="overview-icon repair-icon">🔧</div>
            <div class="overview-content">
              <div class="overview-value">{{ overview.pendingRepairs }}</div>
              <div class="overview-label">待处理报修</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-item">
            <div class="overview-icon complaint-icon">📝</div>
            <div class="overview-content">
              <div class="overview-value">{{ overview.complaints }}</div>
              <div class="overview-label">投诉建议</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-item">
            <div class="overview-icon announcement-icon">📢</div>
            <div class="overview-content">
              <div class="overview-value">{{ overview.announcements }}</div>
              <div class="overview-label">公告数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 业务快捷入口 -->
    <el-card class="quick-access-card">
      <template #header>
        <div class="card-header">
          <span>⚡ 业务快捷入口</span>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="4" v-for="(item, index) in quickAccessItems" :key="index">
          <div class="quick-item" @click="handleQuickAccess(item.path)">
            <div class="quick-icon">{{ item.icon }}</div>
            <div class="quick-label">{{ item.label }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 图表与待办区域 -->
<!--    <el-row :gutter="20">-->
<!--      &lt;!&ndash; 费用收缴趋势图 &ndash;&gt;-->
<!--      <el-col :span="12">-->
<!--        <el-card class="chart-card">-->
<!--          <template #header>-->
<!--            <div class="card-header">-->
<!--              <span>📊 费用收缴趋势</span>-->
<!--            </div>-->
<!--          </template>-->
<!--          <div class="chart-container">-->
<!--            <div ref="paymentTrendChart" class="chart"></div>-->
<!--          </div>-->
<!--        </el-card>-->
<!--      </el-col>-->

<!--      &lt;!&ndash; 报修类型占比 &ndash;&gt;-->
<!--      <el-col :span="12">-->
<!--        <el-card class="chart-card">-->
<!--          <template #header>-->
<!--            <div class="card-header">-->
<!--              <span>🔧 报修类型占比</span>-->
<!--            </div>-->
<!--          </template>-->
<!--          <div class="chart-container">-->
<!--            <div ref="repairTypeChart" class="chart"></div>-->
<!--          </div>-->
<!--        </el-card>-->
<!--      </el-col>-->
<!--    </el-row>-->

    <!-- 待办事项与消息通知 -->
<!--    <el-row :gutter="20">-->
<!--      &lt;!&ndash; 待办事项 &ndash;&gt;-->
<!--      <el-col :span="12">-->
<!--        <el-card class="todo-card">-->
<!--          <template #header>-->
<!--            <div class="card-header">-->
<!--              <span>⏳ 待办事项</span>-->
<!--              <el-tag type="danger" size="small">{{ todoList.length }} 项待处理</el-tag>-->
<!--            </div>-->
<!--          </template>-->

<!--          <div class="todo-list">-->
<!--            <div-->
<!--                v-for="(todo, index) in todoList"-->
<!--                :key="index"-->
<!--                class="todo-item"-->
<!--            >-->
<!--              <div class="todo-icon">{{ todo.icon }}</div>-->
<!--              <div class="todo-content">-->
<!--                <div class="todo-title">{{ todo.title }}</div>-->
<!--                <div class="todo-desc">{{ todo.desc }}</div>-->
<!--                <div class="todo-time">{{ todo.time }}</div>-->
<!--              </div>-->
<!--              <div class="todo-actions">-->
<!--                <el-button type="primary" size="small" @click="handleTodo(todo)">-->
<!--                  处理-->
<!--                </el-button>-->
<!--              </div>-->
<!--            </div>-->

<!--            <div v-if="todoList.length === 0" class="empty-text">-->
<!--              <el-icon><CircleCheck /></el-icon>-->
<!--              <span>暂无待办事项</span>-->
<!--            </div>-->
<!--          </div>-->
<!--        </el-card>-->
<!--      </el-col>-->

<!--      &lt;!&ndash; 消息通知 &ndash;&gt;-->
<!--      <el-col :span="12">-->
<!--        <el-card class="message-card">-->
<!--          <template #header>-->
<!--            <div class="card-header">-->
<!--              <span>📬 消息通知</span>-->
<!--              <el-badge :value="messageList.length" :hidden="messageList.length === 0" />-->
<!--            </div>-->
<!--          </template>-->

<!--          <div class="message-list">-->
<!--            <div-->
<!--                v-for="(msg, index) in messageList"-->
<!--                :key="index"-->
<!--                class="message-item"-->
<!--                :class="{ 'is-unread': !msg.read }"-->
<!--            >-->
<!--              <div class="message-icon">{{ msg.icon }}</div>-->
<!--              <div class="message-content">-->
<!--                <div class="message-title">{{ msg.title }}</div>-->
<!--                <div class="message-desc">{{ msg.desc }}</div>-->
<!--                <div class="message-time">{{ msg.time }}</div>-->
<!--              </div>-->
<!--              <div class="message-actions">-->
<!--                <el-button-->
<!--                    v-if="!msg.read"-->
<!--                    type="primary"-->
<!--                    size="small"-->
<!--                    link-->
<!--                    @click="markAsRead(msg)"-->
<!--                >-->
<!--                  已读-->
<!--                </el-button>-->
<!--              </div>-->
<!--            </div>-->

<!--            <div v-if="messageList.length === 0" class="empty-text">-->
<!--              <el-icon><Bell /></el-icon>-->
<!--              <span>暂无新消息</span>-->
<!--            </div>-->
<!--          </div>-->
<!--        </el-card>-->
<!--      </el-col>-->
<!--    </el-row>-->
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { CircleCheck, Bell } from '@element-plus/icons-vue';
import { getOverviewData } from '@/api/adminApi.js';

const router = useRouter();

// 概览统计数据
const overview = reactive({
  buildingCount: 0,        // 小区楼栋数
  totalHouseholds: 0,      // 总户数
  occupiedHouseholds: 0,   // 在住户数
  paymentRate: 0,          // 本月收缴率
  arrearsHouseholds: 0,    // 欠费户数
  pendingRepairs: 0,       // 待处理报修
  complaints: 0,           // 投诉建议
  announcements: 0         // 公告数量
});

// 快捷入口配置
const quickAccessItems = [
  // { icon: '👤', label: '个人中心', path: '/admin/PersonAdmin' },
  { icon: '👤', label: '业主管理', path: '/admin/OwnerList' },
  // { icon: '🏠', label: '房屋管理', path: '/admin/BuildingList' },
  { icon: '💰', label: '费用管理', path: '/admin/FeeAdmin' },
  { icon: '🔧', label: '报修管理', path: '/admin/RepairAdmin' },
  { icon: '📝', label: '投诉建议', path: '/admin/SuggestionList' },
  { icon: '📢', label: '公告管理', path: '/admin/AnnouncementAdmin' },
  { icon: '🎞️', label: '轮播管理', path: '/admin/CarouselList' },
  { icon: '👥', label: '访客管理', path: '/admin/VisitorAdmin' },
  { icon: '📊', label: '群聊管理', path: '/admin/MessageList' }
];

// 待办事项列表
const todoList = ref([
  {
    id: 1,
    icon: '🔧',
    title: '待处理报修',
    desc: '1 栋 2 单元 502 卫生间漏水',
    time: '10 分钟前',
    type: 'repair'
  },
  {
    id: 2,
    icon: '📝',
    title: '待审核投诉',
    desc: '业主投诉噪音扰民',
    time: '30 分钟前',
    type: 'complaint'
  },
  {
    id: 3,
    icon: '💰',
    title: '欠费催缴',
    desc: '3 栋 1 单元 801 物业费逾期 30 天',
    time: '1 小时前',
    type: 'fee'
  },
  {
    id: 4,
    icon: '🔧',
    title: '待处理报修',
    desc: '2 栋 3 单元 601 电路故障',
    time: '2 小时前',
    type: 'repair'
  },
  {
    id: 5,
    icon: '📢',
    title: '待发布公告',
    desc: '停水通知草稿待审核',
    time: '3 小时前',
    type: 'announcement'
  }
]);

// 消息通知列表
const messageList = ref([
  {
    id: 1,
    icon: '🔔',
    title: '新报修提醒',
    desc: '1 栋 2 单元 502 提交了卫生间漏水报修',
    time: '5 分钟前',
    read: false,
    type: 'repair'
  },
  {
    id: 2,
    icon: '💰',
    title: '缴费提醒',
    desc: '本月物业费收缴率已达 85%，请继续跟进',
    time: '1 小时前',
    read: false,
    type: 'fee'
  },
  {
    id: 3,
    icon: '📝',
    title: '投诉处理完成',
    desc: '业主投诉噪音问题已处理完毕',
    time: '2 小时前',
    read: true,
    type: 'complaint'
  },
  {
    id: 4,
    icon: '🔧',
    title: '报修派单成功',
    desc: '电路故障报修已派单给维修工张三',
    time: '3 小时前',
    read: true,
    type: 'repair'
  }
]);

// 图表实例
const paymentTrendChart = ref(null);
const repairTypeChart = ref(null);

// 快捷入口点击
const handleQuickAccess = (path) => {
  console.log('跳转到:', path);
  router.push(path);
};

// 处理待办事项
const handleTodo = (todo) => {
  console.log('处理待办:', todo);

  // 根据类型跳转不同页面
  const pathMap = {
    repair: '/admin/RepairAdmin',
    complaint: '/admin/ComplaintAdmin',
    fee: '/admin/FeeAdmin',
    announcement: '/admin/AnnouncementAdmin'
  };

  const targetPath = pathMap[todo.type];
  if (targetPath) {
    router.push(targetPath);
  } else {
    ElMessage.info('该功能正在开发中');
  }
};

// 标记消息为已读
const markAsRead = (msg) => {
  msg.read = true;
  console.log('标记为已读:', msg);
};

// 初始化图表（使用 ECharts）
const initCharts = () => {
  // TODO: 引入 ECharts 并初始化图表
  console.log('初始化图表...');

  // 费用收缴趋势图数据
  const paymentTrendData = {
    months: ['1 月', '2 月', '3 月', '4 月', '5 月', '6 月'],
    rates: [75, 78, 82, 80, 85, 85.5]
  };

  // 报修类型占比数据
  const repairTypeData = [
    { name: '水电维修', value: 35 },
    { name: '房屋漏水', value: 25 },
    { name: '电梯故障', value: 20 },
    { name: '门窗维修', value: 15 },
    { name: '其他', value: 5 }
  ];

  console.log('费用收缴趋势数据:', paymentTrendData);
  console.log('报修类型占比数据:', repairTypeData);
};

// 页面加载初始化
onMounted(() => {
  console.log('物业首页加载完成');
  loadOverviewData();
  initCharts();
});

// 加载概览数据
const loadOverviewData = async () => {
  try {
    console.log('开始加载概览数据...');

    const res = await getOverviewData();
    console.log('概览数据响应:', res);

    const businessData = res.data;

    if (businessData && businessData.code === 200) {
      // 填充数据
      const data = businessData.data;
      overview.buildingCount = data.buildingCount || 0;
      overview.totalHouseholds = data.totalHouseholds || 0;
      overview.occupiedHouseholds = data.occupiedHouseholds || 0;
      overview.paymentRate = data.paymentRate ? parseFloat(data.paymentRate.toFixed(2)) : 0;
      overview.arrearsHouseholds = data.arrearsHouseholds || 0;
      overview.pendingRepairs = data.pendingRepairs || 0;
      overview.complaints = data.complaints || 0;
      overview.announcements = data.announcements || 0;

      console.log('概览数据加载成功:', overview);
      console.log('✓ 楼栋数:', overview.buildingCount);
      console.log('✓ 总户数:', overview.totalHouseholds);
      console.log('✓ 在住户数:', overview.occupiedHouseholds);
      console.log('✓ 收缴率:', overview.paymentRate + '%');
      console.log('✓ 欠费户数:', overview.arrearsHouseholds);
      console.log('✓ 待处理报修:', overview.pendingRepairs);
      console.log('✓ 投诉建议:', overview.complaints);
      console.log('✓ 公告数量:', overview.announcements);
    } else {
      console.error('概览数据加载失败:', businessData);
      ElMessage.error(businessData?.msg || '加载概览数据失败');
    }
  } catch (err) {
    console.error('加载概览数据异常:', err);
    ElMessage.error('网络异常，加载概览数据失败');
  }
};
</script>

<style scoped>
.admin-home-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
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

.page-header .description {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

/* 概览卡片 */
.overview-row {
  margin-bottom: 20px;
}

.overview-card {
  text-align: center;
}

.overview-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  padding: 10px 0;
}

.overview-icon {
  font-size: 40px;
}

.building-icon { color: #409eff; }
.household-icon { color: #67c23a; }
.occupied-icon { color: #e6a23c; }
.rate-icon { color: #f56c6c; }
.arrears-icon { color: #e6a23c; }
.repair-icon { color: #409eff; }
.complaint-icon { color: #f56c6c; }
.announcement-icon { color: #67c23a; }

.overview-content {
  text-align: left;
}

.overview-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  line-height: 1;
}

.overview-label {
  font-size: 13px;
  color: #666;
  margin-top: 5px;
}

/* 快捷入口 */
.quick-access-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 10px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s;
}

.quick-item:hover {
  background: #f5f7fa;
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.quick-icon {
  font-size: 36px;
  margin-bottom: 10px;
}

.quick-label {
  font-size: 14px;
  color: #666;
  text-align: center;
}

/* 图表卡片 */
.chart-card {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.chart {
  width: 100%;
  height: 100%;
}

/* 待办事项 */
.todo-card,
.message-card {
  margin-bottom: 20px;
}

.todo-list,
.message-list {
  max-height: 400px;
  overflow-y: auto;
}

.todo-item,
.message-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.todo-item:last-child,
.message-item:last-child {
  border-bottom: none;
}

.todo-item:hover,
.message-item:hover {
  background: #f5f7fa;
  padding-left: 10px;
  padding-right: 10px;
  border-radius: 8px;
}

.message-item.is-unread {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
  padding-left: 12px;
}

.todo-icon,
.message-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.todo-content,
.message-content {
  flex: 1;
  min-width: 0;
}

.todo-title,
.message-title {
  font-size: 15px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.todo-desc,
.message-desc {
  font-size: 13px;
  color: #666;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.todo-time,
.message-time {
  font-size: 12px;
  color: #999;
}

.todo-actions,
.message-actions {
  flex-shrink: 0;
}

/* 空状态 */
.empty-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #909399;
  font-size: 14px;
  gap: 10px;
}

.empty-text .el-icon {
  font-size: 48px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .overview-value {
    font-size: 24px;
  }

  .overview-icon {
    font-size: 32px;
  }
}

@media (max-width: 768px) {
  .admin-home-container {
    padding: 10px;
  }

  .page-header h2 {
    font-size: 22px;
  }

  .overview-item {
    flex-direction: column;
    text-align: center;
  }

  .overview-content {
    text-align: center;
  }

  .quick-item {
    padding: 15px 5px;
  }

  .quick-icon {
    font-size: 28px;
  }

  .quick-label {
    font-size: 12px;
  }

  .chart-container {
    height: 250px;
  }
}
</style>
