package com.mc.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mc.common.annotation.Audit;
import com.mc.common.entity.AuditLog;
import com.mc.common.service.IAuditLogService;
import com.mc.common.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 审计日志切面
 */
@Slf4j
@Aspect
@Component
public class AuditAspect {

    @Autowired
    private IAuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Around("@annotation(com.mc.common.annotation.Audit)")
    public Object around(ProceedingJoinPoint point, Audit audit) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 执行方法
        Object result = point.proceed();

        try {
            // 构建审计日志
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            Audit auditAnnotation = method.getAnnotation(Audit.class);

            AuditLog auditLog = new AuditLog();
            auditLog.setAction(auditAnnotation.action());
            auditLog.setBusinessType(auditAnnotation.businessType());
            auditLog.setOperatorId(SecurityUtil.getCurrentUserId());
            auditLog.setOperatorName(SecurityUtil.getCurrentUsername());
            auditLog.setMethodName(method.getName());
            auditLog.setClassName(point.getTarget().getClass().getSimpleName());
            auditLog.setExecutionTimeMs(System.currentTimeMillis() - startTime);

            // 获取业务ID - 通过参数名称匹配
            Object[] args = point.getArgs();
            String bizIdParam = auditAnnotation.bizIdParam();
            if (args.length > 0 && bizIdParam != null && !"".equals(bizIdParam)) {
                String[] parameterNames = signature.getParameterNames();
                if (parameterNames != null) {
                    for (int i = 0; i < parameterNames.length; i++) {
                        if (parameterNames[i].equalsIgnoreCase(bizIdParam)) {
                            auditLog.setBizId(toLong(args[i]));
                            break;
                        }
                    }
                }
            }

            // 获取IP地址
            try {
                HttpServletRequest request = getHttpServletRequest();
                if (request != null) {
                    auditLog.setIpAddress(request.getRemoteAddr());
                }
            } catch (Exception e) {
                // ignore
            }

            // 保存到数据库
            auditLogService.saveAuditLog(auditLog);

        } catch (Exception e) {
            log.error("审计日志记录失败", e);
        }

        return result;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private HttpServletRequest getHttpServletRequest() {
        try {
            return ((org.springframework.web.context.request.ServletRequestAttributes)
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
                .getRequest();
        } catch (Exception e) {
            return null;
        }
    }
}
