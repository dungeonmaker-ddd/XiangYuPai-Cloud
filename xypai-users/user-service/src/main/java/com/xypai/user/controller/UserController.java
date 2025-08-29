package com.xypai.user.controller;

import com.xypai.common.result.Result;
import com.xypai.user.dto.UserCreateRequest;
import com.xypai.user.dto.UserResponse;
import com.xypai.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 📖 用户控制器 - MVP版本
 * <p>
 * 🔗 路由配置:
 * - 本地调试: http://localhost:8082/users/**
 * - Gateway路由: http://gateway:8080/users/**
 * <p>
 * 🧪 快速测试:
 * 1. 健康检查: GET /users/health
 * 2. 注册用户: POST /users/register (需要JSON请求体)
 * 3. 查询用户: GET /users/1
 * 4. 检查用户名: GET /users/check/username?username=admin
 * <p>
 * 📋 状态码说明:
 * - 200: 操作成功
 * - 400: 请求参数错误
 * - 404: 资源不存在
 * - 500: 服务器内部错误
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Slf4j
@Tag(name = "👤 用户管理", description = "用户基础信息管理相关接口")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 🚀 健康检查
     */
    @Operation(summary = "健康检查", description = "检查用户服务运行状态")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("用户服务运行正常 🚀");
    }

    /**
     * 📖 注册用户
     * <p>
     * 📋 调试请求体示例:
     * {
     * "mobile": "13800138001",
     * "username": "testuser001",
     * "nickname": "测试用户昵称"
     * }
     * <p>
     * 🔧 字段说明:
     * - mobile: 手机号，必填，11位数字，用于登录和验证
     * - username: 用户名，必填，3-20字符，支持字母数字下划线
     * - nickname: 昵称，必填，1-50字符，用于显示
     * <p>
     * 🧪 更多测试用例:
     * 1. 正常注册: {"mobile":"13912345678","username":"normaluser","nickname":"普通用户"}
     * 2. 短用户名测试: {"mobile":"13812345678","username":"ab","nickname":"短名测试"} (应该失败)
     * 3. 手机号重复测试: {"mobile":"13800138000","username":"newuser","nickname":"重复手机号"} (应该失败)
     */
    @Operation(
            summary = "注册用户",
            description = "用户注册接口。手机号和用户名必须唯一，系统会自动分配用户ID和初始状态。注册成功后用户状态为ACTIVE。"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "注册成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "用户注册成功",
                                              "data": {
                                                "id": 10,
                                                "username": "testuser001", 
                                                "mobile": "13800138001",
                                                "nickname": "测试用户昵称",
                                                "avatar": null,
                                                "status": "ACTIVE",
                                                "createTime": "2025-01-02T10:30:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "注册失败",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "code": 400,
                                              "message": "注册失败: 手机号已被注册"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/register")
    public Result<UserResponse> registerUser(
            @RequestBody(
                    description = "用户注册请求，包含手机号、用户名、昵称等必要信息",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserCreateRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "正常注册",
                                            description = "标准的用户注册请求示例",
                                            value = """
                                                    {
                                                      "mobile": "13800138001",
                                                      "username": "testuser001",
                                                      "nickname": "测试用户昵称"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "最小示例",
                                            description = "包含所有必填字段的最小示例",
                                            value = """
                                                    {
                                                      "mobile": "13912345678",
                                                      "username": "minuser",
                                                      "nickname": "最小用户"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "长用户名示例",
                                            description = "测试用户名长度限制",
                                            value = """
                                                    {
                                                      "mobile": "13812345678",
                                                      "username": "very_long_username_20",
                                                      "nickname": "长用户名测试用户"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody UserCreateRequest request) {
        try {
            UserResponse user = userService.createUser(request);
            return Result.success("用户注册成功", user);
        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage(), e);
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    /**
     * 📖 根据ID获取用户
     * <p>
     * 调试参数:
     * - id: 1 (存在的用户)
     * - id: 999 (不存在的用户，测试错误处理)
     */
    @Operation(
            summary = "根据ID获取用户",
            description = "通过用户ID查询用户详细信息。返回用户的基本信息，不包含敏感数据。"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "查询成功",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "code": 200,
                                              "data": {
                                                "id": 1,
                                                "username": "admin",
                                                "mobile": "13800138000", 
                                                "nickname": "管理员",
                                                "avatar": "https://example.com/admin.jpg",
                                                "status": "ACTIVE",
                                                "createTime": "2025-01-01T09:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "用户不存在",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "code": 404,
                                              "message": "用户不存在"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public Result<UserResponse> getUserById(
            @Parameter(
                    description = "用户ID，必须是正整数",
                    required = true,
                    example = "1",
                    schema = @Schema(type = "integer", minimum = "1")
            )
            @PathVariable Long id) {
        try {
            UserResponse user = userService.getUserById(id);
            return Result.success(user);
        } catch (Exception e) {
            log.error("获取用户失败: id={}, error={}", id, e.getMessage());
            return Result.notFound("用户不存在");
        }
    }

    /**
     * 📖 根据用户名获取用户
     * <p>
     * 调试参数:
     * - username: "admin" (管理员用户)
     * - username: "testuser" (普通测试用户)
     * - username: "nonexistent" (不存在用户，测试错误处理)
     */
    @Operation(
            summary = "根据用户名获取用户",
            description = "通过用户名查询用户详细信息。用户名不区分大小写。"
    )
    @GetMapping("/username/{username}")
    public Result<UserResponse> getUserByUsername(
            @Parameter(
                    description = "用户名，长度3-20字符，支持字母数字下划线",
                    required = true,
                    example = "admin",
                    schema = @Schema(type = "string", minLength = 3, maxLength = 20, pattern = "^[a-zA-Z0-9_]+$")
            )
            @PathVariable String username) {
        try {
            UserResponse user = userService.getUserByUsername(username);
            return Result.success(user);
        } catch (Exception e) {
            log.error("获取用户失败: username={}, error={}", username, e.getMessage());
            return Result.notFound("用户不存在");
        }
    }

    /**
     * 📖 获取用户列表
     * <p>
     * 调试说明:
     * - 直接调用即可，无需参数
     * - 返回所有启用状态的用户列表
     * - 数据量较大时考虑分页（后续版本支持）
     */
    @Operation(
            summary = "获取用户列表",
            description = "获取系统中所有用户的基本信息列表。当前版本返回所有用户，后续版本将支持分页和筛选。"
    )
    @GetMapping
    public Result<List<UserResponse>> getUserList() {
        List<UserResponse> users = userService.getUserList();
        return Result.success(users);
    }

    /**
     * 📖 检查手机号是否存在
     * <p>
     * 调试参数:
     * - mobile: "13800138000" (标准格式)
     * - mobile: "18612345678" (另一个测试号码)
     * - mobile: "invalid" (无效格式，测试验证)
     */
    @Operation(
            summary = "检查手机号是否存在",
            description = "验证手机号是否已被注册。返回true表示已存在，false表示可用。"
    )
    @GetMapping("/check/mobile")
    public Result<Boolean> checkMobile(
            @Parameter(
                    description = "手机号码，11位数字，支持中国大陆号段",
                    required = true,
                    example = "13800138000",
                    schema = @Schema(type = "string", pattern = "^1[3-9]\\d{9}$")
            )
            @RequestParam String mobile) {
        boolean exists = userService.existsByMobile(mobile);
        return Result.success(exists);
    }

    /**
     * 📖 检查用户名是否存在
     * <p>
     * 调试参数:
     * - username: "admin" (已存在)
     * - username: "newuser" (可用用户名)
     * - username: "ab" (太短，测试验证)
     */
    @Operation(
            summary = "检查用户名是否存在",
            description = "验证用户名是否已被注册。返回true表示已存在，false表示可用。"
    )
    @GetMapping("/check/username")
    public Result<Boolean> checkUsername(
            @Parameter(
                    description = "用户名，长度3-20字符，支持字母数字下划线",
                    required = true,
                    example = "testuser",
                    schema = @Schema(type = "string", minLength = 3, maxLength = 20, pattern = "^[a-zA-Z0-9_]+$")
            )
            @RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return Result.success(exists);
    }

    /**
     * 📖 启用用户
     * <p>
     * 调试参数:
     * - id: 2 (禁用状态的用户ID)
     * - id: 1 (已启用的用户，测试幂等性)
     * - id: 999 (不存在的用户，测试错误处理)
     */
    @Operation(
            summary = "启用用户",
            description = "将指定用户设置为启用状态。启用后用户可以正常登录和使用系统功能。"
    )
    @PutMapping("/{id}/enable")
    public Result<String> enableUser(
            @Parameter(
                    description = "用户ID，必须是正整数",
                    required = true,
                    example = "2"
            )
            @PathVariable Long id) {
        try {
            userService.enableUser(id);
            return Result.success("用户已启用");
        } catch (Exception e) {
            log.error("启用用户失败: id={}, error={}", id, e.getMessage());
            return Result.error("启用用户失败: " + e.getMessage());
        }
    }

    /**
     * 📖 禁用用户
     * <p>
     * 调试参数:
     * - id: 2 (要禁用的用户ID)
     * - id: 1 (管理员用户，测试权限)
     * - id: 999 (不存在的用户，测试错误处理)
     */
    @Operation(
            summary = "禁用用户",
            description = "将指定用户设置为禁用状态。禁用后用户无法登录，但数据保留。"
    )
    @PutMapping("/{id}/disable")
    public Result<String> disableUser(
            @Parameter(
                    description = "用户ID，必须是正整数",
                    required = true,
                    example = "2"
            )
            @PathVariable Long id) {
        try {
            userService.disableUser(id);
            return Result.success("用户已禁用");
        } catch (Exception e) {
            log.error("禁用用户失败: id={}, error={}", id, e.getMessage());
            return Result.error("禁用用户失败: " + e.getMessage());
        }
    }
}
