package com.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DoorCommand {
    private String type;   // "command"
    private String device; // "door"
    private String action; // "deny", "open", "check"
}