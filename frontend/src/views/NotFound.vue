<!-- src/views/NotFound.vue -->
<template>
  <div class="not-found">
    <h2>404 - 页面不存在</h2>
    <p>您访问的路径不存在，请检查地址！</p>
    <el-button type="primary" @click="goLogin">返回登录页</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const goLogin = () => {
  router.push('/login');
};
</script>

<style scoped>
.not-found {
  padding: 50px;
  text-align: center;
}
</style>