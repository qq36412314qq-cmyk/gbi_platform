# -*- coding: utf-8 -*-
import sys
import io

# 设置标准输出编码为utf-8
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

file_path = r'D:\Office\Project\Java\gbi_platform\gbi_platform_admin\src\views\hr\employee\index.vue'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

old_code = '''const openEditDialog = (row: api.EmployeeVO) => {
  Object.assign(form, { id: row.id, employeeNo: row.employeeNo, name: row.name, gender: row.gender, birthdate: row.birthdate, entryDate: row.entryDate, employmentType: row.employmentType, phone: row.phone, email: row.email, basicSalary: row.basicSalary, remark: row.remark, photoFileId: row.photoFileId })
  photoFileId.value = row.photoFileId
  photoUrl.value = row.photoPreviewUrl || ''
  attachmentHtml.value = row.attachmentContent || ''
  dialogVisible.value = true
}'''

new_code = '''const openEditDialog = (row: api.EmployeeVO) => {
  Object.assign(form, { id: row.id, employeeNo: row.employeeNo, name: row.name, gender: row.gender, birthdate: row.birthdate, entryDate: row.entryDate, employmentType: row.employmentType, phone: row.phone, email: row.email, basicSalary: row.basicSalary, remark: row.remark, photoFileId: row.photoFileId, attachmentContent: row.attachmentContent })
  photoFile.value = null
  photoFileId.value = row.photoFileId
  photoUrl.value = row.photoPreviewUrl || ''
  attachmentHtml.value = row.attachmentContent || ''
  dialogVisible.value = true
}'''

if old_code in content:
    content = content.replace(old_code, new_code)
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
    print('修改成功')
else:
    print('未找到目标代码')
