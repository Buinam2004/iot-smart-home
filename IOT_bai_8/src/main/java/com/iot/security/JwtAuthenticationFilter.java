package com.iot.security;

import com.iot.custom.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        log.info("Request URI: {}", request.getRequestURI());
        String path = request.getRequestURI(); // Lấy đường dẫn yêu cầu

        // Bỏ qua các endpoint public
        if (path.startsWith("/api/auth/")
                || path.equals("/api/auth") )
        {
            filterChain.doFilter(request, response); // request được thông qua
            return;
        }
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            // Nếu đã có xác thực trong SecurityContext, bỏ qua xử lý
            log.info("Security Context: {}", SecurityContextHolder.getContext().getAuthentication());
            filterChain.doFilter(request, response);
            return;
        }

         // Xử lý xác thực JWT
        try {
            String jwt = jwtTokenProvider.extractJwtFromRequest(request);
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                String username = jwtTokenProvider.getUsernameFromJWT(jwt);
                setAuthentication(username, request);
            }
            else {
                log.warn("Invalid or missing JWT token");
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }
        filterChain.doFilter(request, response); // request được thông qua
    }

    private void setAuthentication(String username, HttpServletRequest request) {

        // Tải chi tiết người dùng từ CustomUserDetailsService
        // Điều này sẽ truy xuất cơ sở dữ liệu để lấy thông tin người dùng
        // và đảm bảo có đủ thông tin để tạo đối tượng xác thực
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken( // Tạo đối tượng xác thực
                        userDetails, // principal (chi tiết người dùng)
                        null, // credentials (mật khẩu, không cần thiết ở đây)
                        userDetails.getAuthorities() // authorities (quyền của người dùng)
                );
        // xác định chi tiết về yêu cầu hiện tại vào đối tượng xác thực
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Đặt đối tượng xác thực vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
