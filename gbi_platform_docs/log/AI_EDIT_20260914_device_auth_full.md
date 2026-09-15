# AI_EDIT_LOG

## 【2026-09-14 10:47:00】客户端设备校验安全加固-完整代码输出与执行

- 用户原始需求：在集团多业态一体化管控系统中增加客户端设备校验机制，通过 Go Agent 采集硬件指纹，后端校验设备授权
- 修改涉及文件：
  - gbi_platform_docs/attachments/sql/upgrade_v2.8_device_auth.sql（新增）
  - gbi_platform_server/src/main/java/com/gbi/platform/entity/SysClientDeviceAuth.java（新增）
  - gbi_platform_server/src/main/java/com/gbi/platform/mapper/SysClientDeviceAuthMapper.java（新增）
  - gbi_platform_server/src/main/java/com/gbi/platform/dto/ClientDeviceAuthDTO.java（新增）
  - gbi_platform_server/src/main/java/com/gbi/platform/dto/DeviceHardwareInfoDTO.java（新增）
  - gbi_platform_server/src/main/java/com/gbi/platform/vo/ClientDeviceAuthVO.java（新增）
  - gbi_platform_server/src/main/java/com/gbi/platform/service/SysClientDeviceAuthService.java（新增）
  - gbi_platform_server/src/main/java/com/gbi/platform/service/impl/SysClientDeviceAuthServiceImpl.java（新增）
  - gbi_platform_server/src/main/java/com/gbi/platform/controller/sys/ClientDeviceAuthController.java（新增）
  - gbi_platform_server/src/main/java/com/gbi/platform/controller/sys/DeviceAuthController.java（新增）
  - gbi_platform_server/src/main/java/com/gbi/platform/common/constant/PermissionConst.java（修改）
  - gbi_platform_server/src/main/java/com/gbi/platform/dto/LoginDTO.java（修改）
  - gbi_platform_server/src/main/java/com/gbi/platform/service/AuthService.java（修改）
  - gbi_platform_server/src/main/java/com/gbi/platform/service/impl/AuthServiceImpl.java（修改）
  - gbi_platform_server/src/main/java/com/gbi/platform/config/SecurityConfig.java（修改）
  - agent/go.mod（新增）
  - agent/main.go（新增）
  - agent/pkg/hardware/collector.go（新增）
  - agent/pkg/server/http.go（新增）
  - agent/pkg/server/tray.go（新增）
  - agent/build.bat（新增）
  - agent/resources/app.ico（复制）
  - gbi_platform_admin/src/api/deviceAuth.ts（新增）
  - gbi_platform_admin/src/views/login/index.vue（修改）
  - gbi_platform_admin/src/views/platform/deviceAuth/index.vue（新增）
  - gbi_platform_admin/src/router/index.ts（修改）
  - gbi_platform_admin/src/store/user.ts（修改）
  - gbi_platform_admin/src/api/base.ts（修改）
- 变更摘要：
  1. 数据库新增 sys_client_device_auth 表存储授权设备清单
  2. sys_config 插入设备校验开关和 RSA 密钥配置项
  3. 新增授权设备管理 CRUD 接口（Controller/Service/Mapper/Entity）
  4. 登录接口增加设备校验逻辑（基于开关状态）
  5. 前端登录页请求 Agent 获取硬件信息
  6. Go Agent 程序采集硬件指纹并提供本地接口
  7. 新增设备授权管理页面
- 风险与注意事项：
  - Agent 接口必须限制仅 127.0.0.1 访问（已实现）
  - RSA 私钥存储在后端 sys_config，客户端不持密钥
  - 授权设备表无 company_id（集团统一授权）
  - 设备校验开关关闭时跳过校验
  - 编译已完成，后端已启动，前端已启动
