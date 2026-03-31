import request from '@/utils/request'

/**
 * 上传文件
 * @param file 文件对象
 * @param bizType 业务类型 (如: house_image, house_video)
 * @param bizId 业务ID
 * @param fileType 文件类型 (image/video/document)
 */
export function uploadFile(file, bizType, bizId, fileType) {
  const formData = new FormData()
  formData.append('file', file)
  if (bizType) formData.append('bizType', bizType)
  if (bizId) formData.append('bizId', bizId)
  if (fileType) formData.append('fileType', fileType)
  
  return request({
    url: '/api/file/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 获取业务文件列表
 * @param bizType 业务类型
 * @param bizId 业务ID
 */
export function getBizFiles(bizType, bizId) {
  return request({
    url: '/api/file/biz-files',
    method: 'get',
    params: { bizType, bizId }
  })
}

/**
 * 检查分片 (断点续传)
 */
export function checkChunk(identifier) {
  return request({
    url: '/api/file/chunk',
    method: 'get',
    params: { identifier }
  })
}

/**
 * 分片上传
 */
export function uploadChunk(data) {
  return request({
    url: '/api/file/chunk',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 合并分片
 */
export function mergeChunk(identifier, fileName) {
  return request({
    url: '/api/file/chunk/merge',
    method: 'post',
    params: { identifier, fileName }
  })
}

/**
 * 获取文件列表 (归档日志)
 * @param params { pageNum, pageSize, fileName, fileExt }
 */
export function getFileList(params) {
  return request({
    url: '/api/file/list',
    method: 'get',
    params
  })
}

/**
 * 删除文件 (物理删除)
 */
export function deleteFile(id) {
  return request({
    url: `/api/file/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 更新文件元数据 (重命名)
 */
export function updateFile(data) {
  return request({
    url: '/api/file/update',
    method: 'put',
    data
  })
}

/**
 * 下载文件 (通过后端流)
 */
export function downloadFile(id) {
  return request({
    url: `/api/file/download/${id}`,
    method: 'get',
    responseType: 'blob'
  })
}
