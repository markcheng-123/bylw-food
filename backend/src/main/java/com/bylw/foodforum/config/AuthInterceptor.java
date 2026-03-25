package com.bylw.foodforum.config;

import com.bylw.foodforum.common.context.UserContext;
import com.bylw.foodforum.common.util.AuthTokenUtil;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final List<String> WHITE_LIST = Arrays.asList(
        "/api/users/register",
        "/api/users/login",
        "/api/system/health",
        "/api/categories/options",
        "/api/home",
        "/api/home/stats",
        "/api/strategies",
        "/api/ai/chat",
        "/api/ai/chat/stream"
    );

    private final AuthTokenUtil authTokenUtil;

    public AuthInterceptor(AuthTokenUtil authTokenUtil) {
        this.authTokenUtil = authTokenUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())
            || WHITE_LIST.contains(uri)
            || isPublicPostRequest(request)
            || isPublicStrategyRequest(request)
            || isPublicMerchantCardRequest(request)) {
            trySetCurrentUser(request);
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}");
            return false;
        }

        Long userId = authTokenUtil.parseUserId(authorization.substring(7));
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"登录态无效，请重新登录\",\"data\":null}");
            return false;
        }

        UserContext.setCurrentUserId(userId);
        return true;
    }

    private void trySetCurrentUser(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return;
        }
        Long userId = authTokenUtil.parseUserId(authorization.substring(7));
        if (userId != null) {
            UserContext.setCurrentUserId(userId);
        }
    }

    private boolean isPublicPostRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return "GET".equalsIgnoreCase(request.getMethod()) && ("/api/posts".equals(uri) || uri.startsWith("/api/posts/"));
    }

    private boolean isPublicStrategyRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return "GET".equalsIgnoreCase(request.getMethod()) && ("/api/strategies".equals(uri) || uri.startsWith("/api/strategies/"));
    }

    private boolean isPublicMerchantCardRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return "GET".equalsIgnoreCase(request.getMethod()) && uri.startsWith("/api/merchant-profiles/card/");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
