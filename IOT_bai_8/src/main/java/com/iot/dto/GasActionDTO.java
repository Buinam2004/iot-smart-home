package com.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GasActionDTO {
    String type;
    String device;
    String action;
}
