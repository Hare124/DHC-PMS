<template>
  <div class="owner-deepseek-container">
    <!-- 页面标题 -->
    <el-card class="header-card">
      <div class="page-header">
        <h2>🤖 DeepSeek智能客服</h2>
        <p class="description">有任何问题都可以询问 AI 助手，我们将竭诚为您解答</p>
      </div>
    </el-card>

    <!-- AI 响应结果（改为对话历史列表） -->
    <el-card class="response-card" v-if="chatHistory.length > 0">
      <template #header>
        <div class="card-header">
          <span>💬 对话历史</span>
          <el-tag :type="chatHistory.length > 0 ? 'success' : 'info'" size="small">
            共 {{ chatHistory.length }} 条记录
          </el-tag>
        </div>
      </template>

      <div class="chat-history-container">
        <div
            v-for="(chat, index) in chatHistory"
            :key="index"
            class="chat-item"
        >
          <!-- 用户问题 -->
          <div class="user-message">
            <div class="message-header">
              <span class="user-icon">👤</span>
              <span class="user-label">用户问题</span>
              <el-tag size="small" type="info" style="margin-left: auto;">
                {{ chat.timestamp }}
              </el-tag>
            </div>
            <div class="message-content">
              {{ chat.question }}
            </div>
          </div>

          <!-- AI 回答 -->
          <div class="ai-message">
            <div class="message-header">
              <span class="ai-icon">🤖</span>
              <span class="ai-label">AI 回答</span>
              <el-tag size="small" type="success" style="margin-left: auto;">
                耗时：{{ chat.responseTime }}ms
              </el-tag>
            </div>
            <div class="message-content ai-content">
              {{ chat.response }}
            </div>
          </div>

          <!-- 分隔线（最后一条不显示） -->
          <el-divider v-if="index < chatHistory.length - 1" />
        </div>
      </div>
    </el-card>

    <!-- 聊天输入区域 -->
    <el-card class="chat-card">
      <template #header>
        <div class="card-header">
          <span>💬 继续提问</span>
          <el-button
              size="small"
              @click="clearHistory"
              :disabled="chatHistory.length === 0"
              style="margin-left: auto;"
          >
            清空历史
          </el-button>
        </div>
      </template>

      <el-form label-width="100px">
        <el-form-item label="输入问题">
          <el-input
              v-model="userMessage"
              type="textarea"
              :rows="4"
              placeholder="请输入您的问题，例如：物业费怎么计算？小区有哪些配套设施？..."
              maxlength="1000"
              show-word-limit
              @keyup.enter.ctrl="sendMessage"
          ></el-input>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              size="large"
              @click="sendMessage"
              :loading="loading"
              style="width: 200px;"
          >
            {{ loading ? 'AI 思考中...' : '发送消息' }}
          </el-button>
          <el-button @click="scrollToTop" style="margin-left: 10px;">
            返回顶部
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 调试信息（可折叠） -->
<!--    <el-card class="debug-card">-->
<!--      <template #header>-->
<!--        <div class="card-header">-->
<!--          <span>🔧 调试信息</span>-->
<!--          <el-button size="small" @click="toggleDebug">-->
<!--            {{ showDebug ? '隐藏详情' : '显示详情' }}-->
<!--          </el-button>-->
<!--        </div>-->
<!--      </template>-->

<!--      <el-collapse-transition>-->
<!--        <div v-if="showDebug">-->
<!--          &lt;!&ndash; 请求信息 &ndash;&gt;-->
<!--          <div class="debug-section" v-if="requestInfo">-->
<!--            <h4>📤 请求信息</h4>-->
<!--            <pre class="debug-content request">{{ requestInfo }}</pre>-->
<!--          </div>-->

<!--          &lt;!&ndash; 响应信息 &ndash;&gt;-->
<!--          <div class="debug-section" v-if="responseInfo">-->
<!--            <h4>📥 响应信息</h4>-->
<!--            <pre class="debug-content response">{{ responseInfo }}</pre>-->
<!--          </div>-->

<!--          &lt;!&ndash; 错误信息 &ndash;&gt;-->
<!--          <div class="debug-section" v-if="errorInfo">-->
<!--            <h4>❌ 错误信息</h4>-->
<!--            <pre class="debug-content error">{{ errorInfo }}</pre>-->
<!--          </div>-->
<!--        </div>-->
<!--      </el-collapse-transition>-->
<!--    </el-card>-->

    <!-- 使用指南 -->
    <el-card class="guide-card">
      <template #header>
        <div class="card-header">
          <span>📖 使用指南</span>
        </div>
      </template>
      <ul class="guide-list">
        <li>💡 可以询问任何关于小区的问题，如物业费、停车费、设施使用等</li>
        <li>⏱️ AI 响应时间通常在 1-3 秒，复杂问题可能需要更长时间</li>
        <li>🔍 如需查看详细的请求和响应信息，请点击"显示详情"</li>
        <li>🗑️ 点击"清空历史"可以清除所有对话记录</li>
        <li>⌨️ 使用 Ctrl+Enter 快捷键可以快速发送消息</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { chatWithDeepSeek } from '@/api/ownerApi.js';

// 用户输入的消息
const userMessage = ref('');

// 对话历史（数组形式存储）
const chatHistory = ref([]);

// 加载状态
const loading = ref(false);

// 调试信息
const showDebug = ref(false);
const requestInfo = ref('');
const responseInfo = ref('');
const errorInfo = ref('');

// 切换调试信息显示
const toggleDebug = () => {
  showDebug.value = !showDebug.value;
};

// 清空历史记录
const clearHistory = () => {
  if (chatHistory.value.length === 0) {
    ElMessage.info('暂无历史记录可清空');
    return;
  }

  chatHistory.value = [];
  requestInfo.value = '';
  responseInfo.value = '';
  errorInfo.value = '';
  ElMessage.success('已清空所有对话记录');
};

// 返回顶部
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

// 发送消息给 AI
const sendMessage = async () => {
  if (!userMessage.value.trim()) {
    ElMessage.warning('请输入要询问的内容');
    return;
  }

  loading.value = true;
  requestInfo.value = '';
  responseInfo.value = '';
  errorInfo.value = '';

  const startTime = Date.now();
  const question = userMessage.value;
  const requestData = {
    message: question
  };

  // 记录请求信息
  requestInfo.value = JSON.stringify({
    '请求 URL': '/api/chat',
    '请求方法': 'POST',
    '请求头': {
      'Content-Type': 'application/json',
      'Authorization': localStorage.getItem('token') ? 'Bearer ***' : '未携带 Token',
      'user_id': localStorage.getItem('userId') || '未设置'
    },
    '请求参数': requestData,
    '超时时间': '30000ms'
  }, null, 2);

  try {
    console.log('\n========== DeepSeek 请求开始 ==========');
    console.log('📤 请求时间:', new Date().toLocaleString());
    console.log('📤 请求 URL:', '/api/chat');
    console.log('📤 请求方法:', 'POST');
    console.log('📤 请求数据:', requestData);
    console.log('========================================\n');

    // 调用 DeepSeek API
    const response = await chatWithDeepSeek(requestData);

    const endTime = Date.now();
    const responseTime = endTime - startTime;

    console.log('\n========== DeepSeek 响应开始 ==========');
    console.log('📥 响应时间:', new Date().toLocaleString());
    console.log('⏱️  请求耗时:', responseTime + 'ms');
    console.log('📥 响应状态:', response.status);
    console.log('📥 响应数据:', response.data);
    console.log('========================================\n');

    // 显示 AI 响应
    const responseData = response.data;
    const aiResponse = typeof responseData === 'string'
        ? responseData
        : JSON.stringify(responseData, null, 2);

    // 添加到对话历史
    const now = new Date();
    const timestamp = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`;

    chatHistory.value.push({
      question: question,
      response: aiResponse,
      responseTime: responseTime,
      timestamp: timestamp
    });

    // 记录响应信息
    responseInfo.value = JSON.stringify({
      '响应状态码': response.status || 200,
      '请求耗时': responseTime + 'ms',
      '响应时间': new Date().toLocaleString(),
      '数据长度': typeof aiResponse === 'string' ? aiResponse.length + ' 字符' : '对象',
      '响应内容预览': typeof aiResponse === 'string'
          ? aiResponse.substring(0, 200) + (aiResponse.length > 200 ? '...' : '')
          : JSON.stringify(aiResponse).substring(0, 200) + '...'
    }, null, 2);

    ElMessage.success(`✅ AI 响应成功！耗时：${responseTime}ms`);

    // 清空输入框
    userMessage.value = '';

    // 滚动到最新消息
    await nextTick();
    scrollToBottom();

  } catch (error) {
    const endTime = Date.now();
    const responseTime = endTime - startTime;

    console.error('\n========== DeepSeek 错误开始 ==========');
    console.error('❌ 错误时间:', new Date().toLocaleString());
    console.error('⏱️  请求耗时:', responseTime + 'ms');
    console.error('❌ 错误类型:', error.constructor.name);

    if (error.response) {
      console.error('❌ 错误状态码:', error.response.status);
      console.error('❌ 错误响应:', error.response.data);
      console.error('❌ 错误头信息:', error.response.headers);
    } else if (error.request) {
      console.error('❌ 请求已发送但未收到响应');
      console.error('❌ 请求对象:', error.request);
    } else {
      console.error('❌ 错误消息:', error.message);
    }
    console.error('======================================\n');

    // 构建错误信息
    let errorMessage = `请求失败！\n\n`;
    errorMessage += `❌ 错误类型：${error.constructor.name}\n`;
    errorMessage += `⏱️  请求耗时：${responseTime}ms\n\n`;

    if (error.response) {
      errorMessage += `📛 状态码：${error.response.status}\n`;
      errorMessage += `📛 响应数据：${JSON.stringify(error.response.data, null, 2)}\n`;
    } else if (error.request) {
      errorMessage += `📛 未收到响应，可能原因：\n`;
      errorMessage += `   - 后端服务未启动\n`;
      errorMessage += `   - 网络连接问题\n`;
      errorMessage += `   - 跨域配置问题\n`;
      errorMessage += `   - 请求超时（>30 秒）\n`;
    } else {
      errorMessage += `📛 错误消息：${error.message}\n`;
    }

    // 添加错误记录到历史
    const now = new Date();
    const timestamp = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`;

    chatHistory.value.push({
      question: question,
      response: errorMessage,
      responseTime: responseTime,
      timestamp: timestamp,
      isError: true
    });

    // 记录详细错误信息
    errorInfo.value = JSON.stringify({
      '错误类型': error.constructor.name,
      '错误时间': new Date().toLocaleString(),
      '请求耗时': responseTime + 'ms',
      '状态码': error.response?.status || '无响应',
      '错误消息': error.message || '未知错误',
      '响应数据': error.response?.data ? JSON.stringify(error.response.data) : '无',
      '排查建议': !error.response
          ? '1. 检查后端服务是否启动\n2. 检查网络是否正常\n3. 检查跨域配置\n4. 查看后端控制台日志'
          : '查看后端应用日志，确认 DeepSeek API 调用是否成功'
    }, null, 2);

    // 自动显示调试信息
    showDebug.value = true;

    ElMessage.error(`❌ AI 请求失败：${error.message}`);
  } finally {
    loading.value = false;
  }
};

// 滚动到底部
const scrollToBottom = () => {
  setTimeout(() => {
    const container = document.querySelector('.chat-history-container');
    if (container) {
      container.scrollTop = container.scrollHeight;
    }
  }, 100);
};
</script>

<style scoped>
.owner-deepseek-container {
  padding: 20px;
  max-width: 1200px;
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

.description {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.chat-card,
.response-card,
.debug-card,
.guide-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

/* 对话历史容器 */
.chat-history-container {
  max-height: 600px;
  overflow-y: auto;
  padding: 10px;
}

/* 对话项 */
.chat-item {
  margin-bottom: 30px;
}

/* 用户消息 */
.user-message {
  margin-bottom: 15px;
}

.message-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  gap: 8px;
}

.user-icon,
.ai-icon {
  font-size: 18px;
}

.user-label,
.ai-label {
  font-weight: bold;
  font-size: 14px;
}

.message-content {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  line-height: 1.8;
  font-size: 15px;
  white-space: pre-wrap;
  word-wrap: break-word;
  margin-left: 30px;
}

.ai-content {
  background: #e6f7ff;
  border-left: 4px solid #1890ff;
}

/* 调试信息样式 */
.debug-section {
  margin-bottom: 20px;
}

.debug-section h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #333;
}

.debug-content {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 13px;
  line-height: 1.6;
  max-height: 400px;
  overflow-y: auto;
}

.debug-content.request {
  background: #f5f7fa;
}

.debug-content.response {
  background: #f0f9ff;
}

.debug-content.error {
  background: #fef0f0;
  color: #f56c6c;
}

.guide-list {
  padding-left: 20px;
  margin: 0;
}

.guide-list li {
  margin-bottom: 10px;
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .owner-deepseek-container {
    padding: 10px;
  }

  .page-header h2 {
    font-size: 22px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .message-content {
    margin-left: 0;
  }
}
</style>
