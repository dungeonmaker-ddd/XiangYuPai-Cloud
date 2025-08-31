package com.xypai.common.swagger.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 📚 Swagger/Knife4j 配置属性类
 *
 * @author XyPai
 * @version 3.0.0
 * @since 2025-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "swagger")
public class SwaggerProperties {

    /**
     * 是否启用 Swagger
     */
    private Boolean enabled = true;

    /**
     * API 标题
     */
    private String title = "XyPai 微服务接口文档";

    /**
     * API 描述
     */
    private String description = "基于 Spring Boot 3.x + Knife4j 的企业级微服务架构";

    /**
     * API 版本
     */
    private String version = "3.6.6";

    /**
     * 服务条款 URL
     */
    private String termsOfServiceUrl = "https://www.xypai.com/terms";

    /**
     * 许可证
     */
    private String license = "Apache License 2.0";

    /**
     * 许可证 URL
     */
    private String licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0";

    /**
     * 联系人信息
     */
    private Contact contact = new Contact();

    /**
     * 扫描的包路径
     */
    private List<String> basePackages = new ArrayList<>();

    /**
     * 排除的路径
     */
    private List<String> excludePaths = new ArrayList<>();

    /**
     * 分组配置
     */
    private List<GroupInfo> groups = new ArrayList<>();

    /**
     * 认证配置
     */
    private Authorization authorization = new Authorization();

    /**
     * Knife4j 增强配置
     */
    private Knife4j knife4j = new Knife4j();

    /**
     * 联系人信息
     */
    @Data
    public static class Contact {
        /**
         * 联系人姓名
         */
        private String name = "XyPai Team";

        /**
         * 联系人邮箱
         */
        private String email = "support@xypai.com";

        /**
         * 联系人网站
         */
        private String url = "https://www.xypai.com";
    }

    /**
     * 分组信息
     */
    @Data
    public static class GroupInfo {
        /**
         * 分组名称
         */
        private String name;

        /**
         * 扫描包路径
         */
        private String basePackage;

        /**
         * 路径匹配
         */
        private String pathsToMatch = "/**";

        /**
         * 排除路径
         */
        private List<String> excludePaths = new ArrayList<>();
    }

    /**
     * 认证配置
     */
    @Data
    public static class Authorization {
        /**
         * 认证类型
         */
        private String type = "Bearer";

        /**
         * 认证名称
         */
        private String name = "Authorization";

        /**
         * 认证描述
         */
        private String description = "JWT 认证令牌";

        /**
         * Token 传递位置
         */
        private String keyLocation = "header";

        /**
         * 认证 URL
         */
        private String authUrl = "";

        /**
         * Token URL
         */
        private String tokenUrl = "";

        /**
         * 作用域
         */
        private List<AuthScope> scopes = new ArrayList<>();
    }

    /**
     * 认证作用域
     */
    @Data
    public static class AuthScope {
        /**
         * 作用域名称
         */
        private String scope;

        /**
         * 作用域描述
         */
        private String description;
    }

    /**
     * Knife4j 增强配置
     */
    @Data
    public static class Knife4j {
        /**
         * 是否启用 Knife4j 增强
         */
        private Boolean enable = true;

        /**
         * 生产环境屏蔽
         */
        private Boolean production = false;

        /**
         * Basic 认证
         */
        private Basic basic = new Basic();

        /**
         * 自定义设置
         */
        private Setting setting = new Setting();
    }

    /**
     * Basic 认证配置
     */
    @Data
    public static class Basic {
        /**
         * 是否启用 Basic 认证
         */
        private Boolean enable = false;

        /**
         * 用户名
         */
        private String username = "admin";

        /**
         * 密码
         */
        private String password = "123456";
    }

    /**
     * 自定义设置
     */
    @Data
    public static class Setting {
        /**
         * 是否启用动态参数
         */
        private Boolean enableDynamicParameter = true;

        /**
         * 是否启用调试
         */
        private Boolean enableDebug = true;

        /**
         * 是否启用搜索
         */
        private Boolean enableSearch = true;

        /**
         * 是否启用 Footer
         */
        private Boolean enableFooter = false;

        /**
         * 是否启用 Footer 自定义
         */
        private Boolean enableFooterCustom = true;

        /**
         * Footer 自定义内容
         */
        private String footerCustomContent = "Copyright © 2025 XyPai. All rights reserved.";

        /**
         * 是否启用主页自定义
         */
        private Boolean enableHomeCustom = true;

        /**
         * 主页自定义路径
         */
        private String homeCustomPath = "/doc.html";
    }
}
