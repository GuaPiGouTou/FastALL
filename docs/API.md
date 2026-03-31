# FastCRUD 接口文档

## 基础信息

- **Base URL**: `http://localhost:8080`
- **认证方式**: Sa-Token (Header: `satoken` 或 `Authorization: Bearer {token}`)
- **响应格式**: JSON

## 通用响应结构

```json
{
  "code": 200,
  "data": {},
  "msg": "操作成功"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码，200成功，其他失败 |
| data | object | 返回数据 |
| msg | string | 提示信息 |

---

## 1. 认证模块 (Auth)

### 1.1 用户登录

**接口地址**: `POST /api/auth/login`

**请求参数**:
```json
{
  "username": "admin",
  "password": "123456",
  "captchaVerification": "验证码校验串"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |
| captchaVerification | string | 是 | 滑动验证码校验串 |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "tokenName": "satoken",
    "tokenValue": "xxxx-xxxx-xxxx",
    "tokenTimeout": 86400
  },
  "msg": "登录成功"
}
```

**错误码**:
| code | 说明 |
|------|------|
| 401 | 用户名或密码错误 |
| 401 | 验证码错误 |
| 429 | 账号已被锁定 |

---

### 1.2 用户注册

**接口地址**: `POST /api/auth/regit`

**请求参数**:
```json
{
  "username": "newuser",
  "password": "123456",
  "phone": "13800138000",
  "email": "user@example.com",
  "realName": "张三"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名(3-20字符) |
| password | string | 是 | 密码(至少6位) |
| phone | string | 是 | 手机号(11位) |
| email | string | 是 | 邮箱 |
| realName | string | 否 | 真实姓名 |

**响应示例**:
```json
{
  "code": 200,
  "data": null,
  "msg": "注册成功"
}
```

---

### 1.3 退出登录

**接口地址**: `POST /api/auth/logout`

**请求头**:
```
satoken: {tokenValue}
```

**响应示例**:
```json
{
  "code": 200,
  "data": null,
  "msg": "退出成功"
}
```

---

## 2. 文件管理模块 (File)

### 2.1 文件上传

**接口地址**: `POST /api/file/upload`

**请求方式**: multipart/form-data

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 上传的文件 |

**响应示例**:
```json
{
  "code": 200,
  "data": "http://localhost:9000/ecmo-data/xxx.pdf",
  "msg": "上传成功"
}
```

---

### 2.2 文件列表

**接口地址**: `GET /api/file/list`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认10 |
| fileName | string | 否 | 文件名(模糊查询) |
| fileExt | string | 否 | 文件扩展名 |
| beginTime | datetime | 否 | 开始时间 |
| endTime | datetime | 否 | 结束时间 |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "fileName": "document.pdf",
        "fileExt": "pdf",
        "fileSize": 102400,
        "filePath": "http://localhost:9000/ecmo-data/xxx.pdf",
        "fileMd5": "abc123...",
        "createTime": "2026-03-24 10:00:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1
  },
  "msg": "查询成功"
}
```

---

### 2.3 文件下载

**接口地址**: `GET /api/file/download/{id}`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 文件ID |

**响应**: 文件流下载

---

### 2.4 删除文件

**接口地址**: `DELETE /api/file/delete/{id}`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 文件ID |

**响应示例**:
```json
{
  "code": 200,
  "data": null,
  "msg": "删除成功"
}
```

---

### 2.5 更新文件元数据

**接口地址**: `PUT /api/file/update`

**请求参数**:
```json
{
  "id": 1,
  "fileName": "new_name.pdf"
}
```

**响应示例**:
```json
{
  "code": 200,
  "data": null,
  "msg": "更新成功"
}
```

---

## 3. 分片上传模块 (FileChunk)

### 3.1 检查分片进度

**接口地址**: `GET /api/file/chunk`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| identifier | string | 是 | 文件MD5标识 |

**响应示例**:
```json
{
  "code": 200,
  "data": [1, 2, 3],
  "msg": "查询成功"
}
```

---

### 3.2 上传分片

**接口地址**: `POST /api/file/chunk`

**请求方式**: multipart/form-data

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 分片文件 |
| identifier | string | 是 | 文件MD5标识 |
| chunkNumber | int | 是 | 分片序号 |
| totalChunks | int | 是 | 总分片数 |
| totalSize | long | 是 | 文件总大小 |

**响应示例**:
```json
{
  "code": 200,
  "data": "success",
  "msg": "分片处理成功"
}
```

---

### 3.3 合并分片

**接口地址**: `POST /api/file/chunk/merge`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| identifier | string | 是 | 文件MD5标识 |
| fileName | string | 是 | 原始文件名 |

**响应示例**:
```json
{
  "code": 200,
  "data": "http://localhost:9000/ecmo-data/xxx.pdf",
  "msg": "文件合并并入库成功"
}
```

---

## 4. 日志模块 (Log)

### 4.1 登录日志列表

**接口地址**: `GET /api/syslog/login/pagelist`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | int | 否 | 当前页，默认1 |
| size | int | 否 | 每页条数，默认10 |
| username | string | 否 | 用户名 |
| status | string | 否 | 状态(0成功/1失败) |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "username": "admin",
        "ipAddress": "127.0.0.1",
        "loginLocation": "本地",
        "browser": "Chrome",
        "os": "Windows 10",
        "status": 0,
        "msg": "登录成功",
        "loginTime": "2026-03-24 10:00:00"
      }
    ],
    "total": 100
  },
  "msg": "查询成功"
}
```

---

### 4.2 操作日志列表

**接口地址**: `GET /api/syslog/oper/pagelist`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | int | 否 | 当前页，默认1 |
| size | int | 否 | 每页条数，默认10 |
| title | string | 否 | 操作标题 |
| status | string | 否 | 状态 |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "title": "文件上传",
        "businessType": 0,
        "method": "upload",
        "requestMethod": "POST",
        "operatorName": "admin",
        "operUrl": "/api/file/upload",
        "operIp": "127.0.0.1",
        "operTime": "2026-03-24 10:00:00",
        "status": 0
      }
    ],
    "total": 100
  },
  "msg": "查询成功"
}
```

---

## 5. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 401 | 未授权/认证失败 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 429 | 请求过于频繁/账号锁定 |
| 500 | 服务器内部错误 |

---

## 6. 请求示例

### cURL 示例

```bash
# 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456","captchaVerification":"xxx"}'

# 获取文件列表
curl -X GET "http://localhost:8080/api/file/list?pageNum=1&pageSize=10" \
  -H "satoken: your-token-value"

# 上传文件
curl -X POST http://localhost:8080/api/file/upload \
  -H "satoken: your-token-value" \
  -F "file=@/path/to/file.pdf"
```

### JavaScript (Axios) 示例

```javascript
import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: { 'satoken': localStorage.getItem('token') }
})

// 登录
const login = async (data) => {
  const res = await api.post('/api/auth/login', data)
  return res.data
}

// 获取文件列表
const getFileList = async (params) => {
  const res = await api.get('/api/file/list', { params })
  return res.data
}

// 上传文件
const uploadFile = async (file) => {
  const formData = new FormData()
  formData.append('file', file)
  const res = await api.post('/api/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  return res.data
}
```
