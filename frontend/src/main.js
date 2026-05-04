   import { createApp } from 'vue'
   import { createPinia } from 'pinia'
   import ElementPlus from 'element-plus'
   import 'element-plus/dist/index.css' // 引入样式文件

   import App from './App.vue'
   import router from './router'

   const app = createApp(App)

   app.use(createPinia())
   app.use(router)
   app.use(ElementPlus) // 全局注册 Element Plus

   app.mount('#app')
   