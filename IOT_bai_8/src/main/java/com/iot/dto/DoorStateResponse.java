package com.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DoorStateResponse {
    private Integer deviceId;
    private String action;
    private LocalDateTime receiveAt;
}
