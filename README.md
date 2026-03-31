# FastCRUD - 企业级快速开发平台

## 项目简介

FastCRUD 是一个基于 Spring Boot + Vue3 的企业级快速开发平台，提供用户认证、文件管理、日志审计等核心功能模块，可作为企业级应用的基础框架。

## 技术栈

### 后端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.6.13 | 核心框架 |
| MyBatis Plus | 3.5.3.1 | ORM框架 |
| Sa-Token | 1.37.0 | 权限认证 |
| Redisson | 3.17.7 | 分布式锁/限流/布隆过滤器 |
| AJ-Captcha | 1.3.0 | 滑动验证码 |
| MinIO | 8.5.2 | 对象存储 |
| Hutool | 5.8.25 | 工具库 |

### 前端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.25 | 前端框架 |
| Vite | 7.3.1 | 构建工具 |
| Element Plus | 2.13.5 | UI组件库 |
| Pinia | 3.0.4 | 状态管理 |
| Axios | 1.11.0 | HTTP客户端 |

### 中间件
| 中间件 | 说明 |
|--------|------|
| MySQL | 关系数据库 |
| Redis | 缓存/会话/限流 |
| MinIO | 对象存储 |

## 项目结构

```
ecmo/
├── ecmo/                          # 后端项目
│   ├── src/main/java/org/example/ecmo/
│   │   ├── controller/            # 控制器层
│   │   │   ├── AuthController.java        # 认证控制器
│   │   │   ├── SysFileController.java     # 文件控制器
│   │   │   ├── SysFileChunkController.java # 分片上传控制器
│   │   │   └── SysLogController.java      # 日志控制器
│   │   ├── service/               # 服务层
│   │   │   ├── SysUserService.java        # 用户服务
│   │   │   ├── FileStorageService.java    # 文件存储服务
│   │   │   ├── SysFileInfoService.java    # 文件信息服务
│   │   │   ├── SysFileChunkService.java   # 分片服务
│   │   │   └── LogService.java            # 日志服务
│   │   ├── entity/                # 实体类
│   │   ├── mapper/                # 数据访问层
│   │   ├── config/                # 配置类
│   │   ├── aspect/                # 切面类
│   │   ├── handler/               # 处理器
│   │   ├── component/             # 组件
│   │   ├── annotation/            # 自定义注解
│   │   └── utils/                 # 工具类
│   └── src/main/resources/
│       └── application.yaml       # 配置文件
│
├── html/                          # 前端项目
│   └── src/
│       ├── views/                 # 页面组件
│       │   ├── auth/              # 登录/注册
│       │   ├── file/              # 文件管理
│       │   └── system/            # 系统日志
│       ├── api/                   # API接口
│       ├── store/                 # 状态管理
│       ├── router/                # 路由配置
│       └── layout/                # 布局组件
│
├── README.md                      # 项目文档
├── API.md                         # 接口文档
└── CHANGELOG.md                   # 更新日志
```

## 核心功能

### 1. 用户认证模块
- 用户登录/注册/登出
- 滑动验证码验证
- 布隆过滤器优化用户查询
- 账号锁定机制(30分钟5次失败)
- 限流保护(5分钟5次，1天10次)
- 敏感数据AES加密存储

### 2. 文件管理模块
- 文件上传/下载
- 文件秒传(MD5去重)
- 分片上传(断点续传)
- 存储策略切换(本地/MinIO)
- 文件元数据管理

### 3. 日志审计模块
- 登录日志记录
- 操作审计日志
- AOP自动记录

## 快速开始

### 环境要求
- JDK 1.8+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+
- MinIO (可选)

### 后端启动

```bash
cd ecmo
./mvnw spring-boot:run
```

### 前端启动

```bash
cd html
npm install
npm run dev
```

### 访问地址
- 前端: http://localhost:5173
- 后端: http://localhost:8080

## 配置说明

### 数据库配置 (application.yaml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/ECMO
    username: root
    password: root
  redis:
    host: 127.0.0.1
    port: 6379
    password: root
```

### 文件存储配置

```yaml
file:
  storage:
    type: minio  # localFileStorage(本地) 或 minio(云端)
  local:
    path: E:/ecmo_uploads/
  minio:
    endpoint: http://localhost:9000
    accessKey: admin
    secretKey: admin123456
    bucket: ecmo-data
```

## 安全特性

1. **密码加密**: MD5加密存储(建议升级为BCrypt)
2. **敏感数据加密**: 身份证、手机号、邮箱AES加密
3. **验证码保护**: 滑动验证码防止机器人攻击
4. **限流保护**: 分钟级和天级双重限流
5. **账号锁定**: 连续失败自动锁定
6. **分布式锁**: 防止并发重复操作

## 开发规范

### 代码规范
- 遵循阿里巴巴Java开发规范
- 使用Lombok减少样板代码
- 统一异常处理

### 提交规范
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- refactor: 重构
- style: 代码格式

## 许可证

MIT License

## 联系方式

如有问题请提交 Issue
