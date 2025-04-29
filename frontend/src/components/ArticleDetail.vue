<script>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

dayjs.locale('zh-cn')

export default {
  name: 'ArticleDetail',
  setup() {
    const route = useRoute()
    const article = ref(null)
    const headImage = ref(null)
    const loading = ref(true)
    const articleTags = ref([])
    const generateSummaryLoading = ref(false)

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

    const loadArticle = async () => {
      try {
        console.log('请求文章详情，URL参数：', route.params.id);
        const response = await axios.post('/api/crawler/articles/detail', {
          url: route.params.id
        });
        if (response.data) {
          // 处理文章数据
          article.value = {
            ...response.data,
            author: response.data.author || '未知',
            accountName: response.data.accountName || '未知',
            publishTime: response.data.publishTime || null,
            summary: response.data.summary || null
          }
          console.log('文章数据:', article.value);
          
          // 加载文章头图
          await loadHeadImage();
          
          // 加载文章标签
          await loadArticleTags();
        }
      } catch (error) {
        console.error('加载文章失败:', error);
        console.error('请求URL:', error.config?.url);
        console.error('请求方法:', error.config?.method);
        console.error('支持的方法:', error.response?.headers?.allow);
        console.error('错误详情:', error.response?.data || error.message);
        ElMessage.error('加载文章失败：' + (error.response?.data || error.message))
      } finally {
        loading.value = false;
      }
    }

    const loadHeadImage = async () => {
      if (!article.value || !article.value.id) return;
      
      try {
        // 检查文章是否有ULID和图片
        if (!article.value.ulid) {
          console.warn('文章没有ULID，尝试使用旧API获取头图');
          // 尝试使用旧的API获取头图
          try {
            const response = await axios.get(`/api/crawler/image/${article.value.id}`);
            if (response.data && response.data.imageUrl) {
              headImage.value = response.data.imageUrl;
              console.log('使用旧API获取头图成功:', headImage.value);
              return;
            }
          } catch (err) {
            console.warn('使用旧API获取头图失败:', err);
          }
        }
        
        if (!article.value.images) {
          console.warn('文章没有图片信息');
          return;
        }
        
        // 解析图片路径
        const imagePaths = article.value.images.split(',');
        if (imagePaths.length === 0) {
          console.warn('文章没有图片路径');
          return;
        }
        
        // 使用第一张图片作为头图
        const firstImagePath = imagePaths[0];
        
        // 检查图片路径格式
        if (firstImagePath.includes('/')) {
          // 新格式: articleUlid/imageUlid.jpg
          const imageUrl = `/api/images/${firstImagePath}`;
          console.log('使用新API路径获取头图:', imageUrl);
          headImage.value = imageUrl;
        } else {
          // 旧格式，直接使用路径
          console.log('使用旧格式图片路径:', firstImagePath);
          headImage.value = firstImagePath;
        }
      } catch (error) {
        console.warn('无法加载文章头图:', error);
        // 如果加载失败，静默处理，不显示图片即可
      }
    }

    const loadArticleTags = async () => {
      if (!article.value || !article.value.id) return;
      
      try {
        const response = await axios.get(`/api/tags/article/${article.value.id}`);
        if (response.data && response.data.tags) {
          articleTags.value = response.data.tags;
        }
      } catch (error) {
        console.warn('无法加载文章标签:', error);
        // 如果加载失败，静默处理
      }
    }

    const openArticle = () => {
      if (!article.value) {
        ElMessage.warning('无效的文章数据');
        return;
      }

      // 优先使用原文链接
      let url = article.value.sourceUrl;
      
      // 如果没有原文链接，使用文章URL
      if (!url || url === 'javascript:;') {
        url = article.value.url;
      }

      // 验证URL是否有效
      if (!url || url === 'javascript:;' || url.trim() === '') {
        ElMessage.warning('无法获取有效的文章链接');
        return;
      }

      console.log('打开链接:', url);
      try {
        // 验证URL格式
        new URL(url);
        window.open(url, '_blank');
      } catch (error) {
        console.error('无效的URL格式:', url, error);
        ElMessage.error('无效的文章链接格式');
      }
    }

    const generateSummary = async () => {
      if (!article.value || !article.value.id) {
        ElMessage.warning('无法生成摘要：缺少文章ID');
        return;
      }
      
      generateSummaryLoading.value = true;
      try {
        const response = await axios.post(`/api/articles/${article.value.id}/summarize`);
        
        if (response.data.success) {
          article.value.summary = response.data.summary;
          
          if (response.data.isExisting) {
            ElMessage.info('已加载现有摘要');
          } else {
            ElMessage.success('摘要生成成功');
          }
        } else {
          ElMessage.error('摘要生成失败：' + response.data.message);
        }
      } catch (error) {
        console.error('生成摘要失败:', error);
        ElMessage.error('生成摘要失败：' + (error.response?.data?.message || error.message));
      } finally {
        generateSummaryLoading.value = false;
      }
    }

    onMounted(() => {
      loadArticle()
    })

    return {
      article,
      openArticle,
      formatDate,
      headImage,
      loading,
      articleTags,
      generateSummary,
      generateSummaryLoading
    }
  }
}
</script>

<template>
  <div class="article-detail" v-if="article">
    <el-card>
      <template #header>
        <div class="article-header">
          <h1>{{ article.title }}</h1>
          <div class="article-meta">
            <span>作者：{{ article.author || '未知' }}</span>
            <span>公众号：{{ article.accountName || '未知' }}</span>
            <span>发布时间：
              <span v-if="article.publishTime">
                {{ formatDate(article.publishTime) }}
                <el-tooltip effect="dark" placement="top">
                  <template #content>
                    原始值: {{ article.publishTime }}
                  </template>
                  <i class="el-icon-info" style="margin-left: 5px;"></i>
                </el-tooltip>
              </span>
              <span v-else>暂无时间</span>
            </span>
          </div>
          <div v-if="articleTags.length" class="article-tags">
            <el-tag
              v-for="tag in articleTags"
              :key="tag"
              type="success"
              effect="light"
              class="tag-item"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </template>
      
      <div v-if="headImage" class="article-head-image">
        <img :src="headImage" alt="文章头图" />
      </div>
      
      <div v-if="article.summary" class="article-summary">
        <h3>文章摘要</h3>
        <div class="summary-content">{{ article.summary }}</div>
      </div>
      
      <div v-else class="article-summary-generate">
        <el-button 
          type="primary" 
          @click="generateSummary" 
          :loading="generateSummaryLoading"
          plain
        >
          生成文章摘要
        </el-button>
      </div>
      
      <div class="article-content" v-html="article.content"></div>
      
      <div class="article-footer">
        <el-button type="primary" @click="openArticle">
          {{ article.sourceUrl ? '阅读原文' : '查看文章' }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.article-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.article-header {
  text-align: center;
  margin-bottom: 20px;
}

.article-head-image {
  width: 100%;
  max-height: 400px;
  overflow: hidden;
  margin-bottom: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.article-head-image img {
  width: 100%;
  height: auto;
  display: block;
}

.article-meta {
  color: #666;
  font-size: 14px;
  margin-top: 10px;
}

.article-tags {
  margin-top: 15px;
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  margin: 0 5px;
}

.article-meta span {
  margin: 0 10px;
}

.article-content {
  line-height: 1.8;
  font-size: 16px;
}

.article-summary {
  background-color: #f8f9fa;
  padding: 15px;
  border-left: 4px solid #409EFF;
  margin: 20px 0;
  border-radius: 4px;
}

.article-summary-generate {
  text-align: center;
  margin: 20px 0;
}

.article-summary h3 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #409EFF;
}

.summary-content {
  line-height: 1.6;
  color: #333;
}

.article-footer {
  margin-top: 30px;
  text-align: center;
}
</style> 