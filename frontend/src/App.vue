<script>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

// 更新axios全局配置
axios.defaults.headers.common['Accept'] = 'application/json'
axios.defaults.headers.common['Content-Type'] = 'application/json;charset=UTF-8'

export default {
  name: 'App',
  setup() {
    const crawlerForm = ref({
      url: ''
    })
    const crawling = ref(false)
    const searchKeyword = ref('')
    const articles = ref([])
    const route = useRoute()
    const router = useRouter()
    const activeRoute = computed(() => route.path)

    // 增强菜单修复函数，处理路由变化情况
    const fixElementPlusMenus = () => {
      console.log('开始修复菜单，当前路径:', activeRoute.value);

      setTimeout(() => {
        try {
          // 手动设置菜单项宽度，避免动态计算
          const menuItems = document.querySelectorAll('.el-menu-item');
          if (menuItems && menuItems.length > 0) {
            menuItems.forEach(item => {
              // 确保元素是DOM节点
              if (item && item.nodeType === 1) {
                // 设置基本样式
                item.style.minWidth = '80px';
                item.style.padding = '0 20px';

                // 处理路由匹配：检查路径与菜单index属性
                const itemPath = item.getAttribute('index');
                if (itemPath === activeRoute.value) {
                  console.log('应激活菜单项:', itemPath);
                  // 手动添加激活类
                  item.classList.add('is-active');
                } else if (activeRoute.value.startsWith('/article/') && itemPath === '/') {
                  // 文章详情页激活首页菜单
                  item.classList.add('is-active');
                } else {
                  // 确保其他菜单项不激活
                  item.classList.remove('is-active');
                }
              }
            });
            console.log('已修复菜单宽度计算和激活状态');
          } else {
            console.warn('未找到菜单项元素');
          }
        } catch (err) {
          console.error('菜单修复尝试失败，但已被安全处理', err);
        }
      }, 500); // 延迟500ms确保DOM已渲染
    };

    // 监听菜单刷新事件
    const setupMenuRefreshListener = () => {
      window.addEventListener('menu-refresh', () => {
        console.log('收到菜单刷新事件');
        fixElementPlusMenus();
      });
    }

    // 监听路由变化，更新菜单状态
    const setupRouteListener = () => {
      router.afterEach((to, from) => {
        console.log(`路由变化后处理: ${from.path} -> ${to.path}`);
        // 延迟执行，确保菜单组件已更新
        setTimeout(() => {
          fixElementPlusMenus();
        }, 100);
      });
    }

    // 格式化日期时间
    const formatDate = (dateStr) => {
      if (!dateStr) return '暂无时间';
      try {
        const date = new Date(dateStr);
        if (isNaN(date.getTime())) {
          // 如果日期无效，直接返回原始字符串
          return dateStr;
        }
        return new Intl.DateTimeFormat('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          hour12: false
        }).format(date);
      } catch (error) {
        console.error('日期格式化错误:', error);
        return dateStr;
      }
    }

    // 爬取文章
    const crawlArticle = async () => {
      if (!crawlerForm.value.url) {
        ElMessage.warning('请输入文章URL')
        return
      }

      crawling.value = true
      try {
        const response = await axios.post('/api/crawler/crawl', {
          url: crawlerForm.value.url
        })
        ElMessage.success(response.data)
        crawlerForm.value.url = ''
        loadArticles() // 重新加载文章列表
      } catch (error) {
        ElMessage.error(error.response?.data || '爬取失败')
      } finally {
        crawling.value = false
      }
    }

    // 搜索文章
    const searchArticles = async () => {
      try {
        if (!searchKeyword.value.trim()) {
          // 如果搜索关键词为空，加载所有文章。目前有点小bug
          await loadArticles();
          return;
        }
        const response = await axios.get(`/api/search?keyword=${encodeURIComponent(searchKeyword.value.trim())}`);
        articles.value = response.data;
        if (articles.value.length === 0) {
          ElMessage.info('未找到匹配的文章');
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
        articles.value = response.data
      } catch (error) {
        ElMessage.error('加载文章失败')
      }
    }

    // 删除文章
    const deleteArticle = async (article) => {
      try {
        if (!article || !article.url) {
          ElMessage.error('无效的文章数据');
          return;
        }

        // 显示确认对话框
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

        // 从URL列表中获取第一个URL并进行处理
        const urls = article.url.split(',');
        const firstUrl = urls[0] || '';
        if (!firstUrl) {
          ElMessage.error('无效的URL');
          return;
        }

        // 对URL进行两次编码，以处理特殊字符
        const encodedUrl = encodeURIComponent(encodeURIComponent(firstUrl));

        // 显示加载状态
        const loading = ElLoading.service({
          lock: true,
          text: '正在删除文章...',
          background: 'rgba(0, 0, 0, 0.7)'
        });

        try {
          const response = await axios.delete(`/api/crawler/articles/${encodedUrl}`);

          if (response.status === 200) {
            ElMessage.success('删除成功');
            // 从本地数组中移除该文章，而不是重新加载整个列表
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
          // 如果是HTML格式的错误消息，提取有用的信息
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

    // 处理菜单项选择
    const handleMenuSelect = (index) => {
      console.log('菜单项选择:', index);

      // 如果当前已经在文章详情页，需要先回到首页
      if (route.path.startsWith('/article/')) {
        console.log('从文章详情页导航到:', index);

        // 使用replace而不是push，避免导航历史堆积
        router.replace(index);

        // 延迟修复菜单状态
        setTimeout(fixElementPlusMenus, 100);
        return;
      }

      // 特殊处理搜索页面，防止递归渲染
      if (index === '/search') {
        console.log('导航到搜索页面，使用特殊处理');

        // 如果已经在搜索页面，不执行任何操作
        if (route.path === '/search') {
          console.log('已经在搜索页面，忽略导航');
          return;
        }

        // 特殊处理：使用reload方式跳转，而不是vue-router
        const currentUrl = window.location.href;
        const baseUrl = currentUrl.split('#')[0];
        window.location.href = baseUrl + '#/search';
        setTimeout(() => window.location.reload(), 10);
        return;
      }
    };

    // 在组件挂载后修复菜单和设置事件监听
    onMounted(() => {
      loadArticles();
      fixElementPlusMenus();
      setupMenuRefreshListener();
      setupRouteListener();
    });

    return {
      crawlerForm,
      crawling,
      searchKeyword,
      articles,
      crawlArticle,
      searchArticles,
      deleteArticle,
      formatDate,
      activeRoute,
      handleMenuSelect
    }
  }
}
</script>

<template>
  <div id="app">
    <el-container>
      <el-header>
        <div class="header-container">
          <div class="logo">公众号文章采集系统</div>
          <el-menu
            mode="horizontal"
            router
            :default-active="activeRoute"
            class="safe-el-menu"
            style="min-width: 300px;"
            @select="handleMenuSelect"
          >
            <el-menu-item index="/" style="min-width: 80px;">首页</el-menu-item>
            <el-menu-item index="/search" style="min-width: 80px;" class="search-menu-item">搜索</el-menu-item>
          </el-menu>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
      <el-footer>
        <div class="footer-text">
          微信公众号文章爬虫系统 &copy; {{ new Date().getFullYear() }}
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<style>
/* CSS兼容性修复 */
.el-pagination .el-input__inner {
  -moz-appearance: textfield;
  /* 添加标准属性以支持更多浏览器 */
  appearance: textfield;
}

a,
area,
button,
input,
label,
select,
summary,
textarea,
[tabindex] {
  -ms-touch-action: manipulation;
  /* 添加标准属性以支持更多浏览器 */
  touch-action: manipulation;
}

.immersive-translate-link {
  user-select: none;
  /* 添加浏览器前缀以支持Safari */
  -webkit-user-select: none;
}

#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  min-height: 100vh;
}

.header-container {
  display: flex;
  align-items: center;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  margin-right: 20px;
  color: #409EFF;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #eee;
  padding: 0;
}

.el-main {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.el-footer {
  background-color: #545c64;
  color: #fff;
  text-align: center;
  line-height: 60px;
  padding: 0;
}

.footer-text {
  font-size: 14px;
}

/* 修复Element Plus菜单宽度计算问题 */
.el-menu-item {
  /* 设置固定宽度，避免动态计算 */
  min-width: 80px !important;
  padding: 0 20px !important;
}

/* 确保菜单容器样式正确 */
.el-menu--horizontal {
  display: flex !important;
  height: 60px !important;
  white-space: nowrap !important;
}
</style>
