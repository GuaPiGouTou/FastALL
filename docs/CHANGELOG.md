# FastCRUD 更新日志

本文档记录项目的所有重要变更。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)

---

## [Unreleased] - 待发布

### 计划中
- [ ] 巻加更多单元测试
- [ ] 前端引入TypeScript
- [ ] 完善操作日志记录

---

## [1.3.0] - 2026-03-26

### 新增 (Added)
- **Swagger API文档** - Knife4j集成
  - `SwaggerConfig` 配置类
  - 访问地址: http://localhost:8080/doc.html
  - 自动扫描controller包下的所有接口
- **配置加密工具** - `ConfigEncryptor`
  - AES加密/解密配置敏感信息
  - 支持自定义加密密钥

### 变更 (Changed)
- **SaTokenConfig** - 排除Swagger相关路径
- **application.yaml** - 添加加密密钥配置

---

## [1.2.0] - 2026-03-26

### 新增 (Added)
- **业务异常体系** - 统一异常处理
  - `BusinessException` - 基础业务异常
  - `AuthException` - 认证异常(401)
  - `ValidationException` - 参数校验异常(400)
  - `FileStorageException` - 文件存储异常
- **全局异常处理器增强** - `GlobalExceptionHandler`
  - Sa-Token认证异常细分处理
  - 业务异常统一响应格式
  - 参数校验异常处理
  - 运行时异常友好提示
- **文件上传校验** - 安全增强
  - 文件类型白名单校验(pdf/doc/xls/jpg/png等)
  - 文件大小限制(最大100MB)
  - 文件名和扩展名校验
- **单元测试** - 核心工具类测试
  - `PasswordUtilTest` - 密码工具测试
  - `ExceptionTest` - 异常类测试

### 变更 (Changed)
- **文件服务重构** - 使用业务异常替代RuntimeException
- **配置文件优化** - 添加文件上传相关配置项

---

## [1.1.0] - 2026-03-24

### 新增 (Added)
- **登录验证器体系** - 责任链模式重构
  - `LoginValidator` 接口定义
  - `LoginContext` 上下文对象
  - 6个独立验证器实现:
    - `CaptchaValidator` - 验证码校验
    - `UsernameValidator` - 用户名校验
    - `BloomFilterValidator` - 布隆过滤器校验
    - `AccountLockValidator` - 账号锁定校验
    - `RateLimitValidator` - 限流校验
    - `CredentialValidator` - 密码校验
- **密码加密工具类** - `PasswordUtil`
  - BCrypt加密/校验方法
  - MD5密码自动升级支持

### 变更 (Changed)
- **Login方法重构** - 从91行缩减至核心逻辑
  - 消除5处重复日志记录代码
  - 验证逻辑解耦，易于扩展和测试
- **密码加密升级** - MD5迁移到BCrypt
  - 新注册用户使用BCrypt加密
  - 已有MD5密码自动升级(登录成功时)
  - 兼容MD5和BCrypt双重验证

### 修复 (Fixed)
- **分片上传文件大小计算错误** - 文件删除后调用length()返回0
  - 在删除临时文件前保存文件大小
- **pom.xml主类配置错误** - 修正为正确的Application类

---

## [1.0.0] - 2026-03-24

### 新增 (Added)
- 项目重命名为 **FastCRUD** 企业级快速开发平台
- 项目文档 README.md
- 接口文档 API.md
- 更新日志 CHANGELOG.md

### 变更 (Changed)
- 系统名称从"生物医药中台系统"改为"FastCRUD"
- 登录页标题改为"FastCRUD"
- 注册页标题改为"FastCRUD"
- 侧边栏Logo文字改为"FastCRUD"
- 路由页面标题统一为"FastCRUD"

### 移除 (Removed)
- 删除生物医药相关后端模块:
  - SysDeviceController/Service/Entity/Mapper
  - SysDeviceMaintenanceController/Service/Entity/Mapper
  - SysExperimentTaskController/Service/Entity/Mapper
  - SysReagentController/Service/Entity/Mapper
  - SysReagentQueryDTO
- 删除生物医药相关前端模块:
  - views/device/
  - views/experiment/
  - views/reagent/
  - views/product/
  - views/order/
  - views/search/
  - views/blockchain/
  - views/user/
  - api/device.js
  - api/deviceMaintenance.js
  - api/experimentTask.js
  - api/reagent.js
- 简化路由配置，移除已删除模块的路由
- 简化MainLayout菜单结构

### 修复 (Fixed)
- 修复pom.xml中主类配置错误

---

## 历史版本

### [0.0.1-SNAPSHOT] - 2026-03-15

### 新增 (Added)
- 用户认证模块
  - 登录/注册/登出功能
  - 滑动验证码(AJ-Captcha)
  - 布隆过滤器优化用户查询
  - 账号锁定机制(30分钟5次失败)
  - 限流保护(5分钟5次，1天10次)
  - 分布式锁防止并发注册
- 文件管理模块
  - 文件上传/下载
  - 文件秒传(MD5去重)
  - 分片上传(断点续传)
  - 存储策略切换(本地/MinIO)
- 日志审计模块
  - 登录日志记录
  - 操作审计日志(AOP)
- 安全特性
  - 敏感数据AES加密(身份证/手机号/邮箱)
  - Sa-Token认证授权
  - Redis会话存储
- 前端框架
  - Vue3 + Vite + Element Plus
  - Pinia状态管理
  - 路由守卫

---

## 版本说明

- **[Unreleased]**: 待发布的功能和改进
- **[1.3.0]**: 当前版本 - P2问题修复
- **[1.2.0]**: P1问题修复
- **[1.1.0]**: P0问题修复
- **[1.0.0]**: 稳定版本 - 项目重构
- **[0.0.1-SNAPSHOT]**: 初始开发版本

## 升级指南

### 从 1.2.0 升级到 1.3.0

1. 拉取最新代码
2. 后端重新编译: `./mvnw clean compile`
3. 前端重新安装依赖: `npm install && npm run build`
4. **新增**: 访问 http://localhost:8080/doc.html 查看API文档

### 从 1.1.0 升级到 1.2.0

1. 拉取最新代码
2. 后端重新编译: `./mvnw clean compile`
3. 前端重新安装依赖: `npm install && npm run build`
4. **注意**: 文件上传现在有类型和大小限制

### 从 1.0.0 升级到 1.1.0

1. 拉取最新代码
2. 后端重新编译: `./mvnw clean compile`
3. 前端重新安装依赖: `npm install && npm run build`
4. **注意**: 已有用户的MD5密码会在下次登录成功后自动升级为BCrypt

---

## 贡献指南

提交代码时请遵循以下规范:

- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `refactor`: 重构
- `style`: 代码格式(不影响功能)
- `test`: 测试相关
- `chore`: 构建/工具相关

示例:
```
feat: 添加用户头像上传功能
fix: 修复登录验证码失效问题
docs: 更新API文档
refactor: 重构Login方法验证逻辑
```
