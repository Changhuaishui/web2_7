import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import ArticleDetail from '../views/ArticleDetail.vue'
import ArticleHtml from '../views/ArticleHtml.vue'
import NotFound from '../views/NotFound.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/article/detail',
    name: 'ArticleDetail',
    component: ArticleDetail
  },
  {
    path: '/article/html/:id',
    name: 'ArticleHtml',
    component: ArticleHtml,
    props: true
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 添加全局导航守卫进行参数验证和日志记录
router.beforeEach((to, from, next) => {
  console.log('路由导航:', from.path, '->', to.path)
  console.log('导航目标:', to)
  console.log('导航来源:', from)

  if (to.name === 'ArticleDetail' && !to.query.url) {
    console.error('访问文章详情页缺少URL参数')
    next('/')
    return
  }
  if (to.name === 'ArticleHtml' && !to.params.id) {
    console.error('访问HTML页面缺少ID参数')
    next('/')
    return
  }
  if (to.path === from.path && to.hash === from.hash) {
    console.log(' 检测到重复导航，已阻止')
    return
  }
  next()
})

export default router 