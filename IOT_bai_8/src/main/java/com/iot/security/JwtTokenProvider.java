package com.iot.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    private final int TIME_EXPIRE_ACCESSTOKEN = 3600000; // 1 hour in milliseconds
    private final int TIME_EXPIRE_REFRESHTOKEN = 7 * 86400000; // 24 hours in milliseconds
    private final SecretKey jwtSecret;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret) {
        this.jwtSecret = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // tạo khóa bí mật từ chuỗi ký tự
    }

    public String generateToken(String username, String role) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + TIME_EXPIRE_ACCESSTOKEN); // 1 hour validity
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtSecret)
                .compact();
    }
    public String generateRefreshToken(String username, String role) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + TIME_EXPIRE_REFRESHTOKEN); // 7 days validity
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtSecret)
                .compact();
    }
    public String generateDeviceToken(Integer id, String deviceKey) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + TIME_EXPIRE_ACCESSTOKEN);
        return Jwts.builder()
                .subject(id.toString())
                .claim("deviceKey", deviceKey)
                .claim("role", "DEVICE")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtSecret)
                .compact();
    }

    public Integer getDeviceIdFromJWT(String token) {
        String subject = Jwts.parser()
                .verifyWith(jwtSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Integer.parseInt(subject);
    }

    public String extractJwtDeviceFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("TokenDevice");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);// Loại bỏ "Bearer " để lấy token thực sự
        }
        return null;
    }



    public int getExpirationInMillis() {
        return TIME_EXPIRE_ACCESSTOKEN;
    }
    public int getRefreshExpirationInMillis() {
        return TIME_EXPIRE_REFRESHTOKEN;
    }

    public String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);// Loại bỏ "Bearer " để lấy token thực sự
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(jwtSecret)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public String getUsernameFromJWT(String token) {
        return Jwts.parser()
                .verifyWith(jwtSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    public String getRoleFromJWT(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("role", String.class);
    }
}
