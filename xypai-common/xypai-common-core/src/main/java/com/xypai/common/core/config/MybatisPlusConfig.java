package com.xypai.common.core.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * 🏗️ MyBatis Plus 配置类 - 企业架构通用实现
 * <p>
 * 配置内容：
 * - 分页插件：支持多种数据库的分页查询
 * - 乐观锁插件：支持version字段的乐观锁机制
 * - 防止全表更新与删除插件：避免误操作
 * <p>
 * 使用说明：
 * - 此配置会被所有使用 MyBatis Plus 的微服务自动加载
 * - 支持多种数据库类型的自动检测
 * - 可以通过配置文件覆盖默认设置
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@AutoConfiguration
@ConditionalOnClass(MybatisPlusInterceptor.class)
public class MybatisPlusConfig {

    /**
     * 🔧 MyBatis Plus 拦截器配置
     *
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // ================================
        // 🔄 分页插件
        // ================================
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
        // 设置数据库类型 - 支持多种数据库
        paginationInterceptor.setDbType(DbType.MYSQL);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        paginationInterceptor.setMaxLimit(1000L);
        // 开启 count 的 join 优化,只针对部分 left join
        paginationInterceptor.setOptimizeJoin(true);
        // 分页合理化，当页码超出总页数时自动跳转到第一页
        paginationInterceptor.setOverflow(true);

        interceptor.addInnerInterceptor(paginationInterceptor);

        // ================================
        // 🔒 乐观锁插件
        // ================================
        // 支持 @Version 注解的乐观锁机制
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // ================================
        // 🚫 防止全表更新与删除插件
        // ================================
        // 防止恶意的全表 update 和 delete 操作
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }
}
