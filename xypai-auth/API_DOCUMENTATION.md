# 📚 XyPai认证中心 API文档总览

## 🌟 服务架构概述

XyPai认证中心采用微服务架构，包含3个核心服务，每个服务都提供完整的Swagger Knife4j文档：

### 🎯 服务端口分配

| 🏷️ 服务名称 | 🔌 端口 | 🚀 网关路径                | 📖 文档地址                                                          |
|----------|-------|------------------------|------------------------------------------------------------------|
| APP认证服务  | 8100  | `/api/app/auth/**`     | [http://localhost:8100/doc.html](http://localhost:8100/doc.html) |
| 管理端服务    | 8101  | `/api/admin/**`        | [http://localhost:8101/doc.html](http://localhost:8101/doc.html) |
| APP业务服务  | 8102  | `/api/app/business/**` | [http://localhost:8102/doc.html](http://localhost:8102/doc.html) |

---

## 🎯 APP认证服务 (端口: 8100)

### 📋 服务职责

专为移动端提供认证功能，包括用户登录、Token管理、会话控制等核心认证服务。

### 🔗 访问地址

- **直接访问**: [http://localhost:8100/doc.html](http://localhost:8100/doc.html)
- **网关访问**: [http://localhost:8080/api/app/auth/doc.html](http://localhost:8080/api/app/auth/doc.html)

### 🎯 核心接口

#### 🔐 统一认证接口

```http
POST /auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456",
  "clientType": "APP",
  "deviceId": "iPhone_12_Pro_Max_001",
  "deviceInfo": "iPhone 12 Pro Max, iOS 15.0"
}
```

#### 📱 短信验证码登录

```http
POST /auth/login/sms
Content-Type: application/json

{
  "mobile": "13800138000",
  "verifyCode": "123456",
  "clientType": "APP"
}
```

#### 📱 发送短信验证码

```http
POST /auth/sms/send
Content-Type: application/json

{
  "mobile": "13800138000",
  "type": "LOGIN"
}
```

#### 🔄 刷新Token

```http
POST /auth/refresh
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 🚪 退出登录

```http
DELETE /auth/logout
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 🏛️ 管理端服务 (端口: 8101)

### 📋 服务职责

为后台管理系统提供完整的管理功能，包括管理员认证、用户管理、权限管理、系统配置等。

### 🔗 访问地址

- **直接访问**: [http://localhost:8101/doc.html](http://localhost:8101/doc.html)
- **网关访问**: [http://localhost:8080/api/admin/doc.html](http://localhost:8080/api/admin/doc.html)

### 🎯 核心接口

#### 🏛️ 管理员登录

```http
POST /admin/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123",
  "clientType": "ADMIN"
}
```

#### 👥 用户管理

```http
GET /admin/users
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 🔒 权限管理

```http
GET /admin/permissions
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### ⚙️ 系统配置

```http
GET /admin/config
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 📊 数据统计

```http
GET /admin/statistics
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 📱 APP业务服务 (端口: 8102)

### 📋 服务职责

为移动端提供业务功能，包括个人资料管理、设备管理、推送设置等用户相关业务服务。

### 🔗 访问地址

- **直接访问**: [http://localhost:8102/doc.html](http://localhost:8102/doc.html)
- **网关访问**: [http://localhost:8080/api/app/business/doc.html](http://localhost:8080/api/app/business/doc.html)

### 🎯 核心接口

#### 👤 获取个人资料

```http
GET /app/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 👤 更新个人资料

```http
PUT /app/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "nickname": "新昵称",
  "avatar": "https://example.com/new-avatar.jpg",
  "bio": "个人简介"
}
```

#### 📱 设备管理

```http
GET /app/devices
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 🔔 推送设置

```http
GET /app/push-settings
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### ⚙️ APP配置

```http
GET /app/config
```

---

## 🔧 Knife4j功能特性

### 🌟 UI界面特性

- **中文界面**: 完整的中文本地化
- **接口分组**: 按功能模块分组展示
- **在线调试**: 直接在浏览器中测试API
- **示例代码**: 自动生成多种语言的调用示例

### 📖 文档特性

- **详细描述**: 每个接口都有完整的功能说明
- **参数说明**: 详细的请求参数和响应格式
- **示例数据**: 真实的请求和响应示例
- **错误码说明**: 完整的错误处理指南

### 🎯 开发者工具

- **参数验证**: 自动验证请求参数格式
- **Mock数据**: 支持Mock数据生成
- **导出功能**: 支持导出OpenAPI规范文档
- **版本管理**: 支持API版本控制

---

## 🚀 快速开始

### 1️⃣ 启动服务

```bash
# 启动APP认证服务
cd xypai-auth-app-auth
mvn spring-boot:run

# 启动管理端服务  
cd xypai-auth-admin
mvn spring-boot:run

# 启动APP业务服务
cd xypai-auth-app
mvn spring-boot:run
```

### 2️⃣ 访问文档

服务启动成功后，访问对应的文档地址即可查看完整的API文档。

### 3️⃣ 测试接口

1. 在Knife4j界面中选择要测试的接口
2. 填写请求参数（可以使用提供的示例）
3. 点击"执行"按钮进行测试
4. 查看响应结果和状态码

---

## 📋 注意事项

### 🔐 认证说明

- APP业务服务的接口需要先通过APP认证服务获取Token
- 管理端接口需要管理员权限和有效的管理令牌
- Token在请求头中以 `Authorization: Bearer <token>` 格式传递

### 🌐 CORS配置

- 开发环境已配置允许跨域请求
- 生产环境需要根据实际域名配置CORS策略

### 📊 监控地址

- APP认证服务监控: [http://localhost:8100/actuator](http://localhost:8100/actuator)
- 管理端服务监控: [http://localhost:8101/actuator](http://localhost:8101/actuator)
- APP业务服务监控: [http://localhost:8102/actuator](http://localhost:8102/actuator)

---

## 🔗 相关链接

- [Knife4j官方文档](https://doc.xiaominfo.com/)
- [SpringDoc OpenAPI](https://springdoc.org/)
- [OpenAPI规范](https://swagger.io/specification/)

---

*最后更新时间: 2024年当前时间*  
*文档版本: v4.0.0*
