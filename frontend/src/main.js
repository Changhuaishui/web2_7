import { createApp } from 'vue'
import * as Vue from 'vue'
import App from './App.vue'
import router from './router'

// Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 先应用路由，再设置Element Plus，防止可能的组件冲突
app.use(router)

// 设置Element Plus配置
app.use(ElementPlus, {
  locale: zhCn,
  size: 'default',
  zIndex: 3000,
  // 禁用自动计算宽度功能
  autoInsertSpace: false
})

// 全局错误处理器，防止未捕获的错误破坏整个应用
app.config.errorHandler = (err, vm, info) => {
  console.group('全局应用错误');
  console.error('错误详情:', err);
  console.error('错误信息:', info);
  
  // 记录组件信息
  if (vm) {
    console.log('错误组件:', vm.$options.name || '未命名组件');
    console.log('组件路径:', vm.$route ? vm.$route.path : '未知路径');
    
    // 记录组件数据
    try {
      console.log('组件状态:', JSON.stringify(vm.$data, null, 2));
    } catch (e) {
      console.warn('无法序列化组件状态:', e);
    }
  }
  
  // 为用户显示错误
  try {
    const ElementPlus = app.config.globalProperties.$ELEMENT;
    if (ElementPlus && ElementPlus.Message) {
      ElementPlus.Message.error('应用发生错误，请查看控制台获取详情');
    }
  } catch (displayError) {
    console.error('显示错误消息失败:', displayError);
  }
  
  console.groupEnd();
};

// 监控未捕获的Promise错误
window.addEventListener('unhandledrejection', (event) => {
  console.group('未处理的Promise错误');
  console.error('错误详情:', event.reason);
  console.error('Promise:', event.promise);
  console.groupEnd();
});

// 监控运行时错误
window.addEventListener('error', (event) => {
  console.group('全局JavaScript错误');
  console.error('错误消息:', event.message);
  console.error('错误文件:', event.filename);
  console.error('错误行号:', event.lineno);
  console.error('错误列号:', event.colno);
  console.error('错误对象:', event.error);
  console.groupEnd();
});

// 添加Element Plus的ResizeObserver异常捕获补丁
try {
  // 监听并阻止ResizeObserver循环错误
  window.addEventListener('error', (event) => {
    if (event.message && event.message.includes('ResizeObserver') && event.message.includes('loop')) {
      console.warn('阻止ResizeObserver循环错误', event);
      event.stopPropagation();
      event.preventDefault();
      return false;
    }
  }, true);
  
  // 防止getComputedStyle错误
  const originalGetComputedStyle = window.getComputedStyle;
  window.getComputedStyle = function(element, pseudoElt) {
    if (!element || element.nodeType !== 1) {
      console.warn('阻止对非DOM元素调用getComputedStyle', element);
      return {}; // 返回空对象而不是抛出错误
    }
    return originalGetComputedStyle(element, pseudoElt);
  };
} catch (e) {
  console.error('应用补丁时出错', e);
}

app.mount('#app')
