import request from '@/utils/request'

// 查询全流程物料平衡结果
export function getBalance(calcId) {
  return request({
    url: '/metallurgy/stream/balance/' + calcId,
    method: 'get'
  })
}

// 查询桑基图数据
export function getSankey(calcId, element) {
  return request({
    url: '/metallurgy/stream/sankey/' + calcId,
    method: 'get',
    params: { element: element }
  })
}

// 查询物质流明细（报表）
export function getFlows(calcId) {
  return request({
    url: '/metallurgy/stream/flows/' + calcId,
    method: 'get'
  })
}
