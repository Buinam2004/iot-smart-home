package com.iot.repository;

import com.iot.entity.Led_Pir;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Led_PirRepository extends JpaRepository<Led_Pir, Integer> {

    @Query(
            value = """
                SELECT *
                FROM led_pir_data
                WHERE device_id = :deviceId
                ORDER BY created_at DESC
                LIMIT 1
                """,
            nativeQuery = true
    )
    Optional<Led_Pir> getLastLed_PirState(@Param("deviceId") Integer deviceId);
}
