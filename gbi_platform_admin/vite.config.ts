import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      host: '0.0.0.0',
      port: 5173,
      open: false,
      proxy: {
        // 开发环境统一代理到后端服务，生产环境由 Nginx 反代
        [env.VITE_API_BASE_URL || '/api']: {
          // 使用 127.0.0.1 而非 localhost：Node 17+ 默认优先解析 IPv6 ::1，后端监听 IPv4 时会导致 ECONNREFUSED
          target: 'http://127.0.0.1:8080',
          changeOrigin: true,
          // 去掉 /api 前缀：后端接口无 /api 上下文（/base/login、/org/**、/sys/**）
          rewrite: (path) => path.replace(/^\/api/, ''),
          // 后端未启动时快速失败，避免请求无限挂起
          timeout: 10000,
          proxyTimeout: 10000
        }
      }
    },
    build: {
      chunkSizeWarningLimit: 1500,
      rollupOptions: {
        output: {
          // 路由懒加载分包
          manualChunks: {
            'element-plus': ['element-plus'],
            vendor: ['vue', 'vue-router', 'pinia', 'axios', 'dayjs']
          }
        }
      }
    }
  }
})