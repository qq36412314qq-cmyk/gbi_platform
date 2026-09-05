<template>
  <div class="g-page-wrap stall-canvas-wrap">
    <div class="g-page-header">
      <span class="g-page-title">铺位布局画布</span>
      <div class="g-page-actions">
        <span class="canvas-hint">示例数据展示 · 点击铺位查看详情 · 滚轮缩放</span>
      </div>
    </div>
    <div class="canvas-container">
      <div class="canvas-toolbar">
        <div class="toolbar-group">
          <button class="tool-btn" :class="{active: zoom===100}" @click="zoom=100">1:1</button>
          <button class="tool-btn" @click="zoom=Math.min(200,zoom+25)">放大 +</button>
          <button class="tool-btn" @click="zoom=Math.max(50,zoom-25)">缩小 -</button>
          <button class="tool-btn icon-btn" @click="zoom=100;panX=0;panY=0" title="重置视图">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/></svg>
          </button>
        </div>
        <div class="toolbar-group">
          <label class="tool-label"><input type="checkbox" v-model="showGrid" /> 网格</label>
          <label class="tool-label"><input type="checkbox" v-model="showDimensions" /> 尺寸</label>
        </div>
        <div class="toolbar-group toolbar-legend">
          <span class="legend-item"><span class="legend-dot empty"></span>空置</span>
          <span class="legend-item"><span class="legend-dot occupied"></span>已租</span>
          <span class="legend-item"><span class="legend-dot debt"></span>欠费</span>
          <span class="legend-item"><span class="legend-dot expiring"></span>即将到期</span>
        </div>
      </div>
      <div class="canvas-stage" @wheel.prevent="onWheel">
        <svg :viewBox="svgViewBox" class="canvas-svg" :style="svgStyle">
          <defs>
            <pattern id="smallGrid" width="20" height="20" patternUnits="userSpaceOnUse">
              <path d="M 20 0 L 0 0 0 20" fill="none" stroke="#1e3a5f" stroke-width="0.5" opacity="0.4"/>
            </pattern>
            <pattern id="grid" width="100" height="100" patternUnits="userSpaceOnUse">
              <rect width="100" height="100" fill="url(#smallGrid)"/>
              <path d="M 100 0 L 0 0 0 100" fill="none" stroke="#1e3a5f" stroke-width="1" opacity="0.6"/>
            </pattern>
            <pattern id="hatch" width="8" height="8" patternTransform="rotate(45)" patternUnits="userSpaceOnUse">
              <line x1="0" y1="0" x2="0" y2="8" stroke="#3a5f8a" stroke-width="0.8" opacity="0.5"/>
            </pattern>
            <filter id="glow"><feGaussianBlur stdDeviation="3" result="blur"/><feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge></filter>
            <filter id="shadow"><feDropShadow dx="2" dy="3" stdDeviation="3" flood-color="#000" flood-opacity="0.4"/></filter>
          </defs>
          <g :transform="transform">
            <g class="building-group">
              <rect x="100" y="100" width="800" height="500" fill="#0d1f35" stroke="#2d6a9f" stroke-width="3" rx="2"/>
              <rect x="102" y="102" width="796" height="496" fill="#0a1628"/>
              <rect x="102" y="102" width="796" height="496" fill="url(#hatch)" opacity="0.3"/>
              <rect x="102" y="330" width="796" height="40" fill="#0d1f35" stroke="#1a3a5c" stroke-width="1"/>
              <text x="400" y="355" text-anchor="middle" fill="#2d6a9f" font-size="10" font-family="monospace" letter-spacing="4">主 通 道</text>
              <rect x="450" y="102" width="40" height="496" fill="#0d1f35" stroke="#1a3a5c" stroke-width="1"/>
              <text x="470" y="250" text-anchor="middle" fill="#2d6a9f" font-size="9" font-family="monospace" letter-spacing="2" transform="rotate(90,470,250)">主 通 道</text>
              <rect x="490" y="370" width="408" height="228" fill="#0a1628" stroke="none"/>
              <text x="694" y="485" text-anchor="middle" fill="#1a3a5c" font-size="11" font-family="monospace">A区 · 展示区</text>
              <g v-for="stall in sampleStalls" :key="stall.id"
                 :class="['stall-cell', {selected: selectedStall && selectedStall.id === stall.id}]"
                 @click="selectedStall = stall" filter="url(#shadow)">
                <rect :x="stall.x" :y="stall.y" :width="stall.w" :height="stall.h"
                      :fill="stallFill(stall)" :stroke="stallStroke(stall)"
                      :stroke-width="selectedStall && selectedStall.id === stall.id ? 3 : 1.5"/>
                <text :x="stall.x + stall.w/2" :y="stall.y + stall.h/2 - 6"
                      text-anchor="middle" fill="#e2e8f0" font-size="13" font-weight="bold" font-family="monospace">{{ stall.number }}</text>
                <text :x="stall.x + stall.w/2" :y="stall.y + stall.h/2 + 10"
                      text-anchor="middle" fill="#7fa8cc" font-size="9" font-family="monospace">{{ stall.area }}m2</text>
                <circle :cx="stall.x + stall.w - 8" :cy="stall.y + 8" r="4" :fill="statusColor(stall.status)"/>
              </g>
              <g v-if="showDimensions" class="dimension-group">
                <line x1="100" y1="85" x2="900" y2="85" stroke="#4a90d9" stroke-width="1"/>
                <line x1="100" y1="80" x2="100" y2="90" stroke="#4a90d9" stroke-width="1"/>
                <line x1="900" y1="80" x2="900" y2="90" stroke="#4a90d9" stroke-width="1"/>
                <text x="500" y="80" text-anchor="middle" fill="#4a90d9" font-size="10" font-family="monospace">80000mm</text>
                <line x1="85" y1="100" x2="85" y2="600" stroke="#4a90d9" stroke-width="1"/>
                <line x1="80" y1="100" x2="90" y2="100" stroke="#4a90d9" stroke-width="1"/>
                <line x1="80" y1="600" x2="90" y2="600" stroke="#4a90d9" stroke-width="1"/>
                <text x="78" y="350" text-anchor="middle" fill="#4a90d9" font-size="10" font-family="monospace" transform="rotate(-90,78,350)">50000mm</text>
              </g>
              <g transform="translate(830, 560)">
                <polygon points="0,-20 6,0 -6,0" fill="#4a90d9"/>
                <text x="0" y="-24" text-anchor="middle" fill="#4a90d9" font-size="9" font-family="monospace">N</text>
              </g>
              <g transform="translate(115, 115)">
                <rect x="0" y="0" width="120" height="90" fill="#0d1f35" stroke="#2d6a9f" stroke-width="1" rx="2"/>
                <text x="10" y="18" fill="#4a90d9" font-size="9" font-family="monospace">图 例</text>
                <rect x="10" y="26" width="12" height="10" fill="#1a3a2a" stroke="#2d6a9f" stroke-width="0.5"/>
                <text x="28" y="35" fill="#90aab8" font-size="8" font-family="monospace">空置</text>
                <rect x="10" y="42" width="12" height="10" fill="#3a2a1a" stroke="#2d6a9f" stroke-width="0.5"/>
                <text x="28" y="51" fill="#90aab8" font-size="8" font-family="monospace">已租</text>
                <rect x="10" y="58" width="12" height="10" fill="#3a1a1a" stroke="#2d6a9f" stroke-width="0.5"/>
                <text x="28" y="67" fill="#90aab8" font-size="8" font-family="monospace">欠费</text>
                <rect x="10" y="74" width="12" height="10" fill="#2a2a3a" stroke="#2d6a9f" stroke-width="0.5"/>
                <text x="28" y="83" fill="#90aab8" font-size="8" font-family="monospace">即将到期</text>
              </g>
            </g>
          </g>
        </svg>
      </div>
      <Transition name="slide-fade">
        <div v-if="selectedStall" class="detail-panel" @click.stop>
          <div class="detail-header">
            <span class="detail-title">铺位 {{ selectedStall.number }}</span>
            <button class="close-btn" @click="selectedStall=null">X</button>
          </div>
          <div class="detail-body">
            <div class="detail-row"><span class="detail-label">铺位编号</span><span class="detail-value">{{ selectedStall.number }}</span></div>
            <div class="detail-row"><span class="detail-label">铺位名称</span><span class="detail-value">{{ selectedStall.name || '-' }}</span></div>
            <div class="detail-row"><span class="detail-label">所属市场</span><span class="detail-value">{{ selectedStall.market }}</span></div>
            <div class="detail-row"><span class="detail-label">租赁分类</span><span class="detail-value">{{ selectedStall.category }}</span></div>
            <div class="detail-row"><span class="detail-label">面积</span><span class="detail-value">{{ selectedStall.area }}m2</span></div>
            <div class="detail-row"><span class="detail-label">当前状态</span><span class="detail-value"><el-tag size="small" :type="statusTagType(selectedStall.status)">{{ statusText(selectedStall.status) }}</el-tag></span></div>
            <div v-if="selectedStall.contractNo" class="detail-row"><span class="detail-label">合同编号</span><span class="detail-value contract-no">{{ selectedStall.contractNo }}</span></div>
            <div v-if="selectedStall.tenant" class="detail-row"><span class="detail-label">当前租户</span><span class="detail-value">{{ selectedStall.tenant }}</span></div>
            <div class="detail-row"><span class="detail-label">画布坐标</span><span class="detail-value mono">({{ selectedStall.x }}, {{ selectedStall.y }})</span></div>
          </div>
          <div class="detail-footer">
            <router-link to="/property/lease/contract" class="btn-primary">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
              查看合同
            </router-link>
          </div>
        </div>
      </Transition>
    </div>
  </div>
</template><script setup lang="ts">
import { ref, computed } from 'vue'

const zoom = ref(100)
const panX = ref(0)
const panY = ref(0)
const showGrid = ref(true)
const showDimensions = ref(false)
const selectedStall = ref<SampleStall | null>(null)

interface SampleStall {
  id: number
  number: string
  name: string
  market: string
  category: string
  area: number
  status: number
  contractNo?: string
  tenant?: string
  x: number
  y: number
  w: number
  h: number
}

const sampleStalls: SampleStall[] = [
  { id: 1, number: 'A-001', name: '精品水果档', market: '阳光农贸市场', category: '生鲜区', area: 18, status: 1, contractNo: 'HT-2026-0089', tenant: '张明水果店', x: 120, y: 120, w: 180, h: 100 },
  { id: 2, number: 'A-002', name: '生鲜肉铺', market: '阳光农贸市场', category: '生鲜区', area: 22, status: 1, contractNo: 'HT-2026-0090', tenant: '李记肉铺', x: 300, y: 120, w: 180, h: 100 },
  { id: 3, number: 'A-003', name: '蔬菜档口', market: '阳光农贸市场', category: '生鲜区', area: 20, status: 2, contractNo: 'HT-2026-0091', tenant: '王芳蔬菜', x: 120, y: 220, w: 180, h: 100 },
  { id: 4, number: 'A-004', name: '海鲜水产', market: '阳光农贸市场', category: '生鲜区', area: 25, status: 0, x: 300, y: 220, w: 180, h: 100 },
  { id: 5, number: 'A-005', name: '熟食档口', market: '阳光农贸市场', category: '熟食区', area: 16, status: 1, contractNo: 'HT-2026-0092', tenant: '陈氏熟食', x: 120, y: 380, w: 180, h: 140 },
  { id: 6, number: 'A-006', name: '粮油干货', market: '阳光农贸市场', category: '干货区', area: 24, status: 3, contractNo: 'HT-2026-0093', tenant: '刘记粮油', x: 300, y: 380, w: 180, h: 140 },
  { id: 7, number: 'B-001', name: '服装饰品', market: '阳光农贸市场', category: '百货区', area: 30, status: 1, contractNo: 'HT-2026-0094', tenant: '时尚女装', x: 490, y: 120, w: 180, h: 100 },
  { id: 8, number: 'B-002', name: '日用百货', market: '阳光农贸市场', category: '百货区', area: 28, status: 0, x: 670, y: 120, w: 180, h: 100 },
  { id: 9, number: 'B-003', name: '文具玩具', market: '阳光农贸市场', category: '百货区', area: 20, status: 1, contractNo: 'HT-2026-0095', tenant: '优品文具', x: 490, y: 220, w: 180, h: 100 },
  { id: 10, number: 'B-004', name: '家居用品', market: '阳光农贸市场', category: '百货区', area: 32, status: 2, contractNo: 'HT-2026-0096', tenant: '居家生活馆', x: 670, y: 220, w: 180, h: 100 },
  { id: 11, number: 'B-005', name: '餐饮小吃', market: '阳光农贸市场', category: '餐饮区', area: 35, status: 1, contractNo: 'HT-2026-0097', tenant: '美食街', x: 490, y: 380, w: 180, h: 140 },
  { id: 12, number: 'B-006', name: '饮品甜品', market: '阳光农贸市场', category: '餐饮区', area: 22, status: 0, x: 670, y: 380, w: 180, h: 140 },
  { id: 13, number: 'C-001', name: '临时展销位', market: '阳光农贸市场', category: '展销区', area: 15, status: 0, x: 500, y: 380, w: 90, h: 60 },
  { id: 14, number: 'C-002', name: '节庆特卖', market: '阳光农贸市场', category: '展销区', area: 15, status: 0, x: 595, y: 380, w: 90, h: 60 },
]

const svgViewBox = computed(() => {
  const scale = zoom.value / 100
  const w = 1000 / scale
  const h = 700 / scale
  const cx = w / 2 - panX.value / scale
  const cy = h / 2 - panY.value / scale
  return (cx - w/2) + ' ' + (cy - h/2) + ' ' + w + ' ' + h
})

const svgStyle = computed(() => {
  return { width: '100%', height: '100%', transform: 'scale(' + (zoom.value / 100) + ')', transformOrigin: 'center center' }
})

const transform = computed(() => 'translate(' + panX.value + ', ' + panY.value + ')')

function onWheel(e: WheelEvent): void {
  const delta = e.deltaY > 0 ? -10 : 10
  zoom.value = Math.max(50, Math.min(200, zoom.value + delta))
}

function stallFill(s: SampleStall): string {
  const map: Record<number, string> = { 0: '#1a3a2a', 1: '#3a2a1a', 2: '#3a1a1a', 3: '#2a2a3a' }
  return map[s.status] ?? '#1a2a3a'
}

function stallStroke(s: SampleStall): string {
  const map: Record<number, string> = { 0: '#2d8a5f', 1: '#c88a3a', 2: '#c83a3a', 3: '#5a5ac8' }
  return map[s.status] ?? '#3a6a8a'
}

function statusColor(status: number): string { return stallStroke({ status } as SampleStall) }
function statusText(status: number): string { return ['空置','已租','欠费','即将到期'][status] ?? '未知' }
function statusTagType(status: number): 'success' | 'warning' | 'danger' | 'info' {
  return ['success','warning','danger','info'][status] ?? 'info'
}
</script><style scoped>
.g-page-wrap.stall-canvas-wrap { display: flex; flex-direction: column; height: calc(100vh - 60px); background: #060d18; overflow: hidden; }
.g-page-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 20px; background: #0a1628; border-bottom: 1px solid #1e3a5f; flex-shrink: 0; }
.g-page-title { font-size: 16px; font-weight: 600; color: #e2e8f0; letter-spacing: 1px; }
.canvas-hint { font-size: 12px; color: #4a6a8a; font-family: monospace; }
.canvas-container { flex: 1; display: flex; flex-direction: column; overflow: hidden; position: relative; }
.canvas-toolbar { display: flex; align-items: center; gap: 16px; padding: 8px 16px; background: #0a1628; border-bottom: 1px solid #1e3a5f; flex-wrap: wrap; flex-shrink: 0; }
.toolbar-group { display: flex; align-items: center; gap: 6px; }
.tool-btn { display: inline-flex; align-items: center; justify-content: center; gap: 4px; padding: 4px 10px; background: #0d1f35; border: 1px solid #2d6a9f; color: #7fa8cc; border-radius: 4px; font-size: 12px; cursor: pointer; transition: all 0.15s; font-family: monospace; }
.tool-btn:hover { background: #1a3a5f; color: #e2e8f0; }
.tool-btn.active { background: #2d6a9f; color: #fff; }
.icon-btn { padding: 4px 8px; }
.tool-label { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #7fa8cc; cursor: pointer; font-family: monospace; }
.tool-label input { cursor: pointer; }
.toolbar-legend { margin-left: auto; }
.legend-item { display: inline-flex; align-items: center; gap: 4px; font-size: 11px; color: #7fa8cc; font-family: monospace; }
.legend-dot { display: inline-block; width: 10px; height: 10px; border-radius: 2px; border: 1px solid #2d6a9f; }
.legend-dot.empty   { background: #1a3a2a; }
.legend-dot.occupied { background: #3a2a1a; }
.legend-dot.debt    { background: #3a1a1a; }
.legend-dot.expiring { background: #2a2a3a; }
.canvas-stage { flex: 1; overflow: hidden; background: #060d18; position: relative; cursor: grab; }
.canvas-stage:active { cursor: grabbing; }
.canvas-svg { display: block; width: 100%; height: 100%; }
.stall-cell { cursor: pointer; transition: filter 0.15s; }
.stall-cell:hover { filter: url(#glow); }
.stall-cell.selected rect { stroke-width: 3 !important; filter: url(#glow); }
.detail-panel { position: absolute; right: 16px; top: 16px; width: 260px; background: #0a1628; border: 1px solid #2d6a9f; border-radius: 6px; box-shadow: 0 8px 32px rgba(0,0,0,0.6); z-index: 100; overflow: hidden; }
.detail-header { display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: #0d1f35; border-bottom: 1px solid #1e3a5f; }
.detail-title { font-size: 14px; font-weight: 600; color: #e2e8f0; font-family: monospace; }
.close-btn { background: none; border: none; color: #4a6a8a; cursor: pointer; font-size: 14px; padding: 0 4px; line-height: 1; }
.close-btn:hover { color: #e2e8f0; }
.detail-body { padding: 12px 14px; }
.detail-row { display: flex; justify-content: space-between; align-items: center; padding: 5px 0; border-bottom: 1px solid #1e2d42; }
.detail-row:last-child { border-bottom: none; }
.detail-label { font-size: 12px; color: #4a6a8a; font-family: monospace; }
.detail-value { font-size: 12px; color: #e2e8f0; font-weight: 500; }
.detail-value.mono { font-family: monospace; color: #4a90d9; }
.detail-value.contract-no { color: #c88a3a; font-family: monospace; }
.detail-footer { padding: 10px 14px; border-top: 1px solid #1e2d42; }
.btn-primary { display: inline-flex; align-items: center; gap: 6px; padding: 6px 14px; background: #1a3a5f; border: 1px solid #2d6a9f; color: #7fa8cc; border-radius: 4px; font-size: 12px; text-decoration: none; transition: all 0.15s; width: 100%; justify-content: center; }
.btn-primary:hover { background: #2d6a9f; color: #fff; }
.slide-fade-enter-active, .slide-fade-leave-active { transition: all 0.2s ease; }
.slide-fade-enter-from, .slide-fade-leave-to { opacity: 0; transform: translateX(20px); }
</style>