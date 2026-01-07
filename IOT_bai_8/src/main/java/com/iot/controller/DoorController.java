package com.iot.controller;

import com.iot.dto.CommandRequest;
import com.iot.dto.DoorCommandRequest;
import com.iot.service.MqttPublishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/door")
@Slf4j
public class DoorController {

    @Autowired
    private MqttPublishService mqttPublishService;

    @PostMapping("/publish")
    public ResponseEntity<?> publishFan(@RequestBody DoorCommandRequest doorrequest) {
        Integer deviceId = doorrequest.getDeviceId();

        int state = doorrequest.getState();
        String action = doorrequest.getAction();
        try {
            log.info("Gửi lệnh điều khiển cửa: deviceId={}, state={}", deviceId, state);
            mqttPublishService.sendDoorCommand(deviceId, state, action);
            return ResponseEntity.ok("Đã gửi lệnh điều khiển cửa");
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

}