<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="物料名称" prop="materialName">
        <el-input
          v-model="queryParams.materialName"
          placeholder="请输入物料名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="物料类型" prop="materialType">
        <el-select v-model="queryParams.materialType" placeholder="请选择物料类型" clearable style="width: 200px">
          <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['metallurgy:material:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['metallurgy:material:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['metallurgy:material:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['metallurgy:material:import']">导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['metallurgy:material:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="materialList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="物料名称" align="center" prop="materialName" :show-overflow-tooltip="true" />
      <el-table-column label="物料类型" align="center" prop="materialType" width="90" />
      <el-table-column label="Cu(%)" align="center" prop="cuContent" width="80" />
      <el-table-column label="Fe(%)" align="center" prop="feContent" width="80" />
      <el-table-column label="S(%)" align="center" prop="sulfurContent" width="80" />
      <el-table-column label="SiO2(%)" align="center" prop="sio2Content" width="85" />
      <el-table-column label="CaO(%)" align="center" prop="caoContent" width="80" />
      <el-table-column label="单价(元/吨)" align="center" prop="unitPrice" width="100" />
      <el-table-column label="库存(吨)" align="center" prop="stockQty" width="90" />
      <el-table-column label="来源/产地" align="center" prop="source" :show-overflow-tooltip="true" />
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['metallurgy:material:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['metallurgy:material:remove']">删除</el-button>
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

    <!-- 添加或修改物料对话框 -->
    <el-dialog :title="title" v-model="open" width="720px" append-to-body>
      <el-form ref="materialRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="物料名称" prop="materialName">
              <el-input v-model="form.materialName" placeholder="请输入物料名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料类型" prop="materialType">
              <el-select v-model="form.materialType" placeholder="请选择物料类型" style="width: 100%">
                <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">化学成分（质量分数，%，合计不得超过 100%）</el-divider>
        <el-row>
          <el-col :span="8">
            <el-form-item label="Cu 含量" prop="cuContent">
              <el-input-number v-model="form.cuContent" :min="0" :max="100" :precision="2" :step="0.5" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="Fe 含量" prop="feContent">
              <el-input-number v-model="form.feContent" :min="0" :max="100" :precision="2" :step="0.5" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="S 含量" prop="sulfurContent">
              <el-input-number v-model="form.sulfurContent" :min="0" :max="100" :precision="2" :step="0.5" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="SiO2 含量" prop="sio2Content">
              <el-input-number v-model="form.sio2Content" :min="0" :max="100" :precision="2" :step="0.5" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="CaO 含量" prop="caoContent">
              <el-input-number v-model="form.caoContent" :min="0" :max="100" :precision="2" :step="0.5" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="成分合计">
              <el-tag :type="compositionTotal > 100 ? 'danger' : 'success'">{{ compositionTotal }} %</el-tag>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="单价(元/吨)" prop="unitPrice">
              <el-input-number v-model="form.unitPrice" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存量(吨)" prop="stockQty">
              <el-input-number v-model="form.stockQty" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="来源/产地" prop="source">
              <el-input v-model="form.source" placeholder="请输入来源/产地" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 物料导入对话框 -->
    <el-dialog :title="upload.title" v-model="upload.open" width="420px" append-to-body>
      <el-upload
        ref="uploadRef"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :auto-upload="false"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <div class="el-upload__tip">
              <el-checkbox v-model="upload.updateSupport" /> 是否更新已经存在的物料数据
            </div>
            <span>仅允许导入 xls、xlsx 格式文件。</span>
            <el-link type="primary" :underline="false" style="font-size: 12px; vertical-align: baseline" @click="importTemplate">下载模板</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFileForm">确 定</el-button>
          <el-button @click="upload.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MetallurgyMaterial">
import { listMaterial, getMaterial, delMaterial, addMaterial, updateMaterial } from "@/api/metallurgy/material"
import { getToken } from "@/utils/auth"

const { proxy } = getCurrentInstance()

/** 物料类型选项（与《需求与范围.md》定义的 5 类保持一致） */
const typeOptions = ["铜精矿", "熔剂", "返料", "烟尘", "燃料"]

const materialList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    materialName: undefined,
    materialType: undefined
  },
  rules: {
    materialName: [{ required: true, message: "物料名称不能为空", trigger: "blur" }],
    materialType: [{ required: true, message: "物料类型不能为空", trigger: "change" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 成分合计（实时计算，超过 100% 时前端标红，后端亦会二次校验） */
const compositionTotal = computed(() => {
  const n = [form.value.cuContent, form.value.feContent, form.value.sulfurContent, form.value.sio2Content, form.value.caoContent]
  const sum = n.reduce((acc, cur) => acc + (Number(cur) || 0), 0)
  return Math.round(sum * 100) / 100
})

/** 导入参数 */
const upload = reactive({
  open: false,
  title: "",
  isUploading: false,
  updateSupport: false,
  headers: { Authorization: "Bearer " + getToken() },
  url: import.meta.env.VITE_APP_BASE_API + "/metallurgy/material/importData"
})

/** 查询物料台账列表 */
function getList() {
  loading.value = true
  listMaterial(queryParams.value).then(response => {
    materialList.value = response.rows
    total.value = response.total
    loading.value = false
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
    materialName: undefined,
    materialType: undefined,
    cuContent: 0,
    feContent: 0,
    sulfurContent: 0,
    sio2Content: 0,
    caoContent: 0,
    unitPrice: 0,
    stockQty: 0,
    source: undefined,
    remark: undefined
  }
  proxy.resetForm("materialRef")
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
  title.value = "新增物料"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getMaterial(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改物料"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["materialRef"].validate(valid => {
    if (valid) {
      if (form.value.id != undefined) {
        updateMaterial(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addMaterial(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const delIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除物料编号为"' + delIds + '"的数据项？').then(function () {
    return delMaterial(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("metallurgy/material/export", {
    ...queryParams.value
  }, `material_${new Date().getTime()}.xlsx`)
}

/** 导入按钮操作 */
function handleImport() {
  upload.title = "物料导入"
  upload.open = true
}

/** 下载模板操作 */
function importTemplate() {
  proxy.download("metallurgy/material/importTemplate", {}, `material_template_${new Date().getTime()}.xlsx`)
}

/** 文件上传中处理 */
function handleFileUploadProgress(event, file, fileList) {
  upload.isUploading = true
}

/** 文件上传成功处理 */
function handleFileSuccess(response, file, fileList) {
  upload.open = false
  upload.isUploading = false
  proxy.$refs["uploadRef"].clearFiles()
  proxy.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", { dangerouslyUseHTMLString: true })
  getList()
}

/** 提交上传文件 */
function submitFileForm() {
  proxy.$refs["uploadRef"].submit()
}

getList()
</script>
