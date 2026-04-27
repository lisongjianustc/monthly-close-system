package com.mc.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 安全工具类
 */
public class SecurityUtil {

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        // TODO: 从Spring Security Context或Session中获取
        HttpServletRequest request = getHttpServletRequest();
        if (request != null) {
            Object userId = request.getAttribute("userId");
            if (userId != null) {
                return Long.valueOf(userId.toString());
            }
        }
        return null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        HttpServletRequest request = getHttpServletRequest();
        if (request != null) {
            Object username = request.getAttribute("username");
            if (username != null) {
                return username.toString();
            }
        }
        return "anonymous";
    }

    private static HttpServletRequest getHttpServletRequest() {
        try {
            org.springframework.web.context.request.RequestContextHolder
                .getRequestAttributes();
            return ((org.springframework.web.context.request.ServletRequestAttributes)
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
                .getRequest();
        } catch (Exception e) {
            return null;
        }
    }
}
