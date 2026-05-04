<template>
  <div class="owner-page">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <!-- 左侧：品牌名称 -->
      <div class="navbar-brand">
        <h2 class="brand-name">鼎湖物业</h2>
      </div>

      <!-- 中间：导航按钮 -->
      <nav class="navbar-menu">
        <el-button class="nav-btn" @click="navigateTo('/owner/OwnerIndex02')">首页</el-button>
        <el-button class="nav-btn" @click="navigateTo('/owner/fee/list')">我的账单</el-button>
        <el-button class="nav-btn" @click="navigateTo('/repair/list')">报修服务</el-button>
        <el-button class="nav-btn" @click="navigateTo('/visitor/list')">来访申请</el-button>
        <el-button class="nav-btn" @click="navigateTo('/announcement/list')">公告动态</el-button>
      </nav>

      <!-- 右侧：功能按钮 -->
      <div class="navbar-right">
        <el-button class="mode-btn" @click="switchToSimpleMode">
          <el-icon><Sunny /></el-icon>
          简洁模式
        </el-button>
        <el-dropdown v-if="isLoggedIn" trigger="click" @command="handleUserCommand">
          <span class="user-dropdown">
            <el-icon><User /></el-icon>
            <span class="username">{{ username }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><Avatar /></el-icon>
                个人中心
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button v-else type="primary" class="login-btn" @click="handleLogin">登录</el-button>
      </div>
    </header>

    <!-- 全屏轮播图 -->
    <section class="hero-carousel" v-if="carouselList.length > 0">
    <el-carousel
        ref="carouselRef"
        height="calc(100vh - 60px)"
        arrow="never"
        indicator-position="none"
        :autoplay="true"
        :interval="4000"
        @change="handleCarouselChange"
    >
        <el-carousel-item v-for="(item, index) in carouselList" :key="item.id">
          <div class="carousel-slide" @click="handleCarouselClick(item)">
            <!-- 背景图片 -->
            <el-image
              :src="item.imageUrl"
              fit="cover"
              class="slide-image"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                  <span>图片加载失败</span>
                </div>
              </template>
            </el-image>

            <!-- 半透明遮罩 -->
            <div class="slide-overlay"></div>

            <!-- 轮播图内容 -->
            <div class="slide-content">
              <p class="slide-description" v-if="item.description">{{ item.description }}</p>
            </div>

            <!-- 左右切换箭头 -->
            <div class="carousel-arrows">
              <div class="arrow arrow-left" @click.stop="prevSlide">
                <el-icon><ArrowLeft /></el-icon>
              </div>
              <div class="arrow arrow-right" @click.stop="nextSlide">
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>

            <!-- 底部圆点指示器 -->
            <div class="carousel-indicators">
              <span
                v-for="(dot, dotIndex) in carouselList"
                :key="dotIndex"
                :class="['indicator-dot', { active: dotIndex === currentIndex }]"
                @click.stop="goToSlide(dotIndex)"
              ></span>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 公告展示区域 - 新闻杂志式 -->
    <section class="announcement-section" v-loading="announcementLoading">
      <div class="section-header">
        <div class="section-title">
          <el-icon><Bell /></el-icon>
          <h2>公告动态</h2>
        </div>
        <el-button class="view-all-btn" @click="viewAllAnnouncements">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <!-- 置顶公告（大卡片） -->
      <div class="featured-announcement" v-if="featuredAnnouncement" @click="viewAnnouncementDetail(featuredAnnouncement)">
        <div class="featured-badge">
          <el-icon><Top /></el-icon>
          <span>置顶公告</span>
        </div>
        <div class="featured-image" v-if="featuredAnnouncement.imageUrl">
          <el-image
            :src="featuredAnnouncement.imageUrl"
            fit="cover"
            class="featured-img"
          >
            <template #error>
              <div class="image-error-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </div>
        <div class="featured-content">
          <h3 class="featured-title">{{ featuredAnnouncement.title }}</h3>
          <p class="featured-excerpt">{{ featuredAnnouncement.content }}</p>
          <div class="featured-meta">
            <span class="meta-item">
              <el-icon><User /></el-icon>
              {{ featuredAnnouncement.publisherName || '物业管理处' }}
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ formatDate(featuredAnnouncement.publishTime) }}
            </span>
            <span class="meta-item">
              <el-icon><View /></el-icon>
              阅读 {{ featuredAnnouncement.readCount || 0 }}
            </span>
          </div>
        </div>
      </div>

      <!-- 普通公告（网格卡片） -->
      <div class="announcement-grid">
        <div
          class="announcement-card"
          v-for="item in normalAnnouncements"
          :key="item.id"
          @click="viewAnnouncementDetail(item)"
        >
          <div class="card-image" v-if="item.imageUrl">
            <el-image
              :src="item.imageUrl"
              fit="cover"
              class="card-img"
            >
              <template #error>
                <div class="image-error-placeholder">
                  <el-icon><Document /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
          <div class="card-content">
            <h4 class="card-title">{{ item.title }}</h4>
            <p class="card-excerpt">{{ item.content }}</p>
            <div class="card-meta">
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ formatDate(item.publishTime) }}
              </span>
              <span class="meta-item">
                <el-icon><View /></el-icon>
                {{ item.readCount || 0 }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="!featuredAnnouncement && normalAnnouncements.length === 0" description="暂无公告" />
    </section>

    <!-- 公告详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="公告详情" width="800px" class="announcement-dialog">
      <div class="detail-container" v-loading="detailLoading">
        <div class="detail-header">
          <h2 class="detail-title">{{ detailData.title }}</h2>
          <div class="detail-meta">
            <el-tag v-if="detailData.isTop === 1" type="danger" size="small">置顶</el-tag>
            <span class="meta-item">
              <el-icon><User /></el-icon>
              {{ detailData.publisherName || '物业管理处' }}
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ formatDate(detailData.publishTime) }}
            </span>
            <span class="meta-item">
              <el-icon><View /></el-icon>
              阅读 {{ detailData.readCount || 0 }} 次
            </span>
          </div>
        </div>
        <div class="detail-image" v-if="detailData.imageUrl">
          <el-image
            :src="detailData.imageUrl"
            fit="cover"
            class="detail-img"
          />
        </div>
        <div class="detail-content">
          {{ detailData.content }}
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
    <!-- 建议详情弹窗 -->
    <el-dialog v-model="suggestionDetailVisible" title="建议详情" width="700px" class="suggestion-detail-dialog">
      <div class="suggestion-detail-container" v-loading="suggestionDetailLoading">
        <div class="suggestion-detail-header">
          <div class="suggestion-tags">
            <el-tag :type="getTypeTag(detailSuggestion.type)">
              {{ getTypeLabel(detailSuggestion.type) }}
            </el-tag>
            <el-tag :type="getStatusTag(detailSuggestion.status)">
              {{ getStatusLabel(detailSuggestion.status) }}
            </el-tag>
          </div>
          <h2 class="suggestion-detail-title">{{ detailSuggestion.title }}</h2>
          <div class="suggestion-detail-meta">
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              提交时间：{{ formatDateTime(detailSuggestion.createTime) }}
            </span>
            <span class="meta-item" v-if="detailSuggestion.contactPhone">
              <el-icon><Phone /></el-icon>
              联系电话：{{ detailSuggestion.contactPhone }}
            </span>
          </div>
        </div>

        <div class="suggestion-detail-section">
          <h3 class="section-label">建议内容</h3>
          <div class="section-content">
            {{ detailSuggestion.content }}
          </div>
        </div>

        <div class="suggestion-detail-section" v-if="detailSuggestion.replyContent">
          <h3 class="section-label reply-label">
            <el-icon><ChatLineSquare /></el-icon>
            物业回复
          </h3>
          <div class="section-content reply-content">
            {{ detailSuggestion.replyContent }}
          </div>
          <div class="reply-meta" v-if="detailSuggestion.replyTime">
            <el-icon><Clock /></el-icon>
            回复时间：{{ formatDateTime(detailSuggestion.replyTime) }}
          </div>
        </div>

        <el-empty v-else description="物业暂未回复" :image-size="100" />
      </div>
      <template #footer>
        <el-button @click="suggestionDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 提交建议区域 - 两栏式布局 -->
    <section class="suggestion-section">
      <div class="section-header">
        <div class="section-title">
          <el-icon><EditPen /></el-icon>
          <h2>提交建议</h2>
        </div>
        <p class="section-subtitle">填写以下信息，我们将尽快与您联系</p>
      </div>

      <div class="suggestion-container">
        <!-- 左侧：表单/历史记录 -->
        <div class="suggestion-form-card">
          <!-- 表单视图 -->
          <el-form
              v-show="!showHistoryView"
              ref="suggestionFormRef"
              :model="suggestionForm"
              :rules="suggestionRules"
              label-position="top"
              class="suggestion-form"
          >
            <!-- 第一行：意见标题、电话 -->
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="意见标题 *" prop="title">
                  <el-input
                      v-model="suggestionForm.title"
                      placeholder="请输入意见标题"
                      clearable
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="电话 *" prop="phone">
                  <el-input
                      v-model="suggestionForm.phone"
                      placeholder="请输入联系电话"
                      clearable
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 第二行：意见类型、历史记录 -->
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="意见类型 *" prop="suggestionType">
                  <el-select
                      v-model="suggestionForm.suggestionType"
                      placeholder="请选择意见类型"
                      clearable                      style="width: 100%;"
                  >
                    <el-option label="投诉" value="complaint" />
                    <el-option label="建议" value="suggestion" />
                    <el-option label="报修" value="repair" />
                    <el-option label="咨询" value="consultation" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="历史记录">
                  <el-button
                      type="primary"
                      plain                      style="width: 100%;"
                      @click="viewHistory"
                  >
                    <el-icon><List /></el-icon>
                    查看历史记录
                  </el-button>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 第三行：意见内容 -->
            <el-form-item label="建议内容 *" prop="content">
              <el-input
                  v-model="suggestionForm.content"
                  type="textarea"
                  :rows="5"
                  placeholder="请详细描述您的建议或问题"
                  maxlength="500"
                  show-word-limit
              />
            </el-form-item>

            <el-form-item>
              <el-checkbox v-model="suggestionForm.agreed">
                我已阅读并同意<span class="privacy-link">隐私政策</span>
              </el-checkbox>
            </el-form-item>

            <el-form-item>
              <el-button
                  type="primary"
                  class="submit-btn"
                  :loading="submitting"
                  @click="submitSuggestion"
              >
                提交建议
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 历史记录视图 -->
          <div v-show="showHistoryView" class="history-view">
            <div class="history-header">
              <h3 class="history-title">
                <el-icon><List /></el-icon>
                历史记录
              </h3>
              <el-button type="primary" plain @click="backToForm">
                <el-icon><Back /></el-icon>
                返回表单
              </el-button>
            </div>

            <div v-loading="historyLoading" class="history-list">
              <div
                  v-for="item in historyList"
                  :key="item.id"
                  class="history-card"
                  @click="viewSuggestionDetail(item)"
              >
                <div class="history-card-header">
                  <el-tag :type="getTypeTag(item.type)" size="small">
                    {{ getTypeLabel(item.type) }}
                  </el-tag>
                  <el-tag :type="getStatusTag(item.status)" size="small">
                    {{ getStatusLabel(item.status) }}
                  </el-tag>
                </div>
                <h4 class="history-card-title">{{ item.title }}</h4>
                <p class="history-card-content">{{ item.content }}</p>
                <div class="history-card-footer">
                  <span class="history-time">
                    <el-icon><Clock /></el-icon>
                    {{ formatDateTime(item.createTime) }}
                  </span>
                  <span v-if="item.replyContent" class="reply-indicator">
                    <el-icon><ChatLineSquare /></el-icon>
                    已回复
                  </span>
                </div>
              </div>

              <el-empty v-if="!historyLoading && historyList.length === 0" description="暂无历史记录" />
            </div>
          </div>
        </div>

        <!-- 右侧：图片展示 -->
        <div class="suggestion-image-card">
          <el-image
              src="https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?w=1920&auto=format&fit=crop"
              fit="cover"
              class="suggestion-img"
          >
            <template #error>
              <div class="image-error-placeholder">
                <el-icon><Picture /></el-icon>
                <span>图片加载中...</span>
              </div>
            </template>
          </el-image>
        </div>
      </div>
    </section>

    <!-- 小区平面图区域 -->
    <section class="floorplan-section">
      <div class="section-header">
        <div class="section-title">
          <el-icon><MapLocation /></el-icon>
          <h2>小区平面图</h2>
        </div>
        <p class="section-subtitle">了解小区布局，便捷生活</p>
      </div>

      <div class="floorplan-container">
        <div class="floorplan-card">
          <el-image
              src="/uploads/images/Plan.png"
              fit="contain"
              class="floorplan-image"
              :preview-src-list="['/uploads/images/Plan.png']"
          >
            <template #error>
              <div class="image-error-placeholder">
                <el-icon><MapLocation /></el-icon>
                <span>平面图加载中...</span>
              </div>
            </template>
          </el-image>
        </div>
      </div>
    </section>

    <!-- 页脚区域 - 多栏信息型 -->
    <footer class="footer-section">
      <div class="footer-content">
        <!-- 第一栏：关于我们 -->
        <div class="footer-column">
          <h3 class="column-title">关于我们</h3>
          <ul class="footer-links">
            <li><a href="#">公司简介</a></li>
            <li><a href="#">发展历程</a></li>
            <li><a href="#">团队介绍</a></li>
            <li><a href="#">荣誉资质</a></li>
          </ul>
        </div>

        <!-- 第二栏：服务项目 -->
        <div class="footer-column">
          <h3 class="column-title">服务项目</h3>
          <ul class="footer-links">
            <li><a href="#">物业管理</a></li>
            <li><a href="#">设施维护</a></li>
            <li><a href="#">安保服务</a></li>
            <li><a href="#">绿化养护</a></li>
          </ul>
        </div>

        <!-- 第三栏：联系方式 -->
        <div class="footer-column">
          <h3 class="column-title">联系方式</h3>
          <div class="contact-info">
            <p class="contact-item">
              <el-icon><Phone /></el-icon>
              <span>电话：400-888-8888</span>
            </p>
            <p class="contact-item">
              <el-icon><Message /></el-icon>
              <span>邮箱：service@dinghuproperty.com</span>
            </p>
            <p class="contact-item">
              <el-icon><Location /></el-icon>
              <span>地址：XX市XX区XX路XX号</span>
            </p>
          </div>
        </div>

        <!-- 第四栏：关注我们 -->
        <div class="footer-column">
          <h3 class="column-title">关注我们</h3>
          <div class="qr-code-container">
            <div class="qr-item">
              <div class="qr-code-image">
                <el-image
                    src="/uploads/images/WeChat01.png"
                    fit="contain"
                    class="qr-img"
                />
              </div>
              <p>微信公众号</p>
            </div>
            <div class="qr-item">
              <div class="qr-code-image">
                <el-image
                    src="/uploads/images/WeChat02.png"
                    fit="contain"
                    class="qr-img"
                />
              </div>
              <p>小程序</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部版权信息 -->
      <div class="footer-bottom">
        <div class="bottom-content">
          <p class="copyright">© 2026 鼎湖物业 | 版权所有</p>
          <div class="bottom-links">
            <a href="#">隐私政策</a>
            <span class="divider">|</span>
            <a href="#">使用条款</a>
            <span class="divider">|</span>
            <a href="#">粤ICP备XXXXXXXX号</a>
          </div>
        </div>
      </div>
    </footer>

    <!-- 业主群聊浮动按钮 -->
    <div class="group-chat-float" @click="toggleGroupChatDialog">
      <el-badge :value="unreadCount" :hidden="unreadCount === 0">
        <el-icon><ChatLineRound /></el-icon>
      </el-badge>
      <span>业主群聊</span>
    </div>

    <!-- 在线客服浮动按钮 -->
    <div class="customer-service-float" @click="toggleChatDialog">
      <el-icon><ChatDotRound /></el-icon>
      <span>在线客服</span>
    </div>

    <!-- 业主群聊对话框 -->
    <div class="group-chat-dialog" v-show="groupChatDialogVisible">
      <div class="group-chat-header">
        <div class="group-chat-header-title">
          <el-icon><ChatLineRound /></el-icon>
          <span>鼎湖社区业主群</span>
        </div>
        <el-icon class="group-chat-close" @click="toggleGroupChatDialog"><Close /></el-icon>
      </div>

      <div class="group-chat-messages" ref="groupChatMessagesRef">
        <div
            v-for="msg in groupMessages"
            :key="msg.id"
            :class="['group-message-item', msg.isMine ? 'mine-message' : 'other-message']"
        >
          <div v-if="!msg.isMine" class="group-message-avatar">
            <el-icon><User /></el-icon>
          </div>
          <div class="group-message-content">
            <div class="group-message-info">
              <span class="group-message-name">{{ msg.senderName }}</span>
              <span class="group-message-time">{{ formatMessageTime(msg.sendTime) }}</span>
            </div>
            <div :class="['group-message-bubble', msg.isMine ? 'mine-bubble' : 'other-bubble']">
              <template v-if="msg.isWithdrawn">
                <span class="withdrawn-text">此消息已被撤回</span>
              </template>
              <template v-else-if="msg.messageType === 'text'">
                {{ msg.messageContent }}
              </template>
              <template v-else-if="msg.messageType === 'image'">
                <el-image
                    :src="msg.fileUrl"
                    :preview-src-list="[msg.fileUrl]"
                    fit="cover"
                    class="message-image"
                />
              </template>
              <template v-else>
                {{ msg.messageContent || '未知消息' }}
              </template>
            </div>
          </div>
          <div v-if="msg.isMine" class="group-message-avatar mine-avatar">
            <el-icon><User /></el-icon>
          </div>
        </div>

        <div v-if="groupLoading" class="loading-tip">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>

        <div v-if="groupMessages.length === 0 && !groupLoading" class="empty-tip">
          暂无消息，快来发送第一条消息吧！
        </div>
      </div>

      <div class="group-chat-input-area">
        <div class="group-chat-toolbar">
          <el-upload
              :show-file-list="false"
              :before-upload="handleUploadImage"
              accept="image/*"
          >
            <el-icon class="toolbar-icon"><Picture /></el-icon>
          </el-upload>
          <el-icon class="toolbar-icon" @click="showEmojiPicker = !showEmojiPicker">
            <ChatLineSquare />
          </el-icon>
        </div>
        <el-input
            v-model="groupChatInput"
            type="textarea"
            :rows="2"
            placeholder="输入消息..."
            @keyup.enter.exact="sendGroupMessage"
            class="group-chat-input"
        />
        <el-button
            type="primary"
            class="group-send-btn"
            @click="sendGroupMessage"
            :disabled="!groupChatInput.trim()"
        >
          发送
        </el-button>
      </div>
    </div>

    <!-- 智能客服对话框 -->
    <div :class="['chat-dialog', { maximized: isChatDialogMaximized }]" v-show="chatDialogVisible">
      <div class="chat-header">
        <div class="chat-header-title">
          <el-icon><Service /></el-icon>
          <span>智能客服</span>
        </div>
        <div class="chat-header-actions">
          <el-icon class="chat-fullscreen" @click="toggleChatDialogSize" :title="isChatDialogMaximized ? '还原' : '放大'">
            <FullScreen />
          </el-icon>
          <el-icon class="chat-top" @click="scrollToTop" title="返回顶部">
            <Top />
          </el-icon>
          <el-icon class="chat-clear" @click="clearChatHistory" title="清空记录">
            <Delete />
          </el-icon>
          <el-icon class="chat-close" @click="toggleChatDialog"><Close /></el-icon>
        </div>
      </div>

      <div class="chat-messages" ref="chatMessagesRef">
        <div
            v-for="(msg, index) in chatMessages"
            :key="index"
            :class="['message-item', msg.type === 'user' ? 'user-message' : 'bot-message']"
        >
          <div v-if="msg.type === 'bot'" class="message-avatar">
            <el-icon><Service /></el-icon>
          </div>
          <div class="message-content">
            <div :class="['message-bubble', msg.type === 'user' ? 'user-bubble' : 'bot-bubble']">
              {{ msg.content }}
            </div>
            <div class="message-time">{{ msg.time }}</div>
          </div>
          <div v-if="msg.type === 'user'" class="message-avatar user-avatar">
            <el-icon><User /></el-icon>
          </div>
        </div>

        <div v-if="isTyping" class="message-item bot-message">
          <div class="message-avatar">
            <el-icon><Service /></el-icon>
          </div>
          <div class="message-content">
            <div class="message-bubble bot-bubble typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷问题 -->
      <div class="quick-questions">
        <el-tag
            v-for="(q, index) in quickQuestions"
            :key="index"
            class="quick-tag"
            size="small"
            effect="plain"
            @click="selectQuickQuestion(q)"
        >
          {{ q }}
        </el-tag>
      </div>

      <div class="chat-input-area">
        <el-input
            v-model="chatInput"
            type="textarea"
            :rows="2"
            placeholder="请输入您的问题..."
            @keyup.enter.exact="sendCustomerMessage"
            class="chat-input"
            :disabled="chatLoading"
        />
        <el-button
            type="primary"
            class="send-btn"
            @click="sendCustomerMessage"
            :disabled="!chatInput.trim() || chatLoading"
            :loading="chatLoading"
        >
          {{ chatLoading ? '思考中...' : '发送' }}
        </el-button>
      </div>
    </div>

    <!-- 主内容区 -->
<!--    <main class="main-content">-->
<!--      <router-view />-->
<!--    </main>-->
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick  } from 'vue'
import { useRouter } from 'vue-router'
import {
  Sunny,
  Picture,
  ArrowLeft,
  ArrowRight,
  Bell,
  Top,
  User,
  Clock,
  View,
  Document,
  EditPen,
  MapLocation,
  Phone,
  Message,
  Location,
  ChatDotRound,
  Service,
  Close,
  ChatLineRound,
  Loading,
  ChatLineSquare,
  ArrowDown,
  Avatar,
  SwitchButton,
  Delete,
  FullScreen,
  List,
  Back
} from '@element-plus/icons-vue'
import { getMessageList, sendMessage, withdrawMessage, uploadFile } from '@/api/messageApi.js'
import { ownerGetAnnouncementList, ownerGetAnnouncementDetail } from '@/api/announcementApi.js'
import { getActiveCarousels } from '@/api/adminApi.js'
import { chatWithDeepSeek } from '@/api/visitorApi.js'
import { submitSuggestion as submitSuggestionApi, getOwnerSuggestionList, getSuggestionDetail } from '@/api/suggestionApi.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

// 用户登录状态
const isLoggedIn = ref(false)
const username = ref('')

// 轮播图数据
const carouselList = ref([])
const currentIndex = ref(0)
const carouselRef = ref(null)

// 公告数据
const announcementLoading = ref(false)
const featuredAnnouncement = ref(null)
const normalAnnouncements = ref([])

// 详情弹窗
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref({})
// 建议表单
const suggestionFormRef = ref(null)
const submitting = ref(false)
const suggestionForm = ref({
  title: '',
  phone: '',
  suggestionType: '',
  content: '',
  agreed: false
})
const showHistoryView = ref(false)
const historyLoading = ref(false)
const historyList = ref([])

// 建议详情
const suggestionDetailVisible = ref(false)
const suggestionDetailLoading = ref(false)
const detailSuggestion = ref({})

// 智能客服
const chatDialogVisible = ref(false)
const chatMessages = ref([])
const chatInput = ref('')
const isTyping = ref(false)
const chatMessagesRef = ref(null)
const chatLoading = ref(false)
const isChatDialogMaximized = ref(false)


// 业主群聊
const groupChatDialogVisible = ref(false)
const groupMessages = ref([])
const groupChatInput = ref('')
const groupLoading = ref(false)
const groupChatMessagesRef = ref(null)
const unreadCount = ref(0)
const showEmojiPicker = ref(false)

// 表单校验规则
const suggestionRules = {
  title: [
    { required: true, message: '请输入意见标题', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  suggestionType: [
    { required: true, message: '请选择意见类型', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入建议内容', trigger: 'blur' },
    { min: 10, message: '建议内容至少10个字', trigger: 'blur' }
  ]
}
// 导航跳转
const navigateTo = (path) => {
  router.push(path)
}

// 切换到简洁模式
const switchToSimpleMode = () => {
  // 设置模式状态到localStorage
  localStorage.setItem('isStandardMode', 'false')
  // 跳转到简洁模式首页
  router.push('/owner/OwnerIndex')
  ElMessage.success('已切换到简洁模式')
}

// 切换简洁模式
const toggleMode = () => {
  console.log('切换简洁模式')
}

// 登录按钮
const handleLogin = () => {
  router.push('/login')
}

// 用户下拉菜单命令处理
const handleUserCommand = (command) => {
  if (command === 'profile') {
    router.push('/owner/Person')
  } else if (command === 'logout') {
    handleLogout()
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
    isLoggedIn.value = false
    username.value = ''
    ElMessage.success('退出登录成功')
  }).catch(() => {
    ElMessage.info('已取消退出')
  })
}

// 初始化用户信息
const initUserInfo = () => {
  const token = localStorage.getItem('token')
  const user = localStorage.getItem('user')

  if (token && user) {
    try {
      const userData = JSON.parse(user)
      isLoggedIn.value = true
      username.value = userData.username || userData.name || '用户'
    } catch (error) {
      console.error('解析用户信息失败:', error)
      isLoggedIn.value = false
    }
  }
}

// 加载轮播图数据
const loadCarousels = async () => {
  try {
    const res = await getActiveCarousels()
    console.log('轮播图响应:', res)

    if (res.data && res.data.code === 200) {
      let carouselData = res.data.data
      if (!carouselData) {
        carouselData = []
      } else if (!Array.isArray(carouselData)) {
        carouselData = [carouselData]
      }

      carouselList.value = carouselData.slice(0, 5)
      console.log('轮播图列表:', carouselList.value)

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

// 加载公告数据
const loadAnnouncements = async () => {
  announcementLoading.value = true
  try {
    const res = await ownerGetAnnouncementList({
      pageNum: 1,
      pageSize: 10,
      isTop: null,
      recallStatus: 0
    })

    console.log('公告响应:', res)

    if (res.data && res.data.code === 200 && res.data.data) {
      const records = res.data.data.records || []

      // 分离置顶和普通公告
      const topAnnouncements = records.filter(item => item.isTop === 1)
      const normalList = records.filter(item => item.isTop !== 1)

      // 置顶公告取第一个
      featuredAnnouncement.value = topAnnouncements.length > 0 ? topAnnouncements[0] : null

      // 普通公告最多显示6个
      normalAnnouncements.value = normalList.slice(0, 4)

      console.log('置顶公告:', featuredAnnouncement.value)
      console.log('普通公告:', normalAnnouncements.value)
    } else {
      featuredAnnouncement.value = null
      normalAnnouncements.value = []
    }
  } catch (error) {
    console.error('加载公告失败:', error)
    ElMessage.error('加载公告失败')
    featuredAnnouncement.value = null
    normalAnnouncements.value = []
  } finally {
    announcementLoading.value = false
  }
}

// 查看全部公告
const viewAllAnnouncements = () => {
  router.push('/announcement/list')
}

// 查看公告详情
const viewAnnouncementDetail = async (announcement) => {
  try {
    detailDialogVisible.value = true
    detailLoading.value = true

    const res = await ownerGetAnnouncementDetail(announcement.id)
    console.log('公告详情响应:', res)

    if (res.data && res.data.code === 200) {
      detailData.value = res.data.data
    } else {
      detailData.value = announcement
    }
  } catch (error) {
    console.error('查看公告详情失败:', error)
    ElMessage.error('查看公告详情失败')
    detailData.value = announcement
  } finally {
    detailLoading.value = false
  }
}

// 提交建议
const submitSuggestion = async () => {
  if (!suggestionFormRef.value) return

  try {
    await suggestionFormRef.value.validate()

    if (!suggestionForm.value.agreed) {
      ElMessage.warning('请先同意隐私政策')
      return
    }

    submitting.value = true

    const res = await submitSuggestionApi({
      type: suggestionForm.value.suggestionType,
      title: suggestionForm.value.title,
      content: suggestionForm.value.content,
      contactPhone: suggestionForm.value.phone
    })

    if (res.data && res.data.code === 200) {
      ElMessage.success('建议提交成功，感谢您的反馈！')
      // 重置表单
      suggestionForm.value = {
        title: '',
        phone: '',
        suggestionType: '',
        content: '',
        agreed: false
      }
      suggestionFormRef.value.resetFields()
    } else {
      ElMessage.error(res.data.msg || '提交失败，请稍后重试')
    }
  } catch (error) {
    if (error.name === 'ValidationError') {
      ElMessage.error('请完善必填项后重试')
    } else {
      console.error('提交建议失败:', error)
      ElMessage.error('提交失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

// 查看历史记录
const viewHistory = async () => {
  showHistoryView.value = true
  historyLoading.value = true

  try {
    const res = await getOwnerSuggestionList()

    if (res.data && res.data.code === 200) {
      historyList.value = res.data.data || []
    } else {
      ElMessage.error(res.data.msg || '加载历史记录失败')
      historyList.value = []
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
    ElMessage.error('加载历史记录失败')
    historyList.value = []
  } finally {
    historyLoading.value = false
  }
}

// 返回表单
const backToForm = () => {
  showHistoryView.value = false
  historyList.value = []
}

// 获取类型标签
const getTypeTag = (type) => {
  const tagMap = {
    complaint: 'danger',
    suggestion: 'success',
    repair: 'warning',
    consultation: 'info'
  }
  return tagMap[type] || 'info'
}

// 获取类型文本
const getTypeLabel = (type) => {
  const labelMap = {
    complaint: '投诉',
    suggestion: '建议',
    repair: '报修',
    consultation: '咨询'
  }
  return labelMap[type] || '其他'
}

// 获取状态标签
const getStatusTag = (status) => {
  const tagMap = {
    pending: 'info',
    processing: 'warning',
    processed: 'success',
    replied: 'success'
  }
  return tagMap[status] || 'info'
}

// 获取状态文本
const getStatusLabel = (status) => {
  const labelMap = {
    pending: '待处理',
    processing: '处理中',
    processed: '已处理',
    replied: '已回复'
  }
  return labelMap[status] || '未知'
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return ''
  const date = new Date(datetime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 查看建议详情
const viewSuggestionDetail = async (item) => {
  suggestionDetailVisible.value = true
  suggestionDetailLoading.value = true

  try {
    const res = await getSuggestionDetail(item.id)

    if (res.data && res.data.code === 200) {
      detailSuggestion.value = res.data.data
    } else {
      ElMessage.error(res.data.msg || '加载详情失败')
      suggestionDetailVisible.value = false
    }
  } catch (error) {
    console.error('加载建议详情失败:', error)
    ElMessage.error('加载详情失败')
    suggestionDetailVisible.value = false
  } finally {
    suggestionDetailLoading.value = false
  }
}

// 切换客服对话框
const toggleChatDialog = () => {
  chatDialogVisible.value = !chatDialogVisible.value
  if (chatDialogVisible.value) {
    // 如果是首次打开，初始化欢迎消息
    if (chatMessages.value.length === 0) {
      chatMessages.value.push({
        type: 'bot',
        content: '您好！我是鼎湖物业智能客服，请问有什么可以帮助您的？',
        time: getCurrentTime()
      })
    }
    nextTick(() => {
      if (chatMessagesRef.value) {
        chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
      }
    })
  }
}

// 快捷问题
const quickQuestions = [
  '如何缴纳物业费？',
  '报修流程是什么？',
  '如何申请访客通行证？',
  '停车场收费标准？',
  '物业工作时间？'
]

// 选择快捷问题
const selectQuickQuestion = (question) => {
  chatInput.value = question
  sendCustomerMessage()
}

// 清空聊天记录
const clearChatHistory = () => {
  ElMessageBox.confirm(
      '确定要清空聊天记录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(() => {
    chatMessages.value = []
    ElMessage.success('已清空聊天记录')
  }).catch(() => {})
}

// 滚动到顶部
const scrollToTop = () => {
  if (chatMessagesRef.value) {
    chatMessagesRef.value.scrollTo({
      top: 0,
      behavior: 'smooth'
    })
  }
}

// 切换对话框大小
const toggleChatDialogSize = () => {
  isChatDialogMaximized.value = !isChatDialogMaximized.value
}

// 获取当前时间
const getCurrentTime = () => {
  const now = new Date()
  return now.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 发送消息
const sendCustomerMessage = async () => {
  if (!chatInput.value.trim()) return

  const userMessage = {
    type: 'user',
    content: chatInput.value.trim(),
    time: getCurrentTime()
  }

  chatMessages.value.push(userMessage)
  const question = chatInput.value.trim()
  chatInput.value = ''
  chatLoading.value = true

  // 滚动到底部
  nextTick(() => {
    if (chatMessagesRef.value) {
      chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
    }
  })

  // 显示正在输入
  isTyping.value = true

  try {
    // 调用 DeepSeek AI 接口
    const systemPrompt = `你是鼎湖物业的智能客服助手，请根据以下信息回答业主的问题：

【物业基本信息】
- 物业名称：鼎湖物业
- 服务热线：400-888-8888
- 服务邮箱：service@dinghuproperty.com
- 工作时间：周一至周日 8:00-22:00
- 紧急维修：24小时响应

【物业费缴纳】
- 缴费方式：可通过"我的账单"在线缴纳，支持微信、支付宝、银行卡
- 缴费周期：按月/季度/年缴纳
- 逾期处理：逾期将产生滞纳金（每日万分之五）

【报修服务】
- 报修流程：导航栏"报修服务" -> 填写报修信息 -> 提交 -> 物业24小时内处理
- 紧急报修：请直接拨打服务热线 400-888-8888
- 维修时间：一般报修48小时内完成，紧急报修2小时内响应

【访客管理】
- 访客申请：业主可通过"来访申请"提前登记访客信息
- 访客通行：访客凭二维码或登记信息进入小区
- 停车：访客车辆可临时停车，收费标准请咨询物业

【停车场】
- 业主停车：可办理月卡（300元/月）
- 访客停车：临时停车5元/小时，封顶30元/天
- 充电桩：地下停车场配有充电桩，按需使用

【其他服务】
- 公告查看：及时关注小区公告动态
- 投诉建议：可通过"提交建议"反馈问题
- 业主群聊：加入业主群了解社区动态

请用友好、专业的语气回答，如果问题超出你的知识范围，请建议业主联系人工客服（400-888-8888）。`

    const res = await chatWithDeepSeek({
      message: systemPrompt + '\n\n业主问题：' + question
    })

    isTyping.value = false
    chatLoading.value = false

    console.log('AI原始响应:', res)
    console.log('res.data:', res.data)
    console.log('res.data类型:', typeof res.data)

    // 处理不同的响应格式
    let aiReply = ''

    if (typeof res.data === 'string') {
      // 如果 res.data 是字符串，直接使用
      aiReply = res.data
    } else if (res.data && typeof res.data === 'object') {
      // 如果是对象，尝试提取 content 字段
      aiReply = res.data.content || res.data.message || res.data.answer || res.data.text || JSON.stringify(res.data)
    } else if (typeof res === 'string') {
      // 如果 res 本身是字符串
      aiReply = res
    } else {
      aiReply = '抱歉，我暂时无法回答这个问题，请拨打服务热线 400-888-8888 联系人工客服。'
    }

    // 清理回复内容：移除多余的转义字符和JSON残留
    aiReply = aiReply
        .replace(/\\n/g, '\n')  // 将 \\n 转换为实际换行
        .replace(/\\t/g, '  ')  // 将 \\t 转换为空格
        .replace(/\\\\/g, '\\') // 处理双反斜杠

    console.log('清理后的AI回复:', aiReply)

    chatMessages.value.push({
      type: 'bot',
      content: aiReply,
      time: getCurrentTime()
    })

    nextTick(() => {
      if (chatMessagesRef.value) {
        chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
      }
    })
  } catch (error) {
    console.error('AI回复失败:', error)
    isTyping.value = false
    chatLoading.value = false

    chatMessages.value.push({
      type: 'bot',
      content: '抱歉，智能客服暂时不可用，请拨打服务热线 400-888-8888 联系人工客服。',
      time: getCurrentTime()
    })

    ElMessage.error('智能客服响应失败')
  }
}

// 切换群聊对话框
const toggleGroupChatDialog = () => {
  groupChatDialogVisible.value = !groupChatDialogVisible.value
  if (groupChatDialogVisible.value) {
    loadGroupMessages()
    unreadCount.value = 0
  }
}

// 加载群聊消息
const loadGroupMessages = async () => {
  groupLoading.value = true
  try {
    const res = await getMessageList({
      chatRoomName: '鼎湖社区业主群',
      pageNum: 1,
      pageSize: 50
    })

    console.log('群聊消息响应:', res)
    console.log('res.data:', res.data)
    console.log('res.data.data:', res.data.data)

    if (res.data && res.data.code === 200) {
      const responseData = res.data.data

      // 处理不同的数据结构
      let records = []
      if (responseData.records) {
        // 分页数据格式：{records: [...], total: ...}
        records = responseData.records
      } else if (Array.isArray(responseData)) {
        // 直接返回数组
        records = responseData
      } else if (responseData.list) {
        // 另一种分页格式：{list: [...]}
        records = responseData.list
      }

      console.log('解析后的消息记录:', records)
      if (records.length > 0) {
        console.log('第一条消息示例:', records[0])
      }

      const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
      console.log('当前用户信息:', currentUser)
      console.log('currentUser.id:', currentUser.id)
      console.log('currentUser.userId:', currentUser.userId)

      groupMessages.value = records.map(msg => {
        const isMine = msg.senderId === currentUser.id ||
            msg.senderId === currentUser.userId ||
            String(msg.senderId) === String(currentUser.id) ||
            String(msg.senderId) === String(currentUser.userId)

        if (isMine) {
          console.log('匹配到自己的消息:', {
            msgSenderId: msg.senderId,
            msgSenderName: msg.senderName,
            currentUserId: currentUser.id,
            currentUserName: currentUser.name
          })
        }

        return {
          ...msg,
          isMine: isMine
        }
      })

      console.log('最终消息列表:', groupMessages.value)
      if (groupMessages.value.length > 0) {
        console.log('第一条消息映射后:', groupMessages.value[0])
        console.log('消息内容字段:', groupMessages.value[0].messageContent)
        console.log('消息时间字段:', groupMessages.value[0].sendTime)
      }

      nextTick(() => {
        if (groupChatMessagesRef.value) {
          groupChatMessagesRef.value.scrollTop = groupChatMessagesRef.value.scrollHeight
        }
      })
    } else {
      ElMessage.error(res.data.msg || '加载消息失败')
    }
  } catch (error) {
    console.error('加载群聊消息失败:', error)
    ElMessage.error('加载消息失败')
  } finally {
    groupLoading.value = false
  }
}

// 发送群聊消息
const sendGroupMessage = async () => {
  if (!groupChatInput.value.trim()) return

  const user = JSON.parse(localStorage.getItem('user') || '{}')

  try {
    const res = await sendMessage({
      chatRoomName: '鼎湖社区业主群',
      messageContent: groupChatInput.value.trim(),
      messageType: 'text',
      senderId: user.id,
      senderName: user.name || '业主'
    })

    if (res.data && res.data.code === 200) {
      groupChatInput.value = ''
      loadGroupMessages()
    } else {
      ElMessage.error(res.data.msg || '发送失败')
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送失败')
  }
}

// 上传图片
const handleUploadImage = async (file) => {
  const formData = new FormData()
  formData.append('file', file)

  try {
    const res = await uploadFile(formData)

    if (res.data && res.data.code === 200) {
      const imageUrl = res.data.data
      const user = JSON.parse(localStorage.getItem('user') || '{}')

      await sendMessage({
        chatRoomName: '鼎湖社区业主群',
        messageContent: imageUrl,
        messageType: 'image',
        fileUrl: imageUrl,
        senderId: user.id,
        senderName: user.name || '业主'
      })

      loadGroupMessages()
      ElMessage.success('图片发送成功')
    }
  } catch (error) {
    console.error('上传图片失败:', error)
    ElMessage.error('图片上传失败')
  }

  return false
}

// 格式化日期
const formatDate = (datetime) => {
  if (!datetime) return ''
  const date = new Date(datetime)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 格式化消息时间
const formatMessageTime = (datetime) => {
  if (!datetime) return ''
  const date = new Date(datetime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 点击轮播图
const handleCarouselClick = (item) => {
  if (item.targetUrl) {
    window.open(item.targetUrl, '_blank')
  }
}

// 上一张
const prevSlide = () => {
  if (carouselRef.value) {
    carouselRef.value.prev()
  }
}

// 下一张
const nextSlide = () => {
  if (carouselRef.value) {
    carouselRef.value.next()
  }
}

// 跳转到指定轮播图
const goToSlide = (index) => {
  if (carouselRef.value) {
    carouselRef.value.setActiveItem(index)
  }
}

// 处理轮播图切换事件
const handleCarouselChange = (index) => {
  currentIndex.value = index
}

onMounted(() => {
  loadCarousels()
  loadAnnouncements()
  initUserInfo()
})
</script>

<style scoped>
.owner-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* 导航栏样式 */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  padding: 0 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.navbar-brand {
  flex-shrink: 0;
}

.brand-name {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #ffffff;
  letter-spacing: 2px;
}

.navbar-menu {
  display: flex;
  gap: 8px;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.nav-btn {
  background-color: transparent !important;
  border: none !important;
  color: #ffffff !important;
  font-size: 15px;
  font-weight: 500;
  padding: 10px 18px;
  height: auto;
  transition: all 0.3s ease;
}

.nav-btn:hover {
  background-color: rgba(255, 255, 255, 0.15) !important;
  color: #ffffff !important;
  transform: translateY(-1px);
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #ffffff;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.user-dropdown:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.user-dropdown .username {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-dropdown .el-icon {
  font-size: 18px;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-dropdown-menu__item .el-icon) {
  font-size: 16px;
}
.mode-btn {
  background-color: rgba(255, 255, 255, 0.2) !important;
  border: 1px solid rgba(255, 255, 255, 0.4) !important;
  color: #ffffff !important;
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 20px;
}

.mode-btn:hover {
  background-color: rgba(255, 255, 255, 0.3) !important;
}

.login-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%) !important;
  border: none !important;
  color: #ffffff !important;
  font-size: 15px;
  font-weight: 500;
  padding: 8px 24px;
  border-radius: 20px;
}

.login-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

/* 全屏轮播图 */
.hero-carousel {
  width: 100%;
  height: calc(100vh - 60px);
  position: relative;
  margin-top: 60px;
}

.carousel-slide {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  cursor: pointer;
}

.slide-image {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
}

.slide-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
      to bottom,
      rgba(0, 0, 0, 0.1) 0%,
      rgba(0, 0, 0, 0.2) 50%,
      rgba(0, 0, 0, 0.4) 100%
  );
  z-index: 1;
}

.slide-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 2;
  text-align: center;
  color: #ffffff;
  width: 80%;
  max-width: 1200px;
}

.slide-title {
  font-size: 56px;
  font-weight: 700;
  margin: 0 0 20px 0;
  line-height: 1.2;
  text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.5);
  letter-spacing: 2px;
}

.slide-description {
  font-size: 20px;
  margin: 0;
  opacity: 0.9;
  line-height: 1.6;
  text-shadow: 1px 1px 4px rgba(0, 0, 0, 0.5);
}

.carousel-arrows {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  transform: translateY(-50%);
  z-index: 3;
  display: flex;
  justify-content: space-between;
  padding: 0 30px;
  pointer-events: none;
}

.arrow {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  pointer-events: auto;
  transition: all 0.3s ease;
  color: #ffffff;
  font-size: 24px;
}

.arrow:hover {
  background-color: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.carousel-indicators {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 3;
  display: flex;
  gap: 12px;
  align-items: center;
}

.indicator-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.4);
  border: 2px solid rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.3s ease;
}

.indicator-dot:hover {
  background-color: rgba(255, 255, 255, 0.6);
  transform: scale(1.2);
}

.indicator-dot.active {
  background-color: #ffffff;
  width: 30px;
  border-radius: 6px;
  border-color: #ffffff;
}

/* 公告展示区域 */
.announcement-section {
  padding: 60px 30px;
  max-width: 1400px;
  margin: 0 auto;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e5e7eb;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-title .el-icon {
  font-size: 28px;
  color: #667eea;
}

.section-title h2 {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
}

.view-all-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  color: #ffffff !important;
  font-size: 15px;
  font-weight: 500;
  padding: 10px 24px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.view-all-btn:hover {
  opacity: 0.9;
  transform: translateX(4px);
}

/* 置顶公告（大卡片） */
.featured-announcement {
  position: relative;
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 40px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  min-height: 400px;
}

.featured-announcement:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.featured-badge {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 10;
  background: linear-gradient(135deg, #f5576c 0%, #ff6b6b 100%);
  color: #ffffff;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 2px 8px rgba(245, 87, 108, 0.3);
}

.featured-badge .el-icon {
  font-size: 16px;
}

.featured-image {
  flex: 0 0 50%;
  position: relative;
  overflow: hidden;
}

.featured-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-error-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #e5e7eb 100%);
  color: #909399;
}

.image-error-placeholder .el-icon {
  font-size: 64px;
}

.featured-content {
  flex: 0 0 50%;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.featured-title {
  margin: 0 0 16px 0;
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.4;
}

.featured-excerpt {
  margin: 0 0 24px 0;
  font-size: 16px;
  color: #6b7280;
  line-height: 1.8;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.featured-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #9ca3af;
}

.meta-item .el-icon {
  font-size: 16px;
}

/* 普通公告（网格卡片） */
.announcement-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.announcement-card {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
}

.announcement-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.card-image {
  width: 100%;
  height: 180px;
  overflow: hidden;
}

.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.announcement-card:hover .card-img {
  transform: scale(1.05);
}

.card-content {
  padding: 20px;
}

.card-title {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-excerpt {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  gap: 16px;
}

/* 提交建议区域 */
.suggestion-section {
  padding: 60px 30px;
  max-width: 1400px;
  margin: 0 auto;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 0;
}

.suggestion-section .section-header {
  text-align: center;
  margin-bottom: 40px;
}

.suggestion-section .section-title {
  justify-content: center;
  margin-bottom: 12px;
}

.section-subtitle {
  margin: 0;
  font-size: 16px;
  color: #6b7280;
}

.suggestion-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  align-items: flex-start;
}

/* 左侧表单卡片 */
.suggestion-form-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.suggestion-form {
  max-width: 100%;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #374151;
  font-size: 14px;
  padding-bottom: 8px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #d1d5db inset;
  padding: 10px 12px;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #667eea inset;
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #d1d5db inset;
  padding: 12px;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #d1d5db inset;
}

.privacy-link {
  color: #667eea;
  text-decoration: none;
  margin-left: 4px;
}

.privacy-link:hover {
  text-decoration: underline;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
}

.submit-btn:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 右侧图片卡片 */
.suggestion-image-card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  height: 320px;
  position: sticky;
  top: 80px;
}

.suggestion-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 小区平面图区域 */
.floorplan-section {
  padding: 60px 30px;
  //background: linear-gradient(135deg, #f0f7ff 0%, #e1f0ff 100%);
}

.floorplan-section .section-header {
  text-align: center;
  margin-bottom: 40px;
}

.floorplan-section .section-title {
  justify-content: center;
  margin-bottom: 12px;
}

.floorplan-section .section-title .el-icon {
  font-size: 28px;
  //color: rgba(225, 240, 255, 0.4);
}

.floorplan-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.floorplan-card {
  width: 50%;
  background: #ffffff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  cursor: pointer;
  transition: all 0.3s ease;
}

.floorplan-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.16);
}

.floorplan-image {
  width: 100%;
  border-radius: 8px;
}

/* 在线客服浮动按钮 */
.customer-service-float {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 2000;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  padding: 14px 20px;
  border-radius: 25px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  font-size: 14px;
  font-weight: 500;
}

.customer-service-float:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.customer-service-float .el-icon {
  font-size: 20px;
}

/* 智能客服对话框 */
.chat-dialog {
  position: fixed;
  bottom: 90px;
  right: 30px;
  z-index: 2001;
  width: 380px;
  height: 520px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideUp 0.3s ease;
  transition: all 0.3s ease;
}

.chat-dialog.maximized {
  width: 600px;
  height: 700px;
  bottom: 50%;
  right: 50%;
  transform: translate(50%, 50%);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.chat-header-title .el-icon {
  font-size: 20px;
}

.chat-header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-fullscreen {
  font-size: 18px;
  cursor: pointer;
  transition: transform 0.3s ease;
  opacity: 0.8;
}

.chat-fullscreen:hover {
  opacity: 1;
  transform: scale(1.1);
}

.chat-top {
  font-size: 18px;
  cursor: pointer;
  transition: transform 0.3s ease;
  opacity: 0.8;
}

.chat-top:hover {
  opacity: 1;
  transform: translateY(-2px);
}

.chat-clear {
  font-size: 18px;
  cursor: pointer;
  transition: transform 0.3s ease;
  opacity: 0.8;
}

.chat-clear:hover {
  opacity: 1;
  transform: scale(1.1);
}



.chat-close {
  font-size: 20px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.chat-close:hover {
  transform: rotate(90deg);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.message-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.user-message {
  flex-direction: row-reverse;
  justify-content: flex-start;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-avatar {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.message-avatar .el-icon {
  font-size: 18px;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 260px;
  order: 1;
}

.user-message .message-content {
  align-items: flex-end;
  order: 2;
}

.user-message .message-avatar {
  order: 1;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.bot-bubble {
  background: #ffffff;
  color: #1f2937;
  border-top-left-radius: 4px;
}

.user-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border-top-right-radius: 4px;
}

.message-time {
  font-size: 12px;
  color: #9ca3af;
}

.user-message .message-time {
  text-align: right;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 16px 20px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #6b7280;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

/* 快捷问题 */
.quick-questions {
  padding: 8px 16px;
  background: #f8f9fa;
  border-top: 1px solid #e5e7eb;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-tag {
  cursor: pointer;
  transition: all 0.3s ease;
}

.quick-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

/* 输入区域 */
.chat-input-area {
  padding: 16px;
  background: #ffffff;
  border-top: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-input {
  border-radius: 8px;
}

:deep(.chat-input .el-textarea__inner) {
  border-radius: 8px;
  resize: none;
}

.send-btn {
  align-self: flex-end;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  border-radius: 8px;
  padding: 8px 24px;
}

.send-btn:hover {
  opacity: 0.9;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 业主群聊浮动按钮 */
.group-chat-float {
  position: fixed;
  bottom: 90px;
  right: 30px;
  z-index: 1999;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: #ffffff;
  padding: 14px 20px;
  border-radius: 25px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(16, 185, 129, 0.4);
  transition: all 0.3s ease;
  font-size: 14px;
  font-weight: 500;
}

.group-chat-float:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.5);
}

.group-chat-float .el-icon {
  font-size: 20px;
}

.customer-service-float {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 2000;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  padding: 14px 20px;
  border-radius: 25px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  font-size: 14px;
  font-weight: 500;
}

.customer-service-float:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.customer-service-float .el-icon {
  font-size: 20px;
}
/* 业主群聊对话框 */
.group-chat-dialog {
  position: fixed;
  bottom: 150px;
  right: 30px;
  z-index: 2002;
  width: 420px;
  height: 580px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

.group-chat-header {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: #ffffff;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.group-chat-header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.group-chat-header-title .el-icon {
  font-size: 20px;
}

.group-chat-close {
  font-size: 20px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.group-chat-close:hover {
  transform: rotate(90deg);
}

.group-chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f3f4f6;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.group-chat-messages::-webkit-scrollbar {
  width: 6px;
}

.group-chat-messages::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.group-message-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.mine-message {
  flex-direction: row;
  justify-content: flex-end;
}

.group-message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.mine-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.group-message-avatar .el-icon {
  font-size: 18px;
}

.group-message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 280px;
}

.group-message-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.group-message-name {
  color: #374151;
  font-weight: 500;
}

.mine-message .group-message-info {
  flex-direction: row-reverse;
}

.group-message-time {
  color: #9ca3af;
}

.group-message-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
}

.other-bubble {
  background: #ffffff;
  color: #1f2937;
  border-top-left-radius: 4px;
}

.mine-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border-top-right-radius: 4px;
}

.message-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  cursor: pointer;
}

.withdrawn-tip {
  font-size: 12px;
  color: #9ca3af;
  font-style: italic;
  margin-top: 4px;
}

.withdrawn-text {
  font-size: 12px;
  color: #9ca3af;
  font-style: italic;
}

.loading-tip,
.empty-tip {
  text-align: center;
  color: #9ca3af;
  font-size: 14px;
  padding: 20px;
}

.loading-tip .el-icon {
  margin-right: 8px;
}

/* 群聊输入区域 */
.group-chat-input-area {
  padding: 12px 16px;
  background: #ffffff;
  border-top: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.group-chat-toolbar {
  display: flex;
  gap: 12px;
}

.toolbar-icon {
  font-size: 20px;
  color: #6b7280;
  cursor: pointer;
  transition: color 0.3s ease;
}

.toolbar-icon:hover {
  color: #10b981;
}

.group-chat-input {
  border-radius: 8px;
}

:deep(.group-chat-input .el-textarea__inner) {
  border-radius: 8px;
  resize: none;
}

.group-send-btn {
  align-self: flex-end;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%) !important;
  border: none !important;
  border-radius: 8px;
  padding: 8px 24px;
}

.group-send-btn:hover {
  opacity: 0.9;
}

.group-send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
/* 主内容区 */
.main-content {
  padding-top: 80px;
  min-height: calc(100vh - 60px);
  width: 100%;
}

/* 图片加载失败样式 */
.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 14px;
}

.image-error .el-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

/* Element Plus 轮播图样式覆盖 */
:deep(.el-carousel) {
  height: 100% !important;
}

:deep(.el-carousel__container) {
  height: 100% !important;
}

:deep(.el-carousel__item) {
  overflow: hidden;
}

/* 公告详情弹窗样式 */
:deep(.announcement-dialog .el-dialog__header) {
  border-bottom: 2px solid #f3f4f6;
  padding-bottom: 16px;
}

:deep(.announcement-dialog .el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
}

.detail-container {
  padding: 10px 0;
}

.detail-header {
  margin-bottom: 24px;
}

.detail-title {
  margin: 0 0 16px 0;
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.4;
}

.detail-meta {
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.detail-image {
  margin-bottom: 24px;
  border-radius: 8px;
  overflow: hidden;
}

.detail-img {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
}

.detail-content {
  font-size: 16px;
  color: #374151;
  line-height: 1.8;
  white-space: pre-wrap;
}
/* 页脚区域 */
.footer-section {
  background: linear-gradient(135deg, #1f2937 0%, #111827 100%);
  color: #ffffff;
  padding-top: 60px;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
  padding-bottom: 40px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-column {
  display: flex;
  flex-direction: column;
}

.column-title {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
  position: relative;
  padding-bottom: 12px;
}

.column-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 40px;
  height: 3px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}

.footer-links {
  list-style: none;
  margin: 0;
  padding: 0;
}

.footer-links li {
  margin-bottom: 12px;
}

.footer-links a {
  color: #9ca3af;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s ease;
  display: inline-block;
}

.footer-links a:hover {
  color: #ffffff;
  transform: translateX(4px);
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  color: #9ca3af;
  font-size: 14px;
}

.contact-item .el-icon {
  font-size: 18px;
  color: #667eea;
  flex-shrink: 0;
}

.qr-code-container {
  display: flex;
  gap: 20px;
}

.qr-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.qr-code-image {
  width: 100px;
  height: 100px;
  background: #ffffff;
  border-radius: 8px;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qr-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.qr-item p {
  margin: 0;
  font-size: 12px;
  color: #9ca3af;
}

/* 底部版权信息 */
.footer-bottom {
  background: rgba(0, 0, 0, 0.2);
  padding: 20px 30px;
}

.bottom-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.copyright {
  margin: 0;
  color: #9ca3af;
  font-size: 14px;
}

.bottom-links {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bottom-links a {
  color: #9ca3af;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s ease;
}

.bottom-links a:hover {
  color: #ffffff;
}

.divider {
  color: #4b5563;
}
/* 历史记录视图 */
.history-view {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e5e7eb;
}

.history-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.history-title .el-icon {
  font-size: 24px;
  color: #667eea;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: calc(100% - 60px);
}

.history-list::-webkit-scrollbar {
  width: 6px;
}

.history-list::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.history-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.history-card:hover {
  background: #ffffff;
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.history-card-header {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.history-card-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
}

.history-card-content {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.history-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #9ca3af;
}

.history-time {
  display: flex;
  align-items: center;
  gap: 4px;
}

.history-time .el-icon {
  font-size: 14px;
}

.reply-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #10b981;
  font-weight: 500;
}

.reply-indicator .el-icon {
  font-size: 14px;
}
/* 建议详情弹窗样式 */
:deep(.suggestion-detail-dialog .el-dialog__header) {
  border-bottom: 2px solid #f3f4f6;
  padding-bottom: 16px;
}

:deep(.suggestion-detail-dialog .el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
}

.suggestion-detail-container {
  padding: 10px 0;
}

.suggestion-detail-header {
  margin-bottom: 24px;
}

.suggestion-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.suggestion-detail-title {
  margin: 0 0 16px 0;
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.4;
}

.suggestion-detail-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.suggestion-detail-meta .meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #6b7280;
}

.suggestion-detail-meta .meta-item .el-icon {
  font-size: 16px;
  color: #9ca3af;
}

.suggestion-detail-section {
  margin-bottom: 24px;
}

.section-label {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #374151;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-label .el-icon {
  font-size: 18px;
  color: #667eea;
}

.reply-label .el-icon {
  color: #10b981;
}

.section-content {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  font-size: 15px;
  color: #374151;
  line-height: 1.8;
  white-space: pre-wrap;
}

.reply-content {
  background: #f0fdf4;
  border-left: 4px solid #10b981;
}

.reply-meta {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
}

.reply-meta .el-icon {
  font-size: 14px;
}
</style>
