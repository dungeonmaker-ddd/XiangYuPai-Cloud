# 🏗️ 新架构设计 - 简洁三层模式

## 🎯 设计理念

采用简洁的三层架构，每层职责清晰，便于开发和维护：

```
┌─────────────────────────────────────────┐
│           🔥 AUTH 层                     │
│     认证业务控制层（包含feign）              │
│  ┌─────────────┬──────────────────────┐  │
│  │ Controller  │      Feign           │  │
│  │ + DTO       │      Client          │  │
│  └─────────────┴──────────────────────┘  │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│           ⚙️ COMMON 层                   │
│            配置管理                      │
│  ┌─────────────┬──────────────────────┐  │
│  │ Config      │   Properties         │  │
│  │ Exception   │   Constants          │  │
│  └─────────────┴──────────────────────┘  │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│           🛠️ SERVICE 层                  │
│       支持业务和实际服务层                │
│  ┌─────────────┬──────────────────────┐  │
│  │ Business    │   Repository         │  │
│  │ Service     │   Util               │  │
│  └─────────────┴──────────────────────┘  │
└─────────────────────────────────────────┘
```

## 📁 详细包结构

### 🔥 auth/ - 认证业务控制层

```
auth/
├── controller/                     # REST控制器
│   ├── AuthController.java         # 认证接口
│   └── TokenController.java        # 令牌管理接口
├── feign/                          # Feign客户端
│   ├── AuthServiceFeign.java       # 认证服务客户端
│   └── fallback/                   # 降级处理
│       └── AuthFeignFallback.java
└── dto/                            # 数据传输对象
    ├── request/                    # 请求DTO
    │   ├── AuthRequest.java
    │   └── TokenRequest.java
    └── response/                   # 响应DTO
        ├── AuthResponse.java
        └── TokenResponse.java
```

### ⚙️ common/ - 配置层

```
common/
├── config/                         # 配置类
│   ├── SecurityConfig.java         # 安全配置
│   ├── RedisConfig.java            # Redis配置
│   └── SwaggerConfig.java          # 文档配置
├── properties/                     # 配置属性
│   ├── AuthProperties.java         # 认证配置
│   └── StorageProperties.java      # 存储配置
├── exception/                      # 异常处理
│   ├── AuthException.java          # 认证异常
│   ├── TokenException.java         # 令牌异常
│   └── GlobalExceptionHandler.java # 全局异常处理
└── constant/                       # 常量定义
    ├── AuthConstants.java          # 认证常量
    └── ErrorCodes.java             # 错误码
```

### 🛠️ service/ - 服务层

```
service/
├── business/                       # 业务服务
│   ├── AuthBusiness.java           # 认证业务接口
│   ├── TokenBusiness.java          # 令牌业务接口
│   └── impl/                       # 业务实现
│       ├── PasswordAuthBusiness.java
│       ├── SmsAuthBusiness.java
│       └── JwtTokenBusiness.java
├── repository/                     # 数据访问
│   ├── UserRepository.java         # 用户数据接口
│   ├── TokenRepository.java        # 令牌数据接口
│   └── impl/                       # 数据访问实现
│       ├── MemoryUserRepository.java
│       ├── RedisTokenRepository.java
│       └── JpaUserRepository.java
└── util/                           # 工具类
    ├── JwtUtil.java                # JWT工具
    ├── SecurityUtil.java           # 安全工具
    └── CacheUtil.java              # 缓存工具
```

## 🎯 三层职责分工

### 🔥 AUTH 层 - 认证业务控制层

- **职责**: 处理HTTP请求响应，对外提供API接口
- **包含**:
    - REST Controller：处理认证、令牌等API请求
    - Feign Client：提供给其他服务调用的客户端
    - DTO：数据传输对象，用于API和服务间通信

### ⚙️ COMMON 层 - 配置层

- **职责**: 统一管理配置、异常、常量等公共组件
- **包含**:
    - Config：Spring配置类（安全、数据库、缓存等）
    - Properties：配置属性类，对应application.yml
    - Exception：异常类和全局异常处理
    - Constant：常量定义，避免魔法值

### 🛠️ SERVICE 层 - 服务层

- **职责**: 核心业务逻辑和数据访问
- **包含**:
    - Business：业务逻辑实现，处理认证、令牌等核心逻辑
    - Repository：数据访问抽象，支持多种存储方式
    - Util：工具类，提供通用功能

## 🔄 调用流程

```
用户请求 
    ↓
AUTH/Controller 
    ↓
SERVICE/Business (业务逻辑)
    ↓
SERVICE/Repository (数据访问)
    ↓
COMMON/Config (配置支持)
```

## 💡 设计优势

1. **🎯 职责清晰**: 每层专注自己的职责
2. **🔧 易于维护**: 层次分明，便于定位问题
3. **🚀 易于扩展**: 新功能只需在对应层添加
4. **👥 团队协作**: 不同开发者可专注不同层
5. **🧪 易于测试**: 每层可独立测试

## 📋 迁移计划

1. **阶段1**: 创建新的三层包结构
2. **阶段2**: 将现有代码迁移到对应层
3. **阶段3**: 重构依赖关系，确保分层清晰
4. **阶段4**: 测试验证，确保功能正常

---

这个简洁的三层架构既保持了清晰的职责分离，又避免了过度设计的复杂性。
