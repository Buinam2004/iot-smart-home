package com.iot.service;

import com.iot.dto.SensorDataDTO;
import com.iot.dto.UpdateSensorDataDTO;
import com.iot.entity.SensorData;

import java.util.List;
import java.util.Optional;

public interface ISensorDataService {
    List<SensorDataDTO> getAllSensorData();
    Optional<SensorDataDTO> getSensorDataById(Integer id);
    SensorData createSensorData(SensorData sensorData);
    UpdateSensorDataDTO updateSensorData(Integer id, SensorData sensorDataDetails);
    void deleteSensorData(Integer id);
    List<SensorDataDTO> findByDeviceName(String deviceName);

    List<SensorDataDTO> getAllRecentSensorDataWithDeviceName (String deviceName, int recentSeconds);
}
