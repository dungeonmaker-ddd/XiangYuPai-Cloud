# 🚀 XyPai 智能化 Swagger 配置方案

## 🎯 设计理念

基于**YAGNI**和**DRY**原则，实现了一个智能化的Swagger配置系统，避免每个微服务重复配置OpenAPI Bean。

## 🏗️ 架构设计

### 📦 统一配置层

- `Knife4jAutoConfiguration` - 智能化OpenAPI配置
- `SimpleSwaggerConfig` - 极简配置备选方案
- `SpringDocAutoConfiguration` - 底层SpringDoc配置

### 🎛️ 工作原理

```java
// 1. 自动读取 spring.application.name
// 2. 根据服务名称智能匹配预设配置
// 3. 生成对应的API文档标题、描述、网关路径
// 4. 只在没有自定义OpenAPI Bean时启用(@ConditionalOnMissingBean)
```

## 🎨 支持的微服务

| 服务名称                  | API标题               | 网关路径                | 端口   |
|-----------------------|---------------------|---------------------|------|
| `xypai-auth-admin`    | 🏛️ XyPai 管理端服务API  | `/api/admin`        | 8101 |
| `xypai-auth-app-auth` | 🎯 XyPai APP认证服务API | `/api/app/auth`     | 8100 |
| `xypai-auth-app`      | 📱 XyPai APP业务服务API | `/api/app/business` | 8102 |
| 其他服务                  | 🚀 XyPai 微服务API     | `/api`              | 自动检测 |

## 🔧 使用方式

### ✅ 正确的方式（推荐）

1. **只需要引入依赖**：

```xml
<dependency>
    <groupId>com.xypai</groupId>
    <artifactId>xypai-common-swagger</artifactId>
</dependency>

<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
    <version>${knife4j.version}</version>
</dependency>
```

2. **配置application.yml**：

```yaml
knife4j:
  enable: true
  setting:
    language: zh_CN
    enable-version: true
  production: false

springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
```

3. **无需任何@Configuration类** - 全自动！

### ❌ 错误的方式（避免）

```java
// ❌ 不要再创建这样的配置类
@Configuration
public class Knife4jConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        // 重复配置，会导致Bean冲突
    }
}
```

## 🎯 自定义配置

如果需要特殊定制，可以在配置文件中覆盖：

```yaml
# 覆盖默认配置
springdoc:
  info:
    title: "我的自定义API"
    description: "自定义描述"
    version: "2.0.0"
```

## 🔄 扩展新服务

添加新的微服务时，只需在 `getServiceInfo()` 方法中增加case即可：

```java
case "xypai-new-service" -> new ServiceInfo(
    "🆕 新微服务API",
    "新服务的描述",
    "/api/new"
);
```

## 💡 最佳实践

1. **🎯 统一配置**：所有微服务使用同一套配置逻辑
2. **⚡ 自动化**：基于服务名称自动生成文档信息
3. **🔧 可扩展**：新增服务只需要简单配置
4. **📋 条件化**：支持自定义配置覆盖
5. **🛡️ 防冲突**：@ConditionalOnMissingBean避免Bean冲突

## 🚨 注意事项

- ✅ 确保 `spring.application.name` 配置正确
- ✅ 端口号会自动从 `server.port` 读取
- ✅ 如果需要完全自定义，可以创建自己的@Bean(name="customOpenAPI")
- ❌ 不要在微服务中创建名为 `openAPI` 的Bean
