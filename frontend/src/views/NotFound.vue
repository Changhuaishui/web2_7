<template>
  <div class="not-found">
    <el-result
      icon="warning"
      title="404"
      sub-title="抱歉，您访问的页面不存在"
    >
      <template #extra>
        <div class="error-details" v-if="route.fullPath">
          <p>请求的路径: {{ route.fullPath }}</p>
          <p v-if="route.params && Object.keys(route.params).length">
            路由参数: {{ JSON.stringify(route.params) }}
          </p>
        </div>
        <el-button type="primary" @click="router.push('/')">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script>
import { useRouter, useRoute } from 'vue-router'

export default {
  name: 'NotFound',
  setup() {
    const router = useRouter()
    const route = useRoute()

    return {
      router,
      route
    }
  }
}
</script>

<style scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
}

.error-details {
  margin: 20px 0;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
  font-family: monospace;
}

.error-details p {
  margin: 5px 0;
  color: #666;
}
</style> 