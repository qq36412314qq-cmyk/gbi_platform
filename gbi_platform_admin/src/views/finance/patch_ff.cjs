const fs = require('fs');
const path = 'D:\\Office\\Project\\Java\\gbi_platform\\gbi_platform_admin\\src\\views\\finance\\financeFlow.vue';
let text = fs.readFileSync(path, 'utf8');

const oldImport = "import {\n  getFinanceFlowPageApi,\n  exportFinanceFlowApi,\n  redFlushFlowApi,\n  voidFlowApi,\n  getPrintHtmlApi,\n  type FinanceFlowVO\n} from '@/api/finance'";
const newImport = "import {\n  getFinanceFlowPageApi,\n  exportFinanceFlowApi,\n  redFlushFlowApi,\n  voidFlowApi,\n  getPrintHtmlApi,\n  getFlowItemsApi,\n  type FinanceFlowVO,\n  type PayBillItemVO\n} from '@/api/finance'";
text = text.replace(oldImport, newImport);

const expandSection = "/* ---------------- 展开行子项目 ---------------- */\nconst expandedRowKeys = ref<string[]>([])\n\nasync function fetchFlowItems(row: FinanceFlowVO): Promise<PayBillItemVO[]> {\n  if (!row.payBillId) return []\n  try {\n    return await getFlowItemsApi(row.payBillId)\n  } catch {\n    return []\n  }\n}\n\n/* ---------------- 分页查询 ---------------- */";
text = text.replace("/* ---------------- 分页查询 ---------------- */", expandSection);

text = text.replace(
  '<el-table v-loading="loading" :data="records" border stripe>',
  '<el-table v-loading="loading" :data="records" border stripe :expand-row-keys="expandedRowKeys" row-key="id" @expand-change="(row, expanded) => { if (expanded) fetchFlowItems(row).then(items => { (row as any)._items = items }) }">'
);

const stateColPattern = text.indexOf('flowStatusType');
const afterStateCol = text.indexOf('</el-table-column>', stateColPattern);
const insertPos = afterStateCol + '</el-table-column>'.length;

const expandCol = "\n      <el-table-column type=\"expand\">\n        <template #default=\"{ row }\">\n          <div v-if=\"(row as any)._items && (row as any)._items.length > 0\" class=\"flow-items-wrap\">\n            <div class=\"flow-items-header\">\n              <span class=\"flow-items-title\">聚合支付明细（{{ (row as any)._items.length }} 条）</span>\n            </div>\n            <div class=\"flow-items-table\">\n              <div class=\"flow-item-row flow-item-header\">\n                <span class=\"fi-col fi-col-1\">业务类型</span>\n                <span class=\"fi-col fi-col-2\">收费规则</span>\n                <span class=\"fi-col fi-col-3\">收费项</span>\n                <span class=\"fi-col fi-col-4\">账单月份</span>\n                <span class=\"fi-col fi-col-5\">应收金额</span>\n                <span class=\"fi-col fi-col-6\">已缴</span>\n                <span class=\"fi-col fi-col-7\">未缴</span>\n              </div>\n              <div v-for=\"item in (row as any)._items\" :key=\"item.id\" class=\"flow-item-row\">\n                <span class=\"fi-col fi-col-1\"><el-tag size=\"small\" type=\"info\">{{ item.bizTypeText || item.bizType }}</el-tag></span>\n                <span class=\"fi-col fi-col-2\">{{ item.ruleName || '-' }}</span>\n                <span class=\"fi-col fi-col-3\">{{ item.feeItemName || '-' }}</span>\n                <span class=\"fi-col fi-col-4\">{{ item.billMonth || '-' }}</span>\n                <span class=\"fi-col fi-col-5 g-money\">{{ Number(item.amount).toFixed(2) }}</span>\n                <span class=\"fi-col fi-col-6\">{{ Number(item.paidAmount || 0).toFixed(2) }}</span>\n                <span class=\"fi-col fi-col-7 g-money\">{{ Number(item.unpaidAmount || 0).toFixed(2) }}</span>\n              </div>\n            </div>\n          </div>\n          <div v-else class=\"flow-items-empty\">暂无明细（非聚合支付流水）</div>\n        </template>\n      </el-table-column>";

text = text.substring(0, insertPos) + expandCol + text.substring(insertPos);

const cssEnd = text.lastIndexOf('</style>');
const newCss = "\n.flow-items-wrap { padding: 12px 20px; background: var(--el-fill-color-lighter); border-radius: 4px; }\n.flow-items-header { margin-bottom: 8px; }\n.flow-items-title { font-size: 13px; font-weight: 600; color: var(--el-text-color-primary); }\n.flow-items-table { width: 100%; }\n.flow-item-row { display: flex; align-items: center; padding: 6px 8px; font-size: 12px; border-bottom: 1px solid var(--el-border-color-lighter); }\n.flow-item-row:last-child { border-bottom: none; }\n.flow-item-header { font-weight: 600; color: var(--el-text-color-secondary); background: var(--el-fill-color); }\n.fi-col { flex: 1; text-align: center; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }\n.fi-col-1 { flex: 0 0 90px; }\n.fi-col-2 { flex: 0 0 120px; }\n.fi-col-3 { flex: 0 0 100px; }\n.fi-col-4 { flex: 0 0 90px; }\n.fi-col-5, .fi-col-6, .fi-col-7 { flex: 0 0 80px; }\n.flow-items-empty { padding: 12px; color: var(--el-text-color-secondary); font-size: 13px; text-align: center; }";
text = text.substring(0, cssEnd) + newCss + text.substring(cssEnd);

fs.writeFileSync(path, text, 'utf8');
console.log('financeFlow.vue updated successfully');
