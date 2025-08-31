# 📖 XyPai Knife4j 使用指南

## 🚀 快速开始

### 1. 添加依赖

在需要集成 API 文档的微服务 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>com.xypai</groupId>
    <artifactId>xypai-common-knif4j</artifactId>
</dependency>
```

### 2. 配置文件

在 `application.yml` 中添加基础配置：

```yaml
swagger:
  enabled: true
  title: "您的微服务名称 API"
  description: "微服务功能描述"
  base-packages:
    - "com.xypai.yourmodule"
```

### 3. 启动访问

启动服务后访问：

- **Knife4j 文档**: `http://localhost:8080/doc.html`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

## 📋 Controller 最佳实践

### 基础注解使用

```java
@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "用户管理", description = "用户相关操作接口")
@RequiredArgsConstructor
@Validated
public class UserController extends BaseController {
    
    private final IUserService userService;
    
    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    @Parameter(name = "id", description = "用户ID", required = true, example = "1")
    @RequiresPermissions("user:query")
    @Log(title = "查询用户", businessType = BusinessType.QUERY)
    public R<UserDetailVO> getUser(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }
    
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户账号")
    @RequiresPermissions("user:add")
    @Log(title = "新增用户", businessType = BusinessType.INSERT)
    public R<Void> createUser(@Validated @RequestBody UserAddDTO dto) {
        return toAjax(userService.addUser(dto));
    }
    
    @PutMapping
    @Operation(summary = "更新用户", description = "更新用户信息")
    @RequiresPermissions("user:edit")
    @Log(title = "修改用户", businessType = BusinessType.UPDATE)
    public R<Void> updateUser(@Validated @RequestBody UserUpdateDTO dto) {
        return toAjax(userService.updateUser(dto));
    }
    
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除用户", description = "批量删除用户")
    @Parameter(name = "ids", description = "用户ID数组", required = true, example = "1,2,3")
    @RequiresPermissions("user:remove")
    @Log(title = "删除用户", businessType = BusinessType.DELETE)
    public R<Void> deleteUsers(@PathVariable Long[] ids) {
        return toAjax(userService.deleteByIds(Arrays.asList(ids)));
    }
    
    @GetMapping("/list")
    @Operation(summary = "用户列表", description = "分页查询用户列表")
    @RequiresPermissions("user:list")
    public TableDataInfo<UserListVO> list(UserQueryDTO query) {
        startPage();
        List<UserListVO> list = userService.selectList(query);
        return getDataTable(list);
    }
}
```

## 📝 DTO 文档注解

### 请求 DTO 示例

```java
@Data
@Schema(description = "用户创建请求")
public class UserAddDTO {
    
    @Schema(description = "用户名", example = "zhangsan", required = true)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度2-20个字符")
    private String username;
    
    @Schema(description = "真实姓名", example = "张三")
    @Size(max = 50, message = "姓名长度不超过50个字符")
    private String realName;
    
    @Schema(description = "邮箱地址", example = "zhangsan@xypai.com")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Schema(description = "手机号码", example = "13812345678")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Schema(description = "年龄", example = "25", minimum = "1", maximum = "120")
    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 120, message = "年龄必须小于120")
    private Integer age;
    
    @Schema(description = "性别", example = "1", allowableValues = {"0", "1", "2"})
    private Integer gender;
    
    @Schema(description = "状态", example = "0", allowableValues = {"0", "1"})
    @NotNull(message = "状态不能为空")
    private Integer status;
    
    @Schema(description = "角色ID列表", example = "[1,2,3]")
    private List<Long> roleIds;
}
```

### 响应 VO 示例

```java
@Data
@Schema(description = "用户详情响应")
public class UserDetailVO {
    
    @Schema(description = "用户ID", example = "1")
    private Long id;
    
    @Schema(description = "用户名", example = "zhangsan")
    private String username;
    
    @Schema(description = "真实姓名", example = "张三")
    private String realName;
    
    @Schema(description = "邮箱地址", example = "zhangsan@xypai.com")
    private String email;
    
    @Schema(description = "手机号码", example = "138****5678")
    @Sensitive(type = DesensitizedType.MOBILE_PHONE)
    private String phone;
    
    @Schema(description = "年龄", example = "25")
    private Integer age;
    
    @Schema(description = "性别", example = "1", allowableValues = {"0", "1", "2"})
    private Integer gender;
    
    @Schema(description = "状态", example = "0", allowableValues = {"0", "1"})
    private Integer status;
    
    @Schema(description = "头像URL", example = "https://cdn.xypai.com/avatar/default.png")
    private String avatar;
    
    @Schema(description = "创建时间", example = "2025-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @Schema(description = "角色列表")
    private List<RoleVO> roles;
    
    @Schema(description = "部门信息")
    private DeptVO dept;
}
```

## 🔧 高级配置

### 多模块分组配置

```yaml
swagger:
  groups:
    # 用户相关接口
    - name: "01-用户管理"
      base-package: "com.xypai.user.controller.admin"
      paths-to-match: "/api/v1/user/**"
      exclude-paths:
        - "/api/v1/user/internal/**"
    
    # 认证相关接口
    - name: "02-认证授权"
      base-package: "com.xypai.user.controller.auth"
      paths-to-match: "/api/v1/auth/**"
    
    # 内部调用接口
    - name: "03-Feign接口"
      base-package: "com.xypai.user.controller.feign"
      paths-to-match: "/api/feign/**"
    
    # 文件上传接口
    - name: "04-文件管理"
      base-package: "com.xypai.user.controller.file"
      paths-to-match: "/api/v1/file/**"
```

### 环境隔离配置

```yaml
# 开发环境
---
spring:
  config:
    activate:
      on-profile: dev
swagger:
  enabled: true
  knife4j:
    production: false
    basic:
      enable: false

# 测试环境
---
spring:
  config:
    activate:
      on-profile: test
swagger:
  enabled: true
  knife4j:
    production: false
    basic:
      enable: true
      username: "test"
      password: "test123"

# 生产环境
---
spring:
  config:
    activate:
      on-profile: prod
swagger:
  enabled: false
```

## 🛡️ 安全配置

### JWT 认证集成

```yaml
swagger:
  authorization:
    type: "Bearer"
    name: "Authorization"
    description: "JWT 认证令牌，格式：Bearer {token}"
    key-location: "header"
```

### Basic 认证保护

```yaml
swagger:
  knife4j:
    basic:
      enable: true
      username: "admin"
      password: "${SWAGGER_PASSWORD:admin123}"
```

## 📚 常用注解说明

### OpenAPI 3 注解

| 注解             | 用途      | 示例                                              |
|----------------|---------|-------------------------------------------------|
| `@Tag`         | 标识控制器分组 | `@Tag(name = "用户管理")`                           |
| `@Operation`   | 描述接口操作  | `@Operation(summary = "获取用户")`                  |
| `@Parameter`   | 描述参数    | `@Parameter(name = "id", description = "用户ID")` |
| `@Schema`      | 描述数据模型  | `@Schema(description = "用户信息")`                 |
| `@ApiResponse` | 描述响应    | `@ApiResponse(responseCode = "200")`            |

### 验证注解

| 注解          | 用途    | 示例                                    |
|-------------|-------|---------------------------------------|
| `@NotNull`  | 非空验证  | `@NotNull(message = "不能为空")`          |
| `@NotBlank` | 非空白验证 | `@NotBlank(message = "不能为空")`         |
| `@Size`     | 长度验证  | `@Size(min = 2, max = 20)`            |
| `@Email`    | 邮箱验证  | `@Email(message = "邮箱格式错误")`          |
| `@Pattern`  | 正则验证  | `@Pattern(regexp = "^1[3-9]\\d{9}$")` |

## 🎯 最佳实践

### 1. 接口命名规范

```java
// ✅ 推荐：清晰的业务含义
@PostMapping("/users")
@Operation(summary = "创建用户", description = "创建新的用户账号")

// ❌ 避免：模糊的命名
@PostMapping("/add")
@Operation(summary = "添加", description = "添加")
```

### 2. 错误处理

```java
@PostMapping
@Operation(summary = "创建用户")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "创建成功"),
    @ApiResponse(responseCode = "400", description = "请求参数错误"),
    @ApiResponse(responseCode = "401", description = "未授权"),
    @ApiResponse(responseCode = "403", description = "无权限"),
    @ApiResponse(responseCode = "500", description = "服务器内部错误")
})
public R<Void> createUser(@RequestBody UserAddDTO dto) {
    // 实现逻辑
}
```

### 3. 分页查询

```java
@GetMapping("/list")
@Operation(summary = "用户列表", description = "分页查询用户列表")
@Parameters({
    @Parameter(name = "pageNum", description = "页码", example = "1"),
    @Parameter(name = "pageSize", description = "每页大小", example = "10"),
    @Parameter(name = "username", description = "用户名（模糊查询）", example = "zhang")
})
public TableDataInfo<UserListVO> list(UserQueryDTO query) {
    startPage();
    List<UserListVO> list = userService.selectList(query);
    return getDataTable(list);
}
```

## 🔍 常见问题

### Q1: 文档无法访问？

**A**: 检查以下配置：

- `swagger.enabled: true`
- `swagger.knife4j.production: false`
- 确保依赖正确引入

### Q2: 接口不显示？

**A**: 检查包扫描配置：

```yaml
swagger:
  base-packages:
    - "com.xypai.yourmodule"
```

### Q3: 认证Token如何配置？

**A**: 在界面右上角"Authorize"按钮中输入：`Bearer {your-jwt-token}`

### Q4: 如何隐藏某些接口？

**A**: 使用以下方式：

- 配置文件排除：`exclude-paths`
- 注解排除：`@Hidden`
- 权限控制：`@RequiresPermissions`

### Q5: 生产环境如何关闭？

**A**: 设置配置：

```yaml
swagger:
  enabled: false
# 或者
swagger:
  knife4j:
    production: true
```

## 📞 技术支持

如有问题，请联系：

- 📧 邮箱: tech-support@xypai.com
- 📖 文档: https://wiki.xypai.com/swagger
- 🐛 Issue: https://github.com/xypai/issues
