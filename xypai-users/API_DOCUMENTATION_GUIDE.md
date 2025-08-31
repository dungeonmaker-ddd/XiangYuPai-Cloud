# 📚 XY相遇派用户微服务 API 文档使用指南

## 🚀 快速启动

### 1. 启动服务

```bash
# 进入用户微服务目录
cd xypai-users

# 编译打包
mvn clean compile

# 启动服务
mvn spring-boot:run
```

### 2. 访问 API 文档

服务启动成功后，访问以下地址：

#### 🔪 Knife4j 文档（推荐）

```
http://localhost:9201/xypai-user/doc.html
```

#### 📖 Swagger UI 文档

```
http://localhost:9201/xypai-user/swagger-ui.html
```

#### 🔗 OpenAPI JSON

```
http://localhost:9201/xypai-user/v3/api-docs
```

## 📋 API 分组说明

### 1. 默认接口组

- **路径**: `/api/v1/user/**`
- **说明**: 基础用户接口，包括注册、查询、更新等常用功能
- **权限**: 需要基础用户权限

### 2. 用户管理接口组

- **路径**: `/api/v1/user/admin/**`
- **说明**: 管理员专用接口，包括高级管理功能
- **权限**: 需要管理员权限

### 3. Feign内部接口组

- **路径**: `/api/feign/user/**`
- **说明**: 微服务间内部调用接口
- **权限**: 需要内部服务认证

## 🔐 认证配置

### JWT 认证

在 Knife4j 界面右上角点击 "Authorize" 按钮，输入：

```
Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Basic 认证（开发环境）

如果启用了 Basic 认证，使用以下凭据：

- **用户名**: `xypai`
- **密码**: `dev2025`

## 📝 API 接口分类

### 🚀 系统监控

- `GET /api/v1/user/health` - 服务健康检查

### 📝 用户注册与创建

- `POST /api/v1/user/register` - 用户注册

### 🔍 用户查询

- `GET /api/v1/user/{id}` - 根据ID查询用户
- `GET /api/v1/user/mobile/{mobile}` - 根据手机号查询用户
- `GET /api/v1/user/username/{username}` - 根据用户名查询用户
- `GET /api/v1/user/page` - 分页查询用户列表
- `GET /api/v1/user/vip` - 查询VIP用户列表
- `GET /api/v1/user/active` - 查询活跃用户列表
- `GET /api/v1/user/new` - 查询新用户列表

### ✏️ 用户信息更新

- `PUT /api/v1/user` - 更新用户信息

### 🔄 用户状态管理

- `PUT /api/v1/user/status` - 批量更新用户状态
- `PUT /api/v1/user/{id}/upgrade` - 用户升级VIP
- `PUT /api/v1/user/{id}/verify` - 用户实名认证

### 🗑️ 用户删除

- `DELETE /api/v1/user/{ids}` - 删除用户

### 📊 统计查询

- `GET /api/v1/user/stats/total` - 用户总数统计
- `GET /api/v1/user/stats/type` - 用户类型分布
- `GET /api/v1/user/stats/platform` - 平台分布统计
- `GET /api/v1/user/stats/channel` - 注册渠道统计
- `GET /api/v1/user/stats/location` - 地区分布统计
- `GET /api/v1/user/stats/activity` - 用户活跃度统计
- `GET /api/v1/user/stats/trend` - 用户注册趋势

### ✅ 验证接口

- `GET /api/v1/user/check/mobile` - 检查手机号
- `GET /api/v1/user/check/username` - 检查用户名
- `GET /api/v1/user/check/email` - 检查邮箱
- `GET /api/v1/user/check/code` - 检查用户编码

### 🔐 管理员接口

- `PUT /api/v1/user/admin/{userId}/reset-password` - 强制重置密码
- `PUT /api/v1/user/admin/{userId}/ban` - 封禁用户
- `PUT /api/v1/user/admin/{userId}/unban` - 解封用户
- `DELETE /api/v1/user/admin/batch` - 批量删除用户
- `GET /api/v1/user/admin/cleanup/stats` - 数据清理统计
- `DELETE /api/v1/user/admin/cleanup/zombie` - 清理僵尸用户
- `GET /api/v1/user/admin/export` - 用户数据导出
- `PUT /api/v1/user/admin/config` - 系统配置更新
- `GET /api/v1/user/admin/config` - 获取系统配置
- `GET /api/v1/user/admin/{userId}/logs` - 用户行为日志

### 🔗 Feign 内部接口

- `GET /api/feign/user/info/{userId}` - 根据ID查询用户(内部)
- `GET /api/feign/user/username/{username}` - 根据用户名查询用户(内部)
- `GET /api/feign/user/mobile/{mobile}` - 根据手机号查询用户(内部)
- `POST /api/feign/user/batch` - 批量查询用户信息(内部)
- `GET /api/feign/user/exists/{userId}` - 验证用户是否存在(内部)
- `GET /api/feign/user/basic/{userId}` - 获取用户基础信息(内部)
- `GET /api/feign/user/status/{userId}` - 验证用户状态(内部)

## 📊 数据模型说明

### 📝 请求 DTO

#### UserAddDTO - 用户创建请求

```json
{
  "mobile": "13900000001",
  "username": "xypai_user001",
  "nickname": "XY用户",
  "email": "user@xypai.com",
  "gender": 1,
  "location": "北京市",
  "platform": "iOS",
  "sourceChannel": "app_store",
  "bio": "XY相遇派新用户",
  "deptId": 100
}
```

#### UserQueryDTO - 用户查询请求

```json
{
  "userId": 100000,
  "userCode": "XY202501020001",
  "mobile": "139****0001",
  "username": "xypai_user",
  "nickname": "XY用户",
  "gender": 1,
  "status": 1,
  "userType": 1,
  "isVerified": 1,
  "platform": "iOS",
  "createTimeStart": "2025-01-01T00:00:00",
  "createTimeEnd": "2025-01-31T23:59:59"
}
```

### 📤 响应 VO

#### UserDetailVO - 用户详情响应

```json
{
  "userId": 100000,
  "userCode": "XY202501020001",
  "mobile": "139****0001",
  "username": "xypai_user001",
  "nickname": "XY用户",
  "email": "u***@xypai.com",
  "realName": "*明",
  "gender": 1,
  "genderDesc": "男",
  "avatarUrl": "https://cdn.xypai.com/avatar/default.jpg",
  "location": "北京市",
  "bio": "XY相遇派用户",
  "status": 1,
  "statusDesc": "正常",
  "userType": 1,
  "userTypeDesc": "VIP用户",
  "isVerified": 1,
  "verifiedDesc": "已认证",
  "platform": "iOS",
  "userLevel": 5,
  "userPoints": 1500,
  "balance": 10000,
  "loginCount": 25,
  "lastLoginTime": "2025-01-02T10:30:00",
  "createTime": "2024-12-01T09:00:00",
  "updateTime": "2025-01-02T10:30:00",
  "version": 1
}
```

## 🔧 配置说明

### 环境配置

在不同环境中，Knife4j 的行为会有所不同：

#### 开发环境 (dev)

- 文档完全开放
- 支持在线调试
- 显示所有接口

#### 测试环境 (test)

- 启用 Basic 认证
- 支持在线调试
- 显示所有接口

#### 生产环境 (prod)

- 完全禁用文档
- 无法访问接口文档

### 自定义配置

可以通过 `application.yml` 自定义文档配置：

```yaml
swagger:
  enabled: true
  title: "自定义API标题"
  description: "自定义API描述"
  knife4j:
    basic:
      enable: true
      username: "admin"
      password: "custom123"
```

## 🛠️ 常见问题

### Q1: 文档页面无法访问？

**A**: 检查以下几点：

1. 服务是否正常启动（端口9201）
2. 配置中 `swagger.enabled` 是否为 `true`
3. 当前环境是否为生产环境

### Q2: 接口调用返回401错误？

**A**: 需要先进行认证：

1. 点击页面右上角 "Authorize" 按钮
2. 输入有效的 JWT Token
3. 格式：`Bearer {your-token}`

### Q3: 某些接口不显示？

**A**: 可能的原因：

1. 权限不足，接口被隐藏
2. 包扫描路径配置错误
3. 接口上添加了 `@Hidden` 注解

### Q4: 如何生成测试 Token？

**A**: 可以通过以下方式：

1. 使用认证接口登录获取
2. 联系开发团队获取测试 Token
3. 使用 JWT 工具生成（需要密钥）

### Q5: 接口参数验证失败？

**A**: 请检查：

1. 必填字段是否已填写
2. 数据格式是否正确（邮箱、手机号等）
3. 字段长度是否超出限制
4. 枚举值是否在允许范围内

## 📞 技术支持

如有问题，请联系：

- 📧 **邮箱**: user-team@xypai.com
- 🐛 **Issue**: https://github.com/xypai/xypai-users/issues
- 📖 **Wiki**: https://wiki.xypai.com/user-service
- 💬 **企业微信**: XyPai-用户服务技术支持群

---

**XyPai 用户微服务团队** © 2025
