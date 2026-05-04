<template>
  <div class="register-container">
    <h2>用户注册</h2>
    <el-steps :active="step" finish-status="success">
      <el-step title="验证手机号"></el-step>
      <el-step title="填写业主信息"></el-step>
      <el-step title="设置密码"></el-step>
    </el-steps>

    <!-- 第一步：验证手机号 -->
    <div v-if="step === 0">
      <el-form :model="step1Form" :rules="step1Rules" ref="step1FormRef">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="step1Form.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <el-input v-model="step1Form.code" placeholder="请输入验证码"></el-input>
          <el-button
              :disabled="isCountingDown"
              @click="sendSmsCodeHandler"
          >
            {{ isCountingDown ? `${countdown}s后重新发送` : '获取验证码' }}
          </el-button>
        </el-form-item>
        <el-button type="primary" @click="nextStep">下一步</el-button>
      </el-form>
    </div>

    <!-- 第二步：填写业主信息 -->
    <div v-if="step === 1">
      <el-form :model="step2Form" :rules="step2Rules" ref="step2FormRef">
        <el-form-item label="房号" prop="roomNo">
          <el-input v-model="step2Form.roomNo" placeholder="请输入房号"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="step2Form.name" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="step2Form.idCard" placeholder="请输入身份证号"></el-input>
        </el-form-item>
        <el-button @click="prevStep">上一步</el-button>
        <el-button type="primary" @click="nextStep">下一步</el-button>
      </el-form>
    </div>

    <!-- 第三步：设置密码 -->
    <div v-if="step === 2">
      <el-form :model="step3Form" :rules="step3Rules" ref="step3FormRef">
        <el-form-item label="密码" prop="password">
          <el-input
              v-model="step3Form.password"
              type="password"
              placeholder="请输入密码"
          ></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
              v-model="step3Form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
          ></el-input>
        </el-form-item>
        <el-button @click="prevStep">上一步</el-button>
        <el-button type="primary" @click="handleSubmit">提交注册</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { sendSmsCode, validateCode, register } from '@/api/commonApi'

const router = useRouter()

// 当前步骤
const step = ref(0)

// 表单数据
const step1Form = reactive({ phone: '', code: '' })
const step2Form = reactive({ roomNo: '', name: '', idCard: '' })
const step3Form = reactive({ password: '', confirmPassword: '' })

// 表单校验规则
const step1Rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const step2Rules = {
  roomNo: [{ required: true, message: '请输入房号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ]
}

const step3Rules = {
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 16, message: '密码长度为8-16位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== step3Form.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 验证码倒计时
const isCountingDown = ref(false)
const countdown = ref(60)
let timer = null

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

// 消息防抖：避免重复弹出提示
let messageInstance = null
const showMessage = (type, msg) => {
  if (messageInstance) messageInstance.close()
  messageInstance = ElMessage[type](msg)
}

// 发送验证码处理方法（核心修复：适配后端响应格式 + 防抖提示）
const sendSmsCodeHandler = async () => {
  if (!step1Form.phone) {
    showMessage('warning', '请先输入手机号')
    return
  }

  try {
    const result = await sendSmsCode(step1Form.phone)
    // 适配后端真实响应格式：判断 code === 200（而非 success）
    if (result.code === 200) {
      showMessage('success', result.msg || '验证码已发送')
      startCountdown()
    } else {
      showMessage('error', result.msg || '发送验证码失败')
    }
  } catch (error) {
    // 仅捕获网络/系统错误，避免重复提示
    showMessage('error', '网络异常，发送验证码失败')
    console.error('发送验证码系统错误：', error)
  }
}

// 校验验证码接口（修复：适配后端响应格式）
const validateCodeHandler = async () => {
  try {
    const result = await validateCode(step1Form.phone, step1Form.code)
    // 适配后端格式：判断 code === 200
    return result.code === 200
  } catch (error) {
    showMessage('error', '验证码校验失败：' + (error.message || '验证码错误或已过期'))
    return false
  }
}

// 步骤切换
const step1FormRef = ref(null)
const step2FormRef = ref(null)
const step3FormRef = ref(null)

const nextStep = async () => {
  if (step.value === 0) {
    // 第一步：先校验表单再验验证码
    step1FormRef.value.validate(async (valid) => {
      if (!valid) return
      const isValid = await validateCodeHandler()
      if (isValid) {
        step.value++
      } else {
        showMessage('error', '验证码错误或已过期')
      }
    })
  } else if (step.value === 1) {
    step2FormRef.value.validate((valid) => {
      if (valid) step.value++
    })
  }
}

const prevStep = () => {
  step.value--
}

// 提交注册（修复：适配后端响应格式 + 防抖提示）
const handleSubmit = async () => {
  step3FormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const result = await register({
          phone: step1Form.phone,
          code: step1Form.code,
          roomNo: step2Form.roomNo,
          name: step2Form.name,
          idCard: step2Form.idCard,
          password: step3Form.password
        })

        if (result.code === 200) {
          showMessage('success', '注册成功，请登录')
          router.push('/login')
        } else {
          showMessage('error', result.msg || '注册失败')
        }
      } catch (error) {
        showMessage('error', '网络异常，注册失败')
        console.error('注册系统错误：', error)
      }
    }
  })
}

// 组件卸载时清除定时器和消息实例，防止内存泄漏
onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (messageInstance) messageInstance.close()
})
</script>

<style scoped>
.register-container {
  max-width: 500px;
  margin: 50px auto;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
}
</style>