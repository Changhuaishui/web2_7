<template>
  <div class="article-html-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="header">
          <div class="title-section">
            <h2>原始HTML内容</h2>
            <span v-if="article" class="article-info">
              {{ article.title }}
              <el-tag size="small" type="info" class="publish-time" v-if="article.publish_time">
                {{ new Date(article.publish_time).toLocaleString() }}
              </el-tag>
            </span>
          </div>
          <div class="header-actions">
            <el-button @click="$router.push('/')" size="small" type="primary">返回列表</el-button>
            <el-button @click="$router.back()" size="small">返回上页</el-button>
          </div>
        </div>
      </template>
      
      <div v-if="html" v-html="html" class="html-view" />
      <el-empty v-else-if="!loading" description="该文章无原始HTML记录">
        <template #extra>
          <el-button type="primary" @click="$router.push('/')">
            返回文章列表
          </el-button>
        </template>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import DOMPurify from 'dompurify'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const html = ref('')
const loading = ref(true)
const article = ref(null)

// 获取文章基本信息
onMounted(async () => {
  const id = route.params.id
  try {
    // 1. 先获取文章基本信息，访问MySQL的article_table表，查询id的记录
    const articleResponse = await axios.get(`/api/crawler/articles/${id}`)
    article.value = articleResponse.data

    // 2. 获取HTML内容，访问MySQL的article_full_html表，使用article_id作为查询参数
    const htmlResponse = await axios.get(`/api/crawler/${id}/html`)
    
    if (!htmlResponse.data || !htmlResponse.data.fullHtml) {
      ElMessage.error(`文章ID=${id} 无原始HTML记录（请确认是否爬取过）`)
      router.push('/')
      return
    }

    // 清理和处理HTML内容
    let cleanHtml = DOMPurify.sanitize(htmlResponse.data.fullHtml)
    // 注入微信文章样式类
    cleanHtml = `<div class="wx_article">${cleanHtml}</div>`
    html.value = cleanHtml
  } catch (error) {
    console.error('加载失败:', error)
    if (error.response?.status === 404) {
      ElMessage.error('未找到该文章的原始HTML内容')
      setTimeout(() => router.push('/'), 1500)
    } else {
      ElMessage.error(`加载失败: ${error.response?.data?.message || error.message}`)
    }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.article-html-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.title-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.title-section h2 {
  margin: 0;
}

.article-info {
  color: #666;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.publish-time {
  font-size: 12px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

:deep(.html-view) {
  padding: 1.5rem;
  line-height: 1.6;
}

:deep(.wx_article) {
  font-size: 16px;
  color: #333;
  line-height: 1.8;
  word-wrap: break-word;
}

:deep(.wx_article img) {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 1rem auto;
  border-radius: 4px;
}

:deep(.wx_article p) {
  margin: 1em 0;
}

:deep(.wx_article .rich_media_content) {
  overflow: hidden;
  font-size: 16px;
}

:deep(.wx_article section) {
  margin: 1em 0;
}

:deep(.wx_article h1),
:deep(.wx_article h2),
:deep(.wx_article h3) {
  font-weight: bold;
  margin: 1.5em 0 1em;
  line-height: 1.4;
}

:deep(.wx_article blockquote) {
  padding: 10px 20px;
  margin: 1em 0;
  border-left: 4px solid #ddd;
  color: #666;
  background: #f9f9f9;
}

:deep(.wx_article code) {
  background: #f7f7f7;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: Consolas, Monaco, monospace;
}

:deep(.wx_article pre) {
  background: #f7f7f7;
  padding: 15px;
  border-radius: 5px;
  overflow-x: auto;
}

@media (max-width: 768px) {
  .article-html-container {
    padding: 0 10px;
  }
  
  .header {
    flex-direction: column;
    gap: 15px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: center;
  }
  
  .title-section {
    text-align: center;
    width: 100%;
  }
  
  :deep(.html-view) {
    padding: 1rem;
  }
}
</style> 