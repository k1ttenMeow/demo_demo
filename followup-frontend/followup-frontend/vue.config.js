const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    // 关键：强制前端端口为8080，优先级最高，覆盖所有其他配置
    port: 8080,
    // 端口被占用时，禁止自动跳转（绝对避免跑到8081）
    client: {
      webSocketURL: 'ws://localhost:8080/ws'
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        pathRewrite: {
          '^/api': ''
        }
      }
    }
  }
})