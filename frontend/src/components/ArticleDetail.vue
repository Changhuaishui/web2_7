<script>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import ArticleContentProcessor from '@/utils/ArticleContentProcessor'

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
    const processedContent = ref('')
    const relatedArticles = ref([])
    const loadingRelated = ref(false)
    const generatingKeywords = ref(false)
    const regeneratingAll = ref(false)
    
    // 计算属性：将关键词字符串拆分为数组
    const keywordsList = computed(() => {
      if (article.value?.keywords) {
        return article.value.keywords.split(',').map(k => k.trim()).filter(k => k);
      }
      return [];
    });

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
            summary: response.data.summary || null,
            keywords: response.data.keywords || null
          }
          console.log('文章数据:', article.value);
          
          // 调试 - 检查关键词
          if (article.value.keywords) {
            console.log('文章关键词:', article.value.keywords);
            console.log('关键词数组:', article.value.keywords.split(','));
          } else {
            console.warn('文章没有关键词');
          }
          
          // 调试 - 检查图片映射信息
          console.log('图片映射信息(imageMappings):', article.value.imageMappings);
          
          // 检查内容中是否有占位符格式
          const placeholders = (article.value.content.match(/\[\[IMG:[^\]]+\]\]/g) || []);
          console.log(`内容中包含 ${placeholders.length} 个图片占位符`);
          
          // 处理文章内容，合并图文
          if (article.value.imageMappings) {
            console.log('找到图片映射信息，开始处理图文内容');
            try {
              // 尝试解析映射信息
              const mappings = JSON.parse(article.value.imageMappings);
              console.log('解析后的映射信息:', mappings);
              
              // 计算占位符数量
              const placeholderCount = (article.value.content.match(/\[\[IMG:[^\]]+\]\]/g) || []).length;
              console.log(`文章内容中找到 ${placeholderCount} 个图片占位符`);
              
              processedContent.value = ArticleContentProcessor.processArticleContent(article.value);
            } catch (error) {
              console.error('处理图文内容时出错:', error);
              processedContent.value = article.value.content;
            }
          } else if (placeholders.length > 0) {
            // 没有映射信息但有占位符，尝试自动修复
            console.log('没有图片映射信息，但发现占位符，尝试自动修复');
            processedContent.value = ArticleContentProcessor.autoFixPlaceholders(article.value, placeholders);
          } else {
            console.log('没有图片映射信息，使用原始内容');
            processedContent.value = article.value.content;
          }
          
          // 加载文章头图
          await loadHeadImage();
          
          // 加载文章标签
          await loadArticleTags();
          
          // 加载相关文章
          if (article.value.id) {
            await loadRelatedArticles(article.value.id);
          }
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
          // 确保旧格式路径也使用正确的API前缀
          if (firstImagePath && !firstImagePath.startsWith('/api/')) {
            headImage.value = `/api/crawler/images/${firstImagePath}`;
            console.log('使用旧格式图片路径(修正API前缀):', headImage.value);
          } else {
            headImage.value = firstImagePath;
          }
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
    
    const loadRelatedArticles = async (articleId) => {
      try {
        const response = await axios.get(`/api/articles/${articleId}/related`);
        if (response.data && response.data.success) {
          relatedArticles.value = response.data.relatedArticles || [];
          console.log(`加载了${relatedArticles.value.length}篇相关文章`);
        }
      } catch (error) {
        console.warn('无法加载相关文章:', error);
        // 如果加载失败，静默处理
      }
    }
    
    const fetchRelatedArticles = async () => {
      if (!article.value || !article.value.id) {
        ElMessage.warning('无法获取相关文章：缺少文章ID');
        return;
      }
      
      loadingRelated.value = true;
      try {
        const response = await axios.post(`/api/articles/${article.value.id}/crawl-related`);
        
        if (response.data && response.data.success) {
          const count = response.data.count || 0;
          
          if (count > 0) {
            ElMessage.success(`成功找到${count}篇相关文章`);
            // 重新加载相关文章列表
            await loadRelatedArticles(article.value.id);
          } else {
            ElMessage.info('未找到相关文章');
          }
        } else {
          ElMessage.error('获取相关文章失败：' + (response.data.message || '未知错误'));
        }
      } catch (error) {
        console.error('获取相关文章失败:', error);
        ElMessage.error('获取相关文章失败：' + (error.response?.data?.message || error.message));
      } finally {
        loadingRelated.value = false;
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
          
          if (response.data.keywords) {
            article.value.keywords = response.data.keywords;
          }
          
          if (response.data.isExisting) {
            ElMessage.info('已加载现有摘要');
          } else {
            ElMessage.success('摘要生成成功');
            
            // 如果成功生成了相关文章，刷新相关文章列表
            if (response.data.relatedArticlesCount > 0) {
              await loadRelatedArticles(article.value.id);
            }
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

    // 重新生成摘要和关键词的方法
    const regenerateSummaryAndKeywords = async () => {
      if (!article.value || !article.value.id) {
        ElMessage.warning('无法重新生成摘要和关键词：缺少文章ID');
        return;
      }
      
      regeneratingAll.value = true;
      try {
        // 使用与generateSummary相同的接口，但添加force=true参数强制重新生成
        const response = await axios.post(`/api/articles/${article.value.id}/summarize?force=true`);
        
        if (response.data.success) {
          article.value.summary = response.data.summary;
          
          if (response.data.keywords) {
            article.value.keywords = response.data.keywords;
          }
          
          ElMessage.success('摘要和关键词重新生成成功');
          
          // 如果成功生成了相关文章，刷新相关文章列表
          if (response.data.relatedArticlesCount > 0) {
            await loadRelatedArticles(article.value.id);
            ElMessage.info(`已找到${response.data.relatedArticlesCount}篇相关文章`);
          }
        } else {
          ElMessage.error('重新生成失败：' + response.data.message);
        }
      } catch (error) {
        console.error('重新生成摘要和关键词失败:', error);
        ElMessage.error('重新生成失败：' + (error.response?.data?.message || error.message));
      } finally {
        regeneratingAll.value = false;
      }
    }

    const generateKeywords = async () => {
      if (!article.value || !article.value.id) {
        ElMessage.warning('无法生成关键词：缺少文章ID');
        return;
      }
      
      generatingKeywords.value = true;
      try {
        const response = await axios.post(`/api/articles/${article.value.id}/generate-keywords`);
        
        if (response.data.success) {
          article.value.keywords = response.data.keywords;
          ElMessage.success('关键词生成成功');
          
          // 如果成功生成了相关文章，刷新相关文章列表
          if (response.data.relatedArticlesCount > 0) {
            await loadRelatedArticles(article.value.id);
            ElMessage.info(`已找到${response.data.relatedArticlesCount}篇相关文章`);
          }
        } else {
          ElMessage.error('关键词生成失败：' + response.data.message);
        }
      } catch (error) {
        console.error('生成关键词失败:', error);
        ElMessage.error('生成关键词失败：' + (error.response?.data?.message || error.message));
      } finally {
        generatingKeywords.value = false;
      }
    }

    const openRelatedArticle = (url) => {
      if (!url) {
        ElMessage.warning('文章链接不可用');
        console.error('文章链接为空');
        return;
      }
      
      console.log('打开相关文章链接:', url);
      
      // 验证URL
      try {
        // 修正常见的URL问题
        let fixedUrl = url;
        
        // 处理URL中的HTML实体编码问题（例如&amp;会被错误解析）
        if (fixedUrl.includes('&amp;')) {
          fixedUrl = fixedUrl.replace(/&amp;/g, '&');
          console.log('修复HTML实体编码:', fixedUrl);
        }
        
        // 处理以/开头的相对URL，添加搜狗域名
        if (fixedUrl.startsWith('/link?url=')) {
          fixedUrl = 'https://weixin.sogou.com' + fixedUrl;
          console.log('添加域名到相对URL:', fixedUrl);
        }
        
        // 检查URL是否是编码后的搜狗URL（以后端存储的格式）
        if (fixedUrl.includes('weixin.sogou.com/link?url=')) {
          console.log('检测到搜狗链接，直接跳转:', fixedUrl);
          // 对于搜狗链接，在新标签页打开
          window.open(fixedUrl, '_blank');
          return;
        }
        
        // 处理搜狗链接格式 - 尝试直接从URL提取微信链接
        if (fixedUrl.includes('url=')) {
          try {
            // 提取url参数
            const urlMatch = fixedUrl.match(/url=([^&]+)/);
            if (urlMatch && urlMatch[1]) {
              let targetUrl = decodeURIComponent(urlMatch[1]);
              console.log('从搜狗链接提取目标URL:', targetUrl);
              
              // 检查提取的URL是否需要添加前缀
              if (!targetUrl.startsWith('http') && 
                  (targetUrl.includes('mp.weixin.qq.com') || 
                   targetUrl.includes('weixin.qq.com'))) {
                targetUrl = 'https://' + targetUrl;
                console.log('添加https前缀到微信URL:', targetUrl);
              }
              
              fixedUrl = targetUrl;
            }
          } catch (e) {
            console.warn('提取搜狗链接参数失败:', e);
          }
        }
        
        // 如果URL不包含协议，添加https://
        if (!fixedUrl.startsWith('http://') && !fixedUrl.startsWith('https://')) {
          fixedUrl = 'https://' + fixedUrl;
          console.log('添加协议到URL:', fixedUrl);
        }
        
        // 验证URL格式
        new URL(fixedUrl);
        
        // 添加用户提示
        if (fixedUrl.includes('weixin.sogou.com')) {
          ElMessage({
            message: '正在跳转到搜狗中转页面，可能需要验证',
            type: 'warning',
            duration: 3000
          });
        }
        
        // 使用window.open在新标签页打开，而不是在当前页面跳转
        console.log('最终跳转URL:', fixedUrl);
        window.open(fixedUrl, '_blank');
      } catch (error) {
        console.error('无效的URL格式:', url, error);
        
        // 尝试最后的后备方案 - 如果链接格式无效但包含weixin.sogou.com，尝试直接打开
        if (url.includes('weixin.sogou.com') || url.includes('sogou')) {
          console.log('尝试直接打开搜狗URL:', url);
          ElMessage({
            message: '链接格式异常，尝试直接跳转到搜狗页面',
            type: 'warning',
            duration: 3000
          });
          window.open('https://weixin.sogou.com', '_blank');
          return;
        }
        
        ElMessage.error('无效的文章链接格式');
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
      generateSummaryLoading,
      processedContent,
      relatedArticles,
      loadingRelated,
      fetchRelatedArticles,
      keywordsList,
      generateKeywords,
      generatingKeywords,
      regenerateSummaryAndKeywords,
      regeneratingAll,
      openRelatedArticle
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
              type="primary"
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
        <div v-if="article.keywords" class="keywords-in-summary">
          <span class="keywords-label">关键词：</span>
          <el-tag
            v-for="keyword in keywordsList"
            :key="keyword"
            type="primary"
            effect="light"
            class="keyword-item"
            size="small"
          >
            {{ keyword }}
          </el-tag>
        </div>
        <div v-else class="keywords-in-summary">
          <el-button 
            type="primary" 
            @click="generateKeywords" 
            :loading="generatingKeywords"
            size="small"
            plain
          >
            生成文章关键词
          </el-button>
        </div>
        
        <div class="regenerate-buttons" style="margin-top: 15px;">
          <el-button 
            type="primary" 
            @click="regenerateSummaryAndKeywords" 
            :loading="regeneratingAll"
            size="small"
            plain
          >
            重新生成摘要和关键词
          </el-button>
        </div>
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
      
      <div class="article-content" v-html="processedContent"></div>
      
      <!-- 相关文章部分 -->
      <div v-if="relatedArticles.length" class="related-articles">
        <h3>相关文章</h3>
        <ul class="related-article-list">
          <li v-for="article in relatedArticles" :key="article.id" class="related-article-item">
            <a 
              :href="article.relatedUrl" 
              target="_blank" 
              class="related-article-link"
              @click.prevent="openRelatedArticle(article.relatedUrl)"
            >
              {{ article.title || '相关文章' }}
              <span v-if="article.relatedUrl" class="article-link-debug" style="display: none;">{{ article.relatedUrl }}</span>
            </a>
          </li>
        </ul>
      </div>
      
      <div class="article-footer">
        <el-button type="primary" @click="openArticle">
          {{ article.sourceUrl ? '阅读原文' : '查看文章' }}
        </el-button>
        
        <el-button v-if="!relatedArticles.length" type="info" @click="fetchRelatedArticles" :loading="loadingRelated">
          查找相关文章
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
  object-fit: contain;
  max-height: 400px;
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

.article-keywords {
  margin-top: 15px;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
}

.keywords-in-summary {
  margin-top: 15px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.keywords-label {
  color: #666;
  font-size: 14px;
  margin-right: 10px;
}

.keyword-item {
  margin: 0 3px;
}

.related-articles {
  margin-top: 30px;
  border-top: 1px dashed #ddd;
  padding-top: 15px;
}

.related-articles h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 15px;
}

.related-article-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.related-article-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.related-article-link {
  color: #409EFF;
  text-decoration: none;
  display: block;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.related-article-link:hover {
  background-color: #f0f7ff;
  color: #0056b3;
}

/* 添加全局图片样式，确保所有图片适应容器宽度 */
:deep(.article-content img) {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 10px auto;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style> 