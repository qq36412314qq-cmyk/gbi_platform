/**
 * 项目入口：Vue3 + Element Plus + Pinia + Router
 */
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import pinia from './store'
import '@/assets/style/index.css'

const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// 全局注册 Element Plus 图标（Sidebar 菜单 icon 按名称动态渲染）
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 全局注册通用组件（页面无需重复引入）
import AuthBtn from '@/components/common/AuthBtn.vue'
import SearchBar from '@/components/common/SearchBar.vue'
import TablePage from '@/components/common/TablePage.vue'
import CommonDialog from '@/components/common/CommonDialog.vue'

app.component('AuthBtn', AuthBtn)
app.component('SearchBar', SearchBar)
app.component('TablePage', TablePage)
app.component('CommonDialog', CommonDialog)

app.mount('#app')