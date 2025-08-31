# SMS Service 架构重构说明

## 架构变更概述

本项目已从DDD（领域驱动设计）架构重构为**业务控制层**架构，符合现代微服务开发的最佳实践。

## 最终优化的目录结构

```
sms-service/
├── controller/                # 🔥 业务控制层
│   ├── rest/                  # REST API控制器
│   │   ├── SmsController      # 短信发送控制器
│   │   └── SmsTemplateController # 模板管理控制器
│   ├── feign/                 # Feign远程调用客户端
│   │   ├── UserServiceClient  # 用户服务客户端
│   │   └── NotificationServiceClient # 通知服务客户端
│   └── dto/                   # 数据传输对象
│       ├── SmsSendRequest     # 短信发送请求
│       ├── SmsSendResponse    # 短信发送响应
│       ├── SmsTemplateDto     # 短信模板DTO
│       ├── SmsStatusResponse  # 状态响应
│       └── TemplateQueryRequest # 模板查询请求
├── common/                    # ⚙️ 配置层
│   ├── config/                # 配置类
│   │   ├── DatabaseConfig     # 数据库配置
│   │   ├── RedisConfig        # Redis配置
│   │   ├── MqConfig           # 消息队列配置
│   │   └── SwaggerConfig      # API文档配置
│   ├── properties/            # 配置属性
│   │   ├── SmsProperties      # SMS配置属性
│   │   └── ChannelProperties  # 渠道配置属性
│   ├── exception/             # 异常处理
│   │   ├── BusinessException  # 业务异常
│   │   └── GlobalExceptionHandler # 全局异常处理器
│   └── constant/              # 常量定义
│       ├── SmsConstants       # SMS常量
│       └── ChannelConstants   # 渠道常量
└── service/                   # 🛠️ 服务层
    ├── business/              # 业务服务
    │   ├── SmsService         # 短信发送业务
    │   ├── TemplateService    # 模板管理业务
    │   ├── ChannelService     # 渠道管理业务
    │   └── ValidationService  # 数据验证业务
    ├── repository/            # 数据访问
    │   ├── SmsTemplateRepositoryService    # 模板仓储服务
    │   └── SendRecordRepositoryService     # 发送记录仓储服务
    └── util/                  # 工具类
        ├── ValidationUtil     # 验证工具
        ├── DateUtil          # 日期工具
        └── JsonUtil          # JSON工具
```

## 架构原则

### 1. Records优先原则

- 所有DTO使用Java Records
- 利用Records的紧凑构造器进行数据验证
- 不可变性保证数据安全

### 2. 层次清晰原则

- **Controller层**：处理HTTP请求，数据验证，调用业务服务
- **Common层**：配置管理，异常处理，常量定义
- **Service层**：业务逻辑，数据访问，工具方法

### 3. 职责单一原则

- 每个类只负责一个明确的职责
- 业务逻辑与基础设施代码分离
- 配置集中管理

### 4. 实用优先原则

- 避免过度工程化
- 选择适合团队规模的技术复杂度
- 代码简洁易维护

## 关键特性

### Records使用规范

```java
// ✅ 正确的Records使用
public record SmsSendRequest(
    @NotBlank String templateCode,
    @NotEmpty Set<String> phoneNumbers
) {
    // 紧凑构造器验证
    public SmsSendRequest {
        Objects.requireNonNull(templateCode, "模板代码不能为空");
        if (phoneNumbers.isEmpty()) {
            throw new IllegalArgumentException("手机号列表不能为空");
        }
    }
}
```

### 异常处理策略

```java
// 业务异常统一处理
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        // 统一的异常响应格式
    }
}
```

### 配置管理

```java
// 配置属性集中管理
@ConfigurationProperties(prefix = "xypai.sms")
public class SmsProperties {
    private SendConfig send = new SendConfig();
    private Map<String, ChannelConfig> channels;
}
```

## 与DDD架构的对比

| 方面   | DDD架构         | 业务控制层架构     |
|------|---------------|-------------|
| 复杂度  | 高，需要深入理解DDD概念 | 中等，符合传统分层思维 |
| 学习成本 | 高，需要领域建模知识    | 低，易于理解和上手   |
| 维护性  | 好，但需要专业知识     | 很好，结构清晰     |
| 适用场景 | 复杂业务领域        | 大多数企业应用     |
| 团队要求 | 需要DDD专家       | 普通开发团队即可    |

## 迁移说明

### 已迁移的组件

1. **控制器**：从 `interfaces.web` 迁移到 `controller`
2. **配置类**：从 `config` 迁移到 `common.config`
3. **异常处理**：从 `interfaces.exception` 迁移到 `common.exception`
4. **DTO对象**：新建 `dto` 包，使用Records重写

### 保留的组件

1. **数据库相关**：保留原有的 `infrastructure.persistence` 结构
2. **领域模型**：保留 `domain.model` 作为数据模型参考
3. **事件机制**：可选保留，用于复杂业务场景

## 开发指南

### 新增功能流程

1. **定义DTO**：在 `dto` 包中使用Records定义请求/响应对象
2. **实现Controller**：在 `controller` 包中实现REST API
3. **编写业务逻辑**：在 `service.business` 包中实现业务服务
4. **数据访问**：在 `service.repository` 包中实现数据访问逻辑

### 代码规范

1. 使用Records替代传统POJO
2. 业务异常继承BusinessException
3. 配置属性使用@ConfigurationProperties
4. 工具方法放在util包中

## 性能与扩展性

### 性能优化

- Records减少了内存占用
- 简化的架构降低了方法调用开销
- 集中的配置管理提高了启动速度

### 扩展性

- 清晰的层次结构便于功能扩展
- 模块化设计支持独立开发和测试
- 标准化的异常处理便于监控和调试

## 架构优化亮点

### 🎯 清晰的层次分离

- **Controller层**：专注于接口定义和数据传输
    - `rest/`：REST API控制器
    - `feign/`：远程服务调用客户端
    - `dto/`：数据传输对象
- **Common层**：统一的基础设施管理
- **Service层**：核心业务逻辑实现

### 📦 合理的包结构

- 每个层级职责明确，包名见名知意
- DTO与Controller紧密耦合，便于接口管理
- 工具类集中管理，提高复用性

### 🔧 现代化实践

- Records优先，减少样板代码
- 配置集中化管理
- 异常统一处理
- 常量标准化定义

## 总结

经过彻底的架构重构，新的业务控制层架构实现了：

1. **简化复杂度**：从DDD的4层架构简化为3层清晰架构
2. **提高可维护性**：标准化的目录结构，降低学习成本
3. **增强可扩展性**：模块化设计，便于功能扩展
4. **优化开发体验**：符合主流开发习惯，团队上手容易

这种架构更适合大多数企业级应用的需求，在保持代码质量的同时，大大提高了开发效率。
