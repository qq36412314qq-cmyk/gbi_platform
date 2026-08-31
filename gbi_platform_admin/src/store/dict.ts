/**
 * 字典状态：启动/登录后加载，前端缓存，定时刷新
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getDictDataApi } from '@/api/base'

export interface DictItem {
  id: number
  dictTypeId: number
  dictValue: string
  dictKey: string
  sortOrder: number
  status: number
}

export const useDictStore = defineStore('dict', () => {
  /** 字典缓存：dictCode -> DictItem[] */
  const dictMap = ref<Record<string, DictItem[]>>({})
  const loadingTypes = ref<string[]>([])

  /** 获取字典（带缓存） */
  async function getDict(dictCode: string): Promise<DictItem[]> {
    if (dictMap.value[dictCode]) {
      return dictMap.value[dictCode]
    }
    if (loadingTypes.value.includes(dictCode)) {
      return dictMap.value[dictCode] || []
    }
    loadingTypes.value.push(dictCode)
    try {
      const data = await getDictDataApi(dictCode)
      dictMap.value[dictCode] = data || []
    } finally {
      loadingTypes.value = loadingTypes.value.filter((item) => item !== dictCode)
    }
    return dictMap.value[dictCode] || []
  }

  /** 字典值转文案 */
  function getDictItem(dictCode: string, key: string): DictItem | undefined {
    const list = dictMap.value[dictCode] || []
    return list.find((item) => item.dictKey === key && item.status === 1)
  }

  /** 清除缓存（字典维护后调用） */
  function clearDict(dictCode?: string): void {
    if (dictCode) {
      delete dictMap.value[dictCode]
    } else {
      dictMap.value = {}
    }
  }

  return { dictMap, getDict, getDictItem, clearDict }
})