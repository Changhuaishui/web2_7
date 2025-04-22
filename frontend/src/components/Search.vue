<template>
  <div class="search-container">
    <div class="search-header">
      <el-input
        v-model="keyword"
        placeholder="请输入关键词搜索"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="error" class="error-container">
      <el-alert :title="error" type="error" :closable="false" />
    </div>

    <div v-else>
      <el-empty v-if="!articles.length" description="暂无搜索结果" />
      
      <el-table v-else :data="articles" style="width: 100%">
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="scope">
            <router-link :to="{ 
              name: 'ArticleDetail', 
              query: { 
                url: encodeURIComponent(scope.row.url)
              }
            }">
              {{ scope.row.title }}
            </router-link>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="120"></el-table-column>
        <el-table-column prop="accountName" label="公众号" width="150"></el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180">
          <template #default="scope">
            <span v-if="scope.row.publishTime">
              {{ formatDate(scope.row.publishTime) }}
              <el-tooltip effect="dark" placement="top">
                <template #content>
                  原始值: {{ scope.row.publishTime }}
                </template>
                <i class="el-icon-info" style="margin-left: 5px;"></i>
              </el-tooltip>
            </span>
            <span v-else>暂无时间</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="openArticle(scope.row)"
            >
              {{ scope.row.sourceUrl ? '阅读原文' : '查看文章' }}
            </el-button>
            <el-button
              size="small"
              type="warning"
              @click="viewHtml(scope.row)"
            >
              显示原HTML
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="deleteArticle(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const keyword = ref('')
const articles = ref([])
const loading = ref(false)
const error = ref('')

const handleSearch = async () => {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  loading.value = true
  error.value = ''

  try {
    const response = await axios.get(`/api/crawler/articles/search?keyword=${encodeURIComponent(keyword.value)}`)
    articles.value = response.data
  } catch (err) {
    console.error('搜索失败:', err)
    error.value = '搜索失败: ' + (err.response?.data || err.message)
  } finally {
    loading.value = false
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const openArticle = (article) => {
  if (article.sourceUrl) {
    window.open(article.sourceUrl, '_blank')
  } else {
    console.log('文章URL:', article.url) // 调试日志
    router.push({
      name: 'ArticleDetail',
      query: { url: encodeURIComponent(article.url) }
    })
  }
}

const viewHtml = async (article) => {
  try {
    console.log('跳转到HTML页面，文章ID:', article.id)
    await router.push({
      name: 'ArticleHtml',
      params: { id: article.id.toString() }
    })
  } catch (err) {
    console.error('跳转失败:', err)
    ElMessage.error('页面跳转失败')
  }
}

const deleteArticle = async (article) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？', '提示', {
      type: 'warning'
    })
    
    await axios.delete(`/api/crawler/articles/${article.id}`)
    ElMessage.success('文章已删除')
    
    // 从列表中移除
    articles.value = articles.value.filter(item => item.id !== article.id)
  } catch (err) {
    if (err !== 'cancel') {
      console.error('删除失败:', err)
      ElMessage.error('删除失败: ' + (err.response?.data || err.message))
    }
  }
}
</script>

<style scoped>
.search-container {
  padding: 20px;
}

.search-header {
  margin-bottom: 20px;
  max-width: 600px;
}

.loading-container,
.error-container {
  margin-top: 20px;
}

.el-table {
  margin-top: 20px;
}

:deep(.el-table__row) {
  cursor: pointer;
}

:deep(.el-button + .el-button) {
  margin-left: 10px;
}
</style> 