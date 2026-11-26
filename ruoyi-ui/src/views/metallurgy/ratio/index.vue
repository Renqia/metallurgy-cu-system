<template>
  <div class="app-container">
    <el-card shadow="never" class="mb8">
      <template #header>
        <span>工艺参数（可直接选用已保存的参数方案，或手动调整）</span>
      </template>
      <el-form :model="query" inline>
        <el-form-item label="参数方案">
          <el-select v-model="query.planId" placeholder="请选择参数方案" clearable style="width: 240px" @change="onPlanChange">
            <el-option v-for="p in planList" :key="p.id" :label="p.planName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标冰铜品位(%)">
          <el-input-number v-model="query.matteGrade" :min="30" :max="79" :precision="2" :step="1" controls-position="right" style="width: 150px" />
        </el-form-item>
        <el-form-item label="目标硅铁比 Fe/SiO2">
          <el-input-number v-model="query.feSio2Ratio" :min="0.3" :max="3" :precision="3" :step="0.05" controls-position="right" style="width: 150px" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="query.remark" placeholder="如：基准工况" style="width: 200px" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="mb8">
      <template #header>
        <span>参与计算的物料（勾选后求解；可"锁定"部分物料的配比，其余由系统求解）</span>
      </template>
      <el-table :data="materialList" @selection-change="onSelect" size="small" height="320">
        <el-table-column type="selection" width="45" align="center" />
        <el-table-column label="物料名称" align="center" prop="materialName" :show-overflow-tooltip="true" />
        <el-table-column label="类型" align="center" prop="materialType" width="80" />
        <el-table-column label="Cu(%)" align="center" prop="cuContent" width="75" />
        <el-table-column label="Fe(%)" align="center" prop="feContent" width="75" />
        <el-table-column label="S(%)" align="center" prop="sulfurContent" width="70" />
        <el-table-column label="SiO2(%)" align="center" prop="sio2Content" width="80" />
        <el-table-column label="CaO(%)" align="center" prop="caoContent" width="75" />
        <el-table-column label="单价(元/吨)" align="center" prop="unitPrice" width="95" />
        <el-table-column label="锁定配比" align="center" width="180">
          <template #default="scope">
            <el-switch v-model="scope.row.fixed" active-text="锁定" inline-prompt style="margin-right: 6px" />
            <el-input-number
              v-model="scope.row.fixedRatio"
              :min="0"
              :max="100"
              :precision="2"
              :step="1"
              :disabled="!scope.row.fixed"
              controls-position="right"
              style="width: 90px"
            />
          </template>
        </el-table-column>
      </el-table>
      <div class="mt10">
        <el-button type="primary" icon="Cpu" :loading="solving" @click="solve" v-hasPermi="['metallurgy:calc:ratio']">求解配比</el-button>
        <el-button icon="Refresh" @click="resetAll">重置</el-button>
        <el-button type="success" icon="VideoPlay" :disabled="!result" :loading="running" @click="runBalance" v-hasPermi="['metallurgy:calc:balance']">保存并执行全流程计算</el-button>
        <span class="tip">提示：勾选物料后未锁定者由系统求解；已锁定者按你填写的配比固定。全部锁定且合计为 100% 时，系统仅做约束校核。</span>
      </div>
    </el-card>

    <template v-if="result">
      <el-row :gutter="10" class="mb8">
        <el-col :span="4"><el-card shadow="never"><div class="stat"><div class="stat-label">混合料 Cu 品位</div><div class="stat-value">{{ result.mixedCu }} %</div></div></el-card></el-col>
        <el-col :span="4"><el-card shadow="never"><div class="stat"><div class="stat-label">混合料 Fe</div><div class="stat-value">{{ result.mixedFe }} %</div></div></el-card></el-col>
        <el-col :span="4"><el-card shadow="never"><div class="stat"><div class="stat-label">混合料 S</div><div class="stat-value">{{ result.mixedS }} %</div></div></el-card></el-col>
        <el-col :span="4"><el-card shadow="never"><div class="stat"><div class="stat-label">混合料 SiO2</div><div class="stat-value">{{ result.mixedSio2 }} %</div></div></el-card></el-col>
        <el-col :span="4"><el-card shadow="never"><div class="stat"><div class="stat-label">混合料 CaO</div><div class="stat-value">{{ result.mixedCao }} %</div></div></el-card></el-col>
        <el-col :span="4"><el-card shadow="never"><div class="stat"><div class="stat-label">混合料成本</div><div class="stat-value">{{ result.unitCost }} 元/吨</div></div></el-card></el-col>
      </el-row>

      <el-card shadow="never" class="mb8">
        <template #header><span>约束校核</span></template>
        <el-descriptions :column="4" border>
          <el-descriptions-item label="配比合计">{{ result.ratioSum }} %</el-descriptions-item>
          <el-descriptions-item label="质量守恒残差">{{ result.massBalanceResidual }} %</el-descriptions-item>
          <el-descriptions-item label="目标冰铜品位">{{ result.matteGradeTarget }} %</el-descriptions-item>
          <el-descriptions-item label="冰铜品位(计算)">{{ result.matteGradeAchieved }} %</el-descriptions-item>
          <el-descriptions-item label="目标硅铁比">{{ result.feSio2Target }}</el-descriptions-item>
          <el-descriptions-item label="实际硅铁比">{{ result.feSio2Achieved }}</el-descriptions-item>
          <el-descriptions-item label="硅铁比偏差">{{ result.feSio2Deviation }}</el-descriptions-item>
          <el-descriptions-item label="约束结论">
            <el-tag :type="result.feasible ? 'success' : 'warning'">{{ result.feasible ? '两条约束均满足' : '存在偏差' }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <el-alert v-for="(w, i) in result.warnings" :key="i" type="warning" :closable="false" show-icon :title="w" style="margin-top: 8px" />
        <el-collapse style="margin-top: 8px">
          <el-collapse-item title="求解过程说明（算法透明化，便于人工复核）">
            <div v-for="(n, i) in result.solveNotes" :key="i" class="note-line">{{ i + 1 }}. {{ n }}</div>
          </el-collapse-item>
        </el-collapse>
      </el-card>

      <el-card shadow="never">
        <template #header><span>配比方案</span></template>
        <el-table :data="result.items" border size="small">
          <el-table-column label="序号" type="index" width="60" align="center" />
          <el-table-column label="物料名称" align="center" prop="materialName" :show-overflow-tooltip="true" />
          <el-table-column label="物料类型" align="center" prop="materialType" width="90" />
          <el-table-column label="Cu(%)" align="center" prop="cuContent" width="80" />
          <el-table-column label="Fe(%)" align="center" prop="feContent" width="80" />
          <el-table-column label="S(%)" align="center" prop="sulfurContent" width="75" />
          <el-table-column label="SiO2(%)" align="center" prop="sio2Content" width="85" />
          <el-table-column label="CaO(%)" align="center" prop="caoContent" width="80" />
          <el-table-column label="配比(%)" align="center" prop="ratio" width="90">
            <template #default="scope">
              <span style="font-weight: 600">{{ scope.row.ratio }}</span>
            </template>
          </el-table-column>
          <el-table-column label="配比来源" align="center" width="100">
            <template #default="scope">
              <el-tag size="small" :type="scope.row.fixed ? 'info' : 'success'">{{ scope.row.fixed ? '锁定' : '求解' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>
  </div>
</template>

<script setup name="MetallurgyRatio">
import { listMaterial } from "@/api/metallurgy/material"
import { listParam } from "@/api/metallurgy/param"
import { calcRatio, calcBalance } from "@/api/metallurgy/calc"

const { proxy } = getCurrentInstance()
const router = useRouter()

const materialList = ref([])
const planList = ref([])
const selected = ref([])
const result = ref(null)
const solving = ref(false)
const running = ref(false)

const query = ref({
  planId: undefined,
  matteGrade: 55,
  feSio2Ratio: 1.1,
  remark: ""
})

/** 加载物料台账（参与计算的候选物料） */
function loadMaterials() {
  listMaterial({ pageNum: 1, pageSize: 200 }).then(response => {
    materialList.value = (response.rows || []).map(item => ({
      ...item,
      fixed: false,
      fixedRatio: 0
    }))
  })
}

/** 加载参数方案 */
function loadPlans() {
  listParam({ pageNum: 1, pageSize: 100 }).then(response => {
    planList.value = response.rows || []
    const enabled = planList.value.find(p => p.status === "1")
    if (enabled) {
      query.value.planId = enabled.id
      onPlanChange(enabled.id)
    }
  })
}

/** 选择参数方案时带出参数 */
function onPlanChange(planId) {
  const plan = planList.value.find(p => p.id === planId)
  if (plan) {
    query.value.matteGrade = Number(plan.matteGrade)
    query.value.feSio2Ratio = Number(plan.feSio2Ratio)
  }
}

/** 表格勾选 */
function onSelect(rows) {
  selected.value = rows
}

/** 重置 */
function resetAll() {
  result.value = null
  materialList.value.forEach(item => {
    item.fixed = false
    item.fixedRatio = 0
  })
}

/** 组装请求体 */
function buildRequest() {
  if (selected.value.length < 2) {
    proxy.$modal.msgWarning("请至少勾选 2 种物料参与配料计算")
    return null
  }
  const items = selected.value.map(item => ({
    materialId: item.id,
    materialName: item.materialName,
    materialType: item.materialType,
    cuContent: item.cuContent,
    feContent: item.feContent,
    sulfurContent: item.sulfurContent,
    sio2Content: item.sio2Content,
    caoContent: item.caoContent,
    unitPrice: item.unitPrice,
    fixed: item.fixed,
    fixedRatio: item.fixedRatio
  }))
  return {
    planId: query.value.planId,
    matteGrade: query.value.matteGrade,
    feSio2Ratio: query.value.feSio2Ratio,
    saveRecord: false,
    remark: query.value.remark,
    items: items
  }
}

/** 求解配比 */
function solve() {
  const req = buildRequest()
  if (!req) return
  solving.value = true
  calcRatio(req).then(response => {
    result.value = response.data
    solving.value = false
    if (result.value.feasible) {
      proxy.$modal.msgSuccess("求解完成：质量守恒与渣型约束均已满足")
    } else {
      proxy.$modal.msgWarning("求解完成，但存在约束偏差，请查看提示")
    }
  }).catch(() => {
    solving.value = false
  })
}

/** 保存并执行全流程计算 */
function runBalance() {
  const req = buildRequest()
  if (!req) return
  req.saveRecord = true
  req.items = result.value.items
  running.value = true
  calcBalance(req).then(response => {
    running.value = false
    const calcId = response.data.id
    proxy.$modal.msgSuccess("全流程计算完成，已保存计算记录（编号 " + calcId + "）")
    router.push({ path: "/metallurgy/balance", query: { calcId: calcId } })
  }).catch(() => {
    running.value = false
  })
}

loadMaterials()
loadPlans()
</script>

<style scoped>
.stat {
  text-align: center;
  padding: 4px 0;
}
.stat-label {
  color: #909399;
  font-size: 12px;
}
.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-top: 4px;
}
.tip {
  color: #909399;
  font-size: 12px;
  margin-left: 12px;
}
.note-line {
  font-size: 13px;
  line-height: 22px;
  color: #606266;
}
</style>
