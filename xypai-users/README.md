# 🏗️ XY相遇派用户微服务 - 企业架构实现

> **基于企业微服务架构规范的用户管理微服务**
>
> 集成 Spring Boot 3.x + MyBatis Plus + Redis + Nacos + 企业级通用模块

## 📋 项目概述

XY相遇派用户微服务是基于企业微服务架构设计规范构建的独立微服务，提供完整的用户管理功能。该微服务严格遵循企业级开发规范，集成了安全框架、缓存框架、日志框架等通用模块。

### ✨ 核心特性

- 🏗️ **企业架构设计**: 严格遵循企业微服务架构规范
- 🔐 **安全框架集成**: 内置权限控制、数据权限、敏感数据脱敏
- 💾 **缓存优化**: Redis缓存支持，提升查询性能
- 📝 **操作日志**: 完整的操作审计和日志记录
- 🎯 **数据权限**: 基于部门的数据权限控制
- 📊 **丰富统计**: 多维度用户数据统计分析
- 🔄 **分布式事务**: Seata分布式事务支持
- 📚 **API文档**: 完整的OpenAPI 3.0文档

## 🏛️ 架构设计

### 技术栈

| 技术栈          | 版本     | 说明        |
|--------------|--------|-----------|
| Spring Boot  | 3.x    | 微服务基础框架   |
| MyBatis Plus | 3.x    | ORM框架     |
| Spring Cloud | 2023.x | 微服务治理     |
| Nacos        | 2.x    | 服务发现与配置中心 |
| Redis        | 7.x    | 缓存和会话存储   |
| MySQL        | 8.x    | 主数据库      |
| Seata        | 1.x    | 分布式事务     |

### 模块依赖

```
xypai-user-microservice
├── xypai-common-core        # 核心工具和基础类
├── xypai-common-security    # 安全框架
├── xypai-common-redis       # Redis缓存
├── xypai-common-log         # 日志框架
├── xypai-common-datascope   # 数据权限
├── xypai-common-datasource  # 多数据源
├── xypai-common-seata       # 分布式事务
├── xypai-common-swagger     # API文档
└── xypai-common-sensitive   # 敏感数据脱敏
```

## 📁 项目结构

```
xypai-user-microservice/
├── src/main/java/com/xypai/user/
│   ├── controller/             # 🎛️ 控制器层
│   │   └── UserController.java
│   ├── service/               # 🔧 业务服务层
│   │   ├── IUserService.java
│   │   └── impl/
│   │       └── UserServiceImpl.java
│   ├── mapper/                # 🗄️ 数据访问层
│   │   └── UserMapper.java
│   ├── domain/                # 📋 领域模型
│   │   ├── entity/            # 实体类
│   │   │   └── User.java
│   │   ├── dto/               # 数据传输对象
│   │   │   ├── UserAddDTO.java
│   │   │   ├── UserUpdateDTO.java
│   │   │   └── UserQueryDTO.java
│   │   └── vo/                # 视图对象
│   │       ├── UserDetailVO.java
│   │       └── UserListVO.java
│   ├── enums/                 # 📝 枚举定义
│   │   ├── UserStatus.java
│   │   ├── UserType.java
│   │   └── Gender.java
│   ├── constant/              # 📋 常量定义
│   │   └── UserConstants.java
│   └── UserMicroserviceApplication.java
├── src/main/resources/
│   ├── mapper/                # MyBatis XML映射
│   │   └── UserMapper.xml
│   ├── sql/                   # 数据库脚本
│   │   └── user_microservice_tables.sql
│   ├── application.yml        # 主配置文件
│   ├── application-dev.yml    # 开发环境配置
│   ├── application-prod.yml   # 生产环境配置
│   ├── bootstrap.yml          # 引导配置
│   └── logback-spring.xml     # 日志配置
├── pom.xml                    # Maven配置
└── README.md                  # 项目文档
```

## 🚀 快速开始

### 1. 环境准备

确保以下环境已安装：

- ☕ JDK 17+
- 🛠️ Maven 3.8+
- 🗄️ MySQL 8.0+
- 💾 Redis 7.0+
- 🔧 Nacos 2.0+

### 2. 数据库初始化

```bash
# 执行数据库脚本
mysql -u root -p < src/main/resources/sql/user_microservice_tables.sql
```

### 3. 配置修改

修改 `application-dev.yml` 中的配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xypai_user_dev
    username: your_username
    password: your_password
  
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
  
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
      config:
        server-addr: localhost:8848
```

### 4. 启动服务

```bash
# 编译项目
mvn clean compile

# 启动服务
mvn spring-boot:run
```

### 5. 验证服务

访问以下地址验证服务：

- 🏥 健康检查: http://localhost:9201/xypai-user/api/v1/user/health
- 📚 API文档: http://localhost:9201/xypai-user/swagger-ui.html
- 📊 监控端点: http://localhost:9201/xypai-user/actuator/health

## 📊 API 接口

### 核心接口

| 接口   | 方法   | 路径                          | 说明     |
|------|------|-----------------------------|--------|
| 用户注册 | POST | `/api/v1/user/register`     | 注册新用户  |
| 用户详情 | GET  | `/api/v1/user/{id}`         | 获取用户详情 |
| 用户列表 | GET  | `/api/v1/user/page`         | 分页查询用户 |
| 更新用户 | PUT  | `/api/v1/user`              | 更新用户信息 |
| 用户状态 | PUT  | `/api/v1/user/status`       | 批量更新状态 |
| 用户升级 | PUT  | `/api/v1/user/{id}/upgrade` | 升级用户类型 |
| 实名认证 | PUT  | `/api/v1/user/{id}/verify`  | 用户实名认证 |

### 统计接口

| 接口    | 方法  | 路径                            | 说明      |
|-------|-----|-------------------------------|---------|
| 总数统计  | GET | `/api/v1/user/stats/total`    | 用户总数统计  |
| 类型分布  | GET | `/api/v1/user/stats/type`     | 用户类型分布  |
| 平台分布  | GET | `/api/v1/user/stats/platform` | 平台分布统计  |
| 渠道统计  | GET | `/api/v1/user/stats/channel`  | 注册渠道统计  |
| 地区分布  | GET | `/api/v1/user/stats/location` | 地区分布统计  |
| 活跃度统计 | GET | `/api/v1/user/stats/activity` | 用户活跃度统计 |

### 验证接口

| 接口     | 方法  | 路径                            | 说明         |
|--------|-----|-------------------------------|------------|
| 检查手机号  | GET | `/api/v1/user/check/mobile`   | 验证手机号是否存在  |
| 检查用户名  | GET | `/api/v1/user/check/username` | 验证用户名是否存在  |
| 检查邮箱   | GET | `/api/v1/user/check/email`    | 验证邮箱是否存在   |
| 检查用户编码 | GET | `/api/v1/user/check/code`     | 验证用户编码是否存在 |

## 🔐 权限控制

### 权限列表

| 权限码            | 说明       |
|----------------|----------|
| `user:query`   | 查询用户权限   |
| `user:add`     | 添加用户权限   |
| `user:edit`    | 编辑用户权限   |
| `user:remove`  | 删除用户权限   |
| `user:status`  | 用户状态管理权限 |
| `user:upgrade` | 用户升级权限   |
| `user:verify`  | 用户实名认证权限 |

### 数据权限

支持基于部门的数据权限控制：

- 使用 `@DataScope` 注解自动过滤数据
- 支持部门级别的数据隔离
- 管理员可查看所有数据

## 💾 缓存策略

### 缓存键规则

| 缓存类型 | 键模式                              | 过期时间 |
|------|----------------------------------|------|
| 用户信息 | `xypai:user:info:{userId}`       | 30分钟 |
| 用户编码 | `xypai:user:code:{userCode}`     | 30分钟 |
| 手机号  | `xypai:user:mobile:{mobile}`     | 30分钟 |
| 用户名  | `xypai:user:username:{username}` | 30分钟 |
| 统计数据 | `xypai:user:stats:{type}`        | 60分钟 |

### 缓存更新

- 用户信息变更时自动清除相关缓存
- 统计数据定时刷新
- 支持缓存预热和批量清除

## 📝 操作日志

### 日志记录

使用 `@Log` 注解自动记录操作：

```java
@Log(title = "用户注册", businessType = BusinessType.INSERT)
public R<User> registerUser(@Valid @RequestBody UserAddDTO addDTO) {
    // 业务逻辑
}
```

### 日志类型

- `INSERT`: 新增操作
- `UPDATE`: 更新操作
- `DELETE`: 删除操作
- `QUERY`: 查询操作
- `OTHER`: 其他操作

## 🛠️ 配置说明

### 主要配置项

```yaml
# 数据库配置
spring:
  datasource:
    url: 数据库连接URL
    username: 数据库用户名
    password: 数据库密码
    hikari:
      maximum-pool-size: 连接池最大连接数
      minimum-idle: 连接池最小空闲连接数

# Redis配置
  redis:
    host: Redis主机
    port: Redis端口
    password: Redis密码
    database: Redis数据库索引

# Nacos配置
  cloud:
    nacos:
      discovery:
        server-addr: Nacos服务发现地址
        namespace: 命名空间
        group: 分组
      config:
        server-addr: Nacos配置中心地址
        namespace: 命名空间
        group: 分组

# 自定义配置
xypai:
  user:
    config:
      default-avatar: 默认头像URL
      user-code-prefix: 用户编码前缀
      initial-points: 初始积分
      cache-expire-minutes: 缓存过期时间
    rules:
      batch-max-size: 批量操作最大数量
      page-max-size: 分页最大页大小
      active-user-days: 活跃用户定义天数
      new-user-days: 新用户定义天数
```

## 🧪 测试

### 单元测试

```bash
# 运行单元测试
mvn test

# 生成测试报告
mvn test jacoco:report
```

### 集成测试

```bash
# 运行集成测试
mvn integration-test
```

### API测试

使用提供的 HTTP 请求文件进行API测试：

```bash
# 使用 IDEA HTTP Client 或 Postman 导入测试
```

## 📊 监控指标

### 关键指标

- 用户注册量
- 活跃用户数
- API响应时间
- 错误率
- 缓存命中率
- 数据库连接池状态

### 监控端点

- `/actuator/health` - 健康检查
- `/actuator/metrics` - 性能指标
- `/actuator/prometheus` - Prometheus指标
- `/actuator/info` - 应用信息

## 🔧 运维指南

### 部署建议

1. **资源配置**:
    - 最小配置：2C4G
    - 推荐配置：4C8G
    - 生产配置：8C16G

2. **JVM参数**:
   ```bash
   -Xms2g -Xmx4g 
   -XX:+UseG1GC 
   -XX:MaxGCPauseMillis=200
   ```

3. **数据库连接池**:
    - 开发环境：5-10连接
    - 测试环境：10-20连接
    - 生产环境：20-50连接

### 性能优化

1. **查询优化**:
    - 合理使用索引
    - 避免N+1查询
    - 使用分页查询

2. **缓存优化**:
    - 热点数据缓存
    - 缓存预热
    - 缓存穿透防护

3. **连接池优化**:
    - 合理设置连接池大小
    - 监控连接池状态
    - 定期清理空闲连接

## 🆘 故障排查

### 常见问题

1. **服务启动失败**:
    - 检查数据库连接
    - 检查Redis连接
    - 检查Nacos连接
    - 查看启动日志

2. **API响应缓慢**:
    - 检查数据库性能
    - 检查缓存命中率
    - 分析SQL执行计划
    - 监控JVM内存

3. **数据不一致**:
    - 检查分布式事务
    - 验证缓存更新
    - 确认数据同步

### 日志分析

```bash
# 查看应用日志
tail -f logs/xypai-user-microservice.log

# 查看错误日志
tail -f logs/xypai-user-microservice-error.log

# 查看业务日志
tail -f logs/xypai-user-microservice-business.log
```

## 📞 技术支持

- 📧 邮箱: tech@xypai.com
- 📱 微信: XyPai_Tech
- 🔗 文档: https://docs.xypai.com
- 🐛 问题反馈: https://github.com/xypai/issues

## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可证。

---

**🏗️ XY相遇派用户微服务 - 企业级架构，值得信赖！**
