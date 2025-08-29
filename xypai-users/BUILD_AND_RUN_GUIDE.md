# 🚀 XyPai-Users 构建和运行指南

## 🎯 **项目总览**

基于DDD架构的微服务集群，包含6个聚合根分布在5个服务中：

```
🏗️ 服务端口分配
├── 🚪 gateway-service:8080    - 统一网关
├── 👤 user-service:9106       - UserAggregate
├── 🤝 social-service:9107     - SocialAggregate  
├── 💰 wallet-service:9108     - WalletAggregate
└── 📱 feed-service:9109       - FeedAggregate + InteractionAggregate
```

## 🔧 **环境准备**

### 📋 **必需环境**

```bash
# Java 环境
Java 21+ (推荐 OpenJDK 21)
Maven 3.8+

# 数据库
MySQL 8.0+

# 可选 (推荐)
Nacos 2.3+ (服务发现)
Redis 7.0+ (缓存)
```

### 🔍 **环境验证**

```bash
# 检查 Java 版本
java -version

# 检查 Maven 版本  
mvn -version

# 检查 MySQL 连接
mysql -u root -p -e "SELECT VERSION();"
```

## 🏗️ **构建步骤**

### 1️⃣ **项目构建**

```bash
# 克隆项目 (或创建目录)
cd xypai-users

# 构建所有模块
mvn clean install

# 跳过测试的快速构建
mvn clean install -DskipTests
```

### 2️⃣ **数据库初始化**

#### **方式1: 命令行执行**

```bash
# 用户服务数据库
mysql -u root -p < user-service/src/main/resources/sql/init_user_tables.sql

# 社交服务数据库  
mysql -u root -p < social-service/src/main/resources/sql/init_social_tables.sql

# 钱包服务数据库
mysql -u root -p < wallet-service/src/main/resources/sql/init_wallet_tables.sql

# 动态服务数据库
mysql -u root -p < feed-service/src/main/resources/sql/init_feed_tables.sql
```

#### **方式2: 数据库工具执行**

```sql
-- 使用 MySQL Workbench, Navicat 等工具
-- 依次执行各服务的 SQL 初始化脚本
```

### 3️⃣ **配置调整**

#### **数据库连接配置**

```bash
# 修改各服务的 application.yml 中的数据库连接信息
# 位置: {service-name}/src/main/resources/application.yml

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/{database_name}
    username: your_username
    password: your_password
```

#### **Nacos配置 (可选)**

```bash
# 如果使用 Nacos 服务发现，确保 Nacos 服务运行在 localhost:8848
# 如果不使用，可以注释掉相关配置
```

## 🚀 **服务启动**

### 🔸 **方式1: Maven 启动 (开发推荐)**

```bash
# 启动网关服务
cd gateway-service
mvn spring-boot:run

# 启动用户服务  
cd user-service
mvn spring-boot:run

# 启动社交服务
cd social-service  
mvn spring-boot:run

# 启动钱包服务
cd wallet-service
mvn spring-boot:run

# 启动动态服务
cd feed-service
mvn spring-boot:run
```

### 🔸 **方式2: JAR 包启动 (生产推荐)**

```bash
# 先构建 JAR 包
mvn clean package

# 启动各服务
java -jar gateway-service/target/gateway-service-1.0.0-SNAPSHOT.jar
java -jar user-service/target/user-service-1.0.0-SNAPSHOT.jar  
java -jar social-service/target/social-service-1.0.0-SNAPSHOT.jar
java -jar wallet-service/target/wallet-service-1.0.0-SNAPSHOT.jar
java -jar feed-service/target/feed-service-1.0.0-SNAPSHOT.jar
```

### 🔸 **方式3: Docker 启动 (容器化)**

```bash
# TODO: 后续提供 Docker Compose 配置
```

## ✅ **服务验证**

### 🔍 **健康检查**

```bash
# 网关服务
curl http://localhost:8080/actuator/health

# 用户服务
curl http://localhost:9106/actuator/health  
curl http://localhost:8080/users/health

# 社交服务
curl http://localhost:9107/actuator/health
curl http://localhost:8080/social/health

# 钱包服务  
curl http://localhost:9108/actuator/health
curl http://localhost:8080/wallets/health

# 动态服务
curl http://localhost:9109/actuator/health
curl http://localhost:8080/feeds/health
```

### 🧪 **功能测试**

#### **用户服务测试**

```bash
# 创建用户
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "13912345678",
    "username": "testuser",
    "nickname": "测试用户",
    "clientType": "app"
  }'

# 查询用户列表
curl http://localhost:8080/users

# 根据ID查询用户
curl http://localhost:8080/users/1
```

#### **检查数据库数据**

```sql
-- 验证用户数据
SELECT * FROM xypai_user.user;
SELECT * FROM xypai_user.user_profile;

-- 验证社交数据
SELECT * FROM xypai_social.social_relation;
SELECT * FROM xypai_social.social_settings;

-- 验证钱包数据
SELECT * FROM xypai_wallet.wallet;
SELECT * FROM xypai_wallet.wallet_transaction;

-- 验证动态数据
SELECT * FROM xypai_feed.user_feed;
SELECT * FROM xypai_feed.interaction_target;
```

## 🎯 **API接口总览**

### 👤 **用户服务 API**

```bash
# 基础路径: /users
GET    /users/health           # 健康检查
POST   /users                  # 创建用户  
GET    /users                  # 用户列表
GET    /users/{id}             # 获取用户详情
GET    /users/username/{name}  # 根据用户名获取
PUT    /users/{id}/enable      # 启用用户
PUT    /users/{id}/disable     # 禁用用户
```

### 🤝 **社交服务 API (待实现)**

```bash
# 基础路径: /social
GET    /social/health          # 健康检查
POST   /social/follow          # 关注用户
DELETE /social/follow          # 取消关注
GET    /social/followers       # 获取粉丝列表
GET    /social/following       # 获取关注列表
```

### 💰 **钱包服务 API (待实现)**

```bash
# 基础路径: /wallets  
GET    /wallets/health         # 健康检查
POST   /wallets                # 创建钱包
GET    /wallets/{userId}       # 获取钱包信息
POST   /wallets/recharge       # 充值
POST   /wallets/transfer       # 转账
```

### 📱 **动态服务 API (待实现)**

```bash
# 基础路径: /feeds
GET    /feeds/health           # 健康检查  
POST   /feeds                  # 发布动态
GET    /feeds                  # 获取动态列表
GET    /feeds/{id}             # 获取动态详情
POST   /feeds/{id}/like        # 点赞动态
POST   /feeds/{id}/comment     # 评论动态
```

## 🛠️ **开发工具**

### 📋 **推荐IDE配置**

```bash
# IntelliJ IDEA
- 安装 Lombok Plugin
- 启用 Annotation Processing
- 配置 Java 21

# VS Code  
- 安装 Extension Pack for Java
- 安装 Spring Boot Extension Pack
```

### 🔍 **调试技巧**

```bash
# 启用 DEBUG 日志
logging.level.com.xypai=DEBUG

# 查看 SQL 执行
mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl

# JVM 参数调优
java -Xmx512m -Xms256m -jar {service}.jar
```

## 🚨 **常见问题**

### ❌ **构建失败**

```bash
# Java 版本不匹配
解决: 确保使用 Java 21+

# Maven 依赖下载失败  
解决: 检查网络，或配置国内镜像

# 编译错误
解决: mvn clean compile -X 查看详细错误
```

### ❌ **启动失败**

```bash
# 端口被占用
解决: lsof -i :9106 或 netstat -tulpn | grep 9106

# 数据库连接失败
解决: 检查数据库服务状态和连接配置

# 找不到主类
解决: 确保 JAR 包构建成功
```

### ❌ **接口调用失败**

```bash
# 404 Not Found
解决: 检查 Gateway 路由配置

# 503 Service Unavailable  
解决: 检查目标服务是否启动

# 数据库相关错误
解决: 检查数据库连接和表结构
```

## 🎉 **成功指标**

### ✅ **完全成功的标志**

- [ ] 所有服务健康检查通过
- [ ] 用户CRUD操作正常
- [ ] Gateway路由正确转发
- [ ] 数据库数据一致
- [ ] 日志输出正常

### 📊 **性能指标**

- 服务启动时间 < 30秒
- API响应时间 < 500ms
- 数据库连接池正常
- 内存使用 < 512MB/服务

---

## 🚀 **下一步计划**

1. **🔸 完善API实现** - 实现所有聚合根的完整功能
2. **🔸 添加业务逻辑** - 补充各服务的业务规则
3. **🔸 集成测试** - 编写端到端测试用例
4. **🔸 性能优化** - 添加缓存和优化查询
5. **🔸 监控告警** - 集成监控和日志系统

**🎯 现在您可以开始运行和测试整个微服务架构了！**
