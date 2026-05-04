<template>
  <div class="login-container">
    <div class="login-box">
      <h2>社区物业管理系统</h2>
      <el-form
          :model="loginForm"
          :rules="loginRules"
          ref="loginFormRef"
          label-width="80px"
      >
        <!-- 角色选择 -->
        <el-form-item label="登录角色" prop="role">
          <el-radio-group v-model="loginForm.role">
            <el-radio value="2" style="width: 30px">业主</el-radio>
            <el-radio value="1" style="width: 60px">物业人员</el-radio>
            <el-radio value="3" >物业经理</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 登录方式选择 -->
        <el-form-item label="登录方式" prop="loginType">
          <el-radio-group v-model="loginForm.loginType" @change="resetFormValidate">
            <el-radio value="password" style="width: 60px">密码登录</el-radio>
            <el-radio value="sms">短信登录</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 账号密码登录 -->
        <template v-if="loginForm.loginType === 'password'">
          <el-form-item label="用户名" prop="username" style="width: 100%;">
            <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password" style="width: 100%;">
            <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
            ></el-input>
          </el-form-item>
        </template>

        <!-- 手机号验证码登录 -->
        <template v-else>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="loginForm.phone" placeholder="请输入手机号" ></el-input>
          </el-form-item>
          <el-form-item label="验证码" prop="code">
            <el-input
                v-model="loginForm.code"
                placeholder="请输入验证码"
                style="width: 65%; margin-right: 10px;"
            ></el-input>
            <el-button
                :disabled="isCountingDown || !loginForm.phone"
                @click="sendSmsCode"
                style="width: 30%;"
            >
              {{ isCountingDown ? `${countdown}s后重新发送` : '获取验证码' }}
            </el-button>
          </el-form-item>
        </template>

        <!-- 登录按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%;">登录</el-button>
        </el-form-item>

        <!-- 注册按钮 -->
        <el-form-item>
          <el-button @click="handleRegister" style="width: 100%;">注册</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
// 导入封装的接口
import { login, sendSms } from '@/api/commonApi'

const router = useRouter()

// 表单数据：修复默认角色值为2（业主）
const loginForm = reactive({
  role: '2', // 修复：默认选业主（值为2，匹配后端）
  loginType: 'password',
  username: '',
  password: '',
  phone: '',
  code: ''
})

// 表单引用
const loginFormRef = ref(null)

// 验证码倒计时
const isCountingDown = ref(false)
const countdown = ref(60)
let timer = null

// 动态表单校验规则
const getLoginRules = () => {
  const baseRules = {
    role: [{ required: true, message: '请选择登录角色', trigger: 'change' }],
    loginType: [{ required: true, message: '请选择登录方式', trigger: 'change' }]
  }

  // 根据登录方式动态添加校验规则
  if (loginForm.loginType === 'password') {
    baseRules.username = [{ required: true, message: '请输入用户名', trigger: 'blur' }]
    baseRules.password = [{ required: true, message: '请输入密码', trigger: 'blur' }]
  } else {
    baseRules.phone = [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
    ]
    baseRules.code = [{ required: true, message: '请输入验证码', trigger: 'blur' }]
  }
  return baseRules
}

// 响应式校验规则
const loginRules = ref(getLoginRules())

// 切换登录方式时重置表单校验
const resetFormValidate = () => {
  loginRules.value = getLoginRules()
  if (loginFormRef.value) {
    loginFormRef.value.clearValidate()
    // 清空另一登录方式的输入内容
    if (loginForm.loginType === 'password') {
      loginForm.phone = ''
      loginForm.code = ''
    } else {
      loginForm.username = ''
      loginForm.password = ''
    }
  }
}

// 发送验证码
const sendSmsCode = async () => {
  // 先校验手机号格式
  const phoneValid = /^1[3-9]\d{9}$/.test(loginForm.phone)
  if (!phoneValid) {
    ElMessage.warning('请输入正确格式的手机号')
    return
  }

  try {
    const res = await sendSms({ phone: loginForm.phone })
    if (res.code === 200) {
      ElMessage.success('验证码已发送，请注意查收')
      startCountdown()
    } else {
      ElMessage.error(res.msg || '发送验证码失败')
    }
  } catch (error) {
    ElMessage.error('发送验证码失败，请稍后再试')
    console.error('发送验证码异常：', error)
  }
}

// 开始倒计时
const startCountdown = () => {
  isCountingDown.value = true
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      isCountingDown.value = false
      countdown.value = 60
    }
  }, 1000)
}

// 登录处理：核心修复角色存储和跳转逻辑
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    // 表单校验
    await loginFormRef.value.validate()

    // 构造请求参数
    const requestParams = {
      role: loginForm.role,
      loginType: loginForm.loginType,
      ...(loginForm.loginType === 'password'
          ? { username: loginForm.username, password: loginForm.password }
          : { phone: loginForm.phone, code: loginForm.code })
    }

    // 调用登录接口
    const response = await login(requestParams)

    // 处理登录响应
    if (response.code === 200) {
      ElMessage.success(response.msg || '登录成功！')

      if (response.token) {
        localStorage.setItem('token', response.token)
      }
      // 存储后端返回的 role（数字：2=业主，1=物业）
      localStorage.setItem('userRole', response.role)
      // 存储用户 ID（报修接口需要）
      if (response.user && response.user.id) {
        localStorage.setItem('userId', response.user.id.toString())
      }
      // 可选：存储用户信息
      localStorage.setItem('userInfo', JSON.stringify(response.user))

      // 新增：保存完整的用户对象到 user（供个人中心使用）
      if (response.user) {
        const userData = {
          id: response.user.id,
          username: response.user.username,
          name: response.user.name,
          phone: response.user.phone,
          role: response.user.role,
          status: response.user.status,
          createTime: response.user.createTime
        };
        localStorage.setItem('user', JSON.stringify(userData));
        console.log('已保存用户信息到 localStorage:', userData);
      }

      if (response.role === 2) {
        // 业主跳业主首页
        router.push('/owner/OwnerIndex02')
      } else if (response.role === 1 ) {
        // 跳转到物业管理人员首页
        router.push('/admin/IndexList')
      }
      else if (response.role === 3){
        // 跳转到物业经理首页
        router.push('/admin/IndexList2')
      }
    } else {
      ElMessage.error(response.msg || '登录失败')
    }
  } catch (error) {
    // 表单校验失败 或 接口异常
    if (error.name === 'ValidationError') {
      ElMessage.error('请完善必填项后重试')
    } else {
      ElMessage.error(error.message || '登录失败，请稍后重试')
    }
    console.error('登录异常：', error)
  }
}


// 跳转到注册页面
const handleRegister = () => {
  router.push('/register')
}

// 组件卸载时清除定时器
onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.login-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  background: url('@/assets/images/loginBackground.jpg') no-repeat center center;
  background-size: cover;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding-right: 15%;
}

.login-box {
  max-width: 320px;
  width: 100%;
  padding: 20px 15px;
  margin-right: 60px;
  margin-top: -70px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.login-box h2 {
  text-align: center;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  height: 40px;
  font-size: 15px;
  font-weight: 500;
}

:deep(.el-button--primary:hover) {
  opacity: 0.9;
  transform: translateY(-1px);
}

:deep(.el-form-item:last-child .el-button) {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border: none;
  color: #ffffff;
  height: 40px;
  font-size: 15px;
  font-weight: 500;
}

:deep(.el-form-item:last-child .el-button:hover) {
  opacity: 0.9;
  transform: translateY(-1px);
}

:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  border-radius: 6px;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409eff inset;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}
</style>