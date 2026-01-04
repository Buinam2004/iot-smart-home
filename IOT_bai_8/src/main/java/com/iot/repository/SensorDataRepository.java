package com.iot.repository;

import com.iot.entity.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Integer> {
    
    @Query("SELECT sd FROM SensorData sd JOIN sd.device d WHERE d.name = :deviceName")
    List<SensorData> findByDeviceName(@Param("deviceName") String deviceName);
    
    @Query("SELECT sd FROM SensorData sd JOIN FETCH sd.device")
    List<SensorData> findAllWithDevice();

    @Query("SELECT sd FROM SensorData sd JOIN sd.device d WHERE d.name = :deviceName AND sd.receivedAt >= :fromTime")
    List<SensorData> findAllRecentSensorDataWithDeviceName(@Param("deviceName") String deviceName, @Param("fromTime") LocalDateTime fromTime);

    boolean existsByIdAndDeviceId(Integer id, Integer deviceId);
}
