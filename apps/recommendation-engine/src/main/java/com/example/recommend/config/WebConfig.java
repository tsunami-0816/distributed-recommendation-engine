package com.example.recommend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置。
 * <p>
 * 注册拦截器、CORS 配置等。管理后台 /admin/** 路径通过 AuthInterceptor 校验 X-API-Key。
 * TraceId 通过 TraceIdFilter（Servlet Filter）实现，无需在此注册。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    // 修复Bug-004：注入鉴权拦截器
    private final AuthInterceptor authInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 前台 API
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
        // 管理后台 API（M11）
        registry.addMapping("/admin/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 修复Bug-004：注册管理后台鉴权拦截器，拦截所有 /admin/** 路径
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/admin/**");
    }
}
