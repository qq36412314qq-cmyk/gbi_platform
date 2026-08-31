/**
 * 本地存储封装（token 仅存 Pinia + localStorage，退出清空）
 */
const PREFIX = 'gbi_admin_'

export function getStorage<T = string>(key: string): T | null {
  const raw = localStorage.getItem(PREFIX + key)
  if (raw === null || raw === '') {
    return null
  }
  try {
    return JSON.parse(raw) as T
  } catch {
    return raw as unknown as T
  }
}

export function setStorage(key: string, value: unknown): void {
  localStorage.setItem(PREFIX + key, JSON.stringify(value))
}

export function removeStorage(key: string): void {
  localStorage.removeItem(PREFIX + key)
}

export function clearStorage(): void {
  Object.keys(localStorage)
    .filter((k) => k.startsWith(PREFIX))
    .forEach((k) => localStorage.removeItem(k))
}