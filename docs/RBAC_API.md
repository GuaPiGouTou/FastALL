# RBAC 角色权限管理模块 API 文档

## 概述

本文档描述了FastCRUD系统的RBAC（基于角色的访问控制）模块的API接口。该模块实现了完整的权限管理功能，包括角色管理、权限管理、角色权限分配和用户角色分配。

---

## 1. 角色管理 API

### 1.1 获取所有角色列表

**接口地址**：`GET /api/rbac/roles`

**请求参数**：无

**响应示例**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "roleName": "管理员",
      "roleCode": "admin",
      "description": "系统管理员",
      "status": 1,
      "createTime": "2024-01-01 00:00:00"
    }
  ],
  "message": "success"
}
```

---

### 1.2 获取单个角色

**接口地址**：`GET /api/rbac/roles/{id}`

**路径参数**：
- `id`：角色ID

**响应示例**：
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "roleName": "管理员",
    "roleCode": "admin",
    "description": "系统管理员",
    "status": 1,
    "createTime": "2024-01-01 00:00:00"
  },
  "message": "success"
}
```

---

### 1.3 创建角色

**接口地址**：`POST /api/rbac/roles`

**请求体**：
```json
{
  "roleName": "测试角色",
  "roleCode": "test_role",
  "description": "测试用角色",
  "status": 1
}
```

**响应示例**：
```json
{
  "code": 200,
  "data": "创建成功",
  "message": "success"
}
```

---

### 1.4 更新角色

**接口地址**：`PUT /api/rbac/roles/{id}`

**路径参数**：
- `id`：角色ID

**请求体**：
```json
{
  "roleName": "更新后的角色名",
  "description": "更新后的描述",
  "status": 1
}
```

**响应示例**：
```json
{
  "code": 200,
  "data": "更新成功",
  "message": "success"
}
```

---

### 1.5 删除角色

**接口地址**：`DELETE /api/rbac/roles/{id}`

**路径参数**：
- `id`：角色ID

**响应示例**：
```json
{
  "code": 200,
  "data": "删除成功",
  "message": "success"
}
```

---

## 2. 角色权限分配 API

### 2.1 获取角色的权限ID列表

**接口地址**：`GET /api/rbac/roles/{roleId}/permissions`

**路径参数**：
- `roleId`：角色ID

**响应示例**：
```json
{
  "code": 200,
  "data": [1, 2, 3, 4, 5],
  "message": "success"
}
```

---

### 2.2 批量分配权限给角色

**接口地址**：`POST /api/rbac/roles/{roleId}/permissions`

**路径参数**：
- `roleId`：角色ID

**请求体**：
```json
{
  "permissionIds": [1, 2, 3, 4, 5]
}
```

**响应示例**：
```json
{
  "code": 200,
  "data": "权限分配成功",
  "message": "success"
}
```

---

### 2.3 添加单个权限到角色

**接口地址**：`POST /api/rbac/roles/{roleId}/permissions/{permissionId}`

**路径参数**：
- `roleId`：角色ID
- `permissionId`：权限ID

**响应示例**：
```json
{
  "code": 200,
  "data": "添加权限成功",
  "message": "success"
}
```

---

### 2.4 移除角色的单个权限

**接口地址**：`DELETE /api/rbac/roles/{roleId}/permissions/{permissionId}`

**路径参数**：
- `roleId`：角色ID
- `permissionId`：权限ID

**响应示例**：
```json
{
  "code": 200,
  "data": "移除权限成功",
  "message": "success"
}
```

---

## 3. 用户角色分配 API

### 3.1 获取用户的角色列表

**接口地址**：`GET /api/rbac/users/{userId}/roles`

**路径参数**：
- `userId`：用户ID

**响应示例**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "roleName": "管理员",
      "roleCode": "admin"
    }
  ],
  "message": "success"
}
```

---

### 3.2 获取用户的角色ID列表

**接口地址**：`GET /api/rbac/users/{userId}/role-ids`

**路径参数**：
- `userId`：用户ID

**响应示例**：
```json
{
  "code": 200,
  "data": [1, 2, 3],
  "message": "success"
}
```

---

### 3.3 批量分配角色给用户

**接口地址**：`POST /api/rbac/users/{userId}/roles`

**路径参数**：
- `userId`：用户ID

**请求体**：
```json
{
  "roleIds": [1, 2, 3]
}
```

**响应示例**：
```json
{
  "code": 200,
  "data": "角色分配成功",
  "message": "success"
}
```

---

### 3.4 添加单个角色到用户

**接口地址**：`POST /api/rbac/users/{userId}/roles/{roleId}`

**路径参数**：
- `userId`：用户ID
- `roleId`：角色ID

**响应示例**：
```json
{
  "code": 200,
  "data": "添加角色成功",
  "message": "success"
}
```

---

### 3.5 移除用户的单个角色

**接口地址**：`DELETE /api/rbac/users/{userId}/roles/{roleId}`

**路径参数**：
- `userId`：用户ID
- `roleId`：角色ID

**响应示例**：
```json
{
  "code": 200,
  "data": "移除角色成功",
  "message": "success"
}
```

---

## 4. 用户权限查询 API

### 4.1 获取用户的权限编码列表

**接口地址**：`GET /api/rbac/users/{userId}/permissions`

**路径参数**：
- `userId`：用户ID

**响应示例**：
```json
{
  "code": 200,
  "data": ["user:create", "user:read", "user:update", "user:delete"],
  "message": "success"
}
```

---

### 4.2 检查用户是否拥有某权限

**接口地址**：`GET /api/rbac/users/{userId}/permissions/check`

**路径参数**：
- `userId`：用户ID

**查询参数**：
- `permissionCode`：权限编码

**响应示例**：
```json
{
  "code": 200,
  "data": true,
  "message": "success"
}
```

---

## 5. 权限管理 API

### 5.1 获取所有权限列表

**接口地址**：`GET /api/rbac/permissions`

**请求参数**：无

**响应示例**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "permissionName": "用户管理",
      "permissionCode": "user:manage",
      "resourceType": "menu",
      "resourceUrl": "/system/users",
      "parentId": 0,
      "icon": "User",
      "sortOrder": 1,
      "status": 1
    }
  ],
  "message": "success"
}
```

---

### 5.2 获取权限树结构

**接口地址**：`GET /api/rbac/permissions/tree`

**请求参数**：无

**响应示例**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "label": "系统管理",
      "code": "system",
      "type": "menu",
      "children": [
        {
          "id": 2,
          "label": "用户管理",
          "code": "user:manage",
          "type": "menu",
          "children": []
        }
      ]
    }
  ],
  "message": "success"
}
```

---

### 5.3 按类型获取权限列表

**接口地址**：`GET /api/rbac/permissions/type/{type}`

**路径参数**：
- `type`：资源类型（menu/button/api）

**响应示例**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "permissionName": "用户管理",
      "permissionCode": "user:manage",
      "resourceType": "menu"
    }
  ],
  "message": "success"
}
```

---

## 错误码说明

| 错误码 | 说明 |
|-------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 数据库设计

### 角色表 (sys_role)
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| role_name | VARCHAR(50) | 角色名称 |
| role_code | VARCHAR(50) | 角色编码 |
| description | VARCHAR(200) | 描述 |
| status | TINYINT | 状态(0:禁用,1:启用) |
| create_time | DATETIME | 创建时间 |

### 权限表 (sys_permission)
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| permission_name | VARCHAR(50) | 权限名称 |
| permission_code | VARCHAR(100) | 权限编码 |
| resource_type | VARCHAR(20) | 资源类型 |
| resource_url | VARCHAR(200) | 资源路径 |
| parent_id | BIGINT | 父级ID |
| icon | VARCHAR(50) | 图标 |
| sort_order | INT | 排序 |
| status | TINYINT | 状态 |
| create_time | DATETIME | 创建时间 |

### 角色权限关联表 (sys_role_permission)
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| role_id | BIGINT | 角色ID |
| permission_id | BIGINT | 权限ID |
| create_time | DATETIME | 创建时间 |

### 用户角色关联表 (sys_user_role)
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| role_id | BIGINT | 角色ID |
| create_time | DATETIME | 创建时间 |

---

## 权限缓存策略

系统使用Redis缓存用户权限，缓存策略如下：

1. **缓存键格式**：
   - 用户权限：`user:permissions:{userId}`
   - 用户角色：`user:roles:{userId}`
   - 角色权限：`role:permissions:{roleId}`

2. **缓存过期时间**：2小时

3. **缓存清除时机**：
   - 用户角色变更时清除用户相关缓存
   - 角色权限变更时清除角色相关缓存
   - 用户权限变更时清除用户权限缓存

---

## 版本历史

| 版本 | 日期 | 说明 |
|-----|------|------|
| 1.0 | 2024-01-01 | 初始版本 |
