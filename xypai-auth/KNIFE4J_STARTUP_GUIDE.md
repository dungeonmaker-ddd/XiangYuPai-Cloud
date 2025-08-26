# 🚀 Knife4j文档服务启动指南

## 📋 配置完成清单

✅ **依赖配置**: 已为所有服务添加Knife4j和SpringDoc依赖  
✅ **配置文件**: 已配置各服务的Knife4j参数  
✅ **配置类**: 已创建OpenAPI配置类  
✅ **接口注解**: 已完善Swagger注解和示例  
✅ **文档总览**: 已创建完整的API文档说明

---

## 🔧 启动步骤

### 1️⃣ 编译项目

```bash
# 在项目根目录执行
mvn clean compile
```

### 2️⃣ 启动各个服务

#### 🎯 APP认证服务 (端口: 8100)

```bash
cd xypai-auth-app-auth
mvn spring-boot:run
```

#### 🏛️ 管理端服务 (端口: 8101)

```bash
cd xypai-auth-admin  
mvn spring-boot:run
```

#### 📱 APP业务服务 (端口: 8102)

```bash
cd xypai-auth-app
mvn spring-boot:run
```

### 3️⃣ 验证服务启动

检查各服务是否正常启动：

```bash
# 检查APP认证服务
curl http://localhost:8100/actuator/health

# 检查管理端服务
curl http://localhost:8101/actuator/health

# 检查APP业务服务
curl http://localhost:8102/actuator/health
```

---

## 📖 访问文档地址

### 🎯 APP认证服务文档

- **Knife4j文档**: [http://localhost:8100/doc.html](http://localhost:8100/doc.html)
- **Swagger UI**: [http://localhost:8100/swagger-ui.html](http://localhost:8100/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8100/v3/api-docs](http://localhost:8100/v3/api-docs)

### 🏛️ 管理端服务文档

- **Knife4j文档**: [http://localhost:8101/doc.html](http://localhost:8101/doc.html)
- **Swagger UI**: [http://localhost:8101/swagger-ui.html](http://localhost:8101/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8101/v3/api-docs](http://localhost:8101/v3/api-docs)

### 📱 APP业务服务文档

- **Knife4j文档**: [http://localhost:8102/doc.html](http://localhost:8102/doc.html)
- **Swagger UI**: [http://localhost:8102/swagger-ui.html](http://localhost:8102/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8102/v3/api-docs](http://localhost:8102/v3/api-docs)

---

## 🧪 测试验证

### 验证APP认证服务文档

1. 访问 [http://localhost:8100/doc.html](http://localhost:8100/doc.html)
2. 应该看到"🎯 XyPai APP认证服务API"标题
3. 展开"🎯 统一认证服务"分组
4. 测试登录接口，使用提供的示例数据

### 验证管理端服务文档

1. 访问 [http://localhost:8101/doc.html](http://localhost:8101/doc.html)
2. 应该看到"🏛️ XyPai 管理端服务API"标题
3. 查看"🏛️ 管理端认证服务"和"🏛️ 管理端业务中心"分组
4. 测试配置获取接口

### 验证APP业务服务文档

1. 访问 [http://localhost:8102/doc.html](http://localhost:8102/doc.html)
2. 应该看到"📱 XyPai APP业务服务API"标题
3. 查看"📱 APP业务中心"分组
4. 测试配置获取接口

---

## 🔧 故障排除

### 问题1: 服务启动失败

**症状**: 服务无法启动，出现端口占用错误  
**解决**: 检查端口是否被占用，或修改配置文件中的端口号

### 问题2: 文档页面无法访问

**症状**: 访问 `/doc.html` 返回404  
**可能原因**:

- Knife4j依赖未正确添加
- 配置类未被扫描到
- Knife4j被禁用

**解决步骤**:

1. 检查pom.xml中的Knife4j依赖
2. 确认配置类在启动类的扫描路径下
3. 检查配置文件中 `knife4j.enable: true`

### 问题3: 接口示例数据不显示

**症状**: 文档中接口没有示例数据  
**解决**: 检查控制器中的@ExampleObject注解是否正确配置

### 问题4: Bean定义冲突

**症状**: 启动时出现Bean重复定义错误  
**解决**: 检查是否配置了 `spring.main.allow-bean-definition-overriding: true`

---

## 🎯 功能亮点

### 🌟 Knife4j特色功能

- **离线文档**: 支持导出离线HTML文档
- **接口调试**: 在线调试API接口
- **参数校验**: 自动验证请求参数
- **代码生成**: 生成多种语言的SDK代码

### 📋 文档内容

- **完整示例**: 每个接口都有详细的请求/响应示例
- **错误说明**: 详细的错误码和处理建议
- **认证指南**: Token获取和使用说明
- **业务流程**: 完整的业务调用流程图

---

## 📞 技术支持

如果在配置或使用过程中遇到问题，请检查：

1. **日志信息**: 查看控制台启动日志
2. **配置文件**: 确认所有配置项正确
3. **依赖版本**: 确认Knife4j和SpringDoc版本兼容
4. **端口冲突**: 确认端口未被其他服务占用

---

*配置完成时间: 2024年当前时间*  
*文档版本: v4.0.0*
