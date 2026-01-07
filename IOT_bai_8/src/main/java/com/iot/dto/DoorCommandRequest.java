package com.iot.dto;

import lombok.Data;

@Data
public class DoorCommandRequest {
    private Integer deviceId;
    private int state;
    private String action;
}
