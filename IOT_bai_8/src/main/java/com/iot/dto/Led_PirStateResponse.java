package com.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Led_PirStateResponse {
    private Integer deviceId;
    private int state;
    private LocalDateTime createdAt;
}
