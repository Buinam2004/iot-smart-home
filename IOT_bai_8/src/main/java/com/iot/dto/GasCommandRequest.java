package com.iot.dto;

import lombok.Data;

@Data
public class GasCommandRequest {
    private String device;
    private String action;
    private Integer deviceId;
}
