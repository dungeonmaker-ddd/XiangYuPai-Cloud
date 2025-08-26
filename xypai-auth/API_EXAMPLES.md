# 🎯 统一认证API调用示例

## 📡 接口地址说明

### 网关路由

```bash
# 统一入口（推荐）
POST https://api.xypai.com/api/auth/login

# 客户端专用入口（可选）  
POST https://api.xypai.com/api/admin/auth/login  # 管理端
POST https://api.xypai.com/api/app/auth/login    # 移动端
```

### 实际转发

```bash
# 网关转发到认证服务
Gateway: /api/auth/login → xypai-auth-service: /login
```

## 🖥️ Web端（管理后台）调用示例

### 管理员登录

```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123",
  "clientType": "web"
}

# 响应
{
  "code": 200,
  "message": "success",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "token_type": "Bearer",
    "expires_in": 7200,
    "username": "admin",
    "nickname": "系统管理员",
    "issued_at": "2024-01-15T10:00:00Z"
  }
}
```

### JavaScript调用示例

```javascript
// 管理端登录
async function adminLogin(username, password) {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      username: username,
      password: password,
      clientType: 'web'  // 关键：指定客户端类型
    })
  });
  
  const result = await response.json();
  if (result.code === 200) {
    // 保存token
    localStorage.setItem('token', result.data.access_token);
    return result.data;
  } else {
    throw new Error(result.message);
  }
}

// 获取用户信息
async function getUserInfo() {
  const token = localStorage.getItem('token');
  const response = await fetch('/api/auth/info', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  return response.json();
}
```

## 📱 APP端调用示例

### 用户名密码登录

```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "user001",
  "password": "123456",
  "clientType": "app",
  "deviceId": "device-uuid-123456"
}

# 响应
{
  "code": 200,
  "message": "success", 
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "token_type": "Bearer",
    "expires_in": 86400,  // APP端24小时过期
    "username": "user001",
    "nickname": "张三",
    "issued_at": "2024-01-15T10:00:00Z"
  }
}
```

### 短信验证码登录

```bash
# 1. 发送验证码
POST /api/auth/sms/send?mobile=13800138000&clientType=app

# 响应
{
  "code": 200,
  "message": "success",
  "data": {
    "mobile": "138****8000",
    "message": "验证码已发送",
    "sent_at": "2024-01-15T10:00:00Z",
    "expires_in": 300,
    "next_send_in": 60
  }
}

# 2. 验证码登录
POST /api/auth/login/sms
Content-Type: application/json

{
  "mobile": "13800138000",
  "code": "123456",
  "clientType": "app",
  "deviceId": "device-uuid-123456"
}
```

### Android调用示例

```kotlin
// APP端登录服务
class AuthService {
    private val api = RetrofitClient.authApi
    
    // 用户名密码登录
    suspend fun login(username: String, password: String, deviceId: String): LoginResponse {
        val request = LoginRequest(
            username = username,
            password = password,
            clientType = "app",
            deviceId = deviceId
        )
        
        val response = api.login(request)
        if (response.code == 200) {
            // 保存token
            TokenManager.saveToken(response.data.accessToken)
            return response.data
        } else {
            throw Exception(response.message)
        }
    }
    
    // 短信登录
    suspend fun smsLogin(mobile: String, code: String, deviceId: String): LoginResponse {
        val request = SmsLoginRequest(
            mobile = mobile,
            code = code,
            clientType = "app",
            deviceId = deviceId
        )
        
        val response = api.smsLogin(request)
        if (response.code == 200) {
            TokenManager.saveToken(response.data.accessToken)
            return response.data
        } else {
            throw Exception(response.message)
        }
    }
    
    // 发送验证码
    suspend fun sendSmsCode(mobile: String): SmsCodeResponse {
        val response = api.sendSmsCode(mobile, "app")
        if (response.code == 200) {
            return response.data
        } else {
            throw Exception(response.message)
        }
    }
}

// Retrofit接口定义
interface AuthApi {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>
    
    @POST("/api/auth/login/sms")
    suspend fun smsLogin(@Body request: SmsLoginRequest): ApiResponse<LoginResponse>
    
    @POST("/api/auth/sms/send")
    suspend fun sendSmsCode(
        @Query("mobile") mobile: String,
        @Query("clientType") clientType: String
    ): ApiResponse<SmsCodeResponse>
    
    @GET("/api/auth/info")
    suspend fun getUserInfo(@Header("Authorization") token: String): ApiResponse<UserInfo>
    
    @DELETE("/api/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): ApiResponse<Unit>
}
```

### iOS Swift调用示例

```swift
// APP端认证服务
class AuthService {
    private let baseURL = "https://api.xypai.com/api/auth"
    
    // 用户名密码登录
    func login(username: String, password: String, deviceId: String) async throws -> LoginResponse {
        let url = URL(string: "\(baseURL)/login")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let loginRequest = LoginRequest(
            username: username,
            password: password,
            clientType: "app",
            deviceId: deviceId
        )
        
        request.httpBody = try JSONEncoder().encode(loginRequest)
        
        let (data, _) = try await URLSession.shared.data(for: request)
        let response = try JSONDecoder().decode(ApiResponse<LoginResponse>.self, from: data)
        
        if response.code == 200 {
            // 保存token
            TokenManager.shared.saveToken(response.data.accessToken)
            return response.data
        } else {
            throw AuthError.loginFailed(response.message)
        }
    }
    
    // 短信验证码登录
    func smsLogin(mobile: String, code: String, deviceId: String) async throws -> LoginResponse {
        let url = URL(string: "\(baseURL)/login/sms")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let smsRequest = SmsLoginRequest(
            mobile: mobile,
            code: code,
            clientType: "app",
            deviceId: deviceId
        )
        
        request.httpBody = try JSONEncoder().encode(smsRequest)
        
        let (data, _) = try await URLSession.shared.data(for: request)
        let response = try JSONDecoder().decode(ApiResponse<LoginResponse>.self, from: data)
        
        if response.code == 200 {
            TokenManager.shared.saveToken(response.data.accessToken)
            return response.data
        } else {
            throw AuthError.smsLoginFailed(response.message)
        }
    }
}
```

## 🎮 小程序端调用示例

### 微信小程序

```javascript
// 小程序登录服务
class AuthService {
  static baseURL = 'https://api.xypai.com/api/auth'
  
  // 用户名密码登录
  static async login(username, password) {
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${this.baseURL}/login`,
        method: 'POST',
        header: {
          'Content-Type': 'application/json'
        },
        data: {
          username: username,
          password: password,
          clientType: 'mini'  // 小程序类型
        },
        success: (res) => {
          if (res.data.code === 200) {
            // 保存token
            wx.setStorageSync('token', res.data.data.access_token)
            resolve(res.data.data)
          } else {
            reject(new Error(res.data.message))
          }
        },
        fail: reject
      })
    })
  }
  
  // 短信验证码登录
  static async smsLogin(mobile, code) {
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${this.baseURL}/login/sms`,
        method: 'POST',
        header: {
          'Content-Type': 'application/json'
        },
        data: {
          mobile: mobile,
          code: code,
          clientType: 'mini'
        },
        success: (res) => {
          if (res.data.code === 200) {
            wx.setStorageSync('token', res.data.data.access_token)
            resolve(res.data.data)
          } else {
            reject(new Error(res.data.message))
          }
        },
        fail: reject
      })
    })
  }
  
  // 获取用户信息
  static async getUserInfo() {
    const token = wx.getStorageSync('token')
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${this.baseURL}/info`,
        method: 'GET',
        header: {
          'Authorization': `Bearer ${token}`
        },
        success: (res) => {
          if (res.data.code === 200) {
            resolve(res.data.data)
          } else {
            reject(new Error(res.data.message))
          }
        },
        fail: reject
      })
    })
  }
}

// 页面中使用
Page({
  async onLogin() {
    try {
      const result = await AuthService.login('user001', '123456')
      console.log('登录成功:', result)
      
      // 跳转到主页
      wx.switchTab({
        url: '/pages/index/index'
      })
    } catch (error) {
      wx.showToast({
        title: error.message,
        icon: 'none'
      })
    }
  }
})
```

## 🔄 通用操作示例

### 刷新Token

```bash
POST /api/auth/refresh
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# 响应
{
  "code": 200,
  "message": "success",
  "data": {
    "access_token": "new_token_here...",
    "token_type": "Bearer",
    "expires_in": 7200,
    "username": "admin",
    "nickname": "系统管理员",
    "issued_at": "2024-01-15T11:00:00Z"
  }
}
```

### 退出登录

```bash
DELETE /api/auth/logout
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# 响应
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 检查Token有效性

```bash
GET /api/auth/validate
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# 响应
{
  "code": 200,
  "message": "success",
  "data": {
    "valid": true,
    "username": "admin",
    "clientType": "web",
    "userType": "admin",
    "expireTime": 1705334400000,
    "remainingTime": 7200000
  }
}
```

## 🎯 关键要点

### 1. ClientType参数是核心

```javascript
// ✅ 正确：明确指定客户端类型
{
  "username": "admin",
  "password": "123456",
  "clientType": "web"  // 关键参数
}

// ❌ 错误：缺少clientType
{
  "username": "admin", 
  "password": "123456"
}
```

### 2. 不同客户端使用相同接口

```bash
# 所有客户端都使用相同的登录接口
POST /api/auth/login

# 区别仅在于clientType参数：
# web  → 管理端认证策略
# app  → 移动端认证策略  
# mini → 小程序认证策略(使用app策略)
```

### 3. Token过期时间差异化

```javascript
// 管理端：2小时过期
"expires_in": 7200

// 移动端：24小时过期
"expires_in": 86400
```

这个统一架构的最大优势是：**一个接口，智能路由，简化运维！** 🚀
