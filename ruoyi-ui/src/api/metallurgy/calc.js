import request from '@/utils/request'

// 配料计算（求解各物料配比）
export function calcRatio(data) {
  return request({
    url: '/metallurgy/calc/ratio',
    method: 'post',
    data: data
  })
}

// 全流程物料平衡计算（并保存计算记录）
export function calcBalance(data) {
  return request({
    url: '/metallurgy/calc/balance',
    method: 'post',
    data: data
  })
}
