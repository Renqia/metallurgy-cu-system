import request from '@/utils/request'

// 查询物料台账列表
export function listMaterial(query) {
  return request({
    url: '/metallurgy/material/list',
    method: 'get',
    params: query
  })
}

// 查询物料台账详细
export function getMaterial(id) {
  return request({
    url: '/metallurgy/material/' + id,
    method: 'get'
  })
}

// 新增物料台账
export function addMaterial(data) {
  return request({
    url: '/metallurgy/material',
    method: 'post',
    data: data
  })
}

// 修改物料台账
export function updateMaterial(data) {
  return request({
    url: '/metallurgy/material',
    method: 'put',
    data: data
  })
}

// 删除物料台账
export function delMaterial(id) {
  return request({
    url: '/metallurgy/material/' + id,
    method: 'delete'
  })
}
