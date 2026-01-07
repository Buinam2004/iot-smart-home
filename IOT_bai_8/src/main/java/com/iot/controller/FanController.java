package com.iot.controller;

import com.iot.dto.CommandRequest;
import com.iot.service.MqttPublishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fan")
@Slf4j
public class FanController {

    @Autowired
    private MqttPublishService mqttPublishService;

    @PostMapping("/publish")
    public ResponseEntity<?> publishFan(@RequestBody CommandRequest fanrequest) {
        Integer deviceId = fanrequest.getDeviceId();

        int state = fanrequest.getState();
        try {
            log.info("Gửi lệnh điều khiển quạt: deviceId={}, state={}", deviceId, state);
            mqttPublishService.sendFanCommand(deviceId, state);
            return ResponseEntity.ok("Đã gửi lệnh điều khiển quạt");
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

}
