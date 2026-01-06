package com.iot.repository;

import com.iot.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    RefreshToken findByRefreshToken(String refreshToken);

    @Query(
        "SELECT rt FROM RefreshToken rt " +
                "WHERE rt.refreshToken = ?1"
                    + " AND rt.expiryDate > CURRENT_TIMESTAMP"
    )
    RefreshToken checkRefreshToken(String refreshToken);
}
