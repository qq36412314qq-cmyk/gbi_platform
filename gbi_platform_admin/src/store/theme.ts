/**
 * 主题状态：登录后拉取本公司 sys_ui_theme 配置，实时生效
 * 配置项：主色 / 布局模式 / 卡片圆角 / 暗黑模式
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface ThemeConfig {
  id?: number
  companyId: number
  primaryColor: string
  layoutMode: 'side' | 'top'
  cardRadius: number
  darkMode: 0 | 1
}

export const DEFAULT_THEME: ThemeConfig = {
  companyId: 0,
  primaryColor: '#2f6bff',
  layoutMode: 'side',
  cardRadius: 6,
  darkMode: 0
}

export const useThemeStore = defineStore('theme', () => {
  const theme = ref<ThemeConfig>({ ...DEFAULT_THEME })

  /** 应用主题到全局 CSS 变量 */
  function applyTheme(config?: ThemeConfig): void {
    if (config) {
      theme.value = { ...theme.value, ...config }
    }
    const root = document.documentElement
    root.style.setProperty('--color-primary', theme.value.primaryColor)
    root.style.setProperty('--color-primary-light', hexToRgba(theme.value.primaryColor, 0.1))
    root.style.setProperty('--color-primary-dark', darkenHex(theme.value.primaryColor, 0.75))
    root.style.setProperty('--card-radius', `${theme.value.cardRadius}px`)
    root.style.setProperty('--layout-mode', theme.value.layoutMode)
    // 暗黑模式切换
    if (theme.value.darkMode === 1) {
      root.classList.add('dark')
    } else {
      root.classList.remove('dark')
    }
  }

  /** 十六进制转 rgba（辅助） */
  function hexToRgba(hex: string, alpha: number): string {
    const { r, g, b } = parseHex(hex)
    return `rgba(${r}, ${g}, ${b}, ${alpha})`
  }

  /** 主色加深（hover 态） */
  function darkenHex(hex: string, ratio: number): string {
    const { r, g, b } = parseHex(hex)
    const dark = (v: number) => Math.round(v * ratio)
    return `rgb(${dark(r)}, ${dark(g)}, ${dark(b)})`
  }

  function parseHex(hex: string): { r: number; g: number; b: number } {
    let value = hex.replace('#', '')
    if (value.length === 3) {
      value = value
        .split('')
        .map((c) => c + c)
        .join('')
    }
    const num = parseInt(value, 16)
    return { r: (num >> 16) & 255, g: (num >> 8) & 255, b: num & 255 }
  }

  return { theme, applyTheme }
})