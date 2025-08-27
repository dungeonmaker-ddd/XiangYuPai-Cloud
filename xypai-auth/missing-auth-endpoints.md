# 🚀 需要补充的认证接口

## 📝 用户注册模块

### 1. 用户注册接口

```java
/**
 * 📝 用户注册
 */
@PostMapping("/register")
public ResponseEntity<R<Void>> register(@Valid @RequestBody RegisterRequest request);

/**
 * 📧 发送注册验证码 
 */
@PostMapping("/register/verify/send")
public ResponseEntity<R<Void>> sendRegisterVerifyCode(@Valid @RequestBody VerifyCodeRequest request);

/**
 * ✅ 验证注册验证码
 */
@PostMapping("/register/verify")
public ResponseEntity<R<Void>> verifyRegisterCode(@Valid @RequestBody VerifyRequest request);
```

### 2. 密码重置模块

```java
/**
 * 🔐 发送密码重置验证码
 */
@PostMapping("/password/reset/send")
public ResponseEntity<R<Void>> sendPasswordResetCode(@Valid @RequestBody PasswordResetRequest request);

/**
 * 🔄 重置密码
 */
@PostMapping("/password/reset")
public ResponseEntity<R<Void>> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request);

/**
 * 🔐 修改密码 (需要登录)
 */
@PostMapping("/password/change")
public ResponseEntity<R<Void>> changePassword(@Valid @RequestBody PasswordChangeRequest request);
```

## 🌐 多端支持接口

### 3. Web端认证接口

```java
/**
 * 🌐 Web端登录
 */
@PostMapping("/web/auth/login")
public ResponseEntity<R<LoginResponse>> webLogin(@Valid @RequestBody LoginRequest request);

/**
 * 🌐 Web端退出
 */
@DeleteMapping("/web/auth/logout")  
public ResponseEntity<R<Void>> webLogout(HttpServletRequest request);
```

### 4. 小程序认证接口

```java
/**
 * 📱 微信小程序登录
 */
@PostMapping("/mini/auth/login")
public ResponseEntity<R<LoginResponse>> miniLogin(@Valid @RequestBody MiniLoginRequest request);

/**
 * 📱 小程序手机号绑定
 */
@PostMapping("/mini/auth/bind-mobile")
public ResponseEntity<R<Void>> bindMobile(@Valid @RequestBody MiniBindMobileRequest request);
```

## 🔐 Token管理增强

### 5. Token管理接口

```java
/**
 * 📋 获取在线用户列表 (管理端)
 */
@GetMapping("/admin/auth/online-users")
public ResponseEntity<R<List<OnlineUser>>> getOnlineUsers(@RequestParam(defaultValue = "1") Integer pageNum,
                                                         @RequestParam(defaultValue = "10") Integer pageSize);

/**
 * 🚫 强制下线用户 (管理端)
 */
@DeleteMapping("/admin/auth/kick-out/{userId}")
public ResponseEntity<R<Void>> kickOutUser(@PathVariable Long userId);

/**
 * 🔍 Token详细信息查询
 */
@GetMapping("/auth/token/info")
public ResponseEntity<R<TokenInfo>> getTokenInfo(HttpServletRequest request);

/**
 * 📊 用户登录历史
 */
@GetMapping("/auth/login-history")
public ResponseEntity<R<List<LoginHistory>>> getLoginHistory(@RequestParam(defaultValue = "1") Integer pageNum,
                                                           @RequestParam(defaultValue = "10") Integer pageSize);
```

## 🔔 安全增强接口

### 6. 安全验证接口

```java
/**
 * 🔐 二次验证发送验证码
 */
@PostMapping("/auth/security/verify/send")
public ResponseEntity<R<Void>> sendSecurityVerifyCode(@Valid @RequestBody SecurityVerifyRequest request);

/**
 * ✅ 二次验证确认
 */
@PostMapping("/auth/security/verify")
public ResponseEntity<R<SecurityVerifyResponse>> verifySecurityCode(@Valid @RequestBody SecurityVerifyConfirmRequest request);

/**
 * 📱 绑定设备
 */
@PostMapping("/auth/device/bind")
public ResponseEntity<R<Void>> bindDevice(@Valid @RequestBody DeviceBindRequest request);

/**
 * 📋 获取已绑定设备列表
 */
@GetMapping("/auth/device/list")
public ResponseEntity<R<List<UserDevice>>> getDeviceList();

/**
 * 🗑️ 解绑设备
 */
@DeleteMapping("/auth/device/{deviceId}")
public ResponseEntity<R<Void>> unbindDevice(@PathVariable String deviceId);
```

## 🌍 社交登录接口 (可选)

### 7. 第三方登录

```java
/**
 * 🌍 微信登录
 */
@PostMapping("/auth/social/wechat")
public ResponseEntity<R<LoginResponse>> wechatLogin(@Valid @RequestBody WechatLoginRequest request);

/**
 * 🌍 QQ登录
 */
@PostMapping("/auth/social/qq")
public ResponseEntity<R<LoginResponse>> qqLogin(@Valid @RequestBody QQLoginRequest request);

/**
 * 🌍 绑定第三方账号
 */
@PostMapping("/auth/social/bind")
public ResponseEntity<R<Void>> bindSocialAccount(@Valid @RequestBody SocialBindRequest request);

/**
 * 🗑️ 解绑第三方账号
 */
@DeleteMapping("/auth/social/unbind/{platform}")
public ResponseEntity<R<Void>> unbindSocialAccount(@PathVariable String platform);
```

## 📊 统计分析接口

### 8. 登录统计接口 (管理端)

```java
/**
 * 📊 登录统计分析
 */
@GetMapping("/admin/auth/statistics")
public ResponseEntity<R<LoginStatistics>> getLoginStatistics(@RequestParam String startDate,
                                                            @RequestParam String endDate);

/**
 * 📈 实时在线用户数
 */
@GetMapping("/admin/auth/online-count")
public ResponseEntity<R<OnlineCountResponse>> getOnlineCount();

/**
 * 🌏 登录地域分布
 */
@GetMapping("/admin/auth/geo-distribution")
public ResponseEntity<R<List<GeoDistribution>>> getGeoDistribution();
```

## 🔧 系统配置接口

### 9. 认证配置管理 (管理端)

```java
/**
 * ⚙️ 获取认证配置
 */
@GetMapping("/admin/auth/config")
public ResponseEntity<R<AuthConfig>> getAuthConfig();

/**
 * 🔧 更新认证配置
 */
@PutMapping("/admin/auth/config")
public ResponseEntity<R<Void>> updateAuthConfig(@Valid @RequestBody AuthConfigUpdateRequest request);

/**
 * 🔄 刷新认证配置缓存
 */
@PostMapping("/admin/auth/config/refresh")
public ResponseEntity<R<Void>> refreshAuthConfig();
```

## 📋 网关配置补充建议

### 需要在白名单中添加的路径：

```yaml
security:
  ignore:
    whites:
      # 注册相关
      - /auth/register
      - /auth/register/verify/send
      - /auth/register/verify
      
      # 密码重置相关  
      - /auth/password/reset/send
      - /auth/password/reset
      
      # 社交登录 (如果启用)
      - /auth/social/**
      
      # Web端认证
      - /web/auth/login
      - /web/auth/logout
      
      # 小程序认证
      - /mini/auth/login
```

### 需要添加的路由规则：

```yaml
spring:
  cloud:
    gateway:
      routes:
        # Web端认证服务
        - id: xypai-auth-web
          uri: lb://xypai-auth-web
          predicates:
            - Path=/web/auth/**
          filters:
            - CacheRequestBody
            - ValidateCodeFilter
            - StripPrefix=2
            
        # 小程序认证服务  
        - id: xypai-auth-mini
          uri: lb://xypai-auth-mini
          predicates:
            - Path=/mini/auth/**
          filters:
            - StripPrefix=2
```

## 🎯 优先级建议

### 🔴 高优先级 (必须实现)

1. ✅ 用户注册接口
2. ✅ 密码重置接口
3. ✅ Web端认证接口
4. ✅ Token管理增强

### 🟡 中优先级 (建议实现)

1. 📱 小程序认证
2. 🔐 安全验证增强
3. 📋 在线用户管理
4. 📊 基础统计

### 🟢 低优先级 (可选实现)

1. 🌍 社交登录
2. 📈 高级统计分析
3. 🔧 配置管理界面
