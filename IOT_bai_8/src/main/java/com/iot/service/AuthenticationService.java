package com.iot.service;

import com.iot.custom.CustomUserDetails;
import com.iot.dto.AuthResponseDTO;
import com.iot.dto.TokenDeviceDTO;
import com.iot.repository.DeviceRepository;
import com.iot.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor

public class AuthenticationService implements IAuthenticationService {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final DeviceRepository deviceRepository;


    @Override
    public ResponseEntity<?> login(String username, String password) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            log.info("User {} authenticated successfully", username);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String role = userDetails.getAuthorities().iterator().next().getAuthority(); // vì chỉ có 1 role nên lấy thằng đầu tiên
            log.info("User {} has role {}", username, role);

            String access_token = tokenProvider.generateToken(username, role);
            String refresh_token = tokenProvider.generateRefreshToken(username, role);
            ResponseCookie refreshTokenCookie = buildRefreshTokenCookie(refresh_token, tokenProvider.getRefreshExpirationInMillis());

            AuthResponseDTO respDTO =
                    AuthResponseDTO.builder()
                    .userId(userDetails.getUserId())
                    .username(username)
                    .role(role)
                    .accessToken(access_token)
                    .expiresIn(tokenProvider.getExpirationInMillis())
                    .build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(respDTO);
        }
        catch (BadCredentialsException e) {
            log.warn("Bad credentials" + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
        catch (DisabledException e) {
            log.warn("Account disabled" + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account is disabled");
        }
        catch (Exception e) {
            log.error("Authentication error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during authentication");
        }
    }

    @Override
    public ResponseEntity<?> authenticateDevice(Integer deviceId, String macAddress) {
        boolean checkCredentials = deviceRepository.existsByIdAndMacAddress(deviceId, macAddress);
        if (!checkCredentials) {
            log.warn("Device authentication failed for deviceId={}", deviceId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid device ID or device key");
        }
        String deviceToken = tokenProvider.generateDeviceToken(deviceId, macAddress);
        TokenDeviceDTO response = TokenDeviceDTO.builder()
                .deviceId(deviceId)
                .macAddress(macAddress)
                .tokenDevice(deviceToken)
                .build();
        log.info("Device authenticated successfully: deviceId={}", deviceId);
        return ResponseEntity.ok().body(response);
    }

    @Override
    public ResponseEntity<?> refreshToken(String refreshToken, String authHeader) {
        if(tokenProvider.validateToken(refreshToken)) {
            String username = tokenProvider.getUsernameFromJWT(refreshToken);
            String role = tokenProvider.getRoleFromJWT(refreshToken);

            String newAccessToken = tokenProvider.generateToken(username, role);
            String newRefreshToken = tokenProvider.generateRefreshToken(username, role);

            ResponseCookie refreshTokenCookie = buildRefreshTokenCookie(newRefreshToken, tokenProvider.getRefreshExpirationInMillis());

            AuthResponseDTO respDTO =
                    AuthResponseDTO.builder()
                            .username(username)
                            .role(role)
                            .accessToken(newAccessToken)
                            .expiresIn(tokenProvider.getExpirationInMillis())
                            .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(respDTO);
        } else {
            log.warn("Invalid refresh token during token refresh");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }
    }

    @Override
    public ResponseEntity<?> logout(String refreshToken, String authHeader) {

        if(tokenProvider.validateToken(refreshToken)) {

            ResponseCookie deleteCookie = buildRefreshTokenCookie(null, 0);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                    .body("Logged out successfully");
        } else {
            log.warn("Invalid refresh token during logout");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }
    }

    private ResponseCookie buildRefreshTokenCookie(String refreshToken, long maxAgeMillis) {
        return ResponseCookie.from("RefreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAgeMillis / 1000)
                .build();
    }
}
