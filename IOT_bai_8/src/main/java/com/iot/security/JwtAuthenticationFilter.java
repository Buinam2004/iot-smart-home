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
        String path = request.getRequestURI();

        if (path.startsWith("/api/auth/")
                || path.equals("/api/auth") )
        {
            filterChain.doFilter(request, response);
            return;
        }
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
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
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

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
