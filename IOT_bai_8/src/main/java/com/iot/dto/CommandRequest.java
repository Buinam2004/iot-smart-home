package com.iot.dto;

import lombok.Data;

@Data
public class CommandRequest {
    private Integer deviceId;
    private int state;
}