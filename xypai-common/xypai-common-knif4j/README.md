# 📚 XyPai Common Knife4j

## 📖 模块简介

XyPai Common Knife4j 是 XyPai 微服务架构中的全局 API 文档模块，基于 Knife4j 和 OpenAPI 3 规范，提供统一的接口文档管理和展示功能。

## ✨ 核心特性

### 🔧 基础功能

- ✅ **统一文档风格** - 企业级 API 文档标准
- ✅ **多模块分组** - 支持按业务模块分组展示
- ✅ **JWT 认证集成** - 内置 JWT Token 认证支持
- ✅ **生产环境保护** - 生产环境自动屏蔽文档访问

### 🚀 增强功能

- ✅ **Knife4j 增强** - 美观的文档界面和丰富功能
- ✅ **Basic 认证** - 可选的基础认证保护
- ✅ **自定义配置** - 灵活的界面和功能配置
- ✅ **静态资源管理** - 完整的资源映射配置

### 🛡️ 安全特性

- ✅ **访问控制** - 支持多种认证方式
- ✅ **路径排除** - 敏感接口自动排除
- ✅ **环境隔离** - 生产环境自动保护

## 📦 使用方式

### 1. 添加依赖

在需要使用 API 文档的微服务中添加依赖：

```xml
<dependency>
    <groupId>com.xypai</groupId>
    <artifactId>xypai-common-knif4j</artifactId>
</dependency>
```

### 2. 配置文件

在 `application.yml` 中添加配置：

```yaml
swagger:
  enabled: true
  title: "用户微服务 API"
  description: "用户管理相关接口文档"
  base-packages:
    - "com.xypai.user"
  
  knife4j:
    enable: true
    production: false  # 生产环境设置为 true
```

### 3. 访问文档

启动服务后，访问以下地址：

- **Knife4j 文档**: `http://localhost:8080/doc.html`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## 🔧 配置说明

### 基础配置

```yaml
swagger:
  enabled: true                    # 是否启用文档
  title: "API 文档标题"            # 文档标题
  description: "API 文档描述"       # 文档描述
  version: "1.0.0"                # API 版本
  base-packages:                   # 扫描包路径
    - "com.xypai.user"
```

### 分组配置

```yaml
swagger:
  groups:
    - name: "用户管理"
      base-package: "com.xypai.user.controller"
      paths-to-match: "/api/v1/user/**"
    - name: "系统管理"
      base-package: "com.xypai.system.controller"
      paths-to-match: "/api/v1/system/**"
```

### 认证配置

```yaml
swagger:
  authorization:
    type: "Bearer"
    name: "Authorization"
    description: "JWT 认证令牌"
    
  knife4j:
    basic:
      enable: true
      username: "admin"
      password: "123456"
```

## 🎯 使用示例

### Controller 注解示例

```java
@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "用户管理", description = "用户相关操作接口")
@RequiredArgsConstructor
public class UserController {
    
    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取详细信息")
    @Parameter(name = "id", description = "用户ID", required = true)
    public R<UserVO> getUser(@PathVariable Long id) {
        // 实现逻辑
    }
    
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户")
    public R<Void> createUser(@RequestBody @Valid CreateUserDTO dto) {
        // 实现逻辑
    }
}
```

### DTO 注解示例

```java
@Data
@Schema(description = "用户创建请求")
public class CreateUserDTO {
    
    @Schema(description = "用户名", example = "xiaoming", required = true)
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @Schema(description = "邮箱", example = "xiaoming@xypai.com")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Schema(description = "年龄", example = "25", minimum = "1", maximum = "120")
    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 120, message = "年龄必须小于120")
    private Integer age;
}
```

## 📁 目录结构

```
xypai-common-knif4j/
├── src/main/java/com/xypai/common/swagger/
│   ├── config/
│   │   ├── SwaggerAutoConfiguration.java    # 自动配置类
│   │   ├── SwaggerWebMvcConfig.java        # Web MVC 配置
│   │   └── Knife4jConfig.java              # Knife4j 配置
│   └── properties/
│       └── SwaggerProperties.java          # 配置属性类
├── src/main/resources/
│   ├── META-INF/
│   │   ├── spring.factories                # Spring 2.x 自动配置
│   │   └── spring/
│   │       └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│   └── application.yml                     # 默认配置文件
└── README.md
```

## 🚀 高级功能

### 自定义分组

支持按业务模块、版本等维度进行分组：

```yaml
swagger:
  groups:
    - name: "用户模块 v1"
      base-package: "com.xypai.user.v1"
      paths-to-match: "/api/v1/user/**"
    - name: "用户模块 v2"
      base-package: "com.xypai.user.v2"
      paths-to-match: "/api/v2/user/**"
```

### 环境配置

不同环境使用不同配置：

```yaml
# 开发环境
spring:
  profiles: dev
swagger:
  enabled: true
  knife4j:
    production: false

---
# 生产环境
spring:
  profiles: prod
swagger:
  enabled: false
  knife4j:
    production: true
```

### 自定义样式

```yaml
knife4j:
  setting:
    language: zh-CN
    enable-footer-custom: true
    footer-custom-content: "© 2025 XyPai Technology"
    enable-home-custom: true
    home-custom-path: "/doc.html"
```

## 🔍 常见问题

### Q: 生产环境如何禁用文档？

A: 设置 `swagger.enabled: false` 或 `swagger.knife4j.production: true`

### Q: 如何添加全局请求头？

A: 配置 `swagger.authorization` 部分，支持 JWT、Basic 等认证方式

### Q: 如何排除某些接口？

A: 使用 `swagger.exclude-paths` 配置或在 Controller 上使用 `@Hidden` 注解

### Q: 如何自定义文档主题？

A: 通过 `knife4j.setting` 配置项进行界面自定义

## 📄 许可证

本项目基于 Apache License 2.0 开源协议，详情请参阅 [LICENSE](../../../LICENSE) 文件。

## 👥 贡献指南

欢迎提交 Issue 和 Pull Request 来完善本项目。

---

**XyPai Team** © 2025
