# 🔧 Knife4j 问题诊断指南

## 🎯 问题分析

### ✅ **已修复的问题**

1. **移除冲突配置**
    - ✅ 删除 `@EnableWebMvc` 注解（Spring Boot 不需要）
    - ✅ 简化 `springdoc` 配置
    - ✅ 移除多余的配置项

2. **更新JWT令牌示例**
    - ✅ 所有Controller参数示例更新为JWT格式
    - ✅ 添加完整的Schema注解

3. **增强API文档**
    - ✅ 为所有Model添加详细的Schema描述
    - ✅ 添加示例数据

## 🛠️ 当前配置

### 📝 配置文件 (application-dev.yml)

```yaml
# 📚 API文档配置 (Spring Boot 3.x + Knife4j)
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
  packages-to-scan: com.xypai.security.oauth.controller

# Knife4j 增强配置
knife4j:
  enable: true
  production: false
  setting:
    language: zh-cn
```

### 🔧 Java配置 (Knife4jConfig.java)

```java
@Configuration  // 移除了 @EnableWebMvc
public class Knife4jConfig {
    // OpenAPI配置
    // GroupedOpenApi配置
}
```

## 🌐 访问地址

- **Knife4j文档**: http://localhost:9401/doc.html
- **Swagger UI**: http://localhost:9401/swagger-ui.html
- **OpenAPI JSON**: http://localhost:9401/v3/api-docs

## 🔍 常见问题排查

### 1. **端口检查**

```bash
# 确认服务是否在9401端口启动
netstat -an | findstr 9401
# 或
curl http://localhost:9401/auth/health
```

### 2. **依赖检查**

确认pom.xml中包含正确版本：

```xml
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
    <version>4.5.0</version>
</dependency>
```

### 3. **日志检查**

查看启动日志中是否有以下信息：

```
- Started SecurityOauthApplication
- Mapped URL path [/doc.html]
- Mapped URL path [/swagger-ui.html]
```

### 4. **安全配置检查**

确认SecurityConfig允许访问文档路径：

```java
.requestMatchers(
    "/doc.html",
    "/doc.html/**",
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/v3/api-docs/**"
).permitAll()
```

## 🧪 测试步骤

### 步骤1: 健康检查

```bash
curl http://localhost:9401/auth/health
```

期望响应：

```json
{
  "code": 200,
  "data": {
    "status": "UP",
    "service": "xypai-security-oauth",
    "timestamp": 1704067200000
  }
}
```

### 步骤2: OpenAPI文档

```bash
curl http://localhost:9401/v3/api-docs
```

期望响应：包含完整的OpenAPI JSON

### 步骤3: 访问Knife4j界面

浏览器访问：http://localhost:9401/doc.html

### 步骤4: 测试JWT令牌

1. 使用登录接口获取JWT令牌
2. 复制access_token
3. 在其他接口中使用该令牌测试

## 🚨 故障排除

### 问题1: 404 Not Found

**可能原因**：

- 服务未启动
- 端口冲突
- 路径配置错误

**解决方案**：

```bash
# 检查服务状态
curl http://localhost:9401/actuator/health
# 检查端口占用
netstat -an | findstr 9401
```

### 问题2: 空白页面

**可能原因**：

- 静态资源加载失败
- 浏览器缓存问题
- CORS配置问题

**解决方案**：

1. 清除浏览器缓存
2. 使用无痕模式访问
3. 检查浏览器开发者工具Console

### 问题3: JSON解析错误

**可能原因**：

- 注解配置错误
- Model序列化问题
- 循环引用

**解决方案**：

1. 检查Model的Schema注解
2. 验证JSON序列化配置
3. 查看应用日志

## 📊 预期效果

成功配置后，您应该能够：

- ✅ 访问 Knife4j 文档界面
- ✅ 查看所有API接口
- ✅ 直接在界面中测试接口
- ✅ 查看JWT令牌格式示例
- ✅ 使用中文界面

## 🔗 参考链接

- [Knife4j官方文档](https://doc.xiaominfo.com/)
- [SpringDoc OpenAPI](https://springdoc.org/)
- [Spring Boot 3.x兼容性](https://github.com/xiaoymin/knife4j)

---
> 💡 如果仍有问题，请检查启动日志中的错误信息，或联系开发团队。
