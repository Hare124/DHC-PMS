<template>
  <div class="message-page">
    <el-card shadow="always" class="chat-card">
      <template #header>
        <div class="card-header">
          <div class="room-info">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ currentRoom }}</span>
          </div>
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" is-dot>
            <el-button type="primary" size="small" @click="showUnread = !showUnread">
              <el-icon><Bell /></el-icon> 消息提醒
            </el-button>
          </el-badge>
        </div>
      </template>

      <!-- 消息列表区域 -->
      <div class="message-list" ref="messageListRef">
        <div v-if="messageList.length === 0" class="empty-message">
          <el-empty description="暂无聊天记录，发送第一条消息吧～" />
        </div>

        <div
          v-for="(msg, index) in messageList"
          :key="index"
          class="message-item"
          :class="{ 'my-message': msg.senderId === userId, 'withdrawn-message': msg.isWithdrawn }"
        >
          <!-- 撤回消息提示 -->
          <div v-if="msg.isWithdrawn" class="withdraw-tip">
            <el-icon><InfoFilled /></el-icon>
            {{ msg.senderName }} 撤回了一条消息
          </div>

          <!-- 正常消息 -->
          <template v-else>
            <!-- 发送人头像 -->
            <div class="sender-avatar">
              <el-avatar :size="40" :src="defaultAvatar">
                <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" alt="默认头像" />
              </el-avatar>
            </div>

            <!-- 消息内容 -->
            <div class="message-content">
              <!-- 发送人信息 -->
              <div class="sender-info">
                <span class="sender-name">{{ msg.senderName }}</span>
                <span class="send-time">{{ formatDate(msg.sendTime) }}</span>
                <!-- 撤回按钮（仅本人且 2 分钟内可撤回） -->
                <el-button
                  v-if="msg.senderId === userId && canWithdraw(msg.sendTime)"
                  type="danger"
                  link
                  size="small"
                  @click="handleWithdraw(msg.id)"
                >
                  撤回
                </el-button>
              </div>

              <!-- 消息主体 -->
              <div class="message-body">
                <!-- 文本消息 -->
                <div v-if="msg.messageType === 'text'" class="text-message">
                  {{ msg.messageContent }}
                </div>

                <!-- 图片消息 -->
                <div v-else-if="msg.messageType === 'image'" class="image-message">
                  <el-image
                    :src="msg.fileUrl"
                    fit="cover"
                    style="width: 200px; height: 200px; cursor: pointer;"
                    :preview-src-list="[msg.fileUrl]"
                  />
                  <div v-if="msg.fileName" class="file-name">{{ msg.fileName }}</div>
                </div>

                <!-- 文件消息 -->
                <div v-else-if="msg.messageType === 'file'" class="file-message">
                  <el-button type="primary" link @click="downloadFile(msg)">
                    <el-icon><Document /></el-icon>
                    {{ msg.fileName || '点击下载文件' }}
                  </el-button>
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <!-- 工具栏 -->
        <div class="toolbar">
          <el-button text @click="triggerImageUpload">
            <el-icon><Picture /></el-icon> 图片
          </el-button>
          <el-button text @click="triggerFileUpload">
            <el-icon><Document /></el-icon> 文件
          </el-button>
          <input
            ref="imageInputRef"
            type="file"
            accept="image/*"
            style="display: none;"
            @change="handleImageChange"
          />
          <input
            ref="fileInputRef"
            type="file"
            accept=".doc,.docx,.pdf,.xls,.xlsx,.ppt,.pptx,.zip,.rar"
            style="display: none;"
            @change="handleFileChange"
          />
        </div>

        <!-- 消息输入框 -->
        <div class="message-input">
          <el-input
            v-model="messageContent"
            type="textarea"
            :rows="3"
            placeholder="输入消息内容...（Ctrl+Enter 发送）"
            @keydown.ctrl.enter="handleSendMessage"
          />
        </div>

        <!-- 发送按钮 -->
        <div class="send-btn-area">
          <el-button type="primary" @click="handleSendMessage" :loading="sending">
            发送
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 新消息提醒弹窗 -->
    <el-dialog v-model="showUnread" title="新消息提醒" width="400px">
      <div class="unread-list">
        <div v-for="(msg, index) in unreadMessages" :key="index" class="unread-item">
          <div class="unread-sender">{{ msg.senderName }}:</div>
          <div class="unread-content">{{ msg.messageContent }}</div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showUnread = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { ChatDotRound, Bell, InfoFilled, Document, Picture, Promotion } from '@element-plus/icons-vue';
import { getMessageList, sendMessage, withdrawMessage, uploadFile } from '@/api/messageApi.js';

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 当前群聊名称
const currentRoom = '鼎湖社区业主群';

// 用户 ID（从 localStorage 获取）
const userId = ref(2);

// 用户名（从 localStorage 获取）
const userName = ref('业主');

// 消息列表
const messageList = ref([]);

// 消息内容
const messageContent = ref('');

// 发送状态
const sending = ref(false);

// 消息列表容器引用
const messageListRef = ref(null);

// 文件输入框引用
const imageInputRef = ref(null);
const fileInputRef = ref(null);

// 未读消息相关
const unreadCount = ref(0);
const showUnread = ref(false);
const unreadMessages = ref([]);

// 页面加载时获取数据
onMounted(() => {
  loadUserInfo();
  loadMessages();
  // 模拟轮询获取新消息（实际项目可用 WebSocket）
  // setInterval(() => {
  //   loadMessages();
  // }, 5000);
});

// 加载消息列表
const loadMessages = async () => {
  try {
    const params = {
      chatRoomName: currentRoom,
      pageNum: 1,
      pageSize: 50
    };

    const res = await getMessageList(params);
    console.log('消息列表响应：', res);

    if (res.data && res.data.code === 200) {
      const newMessages = res.data.data?.list || [];

      // 检查是否有新消息
      const newMessageCount = newMessages.filter(
        msg => msg.senderId !== userId.value &&
        !messageList.value.find(m => m.id === msg.id)
      ).length;

      if (newMessageCount > 0) {
        unreadCount.value += newMessageCount;
        unreadMessages.value = newMessages.filter(
          msg => msg.senderId !== userId.value &&
          !messageList.value.find(m => m.id === msg.id)
        );

        // 如果有新消息且用户正在查看，自动弹出提醒
        if (document.visibilityState === 'visible') {
          showUnread.value = true;
          unreadCount.value = 0;
        }
      }

      messageList.value = newMessages;

      // 滚动到底部
      await nextTick();
      scrollToBottom();
    }
  } catch (error) {
    console.error('加载消息列表失败:', error);
  }
};

// 滚动到底部
const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
  }
};

// 加载用户信息
const loadUserInfo = () => {
  try {
    const userStr = localStorage.getItem('userInfo');
    if (userStr) {
      const user = JSON.parse(userStr);
      userId.value = user.id || 2;
      userName.value = user.username || '业主'; // 使用 username 字段
      console.log('当前用户信息：', {
        id: userId.value,
        username: userName.value
      });
    } else {
      console.warn('未找到用户信息，使用默认值');
    }
  } catch (error) {
    console.error('加载用户信息失败:', error);
  }
};

// 发送消息
const handleSendMessage = async () => {
  if (!messageContent.value.trim()) {
    ElMessage.warning('请输入消息内容');
    return;
  }

  try {
    sending.value = true;

    const data = {
      chatRoomName: currentRoom,
      senderId: userId.value,
      senderName: userName.value, // 使用从 localStorage 获取的用户名
      messageContent: messageContent.value,
      messageType: 'text'
    };

    console.log('发送消息数据：', data);

    const res = await sendMessage(data);
    console.log('发送消息响应：', res);

    if (res.data && res.data.code === 200) {
      ElMessage.success('发送成功');
      messageContent.value = '';
      loadMessages();
    } else {
      ElMessage.error(res.data?.msg || '发送失败');
    }
  } catch (error) {
    console.error('发送消息失败:', error);
    ElMessage.error('发送失败，请稍后重试');
  } finally {
    sending.value = false;
  }
};

// 触发图片上传
const triggerImageUpload = () => {
  if (imageInputRef.value) {
    imageInputRef.value.click();
  }
};

// 触发文件上传
const triggerFileUpload = () => {
  if (fileInputRef.value) {
    fileInputRef.value.click();
  }
};

// 处理图片选择
const handleImageChange = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  // 验证文件大小（5MB）
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB');
    return;
  }

  await uploadFileAndSend(file, 'image');
};

// 处理文件选择
const handleFileChange = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  // 验证文件大小（10MB）
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过 10MB');
    return;
  }

  await uploadFileAndSend(file, 'file');
};

// 上传文件并发送
const uploadFileAndSend = async (file, type) => {
  try {
    sending.value = true;

    const formData = new FormData();
    formData.append('file', file);

    const res = await uploadFile(formData);
    console.log('上传文件响应：', res);

    if (res.data && res.data.code === 200) {
      const fileUrl = res.data.data?.url;

      const data = {
        chatRoomName: currentRoom,
        senderId: userId.value,
        senderName: userName.value, // 使用从 localStorage 获取的用户名
        messageContent: type === 'image' ? '[图片]' : '[文件]',
        messageType: type,
        fileUrl: fileUrl,
        fileName: file.name
      };

      console.log('发送文件消息数据：', data);

      const sendRes = await sendMessage(data);
      if (sendRes.data && sendRes.data.code === 200) {
        ElMessage.success('发送成功');
        loadMessages();
      }
    }
  } catch (error) {
    console.error('上传文件失败:', error);
    ElMessage.error('上传失败，请稍后重试');
  } finally {
    sending.value = false;
  }
};

// 撤回消息
const handleWithdraw = async (messageId) => {
  try {
    const res = await withdrawMessage(messageId);
    console.log('撤回消息响应：', res);

    if (res.data && res.data.code === 200) {
      ElMessage.success('撤回成功');
      loadMessages();
    } else {
      ElMessage.error(res.data?.msg || '撤回失败');
    }
  } catch (error) {
    console.error('撤回消息失败:', error);
    ElMessage.error('撤回失败，请稍后重试');
  }
};

// 判断是否可以撤回（2 分钟内）
const canWithdraw = (sendTime) => {
  if (!sendTime) return false;
  const now = new Date();
  const send = new Date(sendTime);
  const diff = (now - send) / 1000 / 60; // 转换为分钟
  return diff <= 2;
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  try {
    const date = new Date(dateStr);
    const now = new Date();
    const diff = (now - date) / 1000; // 秒

    if (diff < 60) return '刚刚';
    if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`;
    if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`;

    return `${date.getMonth() + 1}月${date.getDate()}日 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
  } catch (e) {
    return dateStr;
  }
};

// 下载文件
const downloadFile = (msg) => {
  if (msg.fileUrl) {
    window.open(msg.fileUrl, '_blank');
  }
};
</script>

<style scoped>
.message-page {
  padding: 0;
  margin: 0;
  background-color: #fff;
  overflow: hidden;
  height: 100%;
}

.chat-card {
  height: 100%;
  margin: 0;
  border-radius: 0;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.room-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
}

/* 消息列表区域 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 5px;
  margin-bottom: 20px;
}

.empty-message {
  text-align: center;
  padding: 100px 0;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  align-items: flex-start;
}

.my-message {
  flex-direction: row-reverse;
}

.my-message .message-content {
  align-items: flex-end;
}

.my-message .sender-info {
  flex-direction: row-reverse;
}

.withdrawn-message {
  justify-content: center;
}

.withdraw-tip {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px 10px;
  background: #f0f0f0;
  border-radius: 5px;
}

.sender-avatar {
  flex-shrink: 0;
}

.message-content {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.sender-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
  font-size: 12px;
}

.sender-name {
  font-weight: 600;
  color: #303133;
}

.send-time {
  color: #909399;
}

.message-body {
  background: #fff;
  padding: 12px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.my-message .message-body {
  background: #409eff;
  color: #fff;
}

.text-message {
  line-height: 1.6;
  word-wrap: break-word;
}

.image-message {
  position: relative;
}

.file-name {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.file-message {
  display: flex;
  align-items: center;
}

/* 输入区域 */
.input-area {
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.message-input {
  margin-bottom: 10px;
}

.send-btn-area {
  display: flex;
  justify-content: flex-end;
}

/* 未读消息列表 */
.unread-list {
  max-height: 300px;
  overflow-y: auto;
}

.unread-item {
  padding: 10px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  gap: 10px;
}

.unread-sender {
  font-weight: 600;
  color: #409eff;
  flex-shrink: 0;
}

.unread-content {
  color: #606266;
  flex: 1;
}

:deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
</style>
