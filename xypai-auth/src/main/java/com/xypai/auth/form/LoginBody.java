package com.xypai.auth.form;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户登录对象
 *
 * @author ruoyi
 */
@Schema(description = "用户登录信息")
public class LoginBody {
    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin", required = true)
    private String username;

    /**
     * 用户密码
     */
    @Schema(description = "用户密码", example = "admin123", required = true)
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
