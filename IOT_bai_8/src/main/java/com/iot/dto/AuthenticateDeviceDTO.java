package com.iot.dto;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class AuthenticateDeviceDTO {
    private Integer id;
    private String deviceKey;
}
