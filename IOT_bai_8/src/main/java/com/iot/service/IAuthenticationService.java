package com.iot.service;

import org.springframework.http.ResponseEntity;

public interface IAuthenticationService {
    ResponseEntity<?> login(String username, String password);
    ResponseEntity<?> authenticateDevice(Integer deviceId, String deviceSecret);
    ResponseEntity<?> refreshToken(String refreshToken, String authHeader);
    ResponseEntity<?> logout(String refreshToken, String authHeader);
}
