package com.example.recommend.config;

import com.example.recommend.common.BusinessException;
import com.example.recommend.common.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理后台 API Key 鉴权拦截器（修复Bug-004 管理后台API鉴权完全缺失）。
 * <p>
 * 拦截 /admin/** 路径，校验 X-API-Key 请求头是否与配置的 admin.api-key 一致。
 * 未携带或 Key 不匹配时抛出 UNAUTHORIZED 异常，由 GlobalExceptionHandler 统一返回 401。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    // 修复Bug-004：从配置文件读取管理后台 API Key，默认值对齐前端 request.js 的 admin-dev-key
    @Value("${admin.api-key:admin-dev-key}")
    private String adminApiKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 修复Bug-004：放行 OPTIONS 预检请求（CORS 跨域场景）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String apiKey = request.getHeader("X-API-Key");
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("管理后台请求未携带 X-API-Key: uri={}, method={}", request.getRequestURI(), request.getMethod());
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "缺少 X-API-Key 请求头");
        }

        if (!adminApiKey.equals(apiKey)) {
            log.warn("管理后台请求 API Key 不匹配: uri={}, providedKey={}", request.getRequestURI(), apiKey);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "X-API-Key 无效");
        }

        return true;
    }
}
