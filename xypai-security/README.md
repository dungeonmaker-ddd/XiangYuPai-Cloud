# 🛡️ XY相遇派安全认证模块

XV01:00 统一安全认证解决方案

## 📖 模块概述

基于 `sfbx-security` 架构重构的新一代认证服务，专注于：

- 🎯 **MVP优先**: 简化认证流程，避免过度工程化
- 🏗️ **模块化设计**: 清晰的职责分离
- 📱 **多端支持**: Web、App、小程序统一认证
- 🔒 **安全可靠**: 规范的OAuth2实现

## 📁 模块结构

```
xypai-security/
├── security-interface/           # 🔗 接口定义模块
│   ├── model/                    # 数据模型 (Records)
│   │   ├── AuthRequest.java      # 认证请求
│   │   └── AuthResponse.java     # 认证响应
│   └── feign/                    # Feign客户端
│       ├── AuthServiceFeign.java
│       └── AuthServiceFeignFallback.java
│
├── security-oauth/               # 🛡️ OAuth2核心服务
│   ├── config/                   # 配置类
│   │   ├── TokenProperties.java  # Token配置
│   │   └── SecurityConfig.java   # 安全配置
│   ├── service/                  # 业务服务
│   │   ├── AuthService.java      # 认证接口
│   │   └── impl/
│   │       └── AuthServiceImpl.java
│   ├── controller/               # 控制层
│   │   └── AuthController.java
│   └── SecurityOauthApplication.java
│
└── security-web/                 # 🌐 Web管理端服务
    ├── model/                    # 数据模型 (Records)
    │   ├── AdminConfigResponse.java
    │   └── UserManagementRequest.java
    ├── controller/               # 控制层
    │   └── AdminController.java
    └── SecurityWebApplication.java
```

## 🚀 快速开始

### 方式一：本地开发启动

```bash
# 启动认证中心
cd xypai-security/security-oauth
mvn spring-boot:run

# 启动Web管理端 (另一个终端)
cd xypai-security/security-web  
mvn spring-boot:run
```

### 方式二：Docker 一键部署 (推荐)

```bash
cd xypai-security
./deploy.sh
```

部署脚本会自动完成：

- ✅ Maven 项目构建
- ✅ Docker 镜像构建
- ✅ 启动所有服务 (认证中心、Web管理端、MySQL、Redis、Nacos)
- ✅ 健康检查

**服务端口:**

- 认证中心: `http://localhost:9401`
- Web管理端: `http://localhost:9402`
- MySQL: `localhost:3306`
- Redis: `localhost:6379`
- Nacos: `http://localhost:8848`

### 2. API使用示例

#### 认证中心 APIs

**登录认证**

```bash
curl -X POST http://localhost:9401/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456",
    "client_type": "app",
    "auth_type": "password"
  }'
```

**验证令牌**

```bash
curl "http://localhost:9401/auth/verify?access_token=YOUR_TOKEN"
```

**刷新令牌**

```bash
curl -X POST "http://localhost:9401/auth/refresh?refresh_token=YOUR_REFRESH_TOKEN&client_type=app"
```

#### Web管理端 APIs

**获取管理端配置**

```bash
curl "http://localhost:9402/admin/config"
```

**用户管理**

```bash
curl "http://localhost:9402/admin/users"
```

**系统统计**

```bash
curl "http://localhost:9402/admin/statistics"
```

## 🔧 配置说明

### Token过期时间配置

```yaml
auth:
  token:
    web-expire-time: 7200    # Web端 2小时
    app-expire-time: 86400   # App端 24小时
    mini-expire-time: 86400  # 小程序 24小时
```

### 客户端类型

| 类型     | 说明     | 过期时间 |
|--------|--------|------|
| `web`  | PC端浏览器 | 2小时  |
| `app`  | 移动端APP | 24小时 |
| `mini` | 微信小程序  | 24小时 |

## 🎯 设计原则

### 遵循的开发规则

1. **Records优先**: 所有DTO使用Records
2. **Fail Fast**: 参数验证前置
3. **轻量控制层**: 控制器专注请求响应
4. **业务分离**: 业务逻辑在Service层
5. **MVP优先**: 避免过度工程化

### 示例代码

```java
// ✅ 正确：使用Records
public record AuthRequest(
    @NotBlank String username,
    @Pattern(regexp = "^(web|app|mini)$") String clientType
) {
    public AuthRequest {
        Objects.requireNonNull(username, "用户名不能为空");
        // 验证逻辑
    }
}

// ✅ 正确：轻量控制器
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    
    @PostMapping("/login")
    public R<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return authService.authenticate(request)
                .map(R::ok)
                .orElse(R.fail("认证失败"));
    }
}
```

## 🔄 与现有系统集成

### APP端控制器重构

原有的复杂业务逻辑已简化为Feign调用：

```java
// 重构前：复杂的业务处理
@PostMapping("/login")
public ResponseEntity<R<LoginResponse>> login(@RequestBody LoginRequest request) {
    return authService.processLogin(request); // 大量业务逻辑
}

// 重构后：简洁的委托调用
@PostMapping("/login/password")
public R<AuthResponse> passwordLogin(@RequestBody PasswordLoginRequest request) {
    AuthRequest authRequest = AuthRequest.ofPassword(
        request.username(), request.password(), "app");
    return authServiceFeign.login(authRequest);
}
```

## 🛠️ 开发指南

### MVP版本特性

- ✅ 用户名密码认证
- ✅ 令牌生成和验证
- ✅ 令牌刷新
- ✅ 多客户端支持
- ✅ 内存存储（开发环境）

### 后续扩展计划

- 🔄 短信验证码认证
- 🔄 微信登录集成
- 🔄 Redis + 数据库存储
- 🔄 JWT增强安全
- 🔄 OAuth2完整实现

## 📈 监控和运维

### 健康检查

```bash
curl http://localhost:9401/auth/health
```

### 性能指标

- 访问 `http://localhost:9401/actuator/metrics` 查看指标
- 支持Prometheus导出

## 🔒 安全说明

### MVP版本安全措施

1. **密码加密**: BCrypt算法
2. **参数验证**: Bean Validation
3. **异常处理**: 统一错误响应
4. **令牌存储**: 内存缓存（开发环境）

### 生产环境建议

1. 使用Redis存储令牌
2. 配置HTTPS
3. 实现JWT签名验证
4. 添加访问频率限制
5. 配置安全审计日志

## 🐳 Docker 部署

### 快速部署

```bash
# 一键部署所有服务
./deploy.sh

# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f security-oauth
docker-compose logs -f security-web
```

### 自定义部署

```bash
# 仅启动基础服务
docker-compose up -d mysql redis nacos

# 启动认证服务
docker-compose up -d security-oauth

# 启动Web管理端
docker-compose up -d security-web
```

### 服务管理

```bash
# 停止所有服务
docker-compose down

# 重启特定服务
docker-compose restart security-oauth

# 查看服务健康状态
curl http://localhost:9401/auth/health
curl http://localhost:9402/admin/health
```

### 环境变量配置

| 服务             | 环境变量                 | 默认值                        | 说明      |
|----------------|----------------------|----------------------------|---------|
| security-oauth | `MYSQL_HOST`         | mysql                      | 数据库主机   |
| security-oauth | `REDIS_HOST`         | redis                      | Redis主机 |
| security-oauth | `NACOS_SERVER_ADDR`  | nacos:8848                 | Nacos地址 |
| security-web   | `SECURITY_OAUTH_URL` | http://security-oauth:9401 | 认证服务地址  |

---

**📝 版本**: 1.0.0 MVP  
**👨‍💻 作者**: xypai团队  
**📅 更新**: 2025年1月  
**🏷️ 标签**: OAuth2, 认证服务, MVP, 微服务, Docker
