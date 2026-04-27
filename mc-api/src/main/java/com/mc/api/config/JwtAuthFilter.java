package com.mc.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mc.common.result.Result;
import com.mc.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * JWT 认证过滤器
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** 放行的路径（无需认证） */
    private static final Set<String> EXCLUDED_PATHS = Set.of(
        "/system/auth/login",
        "/swagger-ui",
        "/v3/api-docs",
        "/actuator"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 添加 CORS 响应头
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With");
        response.setHeader("Access-Control-Max-Age", "3600");

        // 处理 CORS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String path = request.getRequestURI();

        // 放行无需认证的路径
        for (String excluded : EXCLUDED_PATHS) {
            if (path.startsWith(excluded)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "缺少认证Token");
            return;
        }

        String token = authHeader.substring(7);
        Map<String, Object> claims = JwtUtil.parseToken(token);
        if (claims == null) {
            writeUnauthorized(response, "Token无效或已过期");
            return;
        }

        // 将用户信息注入 request attribute，供后续 SecurityUtil 获取
        request.setAttribute("userId", claims.get("userId"));
        request.setAttribute("username", claims.get("username"));

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new HashMap<>();
        body.put("code", 401);
        body.put("message", message);
        body.put("success", false);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
