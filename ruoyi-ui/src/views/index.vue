<template>
  <div class="app-container home">
    <!-- 系统简介 -->
    <el-card shadow="never" class="mb8 intro-card">
      <div class="intro">
        <div class="intro-text">
          <h2 class="intro-title">铜闪速冶炼工艺全流程计算与物质流分析系统 V1.0</h2>
          <p class="intro-desc">
            面向铜闪速熔炼工艺的全流程计算与物质流分析软件：以元素守恒（Cu / Fe / S / SiO₂ / CaO）为核心，
            完成配料计算、闪速熔炼 / 转炉吹炼 / 阳极精炼 / 电解精炼的物料平衡，
            并以桑基图与报表量化各工序的物质流向，为工艺布局优化与资源利用提供定量依据。
          </p>
          <div class="flow-steps">
            <span v-for="(s, i) in steps" :key="s" class="step">
              <span class="step-name">{{ s }}</span>
              <span v-if="i < steps.length - 1" class="step-arrow">→</span>
            </span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 概览数据获取失败时局部降级，不影响页面其余部分 -->
    <el-alert
      v-if="summaryError"
      type="warning"
      :closable="false"
      show-icon
      class="mb8"
      title="概览数据获取失败，已降级显示占位内容（可能是会话过期或后端未启动）；记录详情、方案对比与敏感性分析等功能不受影响。"
    />
    <!-- 数据可见范围提示：非超级管理员仅统计本人创建的记录 -->
    <el-alert
      v-if="summary.scope === 'self'"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
      title="数据可见范围：当前账号仅统计与展示本人创建的计算记录；超级管理员可查看全部记录。"
    />

    <!-- 统计指标 -->
    <el-row :gutter="12" class="mb8">
      <el-col :xs="12" :sm="12" :md="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover" class="stat-card" @click="go(card.path)">
          <div class="stat-body">
            <div class="stat-icon" :style="{ background: card.bg }">
              <el-icon :size="22"><component :is="card.icon" /></el-icon>
            </div>
            <div>
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
          <div class="stat-sub" v-if="card.sub">{{ card.sub }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <!-- 快捷入口 -->
      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="mb8">
          <template #header><span class="card-title">快捷入口</span></template>
          <div class="entries">
            <div v-for="e in entries" :key="e.title" class="entry" @click="go(e.path)">
              <div class="entry-icon" :style="{ background: e.bg }">
                <el-icon :size="20"><component :is="e.icon" /></el-icon>
              </div>
              <div class="entry-main">
                <div class="entry-title">{{ e.title }}</div>
                <div class="entry-desc">{{ e.desc }}</div>
              </div>
              <el-icon class="entry-go"><ArrowRight /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 最近计算记录 -->
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="mb8">
          <template #header>
            <span class="card-title">最近全流程计算</span>
            <el-button link type="primary" style="float: right" @click="go('/metallurgy/record')">全部记录</el-button>
          </template>
          <el-table :data="recent" size="small" v-loading="loading">
            <el-table-column label="编号" prop="id" width="60" align="center" />
            <el-table-column label="参数方案" :show-overflow-tooltip="true" align="center">
              <template #default="scope">{{ scope.row.planName || '未关联方案' }}</template>
            </el-table-column>
            <el-table-column label="冰铜品位(%)" width="100" align="center">
              <template #default="scope">{{ display(scope.row.matteGradeCalc) }}</template>
            </el-table-column>
            <el-table-column label="铜直收率(%)" width="105" align="center">
              <template #default="scope">
                <el-tag v-if="scope.row.directYield !== null && scope.row.directYield !== undefined" size="small" type="success">{{ scope.row.directYield }}</el-tag>
                <span v-else class="empty-cell">—</span>
              </template>
            </el-table-column>
            <el-table-column label="计算时间" width="160" align="center">
              <template #default="scope">{{ display(scope.row.calcTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="90" align="center">
              <template #default="scope">
                <el-button link type="primary" @click="goBalance(scope.row.id)">查看平衡</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && recent.length === 0" description="暂无全流程计算记录，请先执行配料计算" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Index">
import request from '@/utils/request'
import { Coin, Setting, Document, TrendCharts, Cpu, Share, List, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(true)
const summaryError = ref(false)
const summary = ref({})
const recent = ref([])
const steps = ['配料', '闪速熔炼', '转炉吹炼', '阳极精炼', '电解精炼']

/** 空值占位：无数据一律显示"—"，不伪造 0 */
function display(value) {
  return value === null || value === undefined || value === '' ? '—' : value
}

/** 统计指标卡 */
const statCards = computed(() => [
  { label: '物料台账', value: summary.value.materialCount ?? '—', sub: '铜精矿 / 熔剂 / 返料 / 烟尘', icon: Coin, bg: '#2f5d9e', path: '/metallurgy/material' },
  { label: '工艺参数方案', value: summary.value.paramCount ?? '—', sub: '富氧浓度 / 硅铁比 / 目标品位', icon: Setting, bg: '#4aa96c', path: '/metallurgy/param' },
  { label: '计算记录', value: summary.value.recordCount ?? '—', sub: '其中全流程平衡 ' + (summary.value.flowCount ?? '—') + ' 条', icon: Document, bg: '#e2a33c', path: '/metallurgy/record' },
  {
    label: '最近直收率',
    value: summary.value.lastDirectYield != null ? summary.value.lastDirectYield + ' %' : '—',
    sub: summary.value.hasFlowRecord ? '计算于 ' + display(summary.value.lastCalcTime) : (summary.value.emptyHint || '暂无全流程计算记录'),
    icon: TrendCharts,
    bg: '#7c6bb5',
    path: '/metallurgy/balance'
  }
])

/** 快捷入口 */
const entries = [
  { title: '配料计算', desc: '质量守恒 + 渣型约束下求解各物料配比', icon: Cpu, bg: '#2f5d9e', path: '/metallurgy/ratio' },
  { title: '物质流分析', desc: '桑基图量化 Cu / Fe / S / SiO₂ 工序流向', icon: Share, bg: '#4aa96c', path: '/metallurgy/stream' },
  { title: '计算记录', desc: '输入与结果快照，全程可追溯复现', icon: List, bg: '#e2a33c', path: '/metallurgy/record' }
]

function go(path) {
  router.push(path)
}

function goBalance(calcId) {
  router.push({ path: '/metallurgy/balance', query: { calcId } })
}

function load() {
  loading.value = true
  summaryError.value = false
  // 两个概览接口分别容错：任一失败都只影响对应区块，不使整页崩溃
  request({ url: '/metallurgy/overview/summary', method: 'get' }).then(res => {
    summary.value = res.data || {}
  }).catch(() => {
    summaryError.value = true
    summary.value = {}
  })
  request({ url: '/metallurgy/overview/recent', method: 'get', params: { limit: 5 } }).then(res => {
    recent.value = res.data || []
  }).catch(() => {
    summaryError.value = true
    recent.value = []
  }).finally(() => {
    loading.value = false
  })
}

load()
</script>

<style scoped>
.intro-card {
  border-left: 4px solid #2f5d9e;
}
.intro-title {
  margin: 0 0 10px;
  font-size: 20px;
  color: #1f2d3d;
}
.intro-desc {
  margin: 0 0 14px;
  color: #5b6b8c;
  font-size: 13px;
  line-height: 22px;
}
.flow-steps {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.step-name {
  display: inline-block;
  padding: 4px 12px;
  background: #e8f0fb;
  color: #23507f;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 600;
}
.step-arrow {
  color: #9db2d0;
  margin-left: 6px;
}
.stat-card {
  cursor: pointer;
}
.stat-body {
  display: flex;
  align-items: center;
  gap: 12px;
}
.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #1f2d3d;
  line-height: 26px;
}
.stat-label {
  font-size: 12px;
  color: #909399;
}
.stat-sub {
  margin-top: 10px;
  font-size: 12px;
  color: #a0a8b8;
  border-top: 1px dashed #e6e9ef;
  padding-top: 8px;
}
.card-title {
  font-weight: 600;
  font-size: 15px;
}
.entries {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.entry {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e6e9ef;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.entry:hover {
  border-color: #2f5d9e;
  box-shadow: 0 2px 8px rgba(47, 93, 158, 0.12);
}
.entry-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.entry-main {
  flex: 1;
}
.entry-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2d3d;
}
.entry-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
.entry-go {
  color: #c0c4cc;
}
.empty-cell {
  color: #c0c4cc;
}
.entry:hover .entry-go {
  color: #2f5d9e;
}
</style>
