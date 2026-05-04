// src/api/commonApi.js
import request from '@/utils/request';
// 导入 Element Plus 消息提示（统一用户反馈）
import { ElMessage } from 'element-plus';

// 后端接口基础路径（统一管理，便于维护）
const API_BASE_URL = '/user';

// 新增：全局消息防抖实例，避免重复弹出提示
let messageInstance = null;
/**
 * 防抖消息提示方法：关闭已有消息，避免重复弹出
 * @param {string} type - 提示类型 success/error/warning/info
 * @param {string} msg - 提示内容
 */
const showMessage = (type, msg) => {
  if (messageInstance) {
    messageInstance.close(); // 关闭上一个消息实例
  }
  messageInstance = ElMessage[type](msg);
};

/**
 * 登录接口
 * @param {Object} data - 登录参数 {username/password/phone, ...}
 * @returns {Promise<Object>} 后端响应数据
 */
export const login = async (data) => {
  try {
    // 格式化参数：确保参数为对象，避免空值
    const requestData = { ...data };
    console.log("登录请求参数：", requestData);

    const res = await request.post(`${API_BASE_URL}/login`, requestData, {
      headers: {
        'Content-Type': 'application/json;charset=utf-8'
      }
    });

    console.log("登录响应数据：", res.data);
    // 适配后端返回的 userId 字段
    if (res.data && res.data.user) {
      const userId = res.data.user.id || res.data.userId || res.data.id;
      if (userId) {
        localStorage.setItem('userId', userId.toString()); // 转为字符串存储，避免类型问题
        console.log("已存储 userId 到 localStorage：", userId);
      }
    }
    if (res.data.role) {
      localStorage.setItem('role', res.data.role.toString());
    }
    if (res.data.token) {
      localStorage.setItem('token', res.data.token);
    }
    showMessage('success', res.data.msg || "登录成功");
    return res.data;
  } catch (error) {
    console.error("登录失败详情：", {
      status: error.response?.status,
      responseData: error.response?.data,
      requestData: data
    });

    const errorMsg = error.response?.data?.message || error.response?.data?.msg || '登录失败';
    showMessage('error', errorMsg);
    return {
      code: error.response?.status || 500,
      msg: errorMsg
    };
  }
};

/**
 * 补充：sendSms 接口（兼容 Login.vue 调用，和 sendSmsCode 逻辑一致）
 * @param {Object} data - 发送验证码参数 {phone}
 * @returns {Promise<Object>} 后端响应数据
 */
export const sendSms = async (data) => {
  try {
    // 前置校验：参数合法性
    if (!data || !data.phone) {
      const errorMsg = "请输入正确的手机号";
      showMessage('warning', errorMsg);
      return { code: 400, msg: errorMsg };
    }

    // 强制格式化参数：确保手机号为非空字符串，去除空格
    const requestData = {
      phone: String(data.phone).trim()
    };
    console.log("发送验证码请求参数：", requestData);

    const res = await request.post(`${API_BASE_URL}/send-sms`, requestData, {
      headers: {
        'Content-Type': 'application/json;charset=utf-8'
      }
    });

    console.log("发送验证码响应数据：", res.data);
    // 防抖消息提示
    showMessage('success', res.data.msg || "验证码发送成功");
    return res.data;

  } catch (error) {
    // 精准打印错误详情，便于排查
    console.error("发送验证码失败详情：", {
      status: error.response?.status, // 真实后端状态码
      responseData: error.response?.data, // 后端返回的具体错误
      requestData: data // 前端发送的参数
    });

    // 统一错误提示逻辑 + 防抖消息
    let errorMsg = '';
    if (error.response?.status === 400) {
      errorMsg = error.response.data.msg || "手机号不能为空";
    } else if (error.response?.status === 401) {
      errorMsg = "接口未授权，请检查拦截器配置";
    } else if (error.response?.status === 500) {
      errorMsg = error.response.data.msg || "服务器内部错误，请稍后重试";
    } else {
      errorMsg = error.response?.data?.message || '发送验证码失败';
    }
    showMessage('error', errorMsg);

    // 返回统一格式错误数据，而非 throw
    return {
      code: error.response?.status || 500,
      msg: errorMsg
    };
  }
};

/**
 * 发送验证码接口（原有命名，兼容历史调用）
 * @param {string/number} phone - 手机号
 * @returns {Promise<Object>} 后端响应数据
 */
export const sendSmsCode = async (phone) => {
  // 复用 sendSms 逻辑，适配单参数调用方式
  return await sendSms({ phone });
};

/**
 * 校验验证码接口
 * @param {string/number} phone - 手机号
 * @param {string/number} code - 验证码
 * @returns {Promise<Object>} 后端响应数据
 */
export const validateCode = async (phone, code) => {
  try {
    // 格式化参数：确保参数为字符串，避免空值
    const requestData = {
      phone: phone ? String(phone).trim() : "",
      code: code ? String(code).trim() : ""
    };
    console.log("校验验证码请求参数：", requestData);

    const res = await request.post(`${API_BASE_URL}/validate-code`, requestData, {
      headers: {
        'Content-Type': 'application/json;charset=utf-8'
      }
    });

    console.log("校验验证码响应数据：", res.data);
    // 修复：使用防抖消息提示
    showMessage('success', res.data.msg || "验证码校验成功");
    return res.data;
  } catch (error) {
    console.error("验证码校验失败详情：", {
      status: error.response?.status,
      responseData: error.response?.data,
      requestData: { phone, code }
    });

    // 修复：使用防抖消息提示，统一错误提示字段
    const errorMsg = error.response?.data?.message || error.response?.data?.msg || '验证码校验失败';
    showMessage('error', errorMsg);
    // 修复：返回统一格式错误数据，而非 throw
    return {
      code: error.response?.status || 500,
      msg: errorMsg
    };
  }
};

/**
 * 注册接口
 * @param {Object} data - 注册参数 {phone, code, password, ...}
 * @returns {Promise<Object>} 后端响应数据
 */
export const register = async (data) => {
  try {
    // 格式化参数：确保参数为对象，敏感字段去空格
    const requestData = {
      ...data,
      phone: data.phone ? String(data.phone).trim() : "",
      code: data.code ? String(data.code).trim() : "",
      password: data.password ? String(data.password).trim() : ""
    };
    console.log("注册请求参数：", requestData);

    const res = await request.post(`${API_BASE_URL}/register`, requestData, {
      headers: {
        'Content-Type': 'application/json;charset=utf-8'
      }
    });

    console.log("注册响应数据：", res.data);
    // 修复：使用防抖消息提示
    showMessage('success', res.data.msg || "注册成功");
    return res.data;
  } catch (error) {
    console.error("注册失败详情：", {
      status: error.response?.status,
      responseData: error.response?.data,
      requestData: data
    });

    // 修复：使用防抖消息提示，统一错误提示字段
    const errorMsg = error.response?.data?.message || error.response?.data?.msg || '注册失败';
    showMessage('error', errorMsg);
    // 修复：返回统一格式错误数据，而非 throw
    return {
      code: error.response?.status || 500,
      msg: errorMsg
    };
  }
};

/**
 * 修改密码接口
 * @param {Object} data - 修改密码参数 {userId, oldPassword, newPassword}
 * @returns {Promise<Object>} 后端响应数据
 */
export const updatePassword = async (data) => {
  try {
    const requestData = {
      userId: data.userId,
      oldPassword: data.oldPassword,
      newPassword: data.newPassword
    };

    console.log("修改密码请求参数：", requestData);

    const res = await request.put(`${API_BASE_URL}/update-password`, requestData, {
      headers: {
        'Content-Type': 'application/json;charset=utf-8'
      }
    });

    console.log("修改密码响应数据：", res.data);
    showMessage('success', res.data.msg || "密码修改成功");
    return res.data;
  } catch (error) {
    console.error("修改密码失败详情：", {
      status: error.response?.status,
      responseData: error.response?.data,
      requestData: data
    });

    const errorMsg = error.response?.data?.message || error.response?.data?.msg || '修改密码失败';
    showMessage('error', errorMsg);
    return {
      code: error.response?.status || 500,
      msg: errorMsg
    };
  }
};

/**
 * 修改手机号接口
 * @param {Object} data - 修改手机号参数 {userId, newPhone, code}
 * @returns {Promise<Object>} 后端响应数据
 */
export const updatePhone = async (data) => {
  try {
    const requestData = {
      userId: data.userId,
      newPhone: data.newPhone,
      code: data.code
    };

    console.log("修改手机号请求参数：", requestData);

    const res = await request.put(`${API_BASE_URL}/update-phone`, requestData, {
      headers: {
        'Content-Type': 'application/json;charset=utf-8'
      }
    });

    console.log("修改手机号响应数据：", res.data);
    showMessage('success', res.data.msg || "手机号修改成功");
    return res.data;
  } catch (error) {
    console.error("修改手机号失败详情：", {
      status: error.response?.status,
      responseData: error.response?.data,
      requestData: data
    });

    const errorMsg = error.response?.data?.message || error.response?.data?.msg || '修改手机号失败';
    showMessage('error', errorMsg);
    return {
      code: error.response?.status || 500,
      msg: errorMsg
    };
  }
};

/**
 * 根据用户 ID 获取业主详细信息接口
 * @param {Long} userId - 用户 ID
 * @returns {Promise<Object>} 后端响应数据
 */
export const getOwnerDetailByUserId = async (userId) => {
  try {
    // 新增：参数校验
    if (!userId || userId === 'null' || userId === null || userId === undefined) {
      console.error("获取业主详细信息失败：用户 ID 为空");
      showMessage('error', '用户信息不完整，请重新登录');
      return {
        code: 400,
        msg: '用户 ID 不能为空'
      };
    }

    console.log("获取业主详细信息请求参数：", userId);

    const res = await request.get(`${API_BASE_URL}/owner-detail/${userId}`);

    console.log("获取业主详细信息响应数据：", res.data);
    return res.data;
  } catch (error) {
    console.error("获取业主详细信息失败详情：", {
      status: error.response?.status,
      responseData: error.response?.data
    });

    const errorMsg = error.response?.data?.message || error.response?.data?.msg || '获取业主信息失败';
    showMessage('error', errorMsg);
    return {
      code: error.response?.status || 500,
      msg: errorMsg
    };
  }
};


