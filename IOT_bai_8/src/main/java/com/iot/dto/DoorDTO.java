package com.iot.dto;

import jakarta.persistence.Column;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DoorDTO {
    private String uid;

    private String event;

    private Integer deviceId;

    private LocalDateTime receiveAt;

    private String type;
}
