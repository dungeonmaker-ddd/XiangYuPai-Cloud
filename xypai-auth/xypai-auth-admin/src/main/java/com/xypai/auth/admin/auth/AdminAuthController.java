package com.xypai.auth.admin.auth;

import com.xypai.auth.admin.service.AdminAuthService;
import com.xypai.auth.common.dto.LoginRequest;
import com.xypai.auth.common.vo.LoginResponse;
import com.xypai.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 🏛️ 管理端认证控制器
 * <p>
 * 管理端独立的认证功能，包含：
 * - 管理员登录（严格验证）
 * - 权限验证
 * - 会话管理
 * - 安全审计
 *
 * @author xypai
 * @version 4.0.0
 */
@Tag(name = "🏛️ 管理端认证服务", description = "后台管理系统专用认证功能")
@RestController
@RequestMapping("/admin/auth")
@Validated
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    /**
     * 🏛️ 管理端登录
     */
    @Operation(summary = "🏛️ 管理端登录", description = "管理员专用登录，执行严格的安全验证")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "🎉 登录成功"),
            @ApiResponse(responseCode = "400", description = "❌ 参数无效"),
            @ApiResponse(responseCode = "401", description = "🚫 认证失败"),
            @ApiResponse(responseCode = "403", description = "🚫 权限不足")
    })
    @PostMapping("/login")
    public ResponseEntity<R<LoginResponse>> adminLogin(@Valid @RequestBody LoginRequest request,
                                                       HttpServletRequest httpRequest) {
        return adminAuthService.processAdminLogin(request, httpRequest);
    }

    /**
     * 🚪 管理端退出登录
     */
    @Operation(summary = "🚪 管理端退出", description = "管理员退出登录，清除会话和权限缓存")
    @DeleteMapping("/logout")
    public ResponseEntity<R<Void>> adminLogout(HttpServletRequest request) {
        return adminAuthService.processAdminLogout(request);
    }

    /**
     * 🔄 刷新管理端令牌
     */
    @Operation(summary = "🔄 刷新管理令牌", description = "延长管理员token的有效期")
    @PostMapping("/refresh")
    public ResponseEntity<R<LoginResponse>> refreshAdminToken(HttpServletRequest request) {
        return adminAuthService.refreshAdminToken(request);
    }

    /**
     * 📋 获取管理员信息
     */
    @Operation(summary = "📋 获取管理员信息", description = "获取当前登录管理员的详细信息和权限")
    @GetMapping("/info")
    public ResponseEntity<R<Object>> getAdminInfo(HttpServletRequest request) {
        return adminAuthService.getCurrentAdminInfo(request);
    }

    /**
     * ✅ 验证管理端权限
     */
    @Operation(summary = "✅ 验证管理权限", description = "验证当前管理员是否具有特定权限")
    @PostMapping("/validate-permission")
    public ResponseEntity<R<Object>> validatePermission(@RequestParam String permission,
                                                        HttpServletRequest request) {
        return adminAuthService.validateAdminPermission(permission, request);
    }

    /**
     * 🔍 获取在线管理员列表
     */
    @Operation(summary = "🔍 在线管理员", description = "获取当前在线的管理员列表")
    @GetMapping("/online-admins")
    public ResponseEntity<R<Object>> getOnlineAdmins() {
        return adminAuthService.getOnlineAdmins();
    }

    /**
     * ⚡ 强制下线管理员
     */
    @Operation(summary = "⚡ 强制下线", description = "强制指定管理员下线")
    @PostMapping("/force-logout/{adminId}")
    public ResponseEntity<R<Void>> forceLogout(@PathVariable Long adminId,
                                               HttpServletRequest request) {
        return adminAuthService.forceAdminLogout(adminId, request);
    }
}
