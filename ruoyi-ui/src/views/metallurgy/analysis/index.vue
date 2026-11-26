<template>
  <div class="app-container">
    <el-card shadow="never" class="mb8">
      <template #header><span>敏感性分析</span></template>
      <el-form :model="form" label-width="120px" inline>
        <el-form-item label="基线计算记录">
          <el-select v-model="form.baseCalcId" placeholder="请选择一条全流程计算记录" style="width: 300px">
            <el-option
              v-for="r in recordList"
              :key="r.id"
              :label="'#' + r.id + ' ' + (r.planName || '未关联方案') + '（' + (r.calcTime || '') + '）'"
              :value="r.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="扫描字段">
          <el-select v-model="form.field" style="width: 220px" @change="resetRange">
            <el-option label="目标冰铜品位(%)" value="matteGrade" />
            <el-option label="转炉吹炼渣硅铁比" value="blisterFeSio2" />
          </el-select>
        </el-form-item>
        <el-form-item label="起点">
          <el-input-number v-model="form.start" :precision="2" :step="0.1" :controls="false" style="width: 110px" />
        </el-form-item>
        <el-form-item label="终点">
          <el-input-number v-model="form.end" :precision="2" :step="0.1" :controls="false" style="width: 110px" />
        </el-form-item>
        <el-form-item label="步长">
          <el-input-number v-model="form.step" :precision="2" :min="0.01" :step="0.1" :controls="false" style="width: 110px" />
        </el-form-item>
        <el-form-item>
          <el-button icon="Refresh" @click="loadRecords">刷新记录</el-button>
          <el-button type="primary" icon="DataAnalysis" :loading="loading" @click="handleScan">开始扫描</el-button>
          <el-button type="warning" icon="Download" :disabled="!result" @click="handleExport" v-hasPermi="['metallurgy:analysis:export']">
            导出扫描结果
          </el-button>
        </el-form-item>
      </el-form>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="扫描值为真实进入计算模型后重算的结果（非只改显示值）；每个扫描点独立从基线复制，工艺不可行点保留为空值、折线断开且不补 0。"
      />
      <el-alert
        type="warning"
        :closable="false"
        show-icon
        class="note"
        title="模型边界：富氧浓度与氧料比不参与元素平衡计算，因此不作为扫描字段；“转炉吹炼渣硅铁比”作用于转炉造渣石英石加入量，与“闪速熔炼目标硅铁比”不是同一个参数。"
      />
    </el-card>

    <el-row :gutter="10" class="mb8" v-if="result">
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat">
            <div class="stat-label">扫描字段</div>
            <div class="stat-value small">{{ result.fieldLabel }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat">
            <div class="stat-label">基线取值</div>
            <div class="stat-value">{{ formatCell(result.baseValue) }}<span class="unit">{{ result.fieldUnit }}</span></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat">
            <div class="stat-label">可算扫描点</div>
            <div class="stat-value">{{ result.successCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat">
            <div class="stat-label">工艺不可行点</div>
            <div class="stat-value danger">{{ result.failedCount }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="mb8" v-if="result">
      <template #header>
        <div class="chart-header">
          <span>指标趋势（横轴：{{ result.fieldLabel }}<template v-if="result.fieldUnit">，单位 {{ result.fieldUnit }}</template>）</span>
          <el-select v-model="indicatorName" size="small" style="width: 240px">
            <el-option v-for="ind in result.indicators" :key="ind.name" :label="ind.name" :value="ind.name" />
          </el-select>
        </div>
      </template>
      <div ref="chartRef" style="width: 100%; height: 400px"></div>
    </el-card>

    <el-card shadow="never" v-if="result">
      <template #header>
        <span>扫描数据表（关键指标，共 {{ result.points.length }} 个扫描点；空值为工艺不可行点）</span>
      </template>
      <el-table :data="tableRows" border size="small" max-height="420">
        <el-table-column label="扫描值" prop="scanValue" align="center" width="110" fixed />
        <el-table-column label="是否可行" align="center" width="100" fixed>
          <template #default="scope">
            <el-tag size="small" :type="scope.row.success ? 'success' : 'danger'">{{ scope.row.success ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          v-for="name in keyIndicators"
          :key="name"
          :label="name + unitSuffix(name)"
          :prop="name"
          align="center"
          min-width="130"
          :show-overflow-tooltip="true"
        />
        <el-table-column label="不可行原因" prop="message" align="left" min-width="260" :show-overflow-tooltip="true" />
      </el-table>
      <el-alert
        v-for="(note, i) in result.notes"
        :key="i"
        type="warning"
        :closable="false"
        show-icon
        :title="note"
        class="note"
      />
    </el-card>
  </div>
</template>

<script setup name="MetallurgyAnalysis">
import * as echarts from "echarts"
import { listRecord } from "@/api/metallurgy/record"
import { sensitivityScan, exportAnalysis } from "@/api/metallurgy/analysis"

const { proxy } = getCurrentInstance()

/** 表格与导出展示的关键指标（与后端导出口径一致） */
const keyIndicators = [
  "冰铜品位", "冰铜量", "炉渣量", "铜直收率", "粗铜量",
  "石英石加入量", "阴极铜量", "全流程铜总回收率", "SO2 总产量", "最大元素平衡残差"
]

const recordList = ref([])
const loading = ref(false)
const result = ref(null)
const indicatorName = ref("")
const chartRef = ref(null)
let chart = null

const form = reactive({
  baseCalcId: undefined,
  field: "matteGrade",
  start: 45,
  end: 65,
  step: 2
})

/** 扫描数据表：一行 = 一个扫描点 */
const tableRows = computed(() => {
  const data = result.value
  if (!data) return []
  return data.points.map((point, index) => {
    const row = {
      scanValue: formatCell(point.value),
      success: point.success,
      message: point.message || ""
    }
    keyIndicators.forEach((name) => {
      row[name] = cellOf(name, index)
    })
    return row
  })
})

/** 加载可作为基线的全流程计算记录 */
function loadRecords() {
  listRecord({ pageNum: 1, pageSize: 100, calcType: "2" }).then((response) => {
    recordList.value = response.rows || []
    if (!recordList.value.length) {
      proxy.$modal.msgWarning("暂无可用的全流程计算记录，请先执行全流程物料平衡计算")
    }
  })
}

/** 按扫描字段给出默认区间（切换字段时重置，避免把品位的区间套用到硅铁比上） */
function resetRange() {
  if (form.field === "matteGrade") {
    form.start = 45
    form.end = 65
    form.step = 2
  } else {
    form.start = 1.4
    form.end = 2.2
    form.step = 0.2
  }
}

/** 执行扫描 */
function handleScan() {
  if (!form.baseCalcId) {
    proxy.$modal.msgWarning("请先选择一条全流程计算记录作为基线")
    return
  }
  if (form.start === null || form.end === null || form.step === null) {
    proxy.$modal.msgWarning("请完整填写扫描起点、终点与步长")
    return
  }
  loading.value = true
  sensitivityScan({ ...form })
    .then((response) => {
      result.value = response.data
      const indicators = result.value.indicators || []
      if (!indicatorName.value || !indicators.some((ind) => ind.name === indicatorName.value)) {
        indicatorName.value = indicators.some((ind) => ind.name === "冰铜量") ? "冰铜量" : (indicators[0] ? indicators[0].name : "")
      }
      loading.value = false
      nextTick(() => renderChart())
    })
    .catch(() => {
      loading.value = false
    })
}

/** 渲染折线图：空值处断开（connectNulls=false），不补 0 */
function renderChart() {
  const data = result.value
  if (!chartRef.value || !data) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  const series = (data.indicators || []).find((ind) => ind.name === indicatorName.value)
  const values = series ? series.values.map((v) => (v === null || v === undefined ? null : Number(v))) : []
  chart.setOption(
    {
      tooltip: {
        trigger: "axis",
        formatter: (params) => {
          if (!params || !params.length) return ""
          const param = params[0]
          const point = data.points[param.dataIndex] || {}
          let html = data.fieldLabel + "：" + param.axisValue + "<br/>"
          html += param.seriesName + "：" + (param.data === null || param.data === undefined ? "—" : param.data)
          if (point.success === false) {
            html += '<br/><span style="color:#F56C6C">工艺不可行：' + (point.message || "该点无法计算") + "</span>"
          }
          return html
        }
      },
      grid: { left: 70, right: 30, top: 40, bottom: 50 },
      xAxis: {
        type: "category",
        name: data.fieldLabel,
        nameLocation: "middle",
        nameGap: 30,
        data: data.points.map((p) => formatCell(p.value)),
        axisLabel: { color: "#606266" }
      },
      yAxis: { type: "value", name: indicatorName.value, axisLabel: { color: "#606266" } },
      series: [
        {
          name: indicatorName.value,
          type: "line",
          symbolSize: 8,
          connectNulls: false,
          lineStyle: { width: 2, color: "#409EFF" },
          itemStyle: { color: "#409EFF" },
          data: values
        }
      ]
    },
    true
  )
  chart.resize()
}

/** 导出扫描结果 */
function handleExport() {
  if (!result.value) return
  exportAnalysis("/metallurgy/analysis/sensitivity/export", { ...form }, `敏感性分析_${new Date().getTime()}.xlsx`)
}

/** 取某指标在第 index 个扫描点上的取值 */
function cellOf(name, index) {
  const data = result.value
  if (!data) return "—"
  const series = (data.indicators || []).find((ind) => ind.name === name)
  if (!series || !series.values || index >= series.values.length) return "—"
  return formatCell(series.values[index])
}

/** 表头单位后缀 */
function unitSuffix(name) {
  const data = result.value
  if (!data) return ""
  const series = (data.indicators || []).find((ind) => ind.name === name)
  return series && series.unit ? "(" + series.unit + ")" : ""
}

/** 单元格格式化：无数据显示占位符 */
function formatCell(value) {
  return value === null || value === undefined || value === "" ? "—" : value
}

/** 自适应尺寸 */
function handleResize() {
  if (chart) {
    chart.resize()
  }
}

onMounted(() => {
  window.addEventListener("resize", handleResize)
  loadRecords()
})

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize)
  if (chart) {
    chart.dispose()
    chart = null
  }
})
</script>

<style scoped>
.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.stat {
  text-align: center;
  padding: 4px 0;
}
.stat-label {
  color: #909399;
  font-size: 12px;
}
.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-top: 4px;
}
.stat-value.small {
  font-size: 16px;
}
.stat-value.danger {
  color: #F56C6C;
}
.unit {
  font-size: 12px;
  font-weight: 400;
  color: #909399;
  margin-left: 2px;
}
.note {
  margin-top: 8px;
}
</style>
