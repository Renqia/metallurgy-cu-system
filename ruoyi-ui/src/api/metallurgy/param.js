import request from '@/utils/request'

// 查询工艺参数方案列表
export function listParam(query) {
  return request({
    url: '/metallurgy/param/list',
    method: 'get',
    params: query
  })
}

// 查询启用状态的参数方案
export function listEnabledParam() {
  return request({
    url: '/metallurgy/param/enabled',
    method: 'get'
  })
}

// 查询参数参考区间
export function getParamRanges() {
  return request({
    url: '/metallurgy/param/ranges',
    method: 'get'
  })
}

// 查询工艺参数方案详细
export function getParam(id) {
  return request({
    url: '/metallurgy/param/' + id,
    method: 'get'
  })
}

// 新增工艺参数方案
export function addParam(data) {
  return request({
    url: '/metallurgy/param',
    method: 'post',
    data: data
  })
}

// 修改工艺参数方案
export function updateParam(data) {
  return request({
    url: '/metallurgy/param',
    method: 'put',
    data: data
  })
}

// 删除工艺参数方案
export function delParam(id) {
  return request({
    url: '/metallurgy/param/' + id,
    method: 'delete'
  })
}
