import { createRouter, createWebHistory } from 'vue-router'
import ArticleList from '@/components/ArticleList.vue'
import ArticleDetail from '@/components/ArticleDetail.vue'

const routes = [
  {
    path: '/',
    name: 'ArticleList',
    component: ArticleList
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: ArticleDetail,
    props: true
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router 