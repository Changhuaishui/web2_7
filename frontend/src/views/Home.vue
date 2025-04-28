<script>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import { useRouter } from 'vue-router'

dayjs.locale('zh-cn')

export default {
  name: 'Home',
  setup() {
    const crawlerForm = ref({
      url: ''
    })
    const crawling = ref(false)
    const searchKeyword = ref('')
    const articles = ref([])
    const router = useRouter()
    const articleImages = ref({}) // 用于存储文章头图URL
    const tags = ref([]) // 所有可用标签
    const selectedTag = ref('全部') // 当前选中的标签
    const filteredArticles = ref([]) // 经过标签筛选后的文章
    const rebuildingIndex = ref(false) // 重建索引状态

    // 爬取文章
    const crawlArticle = async () => {
      if (!crawlerForm.value.url) {
        ElMessage.warning('请输入文章URL')
        return
      }

      crawling.value = true
      try {
        // 先检查链接状态
        const checkResponse = await axios.post('/api/crawler/check-link', {
          url: crawlerForm.value.url
        })
        
        // 如果链接已经爬取过
        if (checkResponse.data.status === 'already_exists') {
          ElMessage.info('该文章已经爬取过，无需重复爬取')
          crawlerForm.value.url = ''
          await loadArticles() // 刷新列表
          return
        }
        
        // 如果链接无效
        if (checkResponse.data.status === 'invalid') {
          ElMessage.error('链接无效或已失效，无法爬取，可能是临时链接已过期')
          return
        }
        
        // 链接有效，开始爬取
        const response = await axios.post('/api/crawler/crawl', {
          url: crawlerForm.value.url
        })
        ElMessage.success(response.data)
        crawlerForm.value.url = ''
        loadArticles() // 重新加载文章列表
      } catch (error) {
        console.error('爬取失败:', error)
        if (error.response?.data) {
          ElMessage.error(error.response.data)
        } else {
          ElMessage.error('爬取失败：' + error.message)
        }
      } finally {
        crawling.value = false
      }
    }

    // 搜索文章
    const searchArticles = async () => {
      try {
        if (!searchKeyword.value.trim()) {
          await loadArticles();
          return;
        }
        const response = await axios.get(`/api/search?keyword=${encodeURIComponent(searchKeyword.value.trim())}`);
        console.log('搜索结果响应:', response);
        console.log('搜索结果数据:', response.data);
        articles.value = response.data;
        filteredArticles.value = response.data; // 确保搜索结果也更新到filteredArticles
        if (articles.value.length === 0) {
          ElMessage.info('未找到匹配的文章');
        } else {
          console.log(`找到${articles.value.length}条搜索结果`);
        }
      } catch (error) {
        console.error('搜索失败:', error);
        ElMessage.error('搜索失败：' + (error.response?.data || error.message));
      }
    }

    // 加载所有文章
    const loadArticles = async () => {
      try {
        const response = await axios.get('/api/crawler/articles')
        console.log('API返回的完整数据:', response);
        console.log('文章数据:', response.data);
        if (response.data && response.data.length > 0) {
          console.log('第一篇文章的时间:', response.data[0].publishTime);
        }
        articles.value = response.data;
        filteredArticles.value = response.data; // 初始时显示所有文章
        
        // 加载每篇文章的头图
        for (const article of articles.value) {
          loadArticleImage(article.id);
        }
      } catch (error) {
        console.error('加载文章失败:', error);
        ElMessage.error('加载文章失败')
      }
    }
    
    // 加载文章头图
    const loadArticleImage = async (articleId) => {
      if (!articleId) return;
      
      try {
        // 查找指定ID的文章
        const article = articles.value.find(a => a.id === articleId);
        if (!article) {
          console.warn(`未找到ID为${articleId}的文章`);
          return;
        }
        
        // 检查文章是否有ULID
        if (!article.ulid) {
          console.warn(`文章ID=${articleId}没有ULID，尝试使用旧API获取头图`);
          // 尝试使用旧的API获取头图
          try {
            const response = await axios.get(`/api/crawler/image/${articleId}`);
            if (response.data && response.data.imageUrl) {
              articleImages.value[articleId] = response.data.imageUrl;
              console.log(`文章${articleId}使用旧API获取头图成功:`, response.data.imageUrl);
              return;
            }
          } catch (err) {
            console.warn(`文章${articleId}使用旧API获取头图失败:`, err);
          }
        }
        
        // 解析文章的图片路径
        if (article.images && article.images.trim()) {
          // 取第一张图片作为头图
          const imagePaths = article.images.split(',');
          if (imagePaths.length > 0) {
            const firstImagePath = imagePaths[0];
            
            // 检查是否是ULID格式的路径
            if (firstImagePath.includes('/')) {
              // 路径形如：articleUlid/imageUlid.jpg
              // 直接使用该路径
              const imageUrl = `/api/images/${firstImagePath}`;
              console.log(`文章${articleId}使用新API路径获取头图: ${imageUrl}`);
              articleImages.value[articleId] = imageUrl;
            } else {
              // 旧格式，使用兼容API
              console.log(`文章${articleId}使用旧API路径获取头图: ${firstImagePath}`);
              articleImages.value[articleId] = firstImagePath;
            }
          }
        } else {
          console.warn(`文章ID=${articleId}没有图片`);
        }
      } catch (error) {
        console.warn(`无法加载文章${articleId}的头图:`, error);
        // 如果加载失败，静默处理，不显示图片即可
      }
    }

    // 加载所有可用标签
    const loadTags = async () => {
      try {
        const response = await axios.get('/api/tags');
        tags.value = ['全部', ...response.data];
      } catch (error) {
        console.error('加载标签失败:', error);
        ElMessage.error('加载标签失败');
      }
    }
    
    // 根据标签筛选文章
    const filterByTag = async (tag) => {
      selectedTag.value = tag;
      
      try {
        if (tag === '全部') {
          filteredArticles.value = articles.value;
        } else {
          const response = await axios.get(`/api/tags/filter?tag=${encodeURIComponent(tag)}`);
          filteredArticles.value = response.data;
        }
      } catch (error) {
        console.error('根据标签筛选文章失败:', error);
        ElMessage.error('筛选文章失败');
      }
    }

    // 格式化日期时间
    const formatDate = (dateStr) => {
      if (!dateStr) {
        console.log('时间为空:', dateStr);
        return '暂无时间';
      }
      try {
        const formattedDate = dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss');
        console.log('格式化时间:', dateStr, '->', formattedDate);
        return formattedDate;
      } catch (error) {
        console.error('时间格式化错误:', error, dateStr);
        return dateStr;
      }
    }

    // 打开文章链接
    const openArticle = (article) => {
      if (!article) {
        ElMessage.warning('无效的文章数据');
        return;
      }

      console.log('文章数据:', article);
      console.log('原文链接:', article.sourceUrl);
      console.log('文章链接:', article.url);

      // 优先使用原文链接
      let url = article.sourceUrl;
      
      // 如果原文链接无效，使用文章URL
      if (!url || url === 'javascript:;' || url === 'javascript:void(0);' || url.trim() === '') {
        console.log('原文链接无效，使用文章URL');
        url = article.url;
      }

      // 验证URL是否有效
      if (!url || url === 'javascript:;' || url === 'javascript:void(0);' || url.trim() === '') {
        console.log('无效的URL:', url);
        ElMessage.warning('无法获取有效的文章链接');
        return;
      }

      // 确保URL是以http或https开头
      if (!url.startsWith('http://') && !url.startsWith('https://')) {
        url = 'https://' + url;
      }

      console.log('最终使用的链接:', url);
      try {
        // 验证URL格式
        new URL(url);
        window.open(url, '_blank');
      } catch (error) {
        console.error('无效的URL格式:', url, error);
        ElMessage.error('无效的文章链接格式');
      }
    }

    // 删除文章
    const deleteArticle = async (article) => {
      try {
        if (!article || !article.url) {
          ElMessage.error('无效的文章数据');
          return;
        }

        const confirmResult = await ElMessageBox.confirm(
          `确定要删除文章"${article.title}"吗？`,
          '删除确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        );

        if (confirmResult !== 'confirm') {
          return;
        }

        const urls = article.url.split(',');
        const firstUrl = urls[0] || '';
        if (!firstUrl) {
          ElMessage.error('无效的URL');
          return;
        }

        const encodedUrl = encodeURIComponent(encodeURIComponent(firstUrl));

        const loading = ElLoading.service({
          lock: true,
          text: '正在删除文章...',
          background: 'rgba(0, 0, 0, 0.7)'
        });

        try {
          const response = await axios.delete(`/api/crawler/articles/${encodedUrl}`);

          if (response.status === 200) {
            ElMessage.success('删除成功');
            const index = articles.value.findIndex(a => a.url === article.url);
            if (index !== -1) {
              articles.value.splice(index, 1);
            }
          } else {
            ElMessage.warning(response.data || '删除可能未完成，请刷新页面确认');
          }
        } finally {
          loading.close();
        }
      } catch (error) {
        console.error('删除失败:', error);

        if (error.response?.data) {
          const errorText = error.response.data.includes('<!doctype html>')
            ? '服务器请求错误'
            : error.response.data;
          ElMessage.error(`删除失败：${errorText}`);
        } else if (error.request) {
          ElMessage.error('删除失败：无法连接到服务器');
        } else {
          ElMessage.error(`删除失败：${error.message}`);
        }
      }
    }

    // 添加查看HTML方法
    const viewHtml = (id) => {
      router.push(`/html/${id}`)
    }

    // 重建搜索索引
    const rebuildSearchIndex = async () => {
      try {
        rebuildingIndex.value = true;
        ElMessage.info('正在重建搜索索引，请稍候...');
        
        const response = await axios.post('/api/search/rebuild-index');
        
        if (response.data && response.data.success) {
          ElMessage.success(response.data.message || '搜索索引重建成功');
        } else {
          ElMessage.warning('重建过程完成，但可能存在问题');
        }
      } catch (error) {
        console.error('重建索引失败:', error);
        ElMessage.error('重建搜索索引失败: ' + (error.response?.data?.error || error.message));
      } finally {
        rebuildingIndex.value = false;
      }
    };

    onMounted(() => {
      loadArticles()
      loadTags()
    })

    return {
      crawlerForm,
      crawling,
      searchKeyword,
      articles,
      articleImages,
      tags,
      selectedTag,
      filteredArticles,
      rebuildingIndex,
      crawlArticle,
      searchArticles,
      deleteArticle,
      formatDate,
      openArticle,
      viewHtml,
      filterByTag,
      rebuildSearchIndex
    }
  }
}
</script>

<template>
  <div class="home">
    <!-- 爬虫表单 -->
    <el-card class="crawler-form">
      <template #header>
        <div class="card-header">
          <span>添加文章</span>
        </div>
      </template>

      <el-form :model="crawlerForm" label-width="120px">
        <el-form-item label="文章URL">
          <el-input v-model="crawlerForm.url" placeholder="请输入微信公众号文章URL"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="crawlArticle" :loading="crawling">
            开始爬取
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 搜索框 -->
    <el-card class="search-form">
      <template #header>
        <div class="card-header">
          <span>搜索文章</span>
          <el-button 
            type="warning" 
            size="small" 
            @click="rebuildSearchIndex" 
            :loading="rebuildingIndex"
          >
            重建搜索索引
          </el-button>
        </div>
      </template>

      <el-input
        v-model="searchKeyword"
        placeholder="请输入搜索关键词"
        class="search-input"
        @keyup.enter="searchArticles"
      >
        <template #append>
          <el-button @click="searchArticles">搜索</el-button>
        </template>
      </el-input>
    </el-card>

    <!-- 文章列表 -->
    <el-card v-if="articles.length > 0" class="article-list">
      <template #header>
        <div class="card-header">
          <span>文章列表</span>
          <div class="tag-container">
            <el-tag
              v-for="tag in tags"
              :key="tag"
              :type="selectedTag === tag ? 'primary' : 'info'"
              effect="plain"
              class="tag-item"
              @click="filterByTag(tag)"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </template>

      <div class="waterfall-container">
        <div v-for="article in filteredArticles" :key="article.id" class="article-card">
          <el-card shadow="hover" class="article-item">
            <div v-if="articleImages[article.id]" class="article-image">
              <img :src="articleImages[article.id]" alt="文章头图" />
            </div>
            <div class="article-title">
              <router-link :to="{ 
                name: 'ArticleDetail', 
                params: { 
                  id: encodeURIComponent(article.url)
                }
              }">
                {{ article.title }}
              </router-link>
            </div>
            <div class="article-meta">
              <span class="author">作者：{{ article.author }}</span>
              <span class="account">公众号：{{ article.accountName }}</span>
              <span class="time">发布时间：{{ formatDate(article.publishTime) }}</span>
            </div>
            <div class="article-actions">
              <el-button-group>
                <el-button
                  size="small"
                  type="primary"
                  @click="openArticle(article)"
                >
                  阅读原文
                </el-button>
                <el-button
                  type="warning"
                  size="small"
                  @click="viewHtml(article.id)"
                >
                  显示原HTML
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  @click="deleteArticle(article)"
                >
                  删除
                </el-button>
              </el-button-group>
            </div>
          </el-card>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.home {
  padding: 20px;
}

.crawler-form,
.search-form,
.article-list {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tag-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  cursor: pointer;
  margin-right: 5px;
  transition: all 0.3s;
}

.tag-item:hover {
  transform: scale(1.05);
}

.search-input {
  width: 100%;
}

a {
  color: #409EFF;
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}

.waterfall-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 10px;
}

.article-card {
  break-inside: avoid;
  margin-bottom: 20px;
}

.article-item {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.article-image {
  width: 100%;
  margin-bottom: 15px;
  overflow: hidden;
  border-radius: 4px;
}

.article-image img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.article-image img:hover {
  transform: scale(1.05);
}

.article-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
  line-height: 1.4;
}

.article-title a {
  color: #303133;
  text-decoration: none;
}

.article-title a:hover {
  color: #409EFF;
}

.article-meta {
  font-size: 12px;
  color: #909399;
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.article-actions {
  margin-top: auto;
  padding-top: 10px;
  border-top: 1px solid #EBEEF5;
}

.article-actions .el-button-group {
  display: flex;
  justify-content: flex-start;
  gap: 5px;
}

@media (max-width: 768px) {
  .waterfall-container {
    grid-template-columns: 1fr;
  }
}
</style>
