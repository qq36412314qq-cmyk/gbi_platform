<template>
  <div class="g-error-page">
    <div class="g-error-code">403</div>
    <div class="g-error-desc">抱歉，您没有权限访问该页面</div>
    <el-button type="primary" @click="router.push('/dashboard')">返回首页</el-button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.g-error-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.g-error-code {
  font-size: 72px;
  font-weight: 700;
  color: var(--color-primary);
}

.g-error-desc {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}
</style>