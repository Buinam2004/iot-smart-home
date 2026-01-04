package com.iot.controller;

import com.iot.dto.SensorDataDTO;
import com.iot.dto.UpdateSensorDataDTO;
import com.iot.entity.SensorData;
import com.iot.exception.ResourceNotFoundException;
import com.iot.service.ISensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sensor-data")
@CrossOrigin(origins = "*")
public class SensorDataController {
    
    @Autowired
    private ISensorDataService sensorDataService;
    
    // GET /api/sensor-data - Lấy tất cả sensor data (kèm tên thiết bị)
    // GET /api/sensor-data?deviceName=name - Tìm kiếm theo tên thiết bị
    // GET /api/sensor-data?recentSeconds=60 - Lấy dữ liệu trong 60 giây gần nhất
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SensorDataDTO>> getAllSensorData(
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) Integer recentSeconds) {
        
        List<SensorDataDTO> sensorDataList;

        if (deviceName == null && recentSeconds == null) {
            sensorDataList = sensorDataService.getAllSensorData();
        }
        else if (deviceName != null && recentSeconds == null) {
            sensorDataList = sensorDataService.findByDeviceName(deviceName);
        }
        else {
            sensorDataList = sensorDataService.getAllRecentSensorDataWithDeviceName(deviceName, recentSeconds);
        }



        return ResponseEntity.ok(sensorDataList);
    }
    
    // GET /api/sensor-data/{id} - Lấy sensor data theo ID
    @GetMapping("/{id}")
    @PreAuthorize("@sensorDataService.canGetSensorData(authentication, #id)")
    public ResponseEntity<SensorDataDTO> getSensorDataById(@PathVariable Integer id) {
        SensorDataDTO sensorData = sensorDataService.getSensorDataById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor Data", "id", id));
        return ResponseEntity.ok(sensorData);
    }
    
    // POST /api/sensor-data - Tạo sensor data mới
    @PostMapping
    @PreAuthorize("@sensorDataService.canPostSensorData(authentication, #sensorData)")
    public ResponseEntity<SensorData> createSensorData(@RequestBody SensorData sensorData) {
        SensorData createdSensorData = sensorDataService.createSensorData(sensorData);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSensorData);
    }
    
    // PATCH /api/sensor-data/{id} - Cập nhật sensor data
    @PatchMapping("/{id}")
    @PreAuthorize("@sensorDataService.canUpdateSensorData(authentication , #id, #sensorDataDetails)")
    public ResponseEntity<UpdateSensorDataDTO> updateSensorData(@PathVariable Integer id, @RequestBody SensorData sensorDataDetails) {
        UpdateSensorDataDTO updatedSensorData = sensorDataService.updateSensorData(id, sensorDataDetails);
        return ResponseEntity.ok(updatedSensorData);
    }



    // DELETE /api/sensor-data/{id} - Xóa sensor data
    @DeleteMapping("/{id}")
    @PreAuthorize("@sensorDataService.canDeleteSensorData(authentication, #id)")
    public ResponseEntity<Void> deleteSensorData(@PathVariable Integer id) {
        sensorDataService.deleteSensorData(id);
        return ResponseEntity.noContent().build();
    }
}
