<template>
  <el-dialog title="方案对比" v-model="open" width="1080px" append-to-body @opened="loadData" @closed="destroyChart">
    <div v-loading="loading">
      <el-alert
        type="info"
        :closable="false"
        show-icon
        class="mb8"
        title="对比直接读取各记录保存时的结果快照，不重新计算；缺失数据显示为“—”，不补 0。"
      />

      <el-descriptions v-if="columns.length > 0" :column="columns.length" border size="small" class="mb8">
        <el-descriptions-item v-for="col in columns" :key="col.calcId" :label="'记录 #' + col.calcId">
          <div class="col-title">{{ col.planName || '未关联方案' }}</div>
          <div class="col-sub">{{ col.calcTime || '—' }}</div>
          <el-tag size="small" :type="col.feasible ? 'success' : 'danger'">
            {{ col.feasible ? '约束满足' : '残差超限' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-card shadow="never" class="mb8">
        <template #header>
          <span>指标对比（分组柱状图，共 {{ indicators.length }} 项指标）</span>
        </template>
        <div ref="chartRef" style="width: 100%; height: 420px"></div>
      </el-card>

      <el-table :data="indicators" border size="small" max-height="340">
        <el-table-column label="指标" prop="name" align="left" width="200" :show-overflow-tooltip="true" />
        <el-table-column label="单位" prop="unit" align="center" width="160" :show-overflow-tooltip="true" />
        <el-table-column
          v-for="(col, idx) in columns"
          :key="col.calcId"
          :label="'#' + col.calcId"
          align="center"
          min-width="120"
        >
          <template #default="scope">{{ formatCell(scope.row.values[idx]) }}</template>
        </el-table-column>
      </el-table>

      <el-alert
        v-for="(note, i) in notes"
        :key="i"
        type="warning"
        :closable="false"
        show-icon
        :title="note"
        class="note"
      />
    </div>
    <template #footer>
      <el-button type="warning" icon="Download" @click="handleExport" v-hasPermi="['metallurgy:record:compareExport']">
        导出对比结果
      </el-button>
      <el-button @click="open = false">关 闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup name="MetallurgyCompareDialog">
import * as echarts from "echarts"
import { compareRecords, exportAnalysis } from "@/api/metallurgy/analysis"

const { proxy } = getCurrentInstance()

const props = defineProps({
  visible: { type: Boolean, default: false },
  calcIds: { type: Array, default: () => [] }
})
const emit = defineEmits(["update:visible"])

const open = computed({
  get: () => props.visible,
  set: (val) => emit("update:visible", val)
})

const loading = ref(false)
const columns = ref([])
const indicators = ref([])
const notes = ref([])
const chartRef = ref(null)
let chart = null

/** 查询对比数据 */
function loadData() {
  if (!props.calcIds || props.calcIds.length < 2) {
    proxy.$modal.msgWarning("请先勾选 2~4 条全流程计算记录")
    open.value = false
    return
  }
  loading.value = true
  compareRecords(props.calcIds)
    .then((response) => {
      const data = response.data || {}
      columns.value = data.columns || []
      indicators.value = data.indicators || []
      notes.value = data.notes || []
      loading.value = false
      nextTick(() => renderChart())
    })
    .catch(() => {
      loading.value = false
      open.value = false
    })
}

/** 渲染分组柱状图：类目为指标名，系列为各计算记录 */
function renderChart() {
  if (!chartRef.value || columns.value.length === 0) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  const names = indicators.value.map((item) => item.name)
  const series = columns.value.map((col, idx) => ({
    name: "#" + col.calcId + " " + (col.planName || "未关联方案"),
    type: "bar",
    barMaxWidth: 22,
    emphasis: { focus: "series" },
    data: indicators.value.map((item) => {
      const value = item.values ? item.values[idx] : null
      return value === null || value === undefined ? null : Number(value)
    })
  }))
  chart.setOption(
    {
      tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
      legend: { type: "scroll", top: 0 },
      grid: { left: 60, right: 24, top: 44, bottom: 74 },
      xAxis: {
        type: "category",
        data: names,
        axisLabel: { interval: 0, rotate: 42, fontSize: 11, color: "#606266" }
      },
      yAxis: { type: "value", name: "指标值", axisLabel: { color: "#606266" } },
      dataZoom: [{ type: "slider", height: 16, bottom: 30, start: 0, end: 100 }],
      series
    },
    true
  )
  chart.resize()
}

/** 销毁图表实例，避免弹窗反复打开造成内存泄漏 */
function destroyChart() {
  if (chart) {
    chart.dispose()
    chart = null
  }
  columns.value = []
  indicators.value = []
  notes.value = []
}

/** 导出对比结果 */
function handleExport() {
  exportAnalysis(
    "/metallurgy/analysis/compare/export",
    { calcIds: props.calcIds },
    `方案对比_${new Date().getTime()}.xlsx`
  )
}

/** 单元格格式化：无数据统一显示占位符 */
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
})

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize)
  destroyChart()
})
</script>

<style scoped>
.col-title {
  font-weight: 600;
  color: #303133;
}
.col-sub {
  font-size: 12px;
  color: #909399;
  margin: 2px 0 4px 0;
}
.note {
  margin-top: 8px;
}
</style>
