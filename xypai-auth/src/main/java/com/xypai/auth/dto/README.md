# 📋 Java Records 最佳实践实现

## 🎯 符合 2024-2025 年企业级标准

本项目严格遵循 Netflix、Google 等企业的 Java Records 最佳实践，确保代码质量和可维护性。

## ✅ 已实现的最佳实践

### 📕 强制规则 (MUST)

#### ✅ Rule R1: Records for All Data Transfer Objects

- 所有请求/响应对象都使用 Records
- 不使用传统的 POJO 和 getter/setter

#### ✅ Rule R2: Validation in Compact Constructors

- 在紧凑构造器中验证不变量
- 使用 `Objects.requireNonNull` 进行空值检查
- 立即抛出异常防止无效状态

#### ✅ Rule R3: Bean Validation Annotations

- 使用 Jakarta Bean Validation 注解
- 在 API 端点使用 `@Valid` 自动验证
- 清晰的错误消息

#### ✅ Rule R4: Immutability Preservation

- 使用 `Set.copyOf()` 进行防御性复制
- 不暴露可变集合
- 保证数据不可变性

#### ✅ Rule R5: No Business Logic in Records

- Records 只作为纯数据载体
- 不包含业务逻辑方法
- 保持简单和专注

### 📘 推荐规则 (SHOULD)

#### ✅ Rule R6: Naming Conventions

- 使用 `Request`/`Response` 后缀
- 不使用过时的 `DTO`/`VO` 后缀
- 清晰描述性的命名

#### ✅ Rule R7: Factory Methods for Complex Creation

- 提供静态工厂方法
- 描述性的方法名 (`of`, `web`, `app`)
- 简化对象创建

#### ✅ Rule R8: JSON Serialization Annotations

- 使用 `@JsonProperty` 处理命名
- 配置时间格式化
- 保持 API 兼容性

## 📁 文件结构

```
dto/
├── LoginRequest.java          # 密码登录请求
├── SmsLoginRequest.java       # 短信登录请求
├── UserCreateRequest.java     # 用户创建请求
├── PasswordChangeRequest.java # 密码修改请求
└── converter/
    └── AuthDTOConverter.java  # 转换器工具

vo/
├── LoginResponse.java         # 登录响应
└── SmsCodeResponse.java       # 短信发送响应
```

## 🔧 使用示例

### 创建请求对象

```java
// 使用工厂方法
LoginRequest webLogin = LoginRequest.web("admin", "password123");
LoginRequest appLogin = LoginRequest.app("user", "pass", "device123");

// 直接构造
UserCreateRequest userRequest = new UserCreateRequest(
    "zhangsan", "password", "张三", 
    "zhang@example.com", "13800138000", 
    Set.of(1L, 2L), 1L
);
```

### 控制器中使用

```java
@PostMapping("/login")
public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    // 自动验证，无需手动检查
    LoginUser user = loginService.authenticate(request);
    return R.ok(LoginResponse.of(token, user.getUsername(), user.getNickname()));
}
```

### JSON 响应格式

```json
{
  "code": 200,
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "token_type": "Bearer",
    "username": "admin",
    "nickname": "管理员",
    "issued_at": "2024-01-15T10:30:00Z"
  }
}
```

## 🚫 禁止的模式

### ❌ 不要这样做

```java
// 错误：使用传统 POJO
public class UserRequest {
    private String username;
    // getters and setters
}

// 错误：在 Record 中包含业务逻辑
public record LoginRequest(String username, String password) {
    public boolean isValid() { // 业务逻辑 - 禁止
        return username != null && password != null;
    }
}

// 错误：过时的命名
public record UserDTO(...) {}  // 不要用 DTO 后缀
public record UserVO(...) {}   // 不要用 VO 后缀
```

### ✅ 正确做法

```java
// 正确：纯数据 Record
public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password
) {
    public LoginRequest {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        username = username.trim();
        password = password.trim();
    }
    
    // 静态工厂方法是允许的
    public static LoginRequest of(String username, String password) {
        return new LoginRequest(username, password);
    }
}
```

## 📊 质量检查清单

在创建新的 Record 时，请确保：

- [ ] 使用 `Request`/`Response` 后缀命名
- [ ] 添加了适当的 Bean Validation 注解
- [ ] 在紧凑构造器中验证不变量
- [ ] 对集合进行防御性复制
- [ ] 不包含业务逻辑方法
- [ ] 提供了有用的静态工厂方法
- [ ] 添加了 Swagger 文档注解
- [ ] JSON 序列化配置正确

## 🎯 性能考虑

- Records 是不可变的，适合缓存
- 编译器优化了 Records 的性能
- 避免在紧凑循环中创建大量 Records
- 对于大型数据集，考虑使用流式处理

---

**记住：Records 是数据载体，不是业务逻辑容器！** 🎊
