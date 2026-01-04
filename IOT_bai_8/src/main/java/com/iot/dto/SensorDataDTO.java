package com.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorDataDTO {
    private Integer id;
    private Integer deviceId;
    private String type;
    private Double value;
    private LocalDateTime receivedAt;
    private String deviceName; // Tên thiết bị
}
