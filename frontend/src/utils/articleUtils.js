import axios from 'axios';
import { ElMessage } from 'element-plus';

/**
 * 处理失效链接
 * @param {string} url - 失效的链接URL
 * @returns {Promise<string>} - 处理结果
 */
export const handleInvalidLink = async (url) => {
  try {
    // 调用后端API记录失效链接
    const response = await axios.get(`/api/crawler/invalid-links?url=${encodeURIComponent(url)}`);
    console.log('失效链接处理响应:', response.data);
    
    // 显示提示信息
    ElMessage.warning(response.data);
    return response.data;
  } catch (error) {
    console.error('处理失效链接请求失败:', error);
    throw error;
  }
};

/**
 * 检查文章链接是否有效
 * @param {string} url - 要检查的URL
 * @returns {Promise<boolean>} - 链接是否有效
 */
export const checkArticleLink = async (url) => {
  try {
    const response = await axios.get(url, { timeout: 5000 });
    
    // 检查返回的HTML内容是否包含微信失效链接的特定标记
    if (response.data && typeof response.data === 'string' && 
        response.data.includes('<div class="weui-msg__title warn">临时链接已失效</div>')) {
      console.warn('检测到失效链接:', url);
      
      // 调用后端接口记录失效链接
      await handleInvalidLink(url);
      
      // 显示错误提示
      ElMessage.error('该文章链接已失效，无法访问原文');
      return false;
    }
    
    return true;
  } catch (error) {
    console.error('访问链接失败:', error);
    
    // 请求失败也可能是链接失效，调用后端接口记录
    await handleInvalidLink(url);
    
    // 显示错误提示
    ElMessage.error('无法访问该文章链接，可能已失效');
    return false;
  }
};

/**
 * 打开文章链接
 * @param {Object} article - 文章对象
 */
export const openArticleLink = async (article) => {
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
    
    // 直接打开链接，不进行预检查
    window.open(url, '_blank');
  } catch (error) {
    console.error('无效的URL格式:', url, error);
    ElMessage.error('无效的文章链接格式');
  }
}; 