package com.iot.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Builder
@Data
@Getter
@Setter
public class TokenDeviceDTO {
    Integer deviceId;
    String deviceKey;
    String tokenDevice;
}
