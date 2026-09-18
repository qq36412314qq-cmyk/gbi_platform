# -*- coding: utf-8 -*-
import subprocess
import os

os.chdir(r"D:\Office\Project\Java\gbi_platform")

def run_cmd(cmd):
    print(f"\n{'='*50}")
    print(f"执行命令: {cmd}")
    print(f"{'='*50}")
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True, encoding='utf-8')
    print(result.stdout)
    if result.stderr:
        print(f"错误: {result.stderr}")
    return result.returncode

# 1. 查看状态
run_cmd("git status")

# 2. 查看远程配置
run_cmd("git remote -v")

# 3. 添加所有变更
run_cmd("git add .")

# 4. 提交变更
run_cmd('git commit -m "fix: 补全入职申请工作经历/学业经历表格，修复hr模块前端功能，优化组织岗位管理\n\n- transfer/index.vue: 补全工作经历和学业经历动态表格\n- hr.ts: 补充缺失的人事异动API接口\n- HrOrgServiceImpl.java: 修复SysOrg导入，优化组织路径构建\n- org/index.vue: 优化所属组织下拉选择和岗位联动\n- 修复编译错误和前端运行时错误"')

# 5. 查看提交结果
run_cmd("git log --oneline -3")

# 6. 尝试推送（如果远程配置有问题，跳过）
print("\n注意: 远程仓库使用了 ghproxy.com 镜像代理，可能导致连接超时。")
print("建议直接推送:\ngit push origin main")
