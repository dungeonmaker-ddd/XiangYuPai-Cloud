# 📱 XyPai SMS 微服务

基于DDD（领域驱动设计）和事件驱动架构的现代化短信微服务。

## 🎯 核心特性

### 📋 基础功能

- ✅ 多渠道短信发送（阿里云、腾讯云、百度云）
- ✅ 智能负载均衡和故障转移
- ✅ 短信模板管理和内容审核
- ✅ 异步消息处理
- ✅ 发送状态实时跟踪
- ✅ 黑名单管理
- ✅ 发送频率限制

### 🏗️ 架构特点

- **DDD架构**：值对象、聚合根、领域事件、领域服务
- **事件驱动**：异步事件处理，松耦合设计
- **微服务**：独立部署，可扩展
- **高可用**：多渠道冗余，自动故障转移
- **高性能**：连接池、缓存、异步处理

## 📁 项目结构

```
xypai-sms/
├── sms-interface/                    # 📡 接口定义模块
│   └── src/main/java/com/xypai/sms/
│       ├── api/                      # 🔌 Feign接口
│       └── dto/                      # 📦 数据传输对象
│
└── sms-service/                      # 🎯 服务实现模块
    └── src/main/java/com/xypai/sms/
        ├── domain/                   # 🎭 领域层
        │   ├── model/               # 🏛️ 聚合根和实体
        │   │   └── valueobject/     # 💎 值对象
        │   ├── event/               # 📢 领域事件
        │   ├── service/             # 🔧 领域服务
        │   └── repository/          # 🗄️ 仓储接口
        │
        ├── application/             # 🎪 应用层
        │   ├── service/            # 🎬 应用服务
        │   ├── command/            # 📝 命令对象
        │   ├── query/              # 🔍 查询对象
        │   └── eventhandler/       # 🎯 事件处理器
        │
        ├── infrastructure/         # 🏗️ 基础设施层
        │   ├── persistence/        # 💾 持久化
        │   ├── channel/            # 📡 渠道实现
        │   ├── cache/              # 🚀 缓存
        │   └── mq/                 # 📨 消息队列
        │
        └── interfaces/             # 🌐 接口层
            ├── web/                # 🖥️ REST控制器
            └── dto/                # 📋 DTO转换器
```

## 🚀 核心组件

### 📱 值对象 (Value Objects)

- **PhoneNumber**: 手机号验证和运营商识别
- **SmsContent**: 短信内容模板处理
- **ChannelType**: 渠道类型枚举

### 🏛️ 聚合根 (Aggregate Roots)

- **SmsTemplate**: 短信模板管理
- **SmsSendTask**: 发送任务协调
- **SmsChannel**: 渠道配置管理

### 📢 领域事件 (Domain Events)

- **SmsTemplateCreatedEvent**: 模板创建事件
- **SmsTemplateStatusChangedEvent**: 模板状态变更事件
- **SmsSentEvent**: 短信发送事件
- **SmsDeliveredEvent**: 短信送达事件

## 🔧 技术栈

### 后端框架

- **Spring Boot 3.2.0**: 基础框架
- **Spring Cloud 2023.0.0**: 微服务生态
- **MyBatis Plus 3.5.5**: 数据访问层

### 数据存储

- **MySQL 8.0**: 主数据库
- **Redis**: 缓存和会话存储
- **RabbitMQ**: 消息队列

### 短信渠道

- **阿里云短信**: 主要渠道
- **腾讯云短信**: 备用渠道
- **百度云短信**: 补充渠道

### 监控运维

- **Spring Boot Actuator**: 应用监控
- **Prometheus**: 指标收集
- **Knife4j**: API文档

## 🏃‍♂️ 快速开始

### 📋 前置条件

- JDK 21+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+

### 🗄️ 数据库初始化

```sql
-- 执行数据库初始化脚本
source sms-service/src/main/resources/sql/init_xypai_sms_schema.sql
```

### ⚙️ 配置说明

```yaml
# application.yml 关键配置
xypai:
  sms:
    channels:
      aliyun:
        enabled: true
        access-key-id: ${ALIYUN_ACCESS_KEY_ID}
        access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET}
      tencent:
        enabled: true
        secret-id: ${TENCENT_SECRET_ID}
        secret-key: ${TENCENT_SECRET_KEY}
        sdk-app-id: ${TENCENT_SMS_SDK_APP_ID}
```

### 🚀 启动服务

```bash
# 编译项目
mvn clean compile

# 启动服务
mvn spring-boot:run -pl sms-service
```

## 📖 API 使用示例

### 📤 发送验证码短信

```bash
curl -X POST http://localhost:9107/api/v1/sms/send \
  -H "Content-Type: application/json" \
  -d '{
    "templateCode": "USER_REGISTER_VERIFY",
    "phoneNumbers": ["13800138000"],
    "templateParams": {
      "code": "123456",
      "minutes": "5"
    },
    "async": false
  }'
```

### 📋 查询模板列表

```bash
curl -X GET "http://localhost:9107/api/v1/sms/templates?templateType=VERIFICATION"
```

### 📊 查询发送状态

```bash
curl -X GET "http://localhost:9107/api/v1/sms/send/{taskId}/status"
```

## 🎯 业务场景

### 验证码短信

- 用户注册验证
- 密码重置验证
- 登录安全验证
- 支付确认验证

### 通知短信

- 订单状态通知
- 账户安全提醒
- 系统维护通知
- 活动推送消息

### 营销短信

- 促销活动推广
- 新品发布通知
- 会员权益提醒
- 生日祝福消息

## 🔍 监控与运维

### 📊 监控指标

- 发送成功率
- 渠道响应时间
- 错误率统计
- 队列积压情况

### 🚨 告警配置

- 发送失败率超阈值
- 渠道不可用
- 队列积压过多
- 系统资源异常

### 📈 性能优化

- 连接池优化
- 缓存策略调整
- 批量发送优化
- 数据库查询优化

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 开源协议

本项目基于 MIT 协议开源 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 👥 联系我们

- **项目主页**: https://github.com/xypai/xypai-sms
- **问题反馈**: https://github.com/xypai/xypai-sms/issues
- **邮箱**: xypai-team@example.com

---

🎉 **感谢使用 XyPai SMS 微服务！**
