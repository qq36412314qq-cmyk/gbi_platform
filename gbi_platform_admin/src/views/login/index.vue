<template>
  <div class="g-login">
    <div class="g-login-card">
      <div class="g-login-title">集团业务一体化管控平台</div>
      <div class="g-login-subtitle">Group Business Integration Platform</div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent>
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入登录账号" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="g-login-btn" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="g-login-tip">默认账号：admin / 123456（首次登录请修改密码）</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { useThemeStore } from '@/store/theme'
import { getThemeApi } from '@/api/sys'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: ''
})
console.log('form', form)
const rules: FormRules = {
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur' }
  ]
}

/** 登录：成功后拉取主题，跳转来源页 */
async function handleLogin(): Promise<void> {
  console.debug('[login] 0/4 开始登录流程, form =', JSON.stringify(form))
  if (!formRef.value) {
    console.error('[login] formRef.value 未初始化')
    return
  }
  // 注意：validate 传 async 回调时返回 void，回调内异常会成为 unhandled rejection
  // （页面"闪一下、无提示、不跳转"的常见原因），因此改用 Promise 形式
  let valid = false
  try {
    valid = await formRef.value.validate()
    console.debug('[login] 0/4 表单校验通过 =', valid)
  } catch (err) {
    console.warn('[login] 0/4 表单校验失败 =', err)
    return
  }
  if (!valid) {
    console.warn('[login] 0/4 表单校验不通过，中止')
    return
  }
  loading.value = true
  try {
    await userStore.login({ ...form })
    console.debug('[login] 1/4 登录接口成功, token =', (userStore.token || '').slice(0, 20) + '...')

    const info = await userStore.fetchUserInfo()
    console.debug('[login] 2/4 用户信息成功, realName =', info.realName, 'permissions =', JSON.stringify(info.permissions))

    try {
      const themeConfig = await getThemeApi()
      console.debug('[login] 3/4 主题接口成功, themeConfig =', JSON.stringify(themeConfig))
      themeStore.applyTheme(themeConfig as never)
      console.debug('[login] 3/4 主题已应用 =', JSON.stringify(themeConfig))
    } catch (themeErr) {
      console.warn('[login] 3/4 主题获取失败, 使用默认主题, error =', themeErr)
      themeStore.applyTheme()
    }

    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/dashboard'
    console.debug('[login] 4/4 准备跳转 =', redirect)
    await router.replace(redirect)
    console.debug('[login] 4/4 跳转完成, 当前路由 =', router.currentRoute.value.fullPath)
  } catch (err) {
    console.error('[login] 登录流程异常 =', err)
    ElMessage.error((err as Error)?.message || '登录失败，请稍后重试')
  } finally {
    loading.value = false
    console.debug('[login] 流程结束, loading = false')
  }
}
</script>

<style scoped>
.g-login {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-bg-page));
}

.g-login-card {
  width: 420px;
  padding: 40px 36px 24px;
  background: var(--color-bg-card);
  border-radius: calc(var(--card-radius) * 2);
  box-shadow: var(--shadow-hover);
}

.g-login-title {
  font-size: var(--font-size-xxl);
  font-weight: 600;
  text-align: center;
  color: var(--color-text-primary);
}

.g-login-subtitle {
  font-size: var(--font-size-sm);
  text-align: center;
  color: var(--color-text-secondary);
  margin: 6px 0 28px;
}

.g-login-btn {
  width: 100%;
}

.g-login-tip {
  margin-top: 12px;
  font-size: var(--font-size-xs);
  color: var(--color-text-placeholder);
  text-align: center;
}
</style>