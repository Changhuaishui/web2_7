/**
 * 文章内容处理工具
 * 用于处理文章内容中的图片占位符，将其替换为实际的图片元素
 */
export default {
  /**
   * 处理文章内容
   * @param {Object} article 文章对象
   * @returns {String} 处理后的文章内容
   */
  processArticleContent(article) {
    if (!article || !article.content) {
      console.warn('文章内容为空');
      return '';
    }
    
    // 检查内容中是否存在占位符
    const placeholders = article.content.match(/\[\[IMG:([^\]]+)\]\]/g) || [];
    
    // 如果没有图片映射信息但内容中有占位符，尝试自动生成映射信息
    if ((!article.imageMappings || article.imageMappings === '{}') && placeholders.length > 0) {
      console.log(`没有图片映射信息，但发现 ${placeholders.length} 个占位符，尝试自动修复`);
      return this.autoFixPlaceholders(article, placeholders);
    }
    
    // 检查是否有图片映射信息
    if (!article.imageMappings) {
      console.log('没有图片映射信息，返回原始内容');
      return article.content;
    }
    
    try {
      // 解析图片映射
      const imageMappings = JSON.parse(article.imageMappings);
      let processedContent = article.content;
      
      console.log(`处理文章内容，发现 ${Object.keys(imageMappings).length} 张图片需要替换`);
      
      // 先检查一下占位符格式是否正确
      const placeholders = processedContent.match(/\[\[IMG:[^\]]+\]\]/g) || [];
      console.log('找到的占位符:', placeholders);
      
      // 遍历替换所有图片占位符
      for (const imageId in imageMappings) {
        if (!imageId) continue;
        
        const imgData = imageMappings[imageId];
        const placeholder = `[[IMG:${imageId}]]`;
        
        console.log(`处理图片 ${imageId}, 占位符: ${placeholder}, 图片数据:`, imgData);
        
        // 构建图片HTML元素
        let imagePath;
        if (article.ulid) {
          // 使用新的基于ULID的图片路径 - 确保路径格式与ImageController.java期望的格式一致
          // ImageController期望格式: /api/images/{articleUlid}/{imageUlid}
          // 图片ID可能是简单数字或复杂字符串，统一使用ULID格式
          imagePath = `/api/images/${article.ulid}/${imageId}.jpg`;
          console.log(`为图片 ${imageId} 生成规范化路径: ${imagePath}`);
        } else {
          // 兼容旧的图片路径格式
          const imageIndex = imgData.index;
          const images = article.images ? article.images.split(',') : [];
          imagePath = images[imageIndex] || '';
          // 确保旧格式路径也使用正确的API前缀
          if (imagePath && !imagePath.startsWith('/api/')) {
            imagePath = `/api/images/${imagePath}`;
          }
          console.log(`为图片 ${imageId} 使用旧格式路径: ${imagePath}`);
        }
        
        // 创建图片HTML
        const imgHtml = `<img src="${imagePath}" class="article-image" alt="文章图片${Number(imgData.index) + 1}" style="max-width:100%; height:auto;" />`;
        
        // 检查占位符是否存在
        if (processedContent.includes(placeholder)) {
          // 替换占位符
          processedContent = processedContent.replace(placeholder, imgHtml);
          console.log(`替换占位符 ${placeholder} 成功`);
        } else {
          console.warn(`未找到占位符 ${placeholder}`);
          
          // 尝试查找不同格式的占位符
          const altPlaceholder = `[[IMG:${imageId}]]`.replace(/\[/g, '\\[').replace(/\]/g, '\\]');
          const regex = new RegExp(altPlaceholder, 'g');
          
          if (regex.test(processedContent)) {
            processedContent = processedContent.replace(regex, imgHtml);
            console.log(`使用正则表达式替换占位符成功`);
          }
        }
      }
      
      // 最后再检查一次是否还有未替换的占位符
      const remainingPlaceholders = processedContent.match(/\[\[IMG:[^\]]+\]\]/g) || [];
      if (remainingPlaceholders.length > 0) {
        console.warn(`处理后仍有 ${remainingPlaceholders.length} 个未替换的占位符:`, remainingPlaceholders);
      }
      
      return processedContent;
    } catch (error) {
      console.error('处理文章内容出错:', error);
      return article.content;
    }
  },
  
  /**
   * 自动修复没有映射信息但有占位符的文章内容
   * @param {Object} article 文章对象
   * @param {Array} placeholders 找到的占位符列表
   * @returns {String} 处理后的文章内容
   */
  autoFixPlaceholders(article, placeholders) {
    if (!placeholders || !article) return article.content;
    
    // 如果文章已有imageMappings数据，则不需要自动修复
    if (article.imageMappings && Object.keys(JSON.parse(article.imageMappings)).length > 0) {
      console.log('文章已有imageMappings数据，跳过自动修复', article.imageMappings);
      return article.content;
    }
    
    console.log('开始自动修复占位符，文章ID:', article.id);
    
    // 使用正则表达式查找所有图片占位符
    const placeholderRegex = /\[\[IMG:([^\]]+)\]\]/g;
    let match;
    let index = 0;
    let fixedContent = article.content;
    
    while ((match = placeholderRegex.exec(article.content)) !== null) {
      const placeholder = match[0];
      const imageId = match[1];
      console.log(`找到占位符 ${placeholder}，图片ID: ${imageId}`);
      
      // 构建图片HTML元素
      let imagePath;
      if (article.ulid) {
        // 使用新的基于ULID的图片路径 - 确保路径格式与ImageController.java期望的格式一致
        // ImageController期望格式: /api/images/{articleUlid}/{imageUlid}
        // 图片ID可能是简单数字或复杂字符串，统一使用ULID格式
        imagePath = `/api/images/${article.ulid}/${imageId}.jpg`;
        console.log(`为图片 ${imageId} 生成规范化路径: ${imagePath}`);
      } else {
        // 兼容旧的图片路径格式
        const images = article.images ? article.images.split(',') : [];
        imagePath = images[index] || '';
        // 确保路径使用正确的API前缀
        if (imagePath && !imagePath.startsWith('/api/')) {
          imagePath = `/api/images/${imagePath}`;
        }
        console.log(`为图片 ${imageId} 使用旧格式路径: ${imagePath}`);
      }
      
      // 创建图片HTML元素
      const imgHtml = `<div class="article-image-container"><img src="${imagePath}" alt="文章图片 ${index + 1}" style="max-width:100%; height:auto;" /></div>`;
      
      // 替换占位符为图片HTML
      fixedContent = fixedContent.replace(placeholder, imgHtml);
      index++;
    }
    
    console.log(`自动修复完成，共处理了 ${index} 个占位符`);
    return fixedContent;
  }
} 