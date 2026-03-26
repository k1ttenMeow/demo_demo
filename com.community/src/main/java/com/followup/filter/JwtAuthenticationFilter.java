package com.followup.filter;

import com.followup.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 认证过滤器
 * 解析请求头中的 Token，并设置到 Spring Security 上下文中
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 获取请求头中的 Authorization
        String header = request.getHeader("Authorization");

        log.debug("=== JWT 过滤器执行 ===");
        log.debug("请求 URL: {}", request.getRequestURI());
        log.debug("Authorization Header: {}", header);

        // 2. 判断 Token 是否存在
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            log.debug("Token 不存在或格式不正确，继续过滤");
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 提取 Token
        String token = header.substring(7);
        log.debug("提取的 Token: {}", token.substring(0, Math.min(20, token.length())) + "...");

        // 4. 解析 Token
        try {
            Claims claims = jwtUtil.parseToken(token);
            if (claims == null) {
                log.warn("Token 解析失败，claims 为空");
                filterChain.doFilter(request, response);
                return;
            }

            // 5. 获取用户信息
            String username = claims.getSubject();
            Integer role = claims.get("role", Integer.class);

            log.info("Token 解析成功 - 用户名：{}, 角色：{}", username, role);

            // 6. 创建认证对象
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            new ArrayList<>() // 空权限列表
                    );

            // 7. 设置详细信息
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // 8. 将认证信息设置到上下文中
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("认证成功已设置到 SecurityContext");

        } catch (Exception e) {
            // Token 解析失败，继续过滤（不阻断请求）
            log.error("Token 解析异常：", e);
        }

        filterChain.doFilter(request, response);
    }
}
