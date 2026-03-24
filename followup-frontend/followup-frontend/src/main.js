import { createApp } from 'vue'
import App from './App.vue'
// 引入路由
import router from './router/index.js'
// 引入Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 引入图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 创建Vue实例
const app = createApp(App)

// 全局注册图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册路由和Element Plus（必须按顺序）
app.use(router)
app.use(ElementPlus)

// 挂载实例
app.mount('#app')