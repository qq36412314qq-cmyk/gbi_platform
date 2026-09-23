/**
 * 文件上传存储模块接口
 */
import { get, post, upload } from '@/utils/request'
import type { PageResult } from '@/utils/request'

/** 文件上传返回 */
export interface FileUploadResult {
  fileId: number
  fileKey: string
  fileName: string
  fileSize: number
  fileType: string
  fileExt: string
  storageType: string
  previewUrl: string
  md5: string
  duplicateFileId?: number
  duplicateFileKey?: string
}

/** 文件列表项 */
export interface FileItemVO {
  id: number
  fileKey: string
  fileName: string
  fileSize: number
  fileType: string
  fileExt: string
  storageType: string
  md5: string
  bizType: string
  bizId: number
  previewUrl: string
  createTime: string
}

/**
 * 上传文件
 * @param file 文件对象
 * @param bizType 业务类型，默认 hr_entry
 * @param bizId 关联业务ID，默认 0
 */
export function uploadFileApi(file: File, bizType = 'hr_entry', bizId = 0): Promise<FileUploadResult> {
  return upload<FileUploadResult>('/base/file/upload', file, { bizType, bizId })
}

/**
 * 获取文件预览URL
 */
export function getPreviewUrlApi(fileId: number): Promise<string> {
  return get<string>(`/base/file/preview/${fileId}`)
}

/**
 * 删除文件
 */
export function deleteFileApi(fileId: number): Promise<null> {
  return post<null>(`/base/file/delete/${fileId}`)
}

/**
 * 查询文件列表
 */
export function fileListApi(params: { bizType?: string; pageNum: number; pageSize: number }): Promise<PageResult<FileItemVO>> {
  return get<PageResult<FileItemVO>>('/base/file/list', params)
}
