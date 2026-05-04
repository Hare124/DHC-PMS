<template>
  <div class="register-container">
    <div class="register-box">
      <h2>用户注册</h2>
      <el-form
          :model="registerForm"
          :rules="registerRules"
          ref="registerFormRef"
          label-width="80px"
      >
        <!-- 角色选择 -->
        <el-form-item label="注册角色" prop="role">
          <el-radio-group v-model="registerForm.role">
            <el-radio value="2" style="width: 60px">业主</el-radio>
            <el-radio value="1" style="width: 80px">物业人员</el-radio>
            <el-radio value="3">物业经理</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 手机号 -->
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>

        <!-- 验证码 -->
        <el-form-item label="验证码" prop="code">
          <el-input
              v-model="registerForm.code"
              placeholder="请输入验证码"
              style="width: 65%; margin-right: 10px;"
          ></el-input>
          <el-button
              :disabled="isCountingDown || !registerForm.phone"
              @click="sendSmsCode"
              style="width: 30%;"
          >
            {{ isCountingDown ? `${countdown}s后重新发送` : '获取验证码' }}
          </el-button>
        </el-form-item>

        <!-- 密码 -->
        <el-form-item label="密码" prop="password" style="width: 100%;">
          <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
          ></el-input>
        </el-form-item>

        <!-- 确认密码 -->
        <el-form-item label="确认密码" prop="confirmPassword" style="width: 100%;">
          <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
          ></el-input>
        </el-form-item>

        <!-- 业主专属信息（仅当选择业主角色时显示） -->
        <template v-if="registerForm.role === '2'">
          <el-form-item label="楼栋号" prop="buildingNo">
            <el-input v-model="registerForm.buildingNo" placeholder="如1栋"></el-input>
          </el-form-item>
          <el-form-item label="房号" prop="roomNo">
            <el-input v-model="registerForm.roomNo" placeholder="如1001"></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="registerForm.name" placeholder="请输入姓名"></el-input>
          </el-form-item>
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="registerForm.idCard" placeholder="请输入身份证号"></el-input>
          </el-form-item>
        </template>

        <!-- 物业人员/经理专属信息 -->
        <template v-else>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="registerForm.name" placeholder="请输入姓名"></el-input>
          </el-form-item>
          <el-form-item label="工号" prop="staffNo" v-if="registerForm.role !== '2'">
            <el-input v-model="registerForm.staffNo" placeholder="请输入工号"></el-input>
          </el-form-item>
        </template>

        <!-- 注册按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleRegister" style="width: 100%;">注册</el-button>
        </el-form-item>

        <!-- 返回登录 -->
        <el-form-item>
          <el-button text type="primary" @click="goToLogin" style="width: 100%;">已有账号？去登录</el-button>
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
import { register, sendSms } from '@/api/commonApi'

const router = useRouter()

// 表单数据
const registerForm = reactive({
  role: '2', // 默认选业主
  phone: '',
  code: '',
  password: '',
  confirmPassword: '',
  buildingNo: '',
  roomNo: '',
  name: '',
  idCard: '',
  staffNo: ''
})

// 表单引用
const registerFormRef = ref(null)

// 验证码倒计时
const isCountingDown = ref(false)
const countdown = ref(60)
let timer = null

// 表单校验规则
const registerRules = {
  role: [{ required: true, message: '请选择注册角色', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 16, message: '密码长度为 8-16 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  buildingNo: [
    { required: true, message: '请输入楼栋号', trigger: 'blur' }
  ],
  roomNo: [
    { required: true, message: '请输入房号', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  staffNo: [
    { required: true, message: '请输入工号', trigger: 'blur' }
  ]
}

// 发送验证码
const sendSmsCode = async () => {
  // 先校验手机号格式
  const phoneValid = /^1[3-9]\d{9}$/.test(registerForm.phone)
  if (!phoneValid) {
    ElMessage.warning('请输入正确格式的手机号')
    return
  }

  try {
    const res = await sendSms({ phone: registerForm.phone })
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

// 注册处理
const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    // 表单校验
    await registerFormRef.value.validate()

    // 构造请求参数
    const requestParams = {
      role: registerForm.role,
      phone: registerForm.phone,
      code: registerForm.code,
      name: registerForm.name,
      password: registerForm.password
    }

    // 业主专属字段
    if (registerForm.role === '2') {
      requestParams.buildingNo = registerForm.buildingNo
      requestParams.roomNo = registerForm.roomNo
      requestParams.idCard = registerForm.idCard
    }

    // 物业人员/经理专属字段
    if (registerForm.role !== '2') {
      requestParams.staffNo = registerForm.staffNo
    }

    // 调用注册接口
    const response = await register(requestParams)

    // 处理注册响应
    if (response.code === 200) {
      ElMessage.success(response.msg || '注册成功！')
      // 注册成功后跳转到登录页
      goToLogin()
    } else {
      ElMessage.error(response.msg || '注册失败')
    }
  } catch (error) {
    // 表单校验失败 或 接口异常
    if (error.name === 'ValidationError') {
      ElMessage.error('请完善必填项后重试')
    } else {
      ElMessage.error(error.message || '注册失败，请稍后重试')
    }
    console.error('注册异常：', error)
  }
}

// 跳转到登录页
const goToLogin = () => {
  router.push('/login')
}

// 组件卸载时清除定时器
onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.register-container {
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

.register-box {
  max-width: 400px;
  width: 100%;
  padding: 20px 15px;
  margin-right: 60px;
  margin-top: -70px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.register-box h2 {
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
