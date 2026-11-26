import request from '@/utils/request'
import { ElMessage, ElLoading } from 'element-plus'
import { saveAs } from 'file-saver'
import { blobValidate } from '@/utils/ruoyi'
import errorCode from '@/utils/errorCode'

/**
 * 多方案对比（勾选 2~4 条全流程计算记录，读取结果快照并列比较指标）
 */
export function compareRecords(calcIds) {
  return request({
    url: '/metallurgy/analysis/compare',
    method: 'post',
    data: { calcIds }
  })
}

/**
 * 敏感性分析（以一条全流程记录为基线，对目标冰铜品位 / 转炉吹炼渣硅铁比逐点真实重算）
 */
export function sensitivityScan(data) {
  return request({
    url: '/metallurgy/analysis/sensitivity',
    method: 'post',
    data
  })
}

/**
 * 分析结果导出
 *
 * 说明：对比与敏感性的导出接口以 JSON 请求体接收参数（含变长记录 ID 列表），
 * 与若依通用 download 的表单编码方式不兼容，故此处直接以 blob 方式接收并另存，
 * 同时保持与 download 一致的行为——业务异常（HTTP 200 + JSON 提示）时弹出中文错误。
 */
export function exportAnalysis(url, data, filename) {
  const loading = ElLoading.service({ text: '正在导出数据，请稍候', background: 'rgba(0, 0, 0, 0.7)' })
  return request({
    url,
    method: 'post',
    data,
    responseType: 'blob'
  })
    .then(async (res) => {
      if (blobValidate(res)) {
        saveAs(new Blob([res]), filename)
      } else {
        const resText = await res.text()
        const rspObj = JSON.parse(resText)
        ElMessage.error(errorCode[rspObj.code] || rspObj.msg || errorCode['default'])
      }
      loading.close()
    })
    .catch((err) => {
      console.error(err)
      ElMessage.error('导出失败，请稍后重试或联系管理员')
      loading.close()
    })
}
