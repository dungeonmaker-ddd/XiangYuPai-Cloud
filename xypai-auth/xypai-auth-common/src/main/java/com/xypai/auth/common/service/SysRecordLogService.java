package com.xypai.auth.common.service;

import com.xypai.common.core.constant.Constants;
import com.xypai.common.core.constant.SecurityConstants;
import com.xypai.common.core.utils.DateUtils;
import com.xypai.common.core.utils.StringUtils;
import com.xypai.common.core.utils.ip.IpUtils;
import com.xypai.system.api.RemoteLogService;
import com.xypai.system.api.domain.SysLogininfor;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

/**
 * 📝 系统记录日志服务
 * <p>
 * 提供系统登录、登出等操作的日志记录功能：
 * - 登录日志记录
 * - 登出日志记录
 * - 注册日志记录
 * - 基本IP信息获取
 *
 * @author xypai
 * @version 4.1.0
 */
@Service
public class SysRecordLogService {

    private static final Logger logger = LoggerFactory.getLogger(SysRecordLogService.class);

    private final RemoteLogService remoteLogService;

    public SysRecordLogService(RemoteLogService remoteLogService) {
        this.remoteLogService = Objects.requireNonNull(remoteLogService, "远程日志服务不能为空");
    }

    /**
     * 📝 记录登录日志
     */
    public void recordLogininfor(String username, String status, String message) {
        try {
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

            // 创建登录日志对象
            SysLogininfor logininfor = new SysLogininfor();
            logininfor.setUserName(username);
            logininfor.setStatus(status);
            logininfor.setMsg(StringUtils.substring(message, 0, 255));
            logininfor.setAccessTime(DateUtils.getNowDate()); // 使用实际存在的字段

            if (request != null) {
                // 设置IP地址（只使用基本信息）
                String ipAddress = IpUtils.getIpAddr(request);
                logininfor.setIpaddr(ipAddress);
            } else {
                // 非HTTP请求环境的默认值
                logininfor.setIpaddr("127.0.0.1");
            }

            // 异步保存日志
            saveLogininforAsync(logininfor);

            logger.debug("📝 记录登录日志 - 用户: {}, 状态: {}, 消息: {}", username, status, message);

        } catch (Exception e) {
            logger.error("📝 记录登录日志失败 - 用户: {}, 错误: {}", username, e.getMessage(), e);
            // 日志记录失败不影响主业务流程
        }
    }

    /**
     * 📝 记录登录成功日志（便捷方法）
     */
    public void recordLoginSuccess(String username, String message) {
        recordLogininfor(username, Constants.LOGIN_SUCCESS,
                StringUtils.isNotBlank(message) ? message : "登录成功");
    }

    /**
     * 📝 记录登录失败日志（便捷方法）
     */
    public void recordLoginFail(String username, String message) {
        recordLogininfor(username, Constants.LOGIN_FAIL,
                StringUtils.isNotBlank(message) ? message : "登录失败");
    }

    /**
     * 📝 记录登出日志（便捷方法）
     */
    public void recordLogout(String username, String message) {
        recordLogininfor(username, Constants.LOGOUT,
                StringUtils.isNotBlank(message) ? message : "退出成功");
    }

    /**
     * 📝 记录注册日志（便捷方法）
     */
    public void recordRegister(String username, String message) {
        recordLogininfor(username, Constants.REGISTER,
                StringUtils.isNotBlank(message) ? message : "注册成功");
    }

    /**
     * 📤 异步保存登录日志
     */
    private void saveLogininforAsync(SysLogininfor logininfor) {
        try {
            // 调用远程日志服务保存
            remoteLogService.saveLogininfor(logininfor, SecurityConstants.INNER);
            logger.debug("📤 登录日志保存成功 - 用户: {}", logininfor.getUserName());

        } catch (Exception e) {
            logger.error("📤 登录日志保存失败 - 用户: {}, 错误: {}",
                    logininfor.getUserName(), e.getMessage());

            // 日志保存失败不影响主业务流程
            // TODO: 可以考虑将失败的日志保存到本地文件或缓存中，稍后重试
        }
    }

    /**
     * 📊 批量记录登录日志（用于批量操作场景）
     */
    public void recordBatchLoginInfo(String[] usernames, String status, String message) {
        if (usernames == null || usernames.length == 0) {
            return;
        }

        for (String username : usernames) {
            if (StringUtils.isNotBlank(username)) {
                recordLogininfor(username, status, message);
            }
        }
    }
}