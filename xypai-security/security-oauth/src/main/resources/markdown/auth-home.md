# 🔐 XY相遇派认证服务 API 文档

## 🎯 服务介绍

欢迎使用 **XY相遇派认证服务 API**！这是一个基于 Spring Security + OAuth2 构建的现代化认证微服务，提供安全可靠的用户认证和授权功能。

## 🚀 快速开始

### 健康检查

```http
GET /auth/health
```

### 用户登录

```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456",
  "auth_type": "password"
}
```

### 验证令牌

```http
GET /auth/verify?access_token=YOUR_ACCESS_TOKEN
```

### 刷新令牌

```http
POST /auth/refresh?refresh_token=YOUR_REFRESH_TOKEN&client_type=app
```

## 🔑 认证流程

1. **登录获取令牌** → 调用 `/auth/login` 接口
2. **使用令牌访问** → 在请求头或参数中携带 `access_token`
3. **令牌过期刷新** → 使用 `refresh_token` 调用 `/auth/refresh`
4. **安全登出** → 调用 `/auth/logout` 使令牌失效

## 🛠️ 技术架构

| 组件    | 技术选型                          | 版本    |
|-------|-------------------------------|-------|
| 框架    | Spring Boot + Spring Security | 3.x   |
| 认证协议  | OAuth2 + JWT                  | -     |
| 服务发现  | Nacos                         | 2.x   |
| 缓存    | Redis                         | 7.x   |
| API文档 | Knife4j                       | 4.4.0 |

## 📡 服务信息

- **服务名称**: security-oauth
- **服务端口**: 9401
- **注册中心**: Nacos (localhost:8848)
- **缓存**: Redis (localhost:6379)

## 🔗 访问方式

- **网关访问**: http://localhost:8080/auth/
- **直接访问**: http://localhost:9401/auth/
- **健康检查**: http://localhost:9401/auth/health
- **API文档**: http://localhost:9401/doc.html

## 🧪 测试账户

系统内置了以下测试账户：

| 用户名   | 密码     | 角色         | 说明           |
|-------|--------|------------|--------------|
| admin | 123456 | ADMIN,USER | 管理员账户，拥有所有权限 |
| user  | 123456 | USER       | 普通用户账户，基础权限  |

## 🔒 安全特性

- ✅ **CSRF 防护**: 已针对 REST API 优化
- ✅ **令牌管理**: 支持访问令牌和刷新令牌
- ✅ **权限控制**: 基于角色的访问控制
- ✅ **会话管理**: 无状态 JWT 令牌
- ✅ **接口保护**: 敏感接口需要认证

## 📝 API 分组说明

- **🔐 认证管理**: 登录、登出、令牌管理等核心认证功能
- **🚀 系统监控**: 健康检查和系统状态监控

## ⚠️ 重要提示

1. **令牌安全**: 请妥善保管 access_token，避免泄露
2. **生产环境**: 生产环境请修改默认密码和密钥
3. **HTTPS**: 生产环境建议使用 HTTPS 传输
4. **令牌过期**: Access Token 默认24小时过期，Refresh Token 7天过期

---

> 💬 如有问题，请联系开发团队：dev@xypai.com
