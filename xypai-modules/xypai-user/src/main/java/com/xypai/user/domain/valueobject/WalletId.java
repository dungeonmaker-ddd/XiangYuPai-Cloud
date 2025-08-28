package com.xypai.user.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * 钱包ID值对象
 *
 * @author XyPai
 * @since 2025-01-02
 */
public record WalletId(String value) {

    public WalletId {
        Objects.requireNonNull(value, "钱包ID不能为空");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("钱包ID不能为空字符串");
        }
    }

    /**
     * 创建钱包ID
     */
    public static WalletId of(String value) {
        return new WalletId(value);
    }

    /**
     * 生成新的钱包ID
     */
    public static WalletId generate() {
        return new WalletId("wallet_" + UUID.randomUUID().toString().replace("-", ""));
    }

    /**
     * 从用户ID创建钱包ID
     */
    public static WalletId fromUserId(UserId userId) {
        Objects.requireNonNull(userId, "用户ID不能为空");
        return new WalletId("wallet_" + userId.value());
    }

    /**
     * 从长整型ID创建
     */
    public static WalletId of(Long id) {
        Objects.requireNonNull(id, "ID不能为空");
        return new WalletId("wallet_" + id);
    }
}
