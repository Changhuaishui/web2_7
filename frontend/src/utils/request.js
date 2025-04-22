import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request = axios.create({
  // 不需要添加额外的baseURL，因为我们在vite.config.js中已经配置了代理
  baseURL: '',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8',
    'Accept': 'application/json;charset=UTF-8'
  },
  // 确保正确处理响应编码
  responseType: 'json',
  responseEncoding: 'utf8'
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 添加调试信息
    console.log('发送请求:', {
      url: config.url,
      method: config.method,
      data: config.data,
      params: config.params
    })
    return config
  },
  error => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 添加调试信息
    console.log('收到响应:', {
      url: response.config.url,
      status: response.status,
      data: response.data
    })
    return response.data
  },
  error => {
    console.group('API请求错误');
    console.error('请求错误详情：', error);
    
    // 请求配置信息
    if (error.config) {
      console.error('请求URL:', error.config.url);
      console.error('请求方法:', error.config.method);
      console.error('请求头:', error.config.headers);
      console.error('请求数据:', error.config.data);
    }
    
    // 处理不同的错误情况
    if (error.response) {
      // 服务器返回错误状态码
      console.error('响应状态码:', error.response.status);
      console.error('响应数据:', error.response.data);
      
      switch (error.response.status) {
        case 400:
          ElMessage.error('请求参数错误')
          break
        case 401:
          ElMessage.error('未授权，请重新登录')
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(`请求失败: ${error.response.status}`)
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      console.error('未收到响应，请检查网络或服务器状态');
      ElMessage.error('无法连接到服务器')
    } else {
      // 请求配置出错
      console.error('请求配置错误:', error.message);
      ElMessage.error('请求配置错误')
    }
    
    console.groupEnd();
    
    return Promise.reject(error)
  }
)

export default request 