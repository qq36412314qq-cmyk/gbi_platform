const fs = require('fs');
const path = 'D:\\Office\\Project\\Java\\gbi_platform\\gbi_platform_admin\\src\\views\\finance\\financeFlow.vue';
let text = fs.readFileSync(path, 'utf8');

// 1. 更新 import
const oldImport = `import {
  getFinanceFlowPageApi,
  exportFinanceFlowApi,
  redFlushFlowApi,
  voidFlowApi,
  getPrintHtmlApi,
  type FinanceFlowVO
} from '@/api/finance'`;
const newImport = `import {
  getFinanceFlowPageApi,
  exportFinanceFlowApi,
  redFlushFlowApi,
  voidFlowApi,
  getPrintHtmlApi,
  getFlowItemsApi,
  type FinanceFlowVO,
  type PayBillItemVO
} from '@/api/finance'`;
text = text.replace(oldImport, newImport);

// 2. 添加 fetchFlowItems 函数
const expandSection = `/* ---------------- 展开行子项目 ---------------- */
const expandedRowKeys = ref<string[]>([])

async function fetchFlowItems(row: FinanceFlowVO): Promise<PayBillItemVO[]> {
  if (!row.payBillId) return []
  try {
    return await getFlowItemsApi(row.payBillId)
  } catch {
    return []
  }
}

/* ---------------- 分页查询 ---------------- */`;
text = text.replace('/* ---------------- 分页查询 ---------------- */', expandSection);

// 3. 更新 el-table
text = text.replace(
  '<el-table v-loading="loading" :data="records" border stripe>',
  '<el-table v-loading="loading" :data="records" border stripe :expand-row-keys="expandedRowKeys" row-key="id" @expand-change="(row, expanded) => { if (expanded) fetchFlowItems(row).then(items => { (row as any)._items = items }) }">'
);

// 4. 在状态列后插入 expand 列
const stateColPattern = text.indexOf('flowStatusType');
const afterStateCol = text.indexOf('</el-table-column>', stateColPattern);
const insertPos = afterStateCol + '</el-table-column>'.length;

const expandCol = `
      <el-table-column type="expand">
        <template #default="{ row }">
          <div v-if="(row as any)._items && (row as any)._items.length > 0" class="flow-items-wrap">
            <div class="flow-items-header">
              <span class="flow-items-title">聚合支付明细（{{ (row as any)._items.length }} 条）</span>
            </div>
            <div class="flow-items-table">
              <div class="flow-item-row flow-item-header">
                <span class="fi-col fi-col-1">业务类型</span>
                <span class="fi-col fi-col-2">收费规则</span>
                <span class="fi-col fi-col-3">收费项</span>
                <span class="fi-col fi-col-4">账单月份</span>
                <span class="fi-col fi-col-5">应收金额</span>
                <span class="fi-col fi-col-6">已缴</span>
                <span class="fi-col fi-col-7">未缴</span>
              </div>
              <div v-for="item in (row as any)._items" :key="item.id" class="flow-item-row">
                <span class="fi-col fi-col-1"><el-tag size="small" type="info">{{ item.bizTypeText || item.bizType }}</el-tag></span>
                <span class="fi-col fi-col-2">{{ item.ruleName || '-' }}</span>
                <span class="fi-col fi-col-3">{{ item.feeItemName || '-' }}</span>
                <span class="fi-col fi-col-4">{{ item.billMonth || '-' }}</span>
                <span class="fi-col fi-col-5 g-money">{{ Number(item.amount).toFixed(2) }}</span>
                <span class="fi-col fi-col-6">{{ Number(item.paidAmount || 0).toFixed(2) }}</span>
                <span class="fi-col fi-col-7 g-money">{{ Number(item.unpaidAmount || 0).toFixed(2) }}</span>
              </div>
            </div>
          </div>
          <div v-else class="flow-items-empty">暂无明细（非聚合支付流水）</div>
        </template>
      </el-table-column>`;

text = text.substring(0, insertPos) + expandCol + text.substring(insertPos);

// 5. 添加 CSS
const cssEnd = text.lastIndexOf('</style>');
const newCss = `
.flow-items-wrap { padding: 12px 20px; background: var(--el-fill-color-lighter); border-radius: 4px; }
.flow-items-header { margin-bottom: 8px; }
.flow-items-title { font-size: 13px; font-weight: 600; color: var(--el-text-color-primary); }
.flow-items-table { width: 100%; }
.flow-item-row { display: flex; align-items: center; padding: 6px 8px; font-size: 12px; border-bottom: 1px solid var(--el-border-color-lighter); }
.flow-item-row:last-child { border-bottom: none; }
.flow-item-header { font-weight: 600; color: var(--el-text-color-secondary); background: var(--el-fill-color); }
.fi-col { flex: 1; text-align: center; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.fi-col-1 { flex: 0 0 90px; }
.fi-col-2 { flex: 0 0 120px; }
.fi-col-3 { flex: 0 0 100px; }
.fi-col-4 { flex: 0 0 90px; }
.fi-col-5, .fi-col-6, .fi-col-7 { flex: 0 0 80px; }
.flow-items-empty { padding: 12px; color: var(--el-text-color-secondary); font-size: 13px; text-align: center; }`;
text = text.substring(0, cssEnd) + newCss + text.substring(cssEnd);

fs.writeFileSync(path, text, 'utf8');
console.log('financeFlow.vue updated successfully');
