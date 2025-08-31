# 🏗️ MyBatis Plus 通用配置

## 📝 简介

`MybatisPlusConfig` 是一个位于 `xypai-common-core` 模块中的通用配置类，为所有微服务提供统一的 MyBatis Plus 配置。

## ✨ 配置内容

### 1. 🔄 分页插件 (PaginationInnerInterceptor)

- **数据库类型**: MySQL (可扩展支持其他数据库)
- **最大单页限制**: 1000 条记录
- **JOIN 优化**: 开启 COUNT 查询的 JOIN 优化
- **分页合理化**: 页码超出范围时自动跳转到第一页

### 2. 🔒 乐观锁插件 (OptimisticLockerInnerInterceptor)

- 支持 `@Version` 注解
- 自动处理并发更新冲突
- 基于版本号的乐观锁机制

### 3. 🚫 防攻击插件 (BlockAttackInnerInterceptor)

- 防止全表更新操作
- 防止全表删除操作
- 提高数据安全性

## 🚀 使用方式

### 自动配置

由于使用了 `@AutoConfiguration` 注解，所有引入 `xypai-common-core` 的微服务都会自动加载此配置。

```xml
<!-- 在微服务的 pom.xml 中引入 -->
<dependency>
    <groupId>com.xypai</groupId>
    <artifactId>xypai-common-core</artifactId>
</dependency>
```

### 条件装配

使用 `@ConditionalOnClass(MybatisPlusInterceptor.class)` 确保只有在 MyBatis Plus 存在时才加载配置。

## 🔧 自定义配置

如果某个微服务需要特殊的 MyBatis Plus 配置，可以通过以下方式：

### 1. 覆盖默认配置

```java
@Configuration
@Primary
public class CustomMybatisPlusConfig {
    
    @Bean
    @Primary
    public MybatisPlusInterceptor customMybatisPlusInterceptor() {
        // 自定义配置
    }
}
```

### 2. 排除自动配置

```java
@SpringBootApplication(exclude = {MybatisPlusConfig.class})
public class CustomMicroserviceApplication {
    // 自定义启动类
}
```

## 📊 支持的数据库

当前默认支持 MySQL，但可以通过以下方式扩展：

```java
// 检测数据库类型的示例
@Bean
public MybatisPlusInterceptor mybatisPlusInterceptor(@Value("${spring.datasource.url}") String url) {
    DbType dbType = detectDbType(url);
    // 根据数据库类型设置不同配置
}
```

## 🎯 最佳实践

### 1. 版本控制

在实体类中使用 `@Version` 注解：

```java
@Data
public class User extends BaseEntity {
    @Version
    private Integer version;
}
```

### 2. 分页查询

使用 MP 的分页功能：

```java
// Service 中使用
IPage<User> page = page(new Page<>(1, 10), wrapper);
```

### 3. 安全防护

BlockAttackInnerInterceptor 会自动阻止以下操作：

- `UPDATE table_name SET column = value` (无 WHERE 条件)
- `DELETE FROM table_name` (无 WHERE 条件)

## 🔄 版本历史

- **v1.0.0**: 初始版本，支持分页、乐观锁、防攻击
- **v1.1.0**: 添加自动配置支持
- **v1.2.0**: 优化数据库类型检测

## 🤝 贡献

如需修改或扩展此配置，请：

1. 在 `xypai-common-core` 模块中修改
2. 确保向后兼容性
3. 更新相关文档
4. 通知所有微服务团队

---

> 💡 **提示**: 此配置遵循企业微服务架构规范，确保所有微服务的 MyBatis Plus 配置保持一致。
