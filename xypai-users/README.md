# 🚀 XyPai Users 微服务集群 - MVP版本

## 🎯 **项目概述**

基于父子模块架构的微服务集群，采用MVP思维设计，专注核心功能快速验证。

```
🏗️ xypai-users (父模块)
├── 📦 common              - 公共组件
├── 👤 user-service        - 用户服务 (9106)
├── 🤝 social-service      - 社交服务 (9107) [待开发]
├── 💰 wallet-service      - 钱包服务 (9108) [待开发]
├── 📱 feed-service        - 动态服务 (9109) [待开发]
└── 🚪 gateway-service     - 网关服务 (8080)
```

## 🎯 **MVP特点**

- ✅ **简单够用** - 只保留核心功能
- ✅ **快速验证** - 优先验证架构可行性
- ✅ **易于扩展** - 为后续功能预留接口
- ✅ **统一管理** - 父子模块便于构建部署

## 🚀 **快速启动**

### 🔸 **环境要求**

- Java 21+
- Maven 3.8+
- MySQL 8.0+
- Nacos 2.3+ (可选)

### 🔸 **启动步骤**

#### 1️⃣ **构建项目**

```bash
# 在项目根目录执行
cd xypai-users
mvn clean install
```

#### 2️⃣ **初始化数据库**

```bash
# 创建用户数据库
mysql -u root -p < user-service/src/main/resources/sql/init_user_tables.sql
```

#### 3️⃣ **启动服务**

```bash
# 方式1: 使用Maven启动
cd user-service && mvn spring-boot:run
cd gateway-service && mvn spring-boot:run

# 方式2: 使用Java启动
java -jar user-service/target/user-service-1.0.0-SNAPSHOT.jar
java -jar gateway-service/target/gateway-service-1.0.0-SNAPSHOT.jar
```

#### 4️⃣ **验证服务**

```bash
# 健康检查
curl http://localhost:9106/users/health
curl http://localhost:8080/users/health

# 创建用户
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "13912345678",
    "username": "testuser",
    "nickname": "测试用户",
    "clientType": "app"
  }'

# 获取用户列表
curl http://localhost:8080/users
```

## 📊 **服务端口分配**

| 服务名            | 端口   | 状态     | 描述           |
|----------------|------|--------|--------------|
| Gateway        | 8080 | ✅ 完成   | 统一网关，无/api前缀 |
| User Service   | 9106 | ✅ 完成   | 用户基础信息管理     |
| Social Service | 9107 | 🔄 待开发 | 社交关系管理       |
| Wallet Service | 9108 | 🔄 待开发 | 钱包财务管理       |
| Feed Service   | 9109 | 🔄 待开发 | 动态内容管理       |

## 🎯 **API接口**

### 👤 **用户服务 API**

```bash
# 基础路径: /users

GET    /users/health           # 健康检查
POST   /users                  # 创建用户
GET    /users                  # 用户列表
GET    /users/{id}             # 获取用户
GET    /users/username/{name}  # 根据用户名获取
GET    /users/check/mobile     # 检查手机号
GET    /users/check/username   # 检查用户名
PUT    /users/{id}/enable      # 启用用户
PUT    /users/{id}/disable     # 禁用用户
```

### 📝 **请求示例**

**创建用户:**

```json
POST /users
{
  "mobile": "13912345678",
  "username": "testuser", 
  "nickname": "测试用户",
  "clientType": "app"
}
```

**响应示例:**

```json
{
  "code": 200,
  "message": "用户创建成功",
  "data": {
    "id": 1,
    "mobile": "13912345678",
    "username": "testuser",
    "nickname": "测试用户",
    "gender": 0,
    "genderDesc": "未知",
    "status": 1,
    "statusDesc": "正常",
    "clientType": "app",
    "createTime": "2025-01-02T10:30:00"
  },
  "timestamp": "2025-01-02T10:30:00"
}
```

## 🛠️ **技术栈**

- **框架**: Spring Boot 3.2.1
- **服务发现**: Nacos (可选)
- **网关**: Spring Cloud Gateway
- **数据库**: MySQL 8.0 + MyBatis-Plus
- **工具**: Lombok + HuTool
- **语言**: Java 21 (Records优先)

## 📋 **开发规范**

### 🎯 **MVP原则**

1. **功能最小化** - 只实现核心必需功能
2. **快速迭代** - 先跑通再优化
3. **架构预留** - 为扩展功能预留接口
4. **问题聚焦** - 专注解决特定问题

### 🏗️ **代码规范**

1. **Records优先** - 所有DTO使用Record
2. **Fail Fast** - 参数验证前置
3. **统一响应** - 使用Result包装响应
4. **异常处理** - 全局异常处理

## 🔄 **后续规划**

### 🎯 **第一阶段** (当前)

- ✅ 用户服务基础功能
- ✅ Gateway路由配置
- ✅ 统一响应格式

### 🎯 **第二阶段** (规划中)

- 🔄 社交服务 (关注/粉丝)
- 🔄 钱包服务 (余额/交易)
- 🔄 服务间通信机制

### 🎯 **第三阶段** (未来)

- 🔄 动态服务 (发布/浏览)
- 🔄 互动服务 (点赞/评论)
- 🔄 完整的业务闭环

## 🎉 **MVP成果**

✅ **已验证**:

- 微服务架构可行性
- Gateway路由正确性
- 用户CRUD功能完整性
- 统一响应格式规范性

✅ **待验证**:

- 服务间通信
- 数据一致性
- 性能表现
- 业务闭环

---

## 🚀 **立即开始使用**

1. **克隆项目** 并进入目录
2. **初始化数据库** 执行SQL脚本
3. **启动服务** User Service + Gateway
4. **测试接口** 验证功能正常

**现在就开始您的微服务之旅吧！** 🎯
