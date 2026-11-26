<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="方案名称" prop="planName">
        <el-input
          v-model="queryParams.planName"
          placeholder="请输入方案名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['metallurgy:param:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['metallurgy:param:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['metallurgy:param:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['metallurgy:param:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="paramList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="方案名称" align="center" prop="planName" :show-overflow-tooltip="true" />
      <el-table-column label="富氧浓度(vol%)" align="center" prop="oxygenConc" width="130" />
      <el-table-column label="氧料比(Nm3/t)" align="center" prop="oxygenRatio" width="130" />
      <el-table-column label="硅铁比Fe/SiO2" align="center" prop="feSio2Ratio" width="135" />
      <el-table-column label="目标冰铜品位(%)" align="center" prop="matteGrade" width="145" />
      <el-table-column label="处理量(t/h)" align="center" prop="capacity" width="110" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['metallurgy:param:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['metallurgy:param:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改工艺参数方案对话框 -->
    <el-dialog :title="title" v-model="open" width="680px" append-to-body>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="工艺参数参考区间（超出仅提示、不阻断保存，现场工况可能超出文献范围）"
        style="margin-bottom: 12px"
      >
        <div v-for="(val, key) in paramRanges" :key="key" style="font-size: 12px; line-height: 20px">
          {{ rangeLabel(key) }}：{{ val }}
        </div>
      </el-alert>
      <el-form ref="paramRef" :model="form" :rules="rules" label-width="140px">
        <el-form-item label="方案名称" prop="planName">
          <el-input v-model="form.planName" placeholder="请输入方案名称" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="富氧浓度(vol%)" prop="oxygenConc">
              <el-input-number v-model="form.oxygenConc" :min="0" :max="100" :precision="2" :step="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="氧料比(Nm3/t)" prop="oxygenRatio">
              <el-input-number v-model="form.oxygenRatio" :min="0" :precision="4" :step="10" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="硅铁比Fe/SiO2" prop="feSio2Ratio">
              <el-input-number v-model="form.feSio2Ratio" :min="0" :max="5" :precision="3" :step="0.05" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标冰铜品位(%)" prop="matteGrade">
              <el-input-number v-model="form.matteGrade" :min="0" :max="100" :precision="2" :step="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="处理量(t/h)" prop="capacity">
              <el-input-number v-model="form.capacity" :min="0.001" :precision="3" :step="5" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MetallurgyParam">
import { listParam, getParam, delParam, addParam, updateParam, getParamRanges } from "@/api/metallurgy/param"

const { proxy } = getCurrentInstance()

/** 方案状态固定枚举（0 草稿 / 1 启用 / 2 停用），与数据库 status 列注释一致 */
const statusOptions = [
  { value: "0", label: "草稿" },
  { value: "1", label: "启用" },
  { value: "2", label: "停用" }
]

const rangeLabels = {
  oxygenConc: "富氧浓度",
  oxygenRatio: "氧料比",
  feSio2Ratio: "硅铁比 Fe/SiO2",
  matteGrade: "目标冰铜品位",
  capacity: "处理量"
}

const paramList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const paramRanges = ref({})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    planName: undefined,
    status: undefined
  },
  rules: {
    planName: [{ required: true, message: "方案名称不能为空", trigger: "blur" }],
    capacity: [{ required: true, message: "处理量不能为空且需大于 0", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 状态中文标签 */
function statusLabel(status) {
  const hit = statusOptions.find(item => item.value === status)
  return hit ? hit.label : status
}

/** 状态标签配色 */
function statusTagType(status) {
  if (status === "1") return "success"
  if (status === "2") return "info"
  return "warning"
}

/** 参数区间中文名 */
function rangeLabel(key) {
  return rangeLabels[key] || key
}

/** 查询工艺参数方案列表 */
function getList() {
  loading.value = true
  listParam(queryParams.value).then(response => {
    paramList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 加载参数参考区间 */
function loadRanges() {
  getParamRanges().then(response => {
    paramRanges.value = response.data || {}
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    id: undefined,
    planName: undefined,
    oxygenConc: 55,
    oxygenRatio: 0,
    feSio2Ratio: 1.1,
    matteGrade: 55,
    capacity: 60,
    status: "0",
    remark: undefined
  }
  proxy.resetForm("paramRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "新增工艺参数方案"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getParam(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改工艺参数方案"
  })
}

/** 展示后端返回的参数越界警告 */
function showWarnings(response) {
  const warnings = response && response.warnings
  if (warnings && warnings.length > 0) {
    proxy.$modal.alert(warnings.join("<br/>"), "参数越界提示（已保存）", { dangerouslyUseHTMLString: true })
  }
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["paramRef"].validate(valid => {
    if (valid) {
      if (form.value.id != undefined) {
        updateParam(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
          showWarnings(response)
        })
      } else {
        addParam(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
          showWarnings(response)
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const delIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除工艺参数方案编号为"' + delIds + '"的数据项？').then(function () {
    return delParam(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("metallurgy/param/export", {
    ...queryParams.value
  }, `process_param_${new Date().getTime()}.xlsx`)
}

getList()
loadRanges()
</script>
