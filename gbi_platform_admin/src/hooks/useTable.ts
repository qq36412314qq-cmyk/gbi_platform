/**
 * useTable：表格分页查询组合逻辑（统一分页参数 + 防抖）
 * 用法：const { query, records, total, loading, loadData } = useTable(fetchApi)
 */
import { onMounted, reactive, ref } from 'vue'
import type { PageResult } from '@/utils/request'

export interface PageQuery {
  pageNum: number
  pageSize: number
  [key: string]: unknown
}

export function useTable<T = Record<string, unknown>>(
  fetchApi: (params: PageQuery) => Promise<PageResult<T>>,
  initQuery: Record<string, unknown> = {}
) {
  const query = reactive<PageQuery>({ pageNum: 1, pageSize: 10, ...initQuery })
  const records = ref<T[]>([])
  const total = ref(0)
  const loading = ref(false)
  let timer: ReturnType<typeof setTimeout> | null = null

  /** 分页/筛选查询（带防抖） */
  async function loadData(debounce = false): Promise<void> {
    if (timer) {
      clearTimeout(timer)
    }
    const run = async () => {
      loading.value = true
      try {
        const data = await fetchApi({ ...query })
        records.value = data.records || []
        total.value = data.total || 0
      } catch (err) {
        console.error('useTable loadData error:', err)
      } finally {
        loading.value = false
      }
    }
    if (debounce) {
      timer = setTimeout(run, 300)
    } else {
      await run()
    }
  }

  /** 重置查询条件 */
  function resetQuery(): void {
    Object.keys(query).forEach((key) => {
      if (key !== 'pageNum' && key !== 'pageSize') {
        query[key] = undefined
      }
    })
    query.pageNum = 1
  }

  onMounted(() => loadData())

  return { query, records, total, loading, loadData, resetQuery }
}
