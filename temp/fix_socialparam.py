path = r'D:\Office\Project\Java\gbi_platform\gbi_platform_admin\src\views\hr\socialParam\index.vue'
with open(path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

result = []
for i, line in enumerate(lines):
    n = i + 1
    # L22: 险种编码后加险种名称列
    if n == 22 and 'insuranceCode' in line and '险种编码' in line:
        result.append(line)
        result.append('        <el-table-column prop="insuranceName" label="险种名称" width="120" align="center" />\n')
    # L35: 操作列激活按钮前加编辑按钮
    elif n == 35 and 'activate' in line:
        result.append('            <AuthBtn permission="hr:social:param:edit" link type="primary" size="small" @click="handleEdit(row)" :disabled="row.isActive === 1">编辑</AuthBtn>\n')
        result.append(line)
    # L41: dialog title
    elif n == 41 and 'title="新增' in line:
        result.append('    <el-dialog v-model="dialogVisible" :title="form.id ? '\''编辑社保参数配置'\'' : '\''新增社保参数配置'\''" width="560px" :close-on-click-modal="false">\n')
    # L77: footer button
    elif n == 77 and '确定' in line:
        result.append('      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="handleSubmit">{{ form.id ? '\''编辑'\'' : '\''新增'\'' }}</el-button></template>\n')
    # L112: openAdd -> openDialog+handleEdit+openAdd
    elif n == 112 and 'openAdd' in line:
        result.append("const openDialog = () => { Object.assign(form, { id: undefined, cityCode: '', insuranceCode: '', industryCode: '', periodStart: '', periodEnd: '', baseMin: 0, baseMax: 0, personalRate: 0, companyRate: 0 }); dialogVisible.value = true }\n")
        result.append("const handleEdit = (row: any) => { Object.assign(form, { id: row.id, cityCode: row.cityCode, insuranceCode: row.insuranceCode, industryCode: row.industryCode || '', periodStart: row.periodStart, periodEnd: row.periodEnd || '', baseMin: row.baseMin, baseMax: row.baseMax, personalRate: row.personalRate, companyRate: row.companyRate, remark: row.remark || '' }); dialogVisible.value = true }\n")
        result.append("const openAdd = () => openDialog()\n")
    # L119-120: addSocialParamApi -> if/else edit/add
    elif n == 119 and 'addSocialParamApi' in line:
        result.append("    if (form.id) { await api.updateSocialParamApi(form); ElMessage.success('编辑成功') }\n")
        result.append("    else { await api.addSocialParamApi(form); ElMessage.success('新增成功') }\n")
    # L111: form reactive add id and remark
    elif n == 111 and 'const form = reactive' in line:
        result.append("const form = reactive({ id: undefined as number | undefined, cityCode: '', insuranceCode: '', industryCode: '', periodStart: '', periodEnd: '', baseMin: 0, baseMax: 0, personalRate: 0, companyRate: 0, remark: '' })\n")
    else:
        result.append(line)

with open(path, 'w', encoding='utf-8') as f:
    f.writelines(result)
print('done, lines:', len(lines), '->', len(result))
