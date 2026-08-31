<template>
  <div class="g-page-wrap theme-wrap">
    <!-- 顶部操作区 -->
    <div class="g-page-header">
      <span class="g-page-title">UI 主题配置</span>
      <div>
        <AuthBtn permission="theme:edit" type="primary" :loading="saveLoading" @click="handleSave">保存配置</AuthBtn>
      </div>
    </div>

    <div class="g-card g-theme-body">
      <el-form :model="form" label-width="110px">
        <el-form-item label="系统主色">
          <div class="g-theme-color-row">
            <el-color-picker v-model="form.primaryColor" :predefine="predefineColors" />
            <span class="g-theme-color-value">{{ form.primaryColor }}</span>
            <span class="g-theme-color-tip">仅支持低饱和商务色，状态色不可自定义</span>
          </div>
        </el-form-item>
        <el-form-item label="导航布局">
          <el-radio-group v-model="form.layoutMode">
            <el-radio-button value="side">侧边导航</el-radio-button>
            <el-radio-button value="top">顶部导航</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="卡片圆角">
          <el-slider v-model="form.cardRadius" :min="0" :max="16" show-input style="max-width: 320px" />
        </el-form-item>
        <el-form-item label="深色模式">
          <el-switch v-model="form.darkMode" :active-value="1" :inactive-value="0" />
          <span class="g-theme-color-tip g-ml-8">切换后全局即时生效</span>
        </el-form-item>

        <!-- 实时预览 -->
        <el-divider content-position="left">实时预览</el-divider>
        <div class="g-theme-preview">
          <el-button type="primary">主按钮</el-button>
          <el-button type="success">成功按钮</el-button>
          <el-button type="warning">警告按钮</el-button>
          <el-button type="danger">危险按钮</el-button>
          <el-card shadow="hover" class="g-theme-preview-card">
            <div class="g-theme-preview-title">卡片预览</div>
            <div class="g-theme-preview-text">当前主色 {{ form.primaryColor }}，圆角 {{ form.cardRadius }}px</div>
          </el-card>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * UI 主题配置页：sys_ui_theme 集团/子公司隔离配置（保存走审计）
 * 保存后实时全局生效，无需刷新页面
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getThemeApi, saveThemeApi } from '@/api/sys'
import { useThemeStore, DEFAULT_THEME } from '@/store/theme'

const themeStore = useThemeStore()
const saveLoading = ref(false)

/** 预置商务色板 */
const predefineColors = ['#2f6bff', '#0e7a5f', '#7a5af8', '#b7791f', '#d14343', '#0f766e', '#3f5b8b']

const form = reactive({ ...DEFAULT_THEME })

/** 加载当前公司主题 */
async function loadTheme(): Promise<void> {
  try {
    const data = await getThemeApi()
    Object.assign(form, data)
  } catch {
    // 无配置时使用默认主题
    Object.assign(form, DEFAULT_THEME)
  }
  // 应用预览
  themeStore.applyTheme({ ...form })
}

/** 保存主题（实时生效） */
async function handleSave(): Promise<void> {
  saveLoading.value = true
  try {
    await saveThemeApi({ ...form })
    themeStore.applyTheme({ ...form })
    ElMessage.success('主题保存成功，已实时生效')
  } finally {
    saveLoading.value = false
  }
}

onMounted(loadTheme)
</script>

<style scoped>
.g-theme-body {
  max-width: 720px;
}

.g-theme-color-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.g-theme-color-value {
  font-size: var(--font-size-base);
  color: var(--color-text-regular);
  font-family: monospace;
}

.g-theme-color-tip {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}

.g-ml-8 {
  margin-left: 8px;
}

.g-theme-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-start;
}

.g-theme-preview-card {
  width: 260px;
}

.g-theme-preview-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  margin-bottom: 6px;
}

.g-theme-preview-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}
</style>