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