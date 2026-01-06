package com.iot.dto;

import lombok.Data;

@Data
public class FanCommandRequest {
    private Integer deviceId;
    private int state;
}