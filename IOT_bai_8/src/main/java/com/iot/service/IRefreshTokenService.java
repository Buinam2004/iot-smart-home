package com.iot.service;

import com.iot.entity.RefreshToken;
import org.springframework.http.ResponseEntity;

import java.util.Optional;


public interface IRefreshTokenService {

    ResponseEntity<?> createRefreshToken(String refreshToken);
    ResponseEntity<?> deleteRefreshToken(String refreshToken);
    Optional<RefreshToken> checkRefreshToken(String refreshToken);
    ResponseEntity<?> refreshToken(String oldRefreshToken, String newRefreshToken);

}
