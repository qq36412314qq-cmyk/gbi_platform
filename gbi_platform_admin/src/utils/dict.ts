/**
 * 字典转换工具：从 Pinia 字典 store 读取，禁止页面硬编码字典文案
 * 使用方式：getDictLabel('stall_status', 0)
 */
import { useDictStore } from '@/store/dict'

/** 字典值转文字 */
export function getDictLabel(dictCode: string, value: string | number | null | undefined): string {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  const dictStore = useDictStore()
  const item = dictStore.getDictItem(dictCode, String(value))
  return item ? item.dictValue : String(value)
}

/** 获取字典选项列表（用于下拉），异步加载保证缓存就绪 */
export async function getDictOptions(dictCode: string): Promise<{ label: string; value: string }[]> {
  const dictStore = useDictStore()
  const list = await dictStore.getDict(dictCode)
  return list
    .filter((item) => item.status === 1)
    .map((item) => ({ label: item.dictValue, value: item.dictKey }))
}