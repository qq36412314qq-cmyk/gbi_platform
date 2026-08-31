/**
 * 数据格式化统一工具（禁止页面各自写转换逻辑）
 * formatMoney / formatDate / formatDateTime / formatPhone / getDictLabel
 */
import dayjs from 'dayjs'

/** 金额保留两位小数 */
export function formatMoney(value: number | string | null | undefined): string {
  if (value === null || value === undefined || value === '') {
    return '0.00'
  }
  const num = Number(value)
  if (Number.isNaN(num)) {
    return '0.00'
  }
  return num.toFixed(2)
}

/** 标准日期 yyyy-MM-dd */
export function formatDate(value: string | number | Date | null | undefined): string {
  if (!value) {
    return '-'
  }
  return dayjs(value).format('YYYY-MM-DD')
}

/** 标准日期时间 yyyy-MM-dd HH:mm:ss */
export function formatDateTime(value: string | number | Date | null | undefined): string {
  if (!value) {
    return '-'
  }
  return dayjs(value).format('YYYY-MM-DD HH:mm:ss')
}

/** 手机号脱敏 138****1234 */
export function formatPhone(phone: string | null | undefined): string {
  if (!phone || phone.length !== 11) {
    return phone || '-'
  }
  return `${phone.slice(0, 3)}****${phone.slice(7)}`
}

/** 身份证脱敏 410***********1234 */
export function formatIdCard(idCard: string | null | undefined): string {
  if (!idCard || idCard.length < 8) {
    return idCard || '-'
  }
  return `${idCard.slice(0, 4)}***********${idCard.slice(-4)}`
}

/** 银行卡/对公账号仅展示后四位 */
export function formatBankAccount(account: string | null | undefined): string {
  if (!account) {
    return '-'
  }
  if (account.length <= 4) {
    return account
  }
  return `****${account.slice(-4)}`
}