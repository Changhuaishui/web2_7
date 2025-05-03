import { fileURLToPath, URL } from 'node:url'
import path from 'path'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue({
      template: {
        compilerOptions: {
          // 开发环境严格检查模板错误
          whitespace: 'preserve',
          comments: true,
        }
      }
    }),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    },
  },
  server: {
    port: 5174,
    strictPort: false, // 如果端口被占用，自动尝试下一个端口
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
        ws: true,
        // 添加响应头，确保编码正确
        configure: (proxy, options) => {
          proxy.on('proxyRes', function(proxyRes, req, res) {
            // 强制设置内容类型和字符编码
            proxyRes.headers['content-type'] = 'application/json; charset=utf-8';
          });
        }
      }
    },
    // 添加错误处理和Debug选项
    hmr: {
      overlay: true
    },
    // 启用日志详情
    logLevel: 'info',
    // 输出错误堆栈信息
    cors: true
  },
  // 开发环境添加源码映射以便调试
  build: {
    sourcemap: true,
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus']
        }
      }
    }
  }
})
