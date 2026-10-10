package com.example.recommend.config;

import com.example.recommend.common.BusinessException;
import com.example.recommend.common.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 修复Bug-010：AuthInterceptor 单元测试。
 * <p>
 * 覆盖管理后台 API Key 鉴权逻辑（修复Bug-004），包括缺失 Key、Key 不匹配、Key 正确、OPTIONS 预检等场景。
 *
 * @author recommend-team
 * @since 1.0.0
 */
class AuthInterceptorTest {

    private AuthInterceptor authInterceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Object handler;

    private static final String VALID_API_KEY = "admin-dev-key";

    @BeforeEach
    void setUp() {
        authInterceptor = new AuthInterceptor();
        // 通过反射设置 @Value 字段
        ReflectionTestUtils.setField(authInterceptor, "adminApiKey", VALID_API_KEY);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        handler = new Object();
    }

    @Test
    @DisplayName("修复Bug-004：携带正确的 X-API-Key 应放行")
    void preHandle_withValidApiKey_shouldReturnTrue() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("X-API-Key")).thenReturn(VALID_API_KEY);

        boolean result = authInterceptor.preHandle(request, response, handler);

        assertTrue(result, "携带正确 API Key 应放行");
    }

    @Test
    @DisplayName("修复Bug-004：缺失 X-API-Key 请求头应抛 UNAUTHORIZED 异常")
    void preHandle_withMissingApiKey_shouldThrowUnauthorized() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("X-API-Key")).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                authInterceptor.preHandle(request, response, handler));

        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), exception.getErrorCode().getCode(),
                "缺失 API Key 应返回 200001 未授权");
    }

    @Test
    @DisplayName("修复Bug-004：X-API-Key 为空字符串应抛 UNAUTHORIZED 异常")
    void preHandle_withBlankApiKey_shouldThrowUnauthorized() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("X-API-Key")).thenReturn("   ");

        BusinessException exception = assertThrows(BusinessException.class, () ->
                authInterceptor.preHandle(request, response, handler));

        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), exception.getErrorCode().getCode());
    }

    @Test
    @DisplayName("修复Bug-004：X-API-Key 不匹配应抛 UNAUTHORIZED 异常")
    void preHandle_withInvalidApiKey_shouldThrowUnauthorized() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("X-API-Key")).thenReturn("wrong-key");

        BusinessException exception = assertThrows(BusinessException.class, () ->
                authInterceptor.preHandle(request, response, handler));

        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), exception.getErrorCode().getCode(),
                "API Key 不匹配应返回 200001 未授权");
    }

    @Test
    @DisplayName("修复Bug-004：OPTIONS 预检请求应直接放行，不校验 API Key")
    void preHandle_withOptionsMethod_shouldReturnTrueWithoutApiKey() {
        when(request.getMethod()).thenReturn("OPTIONS");
        // 不设置 X-API-Key 请求头

        boolean result = authInterceptor.preHandle(request, response, handler);

        assertTrue(result, "OPTIONS 预检请求应直接放行");
        verify(request, never()).getHeader("X-API-Key");
    }

    @Test
    @DisplayName("修复Bug-004：POST 请求携带正确 API Key 应放行")
    void preHandle_postRequestWithValidApiKey_shouldReturnTrue() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-API-Key")).thenReturn(VALID_API_KEY);

        boolean result = authInterceptor.preHandle(request, response, handler);

        assertTrue(result);
    }

    @Test
    @DisplayName("修复Bug-004：PUT 请求缺失 API Key 应抛异常")
    void preHandle_putRequestWithoutApiKey_shouldThrowUnauthorized() {
        when(request.getMethod()).thenReturn("PUT");
        when(request.getHeader("X-API-Key")).thenReturn(null);

        assertThrows(BusinessException.class, () ->
                authInterceptor.preHandle(request, response, handler));
    }

    @Test
    @DisplayName("修复Bug-004：DELETE 请求缺失 API Key 应抛异常")
    void preHandle_deleteRequestWithoutApiKey_shouldThrowUnauthorized() {
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getHeader("X-API-Key")).thenReturn(null);

        assertThrows(BusinessException.class, () ->
                authInterceptor.preHandle(request, response, handler));
    }
}
