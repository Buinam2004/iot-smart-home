package com.iot.security;

import com.iot.custom.CustomUserDetailsService;
import com.iot.entity.RefreshToken;
import com.iot.service.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshTokenService refreshTokenService;

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
                String refreshToken = extractRefreshToken(request);

                if(refreshToken != null) {
                    Optional<RefreshToken> refreshTokenOptional = refreshTokenService.checkRefreshToken(refreshToken);
                    if (refreshTokenOptional.isPresent()) {

                        String username = jwtTokenProvider.getUsernameFromJWT(refreshToken);
                        setAuthentication(username, request);

                        // Tạo và gửi lại access token mới
                        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                        String role = userDetails.getAuthorities().iterator().next().getAuthority();
                        String newAccessToken = jwtTokenProvider.generateToken(username, role);
                        response.setHeader("Authorization", "Bearer " + newAccessToken);

                        // Tạo và gửi lại refresh token mới
                        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username, role);


                        refreshTokenService.refreshToken(refreshToken, newRefreshToken);
                        setRefreshTokenCookie(response, newRefreshToken);


                    } else {
                        log.warn("Invalid refresh token");
                        clearRefreshTokenCookie(response);
                    }
                }
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


    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {

        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/api/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/api/auth/refresh")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

}
