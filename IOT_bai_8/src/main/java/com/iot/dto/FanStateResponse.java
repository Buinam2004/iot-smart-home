package com.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FanStateResponse {
    private Integer deviceId;
    private int state;
    private LocalDateTime createdAt;
}
