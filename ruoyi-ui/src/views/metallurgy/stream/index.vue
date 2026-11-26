<template>
  <div class="app-container">
    <el-card shadow="never" class="mb8">
      <template #header><span>物质流分析</span></template>
      <el-form inline>
        <el-form-item label="计算记录">
          <el-select v-model="calcId" placeholder="请选择全流程计算记录" style="width: 300px" @change="loadAll">
            <el-option v-for="r in recordList" :key="r.id" :label="'#' + r.id + ' ' + (r.planName || '未关联方案')" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="元素口径">
          <el-radio-group v-model="element" @change="loadSankey">
            <el-radio-button v-for="e in elements" :key="e" :value="e">{{ e }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button icon="Refresh" @click="loadRecords">刷新记录</el-button>
          <el-button type="warning" icon="Download" @click="exportReport" v-hasPermi="['metallurgy:stream:export']">导出物质流报表</el-button>
        </el-form-item>
      </el-form>
      <el-alert type="info" :closable="false" show-icon title="桑基图带宽表示对应元素在工序间的流量；元素口径下流量为 0 的流已自动隐藏，故切换口径时节点数量会变化。" />
    </el-card>

    <el-row :gutter="10" class="mb8" v-if="indicatorList.length > 0">
      <el-col :span="4" v-for="ind in indicatorList" :key="ind.name">
        <el-card shadow="never">
          <div class="stat">
            <div class="stat-label">{{ ind.name }}</div>
            <div class="stat-value">{{ ind.value }}<span class="unit">{{ ind.unit }}</span></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="mb8">
      <template #header>
        <span>物质流桑基图（{{ element }} 口径，节点 {{ sankeyNodes.length }} 个 / 连线 {{ sankeyLinks.length }} 条）</span>
      </template>
      <div ref="chartRef" style="width: 100%; height: 520px"></div>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <span>物质流明细报表（共 {{ flows.length }} 条，单位：kg / 100kg 混合料）</span>
      </template>
      <el-table :data="flows" border size="small" max-height="420">
        <el-table-column label="工序节点" prop="nodeName" align="center" width="110" />
        <el-table-column label="流向" align="center" width="80">
          <template #default="scope">
            <el-tag size="small" :type="scope.row.flowType === '1' ? 'info' : 'success'">{{ scope.row.flowType === '1' ? '输入' : '输出' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="物料/产物" prop="materialName" align="center" :show-overflow-tooltip="true" />
        <el-table-column label="元素/化合物" prop="element" align="center" width="110" />
        <el-table-column label="流量" prop="flowValue" align="center" width="120" />
        <el-table-column label="单位" prop="unit" align="center" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="MetallurgyStream">
import * as echarts from "echarts"
import { listRecord } from "@/api/metallurgy/record"
import { getBalance, getSankey, getFlows } from "@/api/metallurgy/stream"

const { proxy } = getCurrentInstance()
const route = useRoute()

const elements = ["Cu", "Fe", "S", "SiO2", "CaO", "合计"]
const recordList = ref([])
const calcId = ref(undefined)
const element = ref("Cu")
const sankeyNodes = ref([])
const sankeyLinks = ref([])
const flows = ref([])
const balance = ref(null)
const chartRef = ref(null)
let chart = null

/** 指标列表 */
const indicatorList = computed(() => {
  if (!balance.value) return []
  return Object.keys(balance.value.indicators).map(key => ({
    name: key,
    value: balance.value.indicators[key],
    unit: balance.value.indicatorUnits ? balance.value.indicatorUnits[key] || "" : ""
  }))
})

/** 加载计算记录列表 */
function loadRecords() {
  listRecord({ pageNum: 1, pageSize: 100, calcType: "2" }).then(response => {
    recordList.value = response.rows || []
    if (!calcId.value && recordList.value.length > 0) {
      calcId.value = recordList.value[0].id
      loadAll()
    }
  })
}

/** 加载全部数据 */
function loadAll() {
  if (!calcId.value) return
  getBalance(calcId.value).then(response => {
    balance.value = response.data
  })
  loadSankey()
  getFlows(calcId.value).then(response => {
    flows.value = response.data || []
  })
}

/** 加载并渲染桑基图 */
function loadSankey() {
  if (!calcId.value) return
  getSankey(calcId.value, element.value).then(response => {
    const data = response.data || {}
    sankeyNodes.value = data.nodes || []
    sankeyLinks.value = data.links || []
    renderChart()
  })
}

/** 渲染 ECharts 桑基图 */
function renderChart() {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  chart.setOption(
    {
      tooltip: {
        trigger: "item",
        formatter: params => {
          if (params.dataType === "edge") {
            return params.data.source + " → " + params.data.target + "<br/>流量：" + params.data.value + " kg"
          }
          return params.name
        }
      },
      series: [
        {
          type: "sankey",
          left: "3%",
          right: "12%",
          top: "3%",
          bottom: "3%",
          nodeWidth: 18,
          nodeGap: 14,
          layoutIterations: 32,
          emphasis: { focus: "adjacency" },
          lineStyle: { color: "source", curveness: 0.5, opacity: 0.45 },
          label: { fontSize: 12, color: "#303133" },
          data: sankeyNodes.value.map(n => ({ name: n.name })),
          links: sankeyLinks.value.map(l => ({
            source: l.source,
            target: l.target,
            value: Number(l.value)
          }))
        }
      ]
    },
    true
  )
}

/** 自适应尺寸 */
function handleResize() {
  if (chart) {
    chart.resize()
  }
}

/** 导出物质流报表 */
function exportReport() {
  if (!calcId.value) {
    proxy.$modal.msgWarning("请先选择计算记录")
    return
  }
  proxy.download("metallurgy/stream/export/" + calcId.value, {}, `物质流报表_${calcId.value}_${new Date().getTime()}.xlsx`)
}

onMounted(() => {
  window.addEventListener("resize", handleResize)
  if (route.query.calcId) {
    calcId.value = Number(route.query.calcId)
    loadAll()
  }
  loadRecords()
})

// 组件卸载时移除监听并销毁图表实例，避免内存泄漏
onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize)
  if (chart) {
    chart.dispose()
    chart = null
  }
})
</script>

<style scoped>
.stat {
  text-align: center;
  padding: 4px 0;
}
.stat-label {
  color: #909399;
  font-size: 12px;
  height: 32px;
  line-height: 16px;
}
.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-top: 2px;
}
.unit {
  font-size: 12px;
  font-weight: 400;
  color: #909399;
  margin-left: 2px;
}
</style>
