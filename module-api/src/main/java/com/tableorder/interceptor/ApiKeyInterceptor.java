package com.tableorder.interceptor;

import com.tableorder.context.StoreContext;
import com.tableorder.dto.ResponseDTO;
import com.tableorder.service.StoreService;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class ApiKeyInterceptor implements HandlerInterceptor {
    private final ObjectMapper objectMapper;
    private final StoreService storeService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String apiKeyHeader = "Bearer ";
        int httpStatus;

        String apiKey = request.getHeader("Authorization");
        if (apiKey == null || !apiKey.startsWith(apiKeyHeader)) {
            httpStatus = HttpStatus.UNAUTHORIZED.value();
            ResponseDTO responseData = new ResponseDTO<>(httpStatus, "요청에 사용된 API 키가 올바르지 않습니다. 관리자에게 문의해 주세요.");

            response.setStatus(httpStatus);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(responseData));

            return false;
        }

        apiKey = apiKey.substring(apiKeyHeader.length());
        String storeId = storeService.getStoreIdByApiKey(apiKey);

        StoreContext.setStoreId(storeId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        StoreContext.clear();
    }
}
