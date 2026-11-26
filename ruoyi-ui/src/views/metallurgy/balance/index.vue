<template>
  <div class="app-container">
    <el-card shadow="never" class="mb8">
      <template #header><span>全流程物料平衡计算</span></template>
      <el-form inline>
        <el-form-item label="计算记录">
          <el-select v-model="calcId" placeholder="请选择全流程计算记录" style="width: 320px" @change="loadBalance">
            <el-option v-for="r in recordList" :key="r.id" :label="'#' + r.id + ' ' + (r.planName || '未关联方案') + '（' + r.calcTime + '）'" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button icon="Refresh" @click="loadRecords">刷新记录</el-button>
          <el-button type="primary" icon="Share" :disabled="!balance" @click="goStream">查看物质流分析</el-button>
        </el-form-item>
      </el-form>
      <el-alert v-if="!balance" type="info" :closable="false" show-icon title="请选择一条全流程计算记录，或先在配料计算页面点击「保存并执行全流程计算」" />
    </el-card>

    <template v-if="balance">
      <el-row :gutter="10" class="mb8">
        <el-col :span="4" v-for="ind in indicatorList" :key="ind.name">
          <el-card shadow="never">
            <div class="stat">
              <div class="stat-label">{{ ind.name }}</div>
              <div class="stat-value">{{ ind.value }}<span class="unit">{{ ind.unit }}</span></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="never">
            <div class="stat">
              <div class="stat-label">元素平衡最大残差</div>
              <div class="stat-value" :class="{ warn: Number(balance.maxResidual) > 0.5 }">{{ balance.maxResidual }} %</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="mb8">
        <template #header><span>计算基准与模型假设（写入软件说明书"理论依据"）</span></template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="计算基准">{{ balance.basis }}</el-descriptions-item>
          <el-descriptions-item label="约束结论">
            <el-tag :type="balance.feasible ? 'success' : 'warning'">{{ balance.feasible ? '元素守恒满足要求（残差 ≤0.5%）' : '存在超出容差的残差' }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <el-collapse style="margin-top: 8px">
          <el-collapse-item title="模型假设与简化条件">
            <div v-for="(a, i) in balance.assumptions" :key="i" class="note-line">{{ i + 1 }}. {{ a }}</div>
          </el-collapse-item>
        </el-collapse>
        <el-alert v-for="(w, i) in balance.warnings" :key="i" type="warning" :closable="false" show-icon :title="w" style="margin-top: 8px" />
      </el-card>

      <el-card v-for="node in nodes" :key="node.nodeName" shadow="never" class="mb8">
        <template #header>
          <span class="node-title">{{ node.nodeName }}</span>
          <span class="node-sub">输入 {{ node.inputs.length }} 股 / 输出 {{ node.outputs.length }} 股</span>
        </template>

        <el-row :gutter="10">
          <el-col :span="12">
            <div class="sub-title">输入流（kg / 100kg 混合料）</div>
            <el-table :data="node.inputs" size="small" border>
              <el-table-column label="名称" prop="name" align="center" :show-overflow-tooltip="true" />
              <el-table-column label="总质量" prop="totalMass" align="center" width="95" />
              <el-table-column label="Cu" prop="cu" align="center" width="80" />
              <el-table-column label="Fe" prop="fe" align="center" width="80" />
              <el-table-column label="S" prop="s" align="center" width="80" />
              <el-table-column label="SiO2" prop="sio2" align="center" width="80" />
              <el-table-column label="CaO" prop="cao" align="center" width="80" />
            </el-table>
          </el-col>
          <el-col :span="12">
            <div class="sub-title">输出流（kg / 100kg 混合料）</div>
            <el-table :data="node.outputs" size="small" border>
              <el-table-column label="名称" prop="name" align="center" :show-overflow-tooltip="true" />
              <el-table-column label="总质量" prop="totalMass" align="center" width="95" />
              <el-table-column label="Cu" prop="cu" align="center" width="80" />
              <el-table-column label="Fe" prop="fe" align="center" width="80" />
              <el-table-column label="S" prop="s" align="center" width="80" />
              <el-table-column label="SiO2" prop="sio2" align="center" width="80" />
              <el-table-column label="CaO" prop="cao" align="center" width="80" />
            </el-table>
          </el-col>
        </el-row>

        <div class="sub-title mt10">工艺指标</div>
        <el-descriptions :column="4" border size="small">
          <el-descriptions-item v-for="ind in node.indicatorList" :key="ind.name" :label="ind.name">
            {{ ind.value }}<span class="unit">{{ ind.unit }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <div class="sub-title mt10">元素平衡校核（输入 = 输出，残差应为 0）</div>
        <el-table :data="node.elementBalances" size="small" border>
          <el-table-column label="元素/化合物" prop="element" align="center" width="120" />
          <el-table-column label="输入(kg)" prop="inputMass" align="center" />
          <el-table-column label="输出(kg)" prop="outputMass" align="center" />
          <el-table-column label="残差(%)" align="center">
            <template #default="scope">
              <el-tag :type="Math.abs(Number(scope.row.residual)) <= 0.5 ? 'success' : 'danger'" size="small">{{ scope.row.residual }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>
  </div>
</template>

<script setup name="MetallurgyBalance">
import { listRecord } from "@/api/metallurgy/record"
import { getBalance } from "@/api/metallurgy/stream"

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

const recordList = ref([])
const calcId = ref(undefined)
const balance = ref(null)

/** 指标转换为数组（保留后端返回顺序） */
const indicatorList = computed(() => {
  if (!balance.value) return []
  return Object.keys(balance.value.indicators).map(key => ({
    name: key,
    value: balance.value.indicators[key],
    unit: balance.value.indicatorUnits ? balance.value.indicatorUnits[key] || "" : ""
  }))
})

/** 节点加工：指标对象转数组 */
const nodes = computed(() => {
  if (!balance.value) return []
  return (balance.value.nodes || []).map(node => ({
    ...node,
    indicatorList: Object.keys(node.indicators || {}).map(key => ({
      name: key,
      value: node.indicators[key],
      unit: node.indicatorUnits ? node.indicatorUnits[key] || "" : ""
    }))
  }))
})

/** 加载全流程计算记录 */
function loadRecords() {
  listRecord({ pageNum: 1, pageSize: 100, calcType: "2" }).then(response => {
    recordList.value = response.rows || []
    if (!calcId.value && recordList.value.length > 0) {
      calcId.value = recordList.value[0].id
      loadBalance()
    }
  })
}

/** 加载平衡结果 */
function loadBalance() {
  if (!calcId.value) return
  getBalance(calcId.value).then(response => {
    balance.value = response.data
  })
}

/** 跳转物质流分析 */
function goStream() {
  router.push({ path: "/metallurgy/stream", query: { calcId: calcId.value } })
}

if (route.query.calcId) {
  calcId.value = Number(route.query.calcId)
  getBalance(calcId.value).then(response => {
    balance.value = response.data
  })
}
loadRecords()
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
.stat-value.warn {
  color: #e6a23c;
}
.unit {
  font-size: 12px;
  font-weight: 400;
  color: #909399;
  margin-left: 2px;
}
.node-title {
  font-weight: 600;
  font-size: 15px;
}
.node-sub {
  color: #909399;
  font-size: 12px;
  margin-left: 12px;
}
.sub-title {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 6px;
}
.note-line {
  font-size: 13px;
  line-height: 22px;
  color: #606266;
}
.mt10 {
  margin-top: 10px;
}
</style>
