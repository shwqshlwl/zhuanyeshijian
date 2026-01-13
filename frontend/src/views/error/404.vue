<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">404</div>
      <div class="error-title">页面未找到</div>
      <div class="error-desc">抱歉，您访问的页面不存在</div>
      <el-button type="primary" size="large" @click="$router.push('/')">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
}

.error-content {
  text-align: center;
  
  .error-code {
    font-size: 120px;
    font-weight: 700;
    color: #3b82f6;
    line-height: 1;
    margin-bottom: 16px;
    text-shadow: 4px 4px 0 #e0e7ff;
  }
  
  .error-title {
    font-size: 24px;
    font-weight: 600;
    color: #1e293b;
    margin-bottom: 8px;
  }
  
  .error-desc {
    font-size: 14px;
    color: #64748b;
    margin-bottom: 32px;
  }
}
</style>
