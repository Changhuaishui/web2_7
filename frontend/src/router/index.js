import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: () => import('@/components/ArticleDetail.vue'),
    props: true
  },
  {
    path: '/html/:id',
    name: 'ArticleHtml',
    component: () => import('@/views/ArticleHtml.vue'),
    props: true
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL || '/'),
  routes
})

// 防止重复导航到当前路由，并添加详细调试信息
router.beforeEach((to, from, next) => {
  console.group(`路由导航: ${from.path} -> ${to.path}`);
  console.log('导航目标:', to);
  console.log('导航来源:', from);
  
  try {
    // 允许初始导航（首次加载或刷新页面时）
    if (from.matched.length === 0) {
      console.log('初始导航，允许通过');
      console.groupEnd();
      return next();
    }
    
    // 如果要去的路由与当前路由相同，且参数没有变化，则禁止导航
    if (to.path === from.path && JSON.stringify(to.params) === JSON.stringify(from.params)) {
      console.warn('检测到重复导航，已阻止');
      console.groupEnd();
      return next(false);
    }
    
    // 检查路由组件是否存在
    const matchedComponents = to.matched;
    if (matchedComponents.length === 0) {
      console.error('未找到匹配的路由组件');
      console.groupEnd();
      return next('/');
    }
    
    console.log('导航检查通过，继续导航');
    console.groupEnd();
    next();
  } catch (error) {
    console.error('路由守卫出错:', error);
    console.groupEnd();
    next();
  }
});

export default router 