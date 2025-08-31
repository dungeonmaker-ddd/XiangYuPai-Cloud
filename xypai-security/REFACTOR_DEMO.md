# 🏗️ 重构演示 - 新架构使用指南

## 📋 重构后的代码结构特点

### ✅ 已完成的重构部分

1. **业务层 (Business Layer)** - 专注核心业务逻辑
2. **数据访问层 (Repository Layer)** - 统一数据操作接口
3. **Web层 (Web Layer)** - 简化的控制器和DTO
4. **客户端层 (Client Layer)** - 重构的Feign接口
5. **通用层 (Common Layer)** - 异常和常量管理

## 🔥 新架构的优势

### 1. 清晰的分层职责

```
🌐 Controller     ➤ 只处理HTTP请求响应
🔥 Business       ➤ 包含核心业务逻辑  
🗄️ Repository     ➤ 负责数据存储操作
🔧 Common         ➤ 通用工具和异常
```

### 2. 业务逻辑集中化

**重构前**: 业务逻辑分散在Service和Controller中

```java
// 原来的AuthServiceImpl - 混合了业务逻辑和数据操作
@Service
public class AuthServiceImpl implements AuthService {
    // 直接在Service中处理认证、存储、令牌生成等
}
```

**重构后**: 业务逻辑集中在Business层

```java
// 新的PasswordAuthBusiness - 专注密码认证业务
@Service  
public class PasswordAuthBusiness implements AuthBusiness {
    private final UserRepository userRepository;      // 数据访问
    private final TokenBusiness tokenBusiness;        // 令牌业务
    
    public Optional<AuthResponse> authenticate(AuthRequest request) {
        // 1. 验证认证类型
        // 2. 验证用户凭据  
        // 3. 检查账户状态
        // 4. 获取用户信息
        // 5. 生成令牌
    }
}
```

### 3. 数据访问层抽象

**重构前**: 直接使用具体的存储实现

```java
// 原来的代码直接使用Map存储
private final Map<String, AuthResponse.UserInfo> tokenStore = new ConcurrentHashMap<>();
```

**重构后**: 通过Repository接口访问数据

```java
// 新的Repository接口 - 支持多种实现
public interface TokenRepository {
    void storeAccessToken(String token, UserInfo userInfo, long expireSeconds);
    Optional<UserInfo> getAccessToken(String token);
    // 可以有内存实现、Redis实现、数据库实现
}
```

### 4. 简化的控制器

**重构前**: 控制器包含业务逻辑

```java
// 原来的Controller混合了请求处理和业务逻辑
@PostMapping("/login")
public R<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
    return authService.authenticate(authRequest)  // 调用复杂的Service
            .map(R::ok)
            .orElse(R.fail("认证失败"));
}
```

**重构后**: 控制器专注请求响应

```java
// 新的Controller只处理HTTP层面的逻辑
@PostMapping("/login")
public R<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
    return authBusiness.authenticate(authRequest)  // 调用专门的业务层
            .map(R::ok)
            .orElse(R.fail("认证失败"));
}
```

## 🎯 使用示例

### 1. 添加新的认证方式

假设要添加指纹认证，只需：

```java
// 1. 实现AuthBusiness接口
@Service
public class FingerprintAuthBusiness implements AuthBusiness {
    @Override
    public boolean supportsAuthType(String authType) {
        return "fingerprint".equals(authType);
    }
    
    @Override
    public Optional<AuthResponse> authenticate(AuthRequest request) {
        // 指纹认证逻辑
    }
}

// 2. 在AuthRequest中添加指纹数据字段
public record AuthRequest(
    // ... 现有字段
    String fingerprintData  // 新增字段
) {
    // 更新验证逻辑
}
```

### 2. 切换存储方式

从内存存储切换到Redis存储：

```java
// 1. 实现TokenRepository接口的Redis版本
@Service("redisTokenRepository")
public class RedisTokenRepositoryImpl implements TokenRepository {
    private final RedisTemplate<String, String> redisTemplate;
    
    @Override
    public void storeAccessToken(String token, UserInfo userInfo, long expireSeconds) {
        // Redis存储实现
    }
}

// 2. 配置中切换实现
@Configuration
public class RepositoryConfig {
    @Bean
    @Primary
    @ConditionalOnProperty(name = "auth.storage.type", havingValue = "redis")
    public TokenRepository redisTokenRepository() {
        return new RedisTokenRepositoryImpl();
    }
}
```

### 3. 扩展业务逻辑

添加登录失败次数限制：

```java
// 1. 在UserRepository中添加方法
public interface UserRepository {
    int getFailedLoginAttempts(String username);
    void incrementFailedLoginAttempts(String username);
    void resetFailedLoginAttempts(String username);
}

// 2. 在AuthBusiness中添加逻辑
@Override
public Optional<AuthResponse> authenticate(AuthRequest request) {
    // 检查失败次数
    if (userRepository.getFailedLoginAttempts(request.username()) >= 5) {
        throw AuthException.accountLocked();
    }
    
    // 原有认证逻辑...
    
    if (认证成功) {
        userRepository.resetFailedLoginAttempts(request.username());
    } else {
        userRepository.incrementFailedLoginAttempts(request.username());
    }
}
```

## 🔄 迁移策略

### 阶段1: 保持兼容（已完成）

- ✅ 创建新的包结构
- ✅ 创建新的业务层和数据层接口
- ✅ 保持原有Service接口不变

### 阶段2: 逐步迁移

```java
// 原有的AuthServiceImpl改为委托给Business层
@Service
public class AuthServiceImpl implements AuthService {
    private final AuthBusiness authBusiness;  // 注入新的业务层
    
    @Override
    public Optional<AuthResponse> authenticate(AuthRequest request) {
        return authBusiness.authenticate(request);  // 委托给业务层
    }
}
```

### 阶段3: 完全替换

- 📝 更新调用方使用新的Business接口
- 🗑️ 删除旧的Service实现
- 🧪 添加完整的单元测试

## 📊 重构效果对比

| 维度       | 重构前         | 重构后          |
|----------|-------------|--------------|
| **代码职责** | 混合在Service中 | 清晰分层         |
| **可测试性** | 难以单元测试      | 每层独立测试       |
| **可扩展性** | 修改现有代码      | 新增Business实现 |
| **数据访问** | 硬编码实现       | 接口抽象         |
| **业务逻辑** | 分散各处        | 集中在Business层 |
| **代码复用** | 重复逻辑        | 通用组件         |

## 🎉 总结

新的分层架构带来以下优势：

1. **🎯 单一职责**: 每个层只关注自己的职责
2. **🔧 易于测试**: 可以独立测试每个层
3. **🚀 易于扩展**: 新功能只需实现对应接口
4. **🔄 易于维护**: 修改影响范围可控
5. **👥 团队协作**: 不同开发者可以并行开发不同层

这个重构示例展示了如何将复杂的单体业务逻辑，拆分为清晰的分层架构，大大提升了代码的可维护性和扩展性。
