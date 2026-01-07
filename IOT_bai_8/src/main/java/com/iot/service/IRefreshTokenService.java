package com.iot.service;

import com.iot.entity.RefreshToken;

import java.util.Optional;


public interface IRefreshTokenService {
    Optional<RefreshToken> checkRefreshToken(String refreshToken);
    void refreshToken(String oldRefreshToken, String newRefreshToken);
}
