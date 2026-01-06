package com.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDTO {
    private Integer id;
    private Integer userId;
    private String name;
    private String macAddress;
    private Boolean isOnline;
    private LocalDateTime createdAt;
    private String creatorName; // Tên người tạo
}
