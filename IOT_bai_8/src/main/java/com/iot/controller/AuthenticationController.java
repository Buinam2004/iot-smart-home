package com.iot.controller;

import com.iot.dto.AuthenticateDeviceDTO;
import com.iot.dto.LoginRequestDTO;

import com.iot.service.IAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private IAuthenticationService authenticationServicre;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        // Xử lý logic xác thực người dùng ở đây (ví dụ: kiểm tra username và password)

        return authenticationServicre.login(username, password);
    }
    @PostMapping("/authenticate-device")
    public ResponseEntity<?> authenticateDevice(@RequestBody AuthenticateDeviceDTO authenticateDeviceDTO) {
        Integer deviceId = authenticateDeviceDTO.getId();
        String deviceSecret = authenticateDeviceDTO.getDeviceKey();
        return authenticationServicre.authenticateDevice(deviceId, deviceSecret);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return authenticationServicre.refreshToken(refreshToken, authHeader);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return authenticationServicre.logout(refreshToken, authHeader);
    }
}
