package com.iot.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DoorCommandRequest implements Serializable {
    private Integer deviceId;
    private int state;
    private String action;
}
