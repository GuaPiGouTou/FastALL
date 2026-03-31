# FastCRUD 核心功能详细说明文档

## 1. 项目概述

FastCRUD 是一个基于 Spring Boot + Vue.js 的快速开发框架，采用元数据驱动的方式实现 CRUD 操作，支持表格导入导出、RBAC 权限管理等核心功能。

## 2. 核心功能模块

### 2.1 元数据驱动的 CRUD 功能

#### 2.1.1 功能描述
基于表结构元数据，实现动态的 CRUD 操作，无需为每个表编写重复的代码。

#### 2.1.2 技术实现
- **后端控制器**: `CrudController.java`
- **核心服务**: `CrudService.java`
- **数据访问**: `CrudMapper.java`

#### 2.1.3 主要 API 端点

| 端点 | 方法 | 功能描述 |
|------|------|----------|
| `/api/crud/{tableName}/schema` | GET | 获取表结构定义 |
| `/api/crud/{tableName}/list` | GET | 获取数据列表（支持分页、搜索、筛选、排序） |
| `/api/crud/{tableName}/save` | POST | 保存数据（新增或修改） |
| `/api/crud/{tableName}/{id}` | DELETE | 删除数据 |
| `/api/crud/{tableName}/column` | POST | 新增字段 |
| `/api/crud/{tableName}/column` | PUT | 修改字段配置 |
| `/api/crud/{tableName}/export` | POST | 导出数据到 Excel |

#### 2.1.4 关键特性
- **智能搜索**: 自动识别可搜索字段，支持模糊搜索
- **动态筛选**: 支持多种筛选条件组合
- **字段类型映射**: 前端 UI 类型与数据库类型自动映射
- **动态 DDL**: 支持运行时修改表结构
- **批量导出**: 支持导出选中数据或符合条件的全部数据

### 2.2 表格导入功能

#### 2.2.1 功能描述
支持从 Excel 文件导入数据，并自动创建表结构和元数据配置。

#### 2.2.2 技术实现
- **核心服务**: `ExcelImportService.java`
- **辅助服务**: `CsvImportService.java`, `JsonImportService.java`, `DatabaseImportService.java`

#### 2.2.3 主要功能
- **Excel 解析**: 使用 EasyExcel 解析 Excel 文件
- **字段类型推断**: 自动推断列的数据类型
- **表结构创建**: 自动生成 CREATE TABLE 语句
- **元数据配置**: 自动创建字段配置记录
- **数据导入**: 批量插入数据
- **模块注册**: 自动注册为系统模块

#### 2.2.4 支持的导入方式
- Excel 文件导入
- CSV 文件导入
- JSON 数据导入
- 数据库表导入
- 文本输入导入

### 2.3 RBAC 权限管理

#### 2.3.1 功能描述
基于角色的访问控制（RBAC）模型，实现用户、角色、权限的管理和关联。

#### 2.3.2 技术实现
- **后端控制器**: `RbacController.java`
- **核心服务**: `RbacService.java`
- **缓存服务**: `PermissionCacheService.java`
- **数据模型**: `SysUser`, `SysRole`, `SysPermission`, `SysUserRole`, `SysRolePermission`

#### 2.3.3 主要功能
- **角色管理**: 创建、编辑、删除角色
- **权限管理**: 权限项配置和管理
- **角色权限分配**: 为角色分配权限，支持批量操作
- **用户角色分配**: 为用户分配角色，支持多对多关联
- **权限缓存**: 提升权限验证性能
- **权限验证**: 实时权限检查

#### 2.3.4 权限缓存策略
- 角色权限缓存
- 用户权限缓存
- 权限变更时自动清除缓存

### 2.4 系统管理功能

#### 2.4.1 功能描述
系统基础功能，包括用户管理、模块管理、操作日志等。

#### 2.4.2 技术实现
- **控制器**: `SystemController.java`, `moduleController.java`
- **服务**: `moduleService.java`, `SysUserService.java`
- **数据模型**: `module`, `SysUser`, `SysOperationLog`

#### 2.4.3 主要功能
- **用户管理**: 用户信息的增删改查
- **模块管理**: 系统模块的注册和管理
- **操作日志**: 记录系统操作历史

## 3. 技术栈

### 3.1 后端技术
- **框架**: Spring Boot 2.x
- **ORM**: MyBatis-Plus
- **数据库**: MySQL
- **权限框架**: Sa-Token
- **Excel 处理**: EasyExcel
- **JSON 处理**: Fastjson

### 3.2 前端技术
- **框架**: Vue 3
- **构建工具**: Vite
- **UI 组件**: 自定义组件
- **状态管理**: 原生 Vue 响应式
- **路由**: Vue Router

## 4. 数据库设计

### 4.1 核心表结构

#### 4.1.1 系统表
- `sys_user`: 用户信息表
- `sys_role`: 角色表
- `sys_permission`: 权限表
- `sys_user_role`: 用户角色关联表
- `sys_role_permission`: 角色权限关联表
- `sys_field_config`: 字段配置表
- `module`: 模块信息表
- `sys_operation_log`: 操作日志表

#### 4.1.2 业务表
业务表由系统自动创建，遵循 `tb_` 前缀命名规范，包含以下默认字段：
- `id`: 主键，自增
- `created_time`: 创建时间
- `updated_time`: 更新时间

## 5. 项目结构

### 5.1 后端结构
```
java/src/main/java/com/crud/fastcrud/
├── config/          # 配置类
├── controller/      # 控制器
├── dto/             # 数据传输对象
├── entity/          # 实体类
├── mapper/          # 数据访问层
├── service/         # 业务逻辑层
│   └── Impl/        # 服务实现
└── FastCrudApplication.java # 应用入口
```

### 5.2 前端结构
```
vue/crud/src/
├── assets/          # 静态资源
├── components/      # 组件
├── views/           # 页面
│   ├── system/      # 系统管理页面
│   ├── Dashboard.vue        # 仪表盘
│   ├── Login.vue            # 登录页面
│   ├── ModuleDashboard.vue  # 模块管理
│   ├── Profile.vue          # 个人信息
│   └── SmartGridView.vue    # 智能表格视图
├── App.vue          # 应用根组件
├── main.js          # 应用入口
└── style.css        # 全局样式
```

## 6. 核心流程

### 6.1 元数据驱动的 CRUD 流程
1. 前端请求表结构（schema）
2. 后端从 `sys_field_config` 表获取字段配置
3. 前端根据 schema 渲染表单和表格
4. 前端发送数据操作请求（增删改查）
5. 后端执行相应的数据库操作
6. 返回操作结果

### 6.2 Excel 导入流程
1. 上传 Excel 文件
2. 解析表头和数据
3. 推断字段类型
4. 创建表结构
5. 插入元数据配置
6. 批量导入数据
7. 注册为系统模块

### 6.3 RBAC 权限控制流程
1. 用户登录获取 token
2. 请求资源时携带 token
3. 后端验证 token 并获取用户信息
4. 从缓存获取用户权限
5. 验证用户是否有权限访问资源
6. 允许或拒绝访问

## 7. 核心 API 文档

### 7.1 CRUD 相关 API

#### 7.1.1 获取表结构
```
GET /api/crud/{tableName}/schema
```

**返回示例：**
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "tableName": "tb_user",
      "prop": "name",
      "label": "姓名",
      "uiType": "Input",
      "options": null,
      "isShowInList": 1,
      "sortOrder": 1
    }
  ],
  "message": "success"
}
```

#### 7.1.2 获取数据列表
```
GET /api/crud/{tableName}/list?page=1&size=20&keyword=搜索关键词
```

**返回示例：**
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "name": "张三",
        "age": 25
      }
    ],
    "total": 1
  },
  "message": "success"
}
```

#### 7.1.3 保存数据
```
POST /api/crud/{tableName}/save
```

**请求体：**
```json
{
  "id": 1, // 为空时表示新增
  "name": "张三",
  "age": 25
}
```

### 7.2 导入相关 API

#### 7.2.1 Excel 导入
```
POST /api/file/import-excel
```

**请求参数：**
- `file`: Excel 文件
- `moduleName`: 模块名称
- `tableName`: 表名（可选）

### 7.3 RBAC 相关 API

#### 7.3.1 获取角色列表
```
GET /api/rbac/roles
```

#### 7.3.2 分配权限给角色
```
POST /api/rbac/roles/{roleId}/permissions
```

**请求体：**
```json
{
  "permissionIds": [1, 2, 3]
}
```

## 8. 开发指南

### 8.1 新增功能模块
1. 在后端 `controller` 包中创建控制器
2. 在 `service` 包中实现业务逻辑
3. 在 `mapper` 包中定义数据访问方法
4. 在前端 `views` 目录中创建相应页面
5. 配置路由和权限

### 8.2 扩展 CRUD 功能
1. 扩展 `CrudService` 类，添加自定义业务逻辑
2. 在 `CrudController` 中添加新的 API 端点
3. 前端相应更新视图组件

### 8.3 自定义导入方式
1. 实现 `ImportService` 接口
2. 在 `FileController` 中添加相应的 API 端点
3. 前端添加对应的导入界面

## 9. 部署说明

### 9.1 环境要求
- JDK 1.8+
- MySQL 5.7+
- Node.js 14+

### 9.2 部署步骤
1. 执行 SQL 脚本创建数据库结构
2. 配置 `application.yaml` 中的数据库连接信息
3. 构建前端项目：`npm run build`
4. 构建后端项目：`mvn package`
5. 部署打包后的 jar 文件

## 10. 测试指南

### 10.1 单元测试
- 运行 `FastCrudApplicationTests.java` 进行基础测试
- 运行 `UiTableTest.java` 测试表格功能

### 10.2 集成测试
- 使用 Postman 测试 API 端点
- 前端功能测试

## 11. 总结

FastCRUD 是一个功能完备的快速开发框架，通过元数据驱动的方式实现了高度灵活的 CRUD 操作，支持多种数据导入方式，并集成了完善的 RBAC 权限管理系统。该框架可以显著提高开发效率，减少重复代码，同时提供了良好的扩展性和可维护性。

通过本文档，其他 AI 开发者可以快速了解 FastCRUD 的核心功能和架构，从而进行进一步的开发和扩展。