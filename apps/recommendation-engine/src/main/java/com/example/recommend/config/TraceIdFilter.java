package com.example.recommend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * TraceId 过滤器。
 * <p>
 * 生成或透传 X-Trace-Id，写入 MDC 供日志和响应使用，实现全链路追踪。
 * 架构文档 M01-T05 任务要求。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    /** TraceId 请求头名称 */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /** MDC 中 TraceId 的键名 */
    public static final String MDC_TRACE_ID_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 优先透传上游传入的 TraceId，不存在则生成新的
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        // 写入 MDC，供日志模板使用
        MDC.put(MDC_TRACE_ID_KEY, traceId);
        // 响应头回传 TraceId，方便客户端排查
        response.setHeader(TRACE_ID_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束后清理 MDC，避免线程池复用导致 TraceId 污染
            MDC.remove(MDC_TRACE_ID_KEY);
        }
    }
}
