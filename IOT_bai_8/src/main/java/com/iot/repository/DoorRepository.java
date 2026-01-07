package com.iot.repository;

import com.iot.entity.Door;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoorRepository extends JpaRepository<Door, Integer> {

    @Query(
            value = """
                SELECT *
                FROM door_data
                WHERE device_id = :deviceId
                ORDER BY receive_at DESC
                LIMIT 1
                """,
            nativeQuery = true
    )
    Optional<Door> getLastDoorState(@Param("deviceId") Integer deviceId);
}
