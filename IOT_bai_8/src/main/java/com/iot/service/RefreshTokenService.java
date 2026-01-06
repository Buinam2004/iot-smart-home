package com.iot.service;

import com.iot.entity.RefreshToken;
import com.iot.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenService implements IRefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;


    @Override
    public ResponseEntity<?> createRefreshToken(String newRefreshToken) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshToken(newRefreshToken);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(refreshToken);
        return ResponseEntity.ok(refreshToken);

    }

    @Override
    public ResponseEntity<?> deleteRefreshToken(String refreshToken) {
        RefreshToken refreshTokenOptional = refreshTokenRepository.findByRefreshToken(refreshToken);
        if (refreshTokenOptional != null) {
            refreshTokenRepository.delete(refreshTokenOptional);
            return ResponseEntity.ok("Refresh token deleted successfully");
        }
        return ResponseEntity.ok("Refresh token deleted successfully");
    }

    @Override
    public Optional<RefreshToken> checkRefreshToken(String refreshToken) {
        RefreshToken refreshTokenOptional = refreshTokenRepository.checkRefreshToken(refreshToken);
        if (refreshTokenOptional == null) {
            return Optional.empty();
        }

        return Optional.of(refreshTokenOptional);
    }

    @Override
    public ResponseEntity<?> refreshToken(String oldRefreshToken, String newRefreshToken) {
        RefreshToken refreshTokenOptional = refreshTokenRepository.findByRefreshToken(oldRefreshToken);
        if (refreshTokenOptional == null) {
            return ResponseEntity.badRequest().body("Invalid refresh token");
        }
        refreshTokenOptional.setRefreshToken(newRefreshToken);
        refreshTokenOptional.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(refreshTokenOptional);
        return ResponseEntity.ok(refreshTokenOptional);
    }

}
