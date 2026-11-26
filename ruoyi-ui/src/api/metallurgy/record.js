import request from '@/utils/request'

// 查询计算记录列表
export function listRecord(query) {
  return request({
    url: '/metallurgy/record/list',
    method: 'get',
    params: query
  })
}

// 查询计算记录详情（含输入/结果快照）
export function getRecord(id) {
  return request({
    url: '/metallurgy/record/' + id,
    method: 'get'
  })
}

// 删除计算记录
export function delRecord(id) {
  return request({
    url: '/metallurgy/record/' + id,
    method: 'delete'
  })
}
