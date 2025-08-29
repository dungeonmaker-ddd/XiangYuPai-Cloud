# 👤 XY相遇派用户服务 API 文档

## 🎯 服务介绍

欢迎使用 **XY相遇派用户服务 API**！这是一个基于 Spring Boot 3.x 构建的现代化微服务，专门负责用户基础信息的管理。

## 🚀 快速开始

### 健康检查

```http
GET /users/health
```

### 创建用户

```http
POST /users
Content-Type: application/json

{
  "mobile": "13800138000",
  "username": "testuser",
  "nickname": "测试用户"
}
```

### 查询用户

```http
GET /users/{id}
GET /users/username/{username}
```

## 🛠️ 技术架构

| 组件    | 技术选型         | 版本    |
|-------|--------------|-------|
| 框架    | Spring Boot  | 3.x   |
| ORM   | MyBatis-Plus | 3.5.x |
| 数据库   | MySQL        | 8.0+  |
| 服务发现  | Nacos        | 2.x   |
| API文档 | Knife4j      | 4.4.0 |

## 📡 服务信息

- **服务名称**: user-service
- **服务端口**: 9106
- **注册中心**: Nacos (localhost:8848)
- **数据库**: xypai_user

## 🔗 相关链接

- **网关访问**: http://localhost:8080/users/
- **直接访问**: http://localhost:9106/users/
- **健康检查**: http://localhost:9106/users/health
- **API文档**: http://localhost:9106/doc.html

## 📝 API 分组说明

本文档按功能模块分为以下几个组：

- **👤 用户管理**: 用户的创建、查询、更新等基础操作
- **🚀 系统监控**: 健康检查和系统状态监控

## 💡 使用提示

1. **参数验证**: 所有接口都有完整的参数验证，请确保传入正确的参数格式
2. **错误处理**: 接口返回统一的错误格式，包含错误码和错误信息
3. **响应格式**: 所有接口都返回统一的 Result 格式
4. **测试数据**: 系统内置了测试用户数据，可直接用于接口测试

---

> 💬 如有问题，请联系开发团队：dev@xypai.com
