<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="参数方案" prop="planName">
        <el-input v-model="queryParams.planName" placeholder="请输入方案名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="计算类型" prop="calcType">
        <el-select v-model="queryParams.calcType" placeholder="请选择计算类型" clearable style="width: 200px">
          <el-option label="配料计算" value="1" />
          <el-option label="全流程平衡" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="DataAnalysis" :disabled="compareDisabled" @click="handleCompare" v-hasPermi="['metallurgy:record:compare']">方案对比</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['metallurgy:record:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['metallurgy:record:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="recordList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="id" width="70" />
      <el-table-column label="参数方案" align="center" prop="planName" :show-overflow-tooltip="true" />
      <el-table-column label="计算类型" align="center" prop="calcType" width="100">
        <template #default="scope">
          <el-tag size="small" :type="scope.row.calcType === '2' ? 'success' : 'info'">{{ scope.row.calcType === '2' ? '全流程平衡' : '配料计算' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="混合料Cu品位(%)" align="center" prop="mixedCu" width="130" />
      <el-table-column label="冰铜品位(%)" align="center" prop="matteGradeCalc" width="110" />
      <el-table-column label="炉渣硅铁比" align="center" prop="slagFeSio2" width="105" />
      <el-table-column label="铜直收率(%)" align="center" prop="directYield" width="110" />
      <el-table-column label="计算时间" align="center" prop="calcTime" width="160" />
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['metallurgy:record:query']">详情</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['metallurgy:record:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 计算记录详情：展示输入与结果快照，保证计算可追溯 -->
    <el-dialog title="计算记录详情" v-model="open" width="820px" append-to-body>
      <el-descriptions :column="3" border size="small">
        <el-descriptions-item label="记录编号">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="参数方案">{{ detail.planName || '未关联' }}</el-descriptions-item>
        <el-descriptions-item label="计算类型">{{ detail.calcType === '2' ? '全流程平衡' : '配料计算' }}</el-descriptions-item>
        <el-descriptions-item label="混合料Cu品位">{{ detail.mixedCu }} %</el-descriptions-item>
        <el-descriptions-item label="冰铜品位">{{ detail.matteGradeCalc }} %</el-descriptions-item>
        <el-descriptions-item label="炉渣硅铁比">{{ detail.slagFeSio2 }}</el-descriptions-item>
        <el-descriptions-item label="铜直收率">{{ detail.directYield }} %</el-descriptions-item>
        <el-descriptions-item label="计算时间" :span="2">{{ detail.calcTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="3">{{ detail.remark }}</el-descriptions-item>
      </el-descriptions>
      <el-collapse style="margin-top: 10px">
        <el-collapse-item title="输入快照（JSON）">
          <pre class="snapshot">{{ prettyJson(detail.inputSnapshot) }}</pre>
        </el-collapse-item>
        <el-collapse-item title="结果快照（JSON）">
          <pre class="snapshot">{{ prettyJson(detail.resultSnapshot) }}</pre>
        </el-collapse-item>
      </el-collapse>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="open = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 方案对比：勾选 2~4 条全流程记录后并列比较指标（读取快照，不重新计算） -->
    <compare-dialog v-model:visible="compareOpen" :calc-ids="compareIds" />
  </div>
</template>

<script setup name="MetallurgyRecord">
import { listRecord, getRecord, delRecord } from "@/api/metallurgy/record"
import CompareDialog from "@/views/metallurgy/analysis/CompareDialog"

const { proxy } = getCurrentInstance()

const recordList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const selectedRows = ref([])
const multiple = ref(true)
const total = ref(0)
const detail = ref({})
const compareOpen = ref(false)
const compareIds = ref([])

/** 方案对比要求勾选 2~4 条记录，数量不符时禁用按钮 */
const compareDisabled = computed(() => ids.value.length < 2 || ids.value.length > 4)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    planName: undefined,
    calcType: undefined
  }
})

const { queryParams } = toRefs(data)

/** 查询计算记录列表 */
function getList() {
  loading.value = true
  listRecord(queryParams.value).then(response => {
    recordList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 格式化 JSON 快照 */
function prettyJson(text) {
  if (!text) return "（无）"
  try {
    return JSON.stringify(JSON.parse(text), null, 2)
  } catch (e) {
    return text
  }
}

/** 搜索 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选 */
function handleSelectionChange(selection) {
  selectedRows.value = selection
  ids.value = selection.map(item => item.id)
  multiple.value = !selection.length
}

/** 方案对比：仅允许全流程平衡记录参与，配料记录给出明确中文提示 */
function handleCompare() {
  if (ids.value.length < 2 || ids.value.length > 4) {
    proxy.$modal.msgWarning("请勾选 2~4 条全流程计算记录进行对比")
    return
  }
  if (selectedRows.value.some(item => item.calcType !== '2')) {
    proxy.$modal.msgWarning("所选记录中包含配料计算记录，仅全流程平衡记录可参与方案对比")
    return
  }
  compareIds.value = [...ids.value]
  compareOpen.value = true
}

/** 详情 */
function handleDetail(row) {
  getRecord(row.id).then(response => {
    detail.value = response.data
    open.value = true
  })
}

/** 删除 */
function handleDelete(row) {
  const delIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除计算记录编号为"' + delIds + '"的数据项？').then(function () {
    return delRecord(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出 */
function handleExport() {
  proxy.download("metallurgy/record/export", {
    ...queryParams.value
  }, `计算记录_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
.snapshot {
  max-height: 300px;
  overflow: auto;
  background: #f5f7fa;
  padding: 10px;
  font-size: 12px;
  line-height: 18px;
  margin: 0;
}
</style>
