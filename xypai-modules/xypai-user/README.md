# 📱 XyPai-User 微服务

> APP用户管理微服务 - 专门处理APP端用户注册、登录、信息管理等功能

## 🎯 模块概述

### 📋 功能特性

- ✅ **手机号注册** - 支持手机号快速注册
- ✅ **用户信息管理** - 昵称、头像、性别、生日等
- ✅ **多端支持** - web/app/mini 客户端类型区分
- ✅ **状态管理** - 用户启用/禁用状态控制
- ✅ **分页查询** - 支持多条件组合查询
- ✅ **统计分析** - 用户数量、客户端分布统计

### 🏗️ 架构设计

```
xypai-user/
├── controller/          # 控制器层
│   └── AppUserController.java
├── service/            # 服务层
│   ├── AppUserService.java
│   └── impl/
│       └── AppUserServiceImpl.java
├── mapper/             # 数据访问层
│   └── AppUserMapper.java
├── domain/             # 领域模型
│   ├── entity/
│   │   └── AppUser.java
│   └── record/         # DTO记录类
│       ├── AppUserRegisterRequest.java
│       ├── AppUserResponse.java
│       ├── AppUserUpdateRequest.java
│       └── AppUserQueryRequest.java
├── converter/          # 转换器
│   └── AppUserConverter.java
└── sql/               # SQL脚本
    └── app_user.sql
```

## 📊 数据库设计

### 🗃️ 表结构：app_user

| 字段名             | 类型       | 长度  | 约束                 | 默认值               | 描述               |
|-----------------|----------|-----|--------------------|-------------------|------------------|
| user_id         | BIGINT   | -   | PK, AUTO_INCREMENT | -                 | 用户ID             |
| mobile          | VARCHAR  | 11  | NOT NULL, UNIQUE   | -                 | 手机号              |
| username        | VARCHAR  | 30  | UNIQUE             | NULL              | 用户名(可选)          |
| nickname        | VARCHAR  | 30  | NOT NULL           | -                 | 昵称               |
| avatar          | VARCHAR  | 200 | -                  | NULL              | 头像URL            |
| gender          | TINYINT  | -   | -                  | 0                 | 性别(0-未知 1-男 2-女) |
| birth_date      | DATE     | -   | -                  | NULL              | 生日               |
| status          | TINYINT  | -   | -                  | 1                 | 状态(1-正常 0-禁用)    |
| register_time   | DATETIME | -   | -                  | CURRENT_TIMESTAMP | 注册时间             |
| last_login_time | DATETIME | -   | -                  | NULL              | 最后登录时间           |
| client_type     | VARCHAR  | 10  | -                  | 'app'             | 客户端类型            |

### 🔍 索引设计

| 索引名               | 类型 | 字段            | 用途      |
|-------------------|----|---------------|---------|
| PRIMARY           | 主键 | user_id       | 主键索引    |
| uk_mobile         | 唯一 | mobile        | 手机号唯一约束 |
| uk_username       | 唯一 | username      | 用户名唯一约束 |
| idx_status        | 普通 | status        | 状态查询优化  |
| idx_client_type   | 普通 | client_type   | 客户端类型统计 |
| idx_register_time | 普通 | register_time | 时间范围查询  |

## 🔧 API 接口

### 📱 用户管理

| 方法   | 路径                        | 描述        | 请求体                    |
|------|---------------------------|-----------|------------------------|
| POST | `/users/register`         | 用户注册      | AppUserRegisterRequest |
| GET  | `/users/profile/{mobile}` | 按手机号获取用户  | -                      |
| GET  | `/users/profile/id/{id}`  | 按用户ID获取用户 | -                      |
| PUT  | `/users/profile`          | 更新用户信息    | AppUserUpdateRequest   |

### 🔍 查询接口

| 方法   | 路径                          | 描述       | 参数                  |
|------|-----------------------------|----------|---------------------|
| POST | `/users/page`               | 分页查询用户   | AppUserQueryRequest |
| GET  | `/users/status/{status}`    | 按状态查询用户  | status              |
| GET  | `/users/client-type/{type}` | 按客户端类型查询 | clientType          |

### 🛡️ 管理接口

| 方法  | 路径                       | 描述     | 参数 |
|-----|--------------------------|--------|----|
| PUT | `/users/{id}/enable`     | 启用用户   | id |
| PUT | `/users/{id}/disable`    | 禁用用户   | id |
| PUT | `/users/{id}/last-login` | 更新登录时间 | id |

### 📊 统计接口

| 方法  | 路径                         | 描述      | 响应                            |
|-----|----------------------------|---------|-------------------------------|
| GET | `/users/stats`             | 用户统计信息  | AppUserStatsResponse          |
| GET | `/users/stats/client-type` | 客户端类型统计 | List<ClientTypeStatsResponse> |

### ✅ 验证接口

| 方法  | 路径                      | 描述       | 参数                  |
|-----|-------------------------|----------|---------------------|
| GET | `/users/check/mobile`   | 检查手机号可用性 | mobile              |
| GET | `/users/check/username` | 检查用户名可用性 | username, excludeId |

## 📝 DTO 设计

### 🔹 AppUserRegisterRequest（注册请求）

```java
public record AppUserRegisterRequest(
    @NotBlank @Pattern(regexp = "^1[3-9]\\d{9}$") String mobile,
    @Size(max = 30) String username,
    @NotBlank @Size(min = 1, max = 30) String nickname,
    @Size(max = 200) String avatar,
    @Min(0) @Max(2) Integer gender,
    LocalDate birthDate,
    @NotBlank @Pattern(regexp = "^(web|app|mini)$") String clientType
) {}
```

### 🔹 AppUserResponse（响应对象）

```java
public record AppUserResponse(
    Long userId,
    String mobile,
    String username,
    String nickname,
    String avatar,
    Integer gender,
    LocalDate birthDate,
    Integer status,
    LocalDateTime registerTime,
    LocalDateTime lastLoginTime,
    String clientType
) {}
```

### 🔹 AppUserUpdateRequest（更新请求）

```java
public record AppUserUpdateRequest(
    @NotNull Long userId,
    @Size(max = 30) String username,
    @Size(max = 30) String nickname,
    @Size(max = 200) String avatar,
    @Min(0) @Max(2) Integer gender,
    LocalDate birthDate
) {}
```

### 🔹 AppUserQueryRequest（查询请求）

```java
public record AppUserQueryRequest(
    @Min(1) Integer pageNum,
    @Min(1) @Max(100) Integer pageSize,
    @Pattern(regexp = "^1[3-9]\\d{9}$") String mobile,
    @Size(max = 30) String username,
    @Size(max = 30) String nickname,
    @Min(0) @Max(2) Integer gender,
    @Min(0) @Max(1) Integer status,
    LocalDate birthStartDate,
    LocalDate birthEndDate,
    @Pattern(regexp = "^(web|app|mini)$") String clientType
) {}
```

## 🚀 快速开始

### 📋 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 🔧 配置步骤

1. **创建数据库表**
   ```bash
   mysql -u root -p < sql/app_user.sql
   ```

2. **配置数据库连接**
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/xypai_user
       username: root
       password: your_password
   ```

3. **启动服务**
   ```bash
   mvn spring-boot:run
   ```

4. **访问接口文档**
   ```
   http://localhost:port/doc.html
   ```

## 📈 业务规则

### 🔑 核心规则

- ✅ **手机号唯一性** - 每个手机号只能注册一次
- ✅ **用户名唯一性** - 用户名如果设置必须唯一
- ✅ **昵称必填** - 注册时必须提供昵称
- ✅ **客户端类型限制** - 只允许 web/app/mini
- ✅ **状态控制** - 禁用用户无法登录使用

### 📱 客户端类型说明

- **app**: 移动APP端用户
- **web**: 网页端用户
- **mini**: 小程序端用户

### 🛡️ 数据验证

- 手机号格式：`^1[3-9]\\d{9}$`
- 性别值：0(未知) / 1(男) / 2(女)
- 状态值：0(禁用) / 1(正常)
- 生日限制：不能是未来日期

## 🏗️ 数据库架构

### 🎯 **独立数据库设计**

APP用户微服务使用**独立的数据库**，与管理端系统完全分离：

```
📊 数据库架构
├── xypai_system (管理端)
│   ├── sys_user
│   ├── sys_role
│   ├── sys_menu
│   └── sys_dept
│
└── xypai_user (APP端) ⭐ 独立数据库
    └── app_user
```

### 🔑 **独立数据库的优势**

- ✅ **数据隔离** - APP用户数据与管理数据完全分离
- ✅ **性能独立** - 不会因管理端操作影响APP性能
- ✅ **扩展性强** - 可以独立扩容和优化
- ✅ **故障隔离** - 数据库故障不会影响其他服务
- ✅ **技术选型自由** - 可以选择最适合的数据库方案

## 🔄 与其他服务的关系

### 🔗 服务依赖

- **xypai-gateway**: 网关路由分发
- **xypai-auth**: 认证授权服务
- **xypai-common**: 公共组件依赖

### 📊 数据流向

```
客户端请求 → Gateway → xypai-user → xypai_user Database
              ↓
         xypai-auth (认证) → xypai_system Database
```

## 🎯 设计原则

### 📋 遵循规则

- ✅ **Records优先** - 所有DTO使用Record类
- ✅ **Fail Fast** - 参数验证在方法入口
- ✅ **业务实用导向** - 避免过度工程化
- ✅ **纳米级并发** - 适合小规模并发场景
- ✅ **代码简洁** - 单一职责，清晰命名

### 🔧 技术选型

- **MyBatis-Plus** - 数据访问层
- **Bean Validation** - 参数验证
- **Lombok** - 代码简化
- **Swagger** - API文档

## 📚 开发指南

### 🔹 添加新接口

1. 在 `AppUserController` 中添加方法
2. 在 `AppUserService` 中添加接口定义
3. 在 `AppUserServiceImpl` 中实现业务逻辑
4. 如需要，在 `AppUserMapper` 中添加SQL

### 🔹 添加新字段

1. 修改 `AppUser` 实体类
2. 更新 `sql/app_user.sql` 脚本
3. 修改相关的 Record 类
4. 更新 `AppUserConverter` 转换逻辑

### 🔹 性能优化

1. 合理使用索引（已预设常用索引）
2. 避免 N+1 查询问题
3. 使用分页查询处理大数据量
4. 缓存热点数据（如需要）

## 📞 联系方式

- **开发团队**: XyPai Team
- **技术支持**: tech@xypai.com
- **项目地址**: https://github.com/xypai/xypai-cloud

---

> 🎯 **设计理念**: 简洁、实用、可扩展 - 专注于APP用户核心功能，避免过度设计
