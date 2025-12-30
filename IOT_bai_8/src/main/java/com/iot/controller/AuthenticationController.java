package com.iot.controller;

import com.iot.dto.AuthenticateDeviceDTO;
import com.iot.dto.LoginRequestDTO;

import com.iot.service.IAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
