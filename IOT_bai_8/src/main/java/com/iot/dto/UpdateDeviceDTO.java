package com.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDeviceDTO {
    private Integer id;
    private Integer userId;
    private String name;
    private String deviceKey;
    private Boolean isOnline;
    private LocalDateTime createdAt;
}
